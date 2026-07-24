package com.readbridge.app.data.remote.interceptor

import com.readbridge.app.data.local.auth.AuthLocalDataSource
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Attaches the current bearer token to authenticated API requests. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val local: AuthLocalDataSource,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = local.accessToken
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
