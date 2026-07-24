package com.readbridge.app.domain.auth.model

/** Outcome of a login attempt, mapped to user-facing messages by the UI. */
sealed interface LoginResult {
    /** Login succeeded; [serverVersion] is reported by the instance when available. */
    data class Success(val serverVersion: String?) : LoginResult

    /** Wrong username/password (OAuth `invalid_grant`). */
    data object InvalidCredentials : LoginResult

    /** Wrong client id/secret (OAuth `invalid_client`). */
    data object InvalidClient : LoginResult

    /** Server not reachable (DNS/timeout/TLS/offline) or invalid server URL. */
    data object ServerUnreachable : LoginResult

    /** Anything else, with a best-effort message. */
    data class Unknown(val message: String) : LoginResult
}
