package com.readbridge.app.di

import com.readbridge.app.BuildConfig
import com.readbridge.app.data.remote.api.WallabagApi
import com.readbridge.app.data.remote.api.WallabagAuthApi
import com.readbridge.app.data.remote.interceptor.AuthInterceptor
import com.readbridge.app.data.remote.interceptor.HostSelectionInterceptor
import com.readbridge.app.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Retrofit needs a valid absolute base URL; the real host is applied per-request
    // by HostSelectionInterceptor. This placeholder is never actually contacted.
    private const val PLACEHOLDER_BASE_URL = "https://readbridge.invalid/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }

    @Provides
    @Singleton
    @AuthClient
    fun provideAuthClient(
        hostSelection: HostSelectionInterceptor,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = baseClientBuilder()
        .addInterceptor(hostSelection)
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    @ApiClient
    fun provideApiClient(
        hostSelection: HostSelectionInterceptor,
        auth: AuthInterceptor,
        authenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = baseClientBuilder()
        .addInterceptor(hostSelection)
        .addInterceptor(auth)
        .authenticator(authenticator)
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthClient client: OkHttpClient,
        json: Json,
    ): WallabagAuthApi = retrofit(client, json).create(WallabagAuthApi::class.java)

    @Provides
    @Singleton
    fun provideWallabagApi(
        @ApiClient client: OkHttpClient,
        json: Json,
    ): WallabagApi = retrofit(client, json).create(WallabagApi::class.java)

    private fun baseClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

    private fun retrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
