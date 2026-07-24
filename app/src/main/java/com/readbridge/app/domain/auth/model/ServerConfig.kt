package com.readbridge.app.domain.auth.model

/**
 * Connection details for a self-hosted Wallabag instance. The user creates the
 * [clientId]/[clientSecret] pair in Wallabag → "Developer" → "Create a new client".
 *
 * @param serverUrl base URL of the instance, e.g. `https://wallabag.example.com`
 *   (a sub-path install like `https://example.com/wallabag` is also supported).
 */
data class ServerConfig(
    val serverUrl: String,
    val clientId: String,
    val clientSecret: String,
)
