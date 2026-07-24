package com.readbridge.app.data.remote.api

import com.readbridge.app.data.remote.dto.WallabagInfoDto
import retrofit2.http.GET

/**
 * Authenticated Wallabag REST API. Requests are routed to the configured server by
 * [com.readbridge.app.data.remote.interceptor.HostSelectionInterceptor], have the
 * bearer token attached by [com.readbridge.app.data.remote.interceptor.AuthInterceptor],
 * and are transparently retried after a token refresh on 401.
 *
 * Entry/tag/annotation endpoints are added in Phase 2+.
 */
interface WallabagApi {

    @GET("api/info.json")
    suspend fun getInfo(): WallabagInfoDto
}
