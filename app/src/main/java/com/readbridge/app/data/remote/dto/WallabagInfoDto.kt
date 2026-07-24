package com.readbridge.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /api/info.json` — used to confirm the session and read the version. */
@Serializable
data class WallabagInfoDto(
    @SerialName("appname") val appName: String? = null,
    @SerialName("version") val version: String? = null,
    @SerialName("allowed_registration") val allowedRegistration: Boolean? = null,
)
