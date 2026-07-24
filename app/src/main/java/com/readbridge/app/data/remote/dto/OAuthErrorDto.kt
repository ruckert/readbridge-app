package com.readbridge.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Error body returned by the OAuth token endpoint, e.g. `invalid_grant`. */
@Serializable
data class OAuthErrorDto(
    @SerialName("error") val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
)
