package com.readbridge.app.data.repository

import com.readbridge.app.data.local.auth.AuthLocalDataSource
import com.readbridge.app.data.remote.api.WallabagApi
import com.readbridge.app.data.remote.api.WallabagAuthApi
import com.readbridge.app.data.remote.dto.OAuthErrorDto
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.model.AuthState
import com.readbridge.app.domain.auth.model.LoginResult
import com.readbridge.app.domain.auth.model.ServerConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val local: AuthLocalDataSource,
    private val authApi: WallabagAuthApi,
    private val api: WallabagApi,
    private val json: Json,
) : AuthRepository {

    override val authState: StateFlow<AuthState> = local.authState

    override suspend fun login(
        config: ServerConfig,
        username: String,
        password: String,
    ): LoginResult {
        // Persist config first so the host interceptor can route the token request.
        local.saveServerConfig(config)
        return try {
            val tokens = authApi.login(
                clientId = config.clientId,
                clientSecret = config.clientSecret,
                username = username,
                password = password,
            )
            local.saveTokens(tokens.accessToken, tokens.refreshToken, tokens.expiresIn)
            // Best-effort: confirm session and capture server version (feature detection).
            val version = runCatching { api.getInfo().version }.getOrNull()
            LoginResult.Success(version)
        } catch (e: HttpException) {
            local.clearSession()
            mapHttpError(e)
        } catch (e: IOException) {
            local.clearSession()
            LoginResult.ServerUnreachable
        } catch (e: Exception) {
            local.clearSession()
            LoginResult.Unknown(e.message ?: "Unexpected error")
        }
    }

    override suspend fun logout() {
        local.clearSession()
    }

    override fun currentServerConfig(): ServerConfig? = local.serverConfig()

    private fun mapHttpError(e: HttpException): LoginResult {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val oauthError = body?.let {
            runCatching { json.decodeFromString(OAuthErrorDto.serializer(), it) }.getOrNull()
        }
        return when (oauthError?.error) {
            "invalid_grant" -> LoginResult.InvalidCredentials
            "invalid_client", "unauthorized_client" -> LoginResult.InvalidClient
            else -> when (e.code()) {
                401 -> LoginResult.InvalidCredentials
                else -> LoginResult.Unknown(
                    oauthError?.errorDescription ?: "HTTP ${e.code()}",
                )
            }
        }
    }
}
