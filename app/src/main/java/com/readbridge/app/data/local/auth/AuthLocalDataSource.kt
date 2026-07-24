package com.readbridge.app.data.local.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.readbridge.app.domain.auth.model.AuthState
import com.readbridge.app.domain.auth.model.ServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted, at-rest storage for the OAuth session and server/client config.
 * Backed by [EncryptedSharedPreferences] (AES-256). Exposes [authState] as a
 * [StateFlow] so the UI reacts to login/logout without polling.
 */
@Singleton
class AuthLocalDataSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val _authState = MutableStateFlow(computeState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val accessToken: String? get() = prefs.getString(KEY_ACCESS_TOKEN, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun serverConfig(): ServerConfig? {
        val url = prefs.getString(KEY_SERVER_URL, null) ?: return null
        val clientId = prefs.getString(KEY_CLIENT_ID, null) ?: return null
        val clientSecret = prefs.getString(KEY_CLIENT_SECRET, null) ?: return null
        return ServerConfig(url, clientId, clientSecret)
    }

    fun saveServerConfig(config: ServerConfig) {
        prefs.edit()
            .putString(KEY_SERVER_URL, config.serverUrl)
            .putString(KEY_CLIENT_ID, config.clientId)
            .putString(KEY_CLIENT_SECRET, config.clientSecret)
            .apply()
    }

    fun saveTokens(accessToken: String, refreshToken: String, expiresInSeconds: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, currentTimeMillis() + expiresInSeconds * 1_000)
            .apply()
        _authState.value = computeState()
    }

    /** Remove tokens (session ends) but keep server/client config to prefill the form. */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
        _authState.value = computeState()
    }

    private fun computeState(): AuthState =
        if (prefs.getString(KEY_ACCESS_TOKEN, null) != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }

    // Extracted for testability / to keep time access in one place.
    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    private companion object {
        const val PREFS_NAME = "readbridge_auth"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_CLIENT_SECRET = "client_secret"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
