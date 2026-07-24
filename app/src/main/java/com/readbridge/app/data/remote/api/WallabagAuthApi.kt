package com.readbridge.app.data.remote.api

import com.readbridge.app.data.remote.dto.TokenResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Unauthenticated OAuth2 endpoints. Backed by a dedicated OkHttp client WITHOUT the
 * token authenticator, so the refresh call can never recurse into itself.
 */
interface WallabagAuthApi {

    @FormUrlEncoded
    @POST("oauth/v2/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
    ): TokenResponseDto

    @FormUrlEncoded
    @POST("oauth/v2/token")
    suspend fun refresh(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token",
    ): TokenResponseDto
}
