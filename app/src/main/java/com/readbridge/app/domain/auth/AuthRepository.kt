package com.readbridge.app.domain.auth

import com.readbridge.app.domain.auth.model.AuthState
import com.readbridge.app.domain.auth.model.LoginResult
import com.readbridge.app.domain.auth.model.ServerConfig
import kotlinx.coroutines.flow.StateFlow

/** Single source of truth for the Wallabag session. */
interface AuthRepository {
    /** Current session state; emits whenever the user logs in or out. */
    val authState: StateFlow<AuthState>

    /** Authenticate against [config] with the given credentials. */
    suspend fun login(config: ServerConfig, username: String, password: String): LoginResult

    /** Clear the local session (tokens). Server/client config is kept to prefill next login. */
    suspend fun logout()

    /** Last-used server config, if any, to prefill the login form. */
    fun currentServerConfig(): ServerConfig?
}
