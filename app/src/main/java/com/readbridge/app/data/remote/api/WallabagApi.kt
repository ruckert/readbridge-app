package com.readbridge.app.data.remote.api

import com.readbridge.app.data.remote.dto.EntriesResponseDto
import com.readbridge.app.data.remote.dto.EntryDto
import com.readbridge.app.data.remote.dto.WallabagInfoDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    /**
     * List entries. All filters are optional; [archive]/[starred] are 0/1 or null (both).
     * [since] is a UNIX timestamp for incremental sync; [detail]=`full` includes HTML content.
     */
    @GET("api/entries.json")
    suspend fun getEntries(
        @Query("archive") archive: Int? = null,
        @Query("starred") starred: Int? = null,
        @Query("sort") sort: String = "updated",
        @Query("order") order: String = "desc",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 30,
        @Query("since") since: Long? = null,
        @Query("detail") detail: String = "full",
    ): EntriesResponseDto

    /** Update an entry's flags. [archive]/[starred] are 0/1; pass null to leave unchanged. */
    @FormUrlEncoded
    @PATCH("api/entries/{id}.json")
    suspend fun updateEntry(
        @Path("id") id: Long,
        @Field("archive") archive: Int? = null,
        @Field("starred") starred: Int? = null,
    ): EntryDto

    /** Save a new article by URL. Returns the created (or existing) entry. */
    @FormUrlEncoded
    @POST("api/entries.json")
    suspend fun addEntry(
        @Field("url") url: String,
    ): EntryDto
}
