package com.readbridge.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `POST /oauth/v2/token` (password and refresh_token grants). */
@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long = 0,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("scope") val scope: String? = null,
)
