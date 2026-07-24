package com.readbridge.app.data.remote.interceptor

import com.readbridge.app.data.local.auth.AuthLocalDataSource
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit uses a fixed placeholder base URL; this interceptor rewrites each request
 * onto the user's configured Wallabag instance (which is only known at/after login).
 * Supports sub-path installs, e.g. `https://example.com/wallabag`.
 */
@Singleton
class HostSelectionInterceptor @Inject constructor(
    private val local: AuthLocalDataSource,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val base = local.serverConfig()?.serverUrl
            ?: throw IOException("No Wallabag server configured")
        val rewritten = buildUrl(base, request.url.encodedPath, request.url.encodedQuery)
            ?: throw IOException("Invalid server URL: $base")
        return chain.proceed(request.newBuilder().url(rewritten).build())
    }

    companion object {
        /**
         * Resolve [encodedPath] (+ optional [encodedQuery]) against the configured
         * [base] origin/prefix. Returns the absolute URL string, or null if invalid.
         * Package-visible + pure so it can be unit-tested without a server.
         */
        fun buildUrlString(base: String, encodedPath: String, encodedQuery: String?): String? {
            if (base.isBlank()) return null
            val cleanBase = base.trim().trimEnd('/')
            val path = if (encodedPath.startsWith("/")) encodedPath else "/$encodedPath"
            val query = encodedQuery?.let { "?$it" }.orEmpty()
            return cleanBase + path + query
        }

        private fun buildUrl(base: String, encodedPath: String, encodedQuery: String?) =
            buildUrlString(base, encodedPath, encodedQuery)?.toHttpUrlOrNull()
    }
}
