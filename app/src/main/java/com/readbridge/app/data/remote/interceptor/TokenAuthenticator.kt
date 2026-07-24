package com.readbridge.app.data.remote.interceptor

import com.readbridge.app.data.local.auth.AuthLocalDataSource
import com.readbridge.app.data.remote.api.WallabagAuthApi
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On a 401, refreshes the access token via [WallabagAuthApi] and retries the request.
 * [WallabagAuthApi] is injected lazily and runs on a separate OkHttp client without
 * this authenticator, so refresh can never recurse. If refresh fails, the session is
 * cleared and the request is not retried (the UI then routes back to login).
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val local: AuthLocalDataSource,
    private val authApi: Lazy<WallabagAuthApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Give up after two attempts to avoid loops.
        if (responseCount(response) >= 2) return null

        val config = local.serverConfig() ?: return null
        val refreshToken = local.refreshToken ?: return null

        synchronized(this) {
            val currentToken = local.accessToken
            val attemptedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            // Another thread already refreshed: retry with the fresh token.
            if (currentToken != null && currentToken != attemptedToken) {
                return response.request.retryWith(currentToken)
            }

            val newTokens = runCatching {
                runBlocking {
                    authApi.get().refresh(
                        clientId = config.clientId,
                        clientSecret = config.clientSecret,
                        refreshToken = refreshToken,
                    )
                }
            }.getOrNull() ?: run {
                local.clearSession()
                return null
            }

            local.saveTokens(newTokens.accessToken, newTokens.refreshToken, newTokens.expiresIn)
            return response.request.retryWith(newTokens.accessToken)
        }
    }

    private fun Request.retryWith(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
