package com.readbridge.app.data.remote.interceptor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HostSelectionInterceptorTest {

    @Test
    fun `rewrites onto root install`() {
        val url = HostSelectionInterceptor.buildUrlString(
            base = "https://wallabag.example.com",
            encodedPath = "/oauth/v2/token",
            encodedQuery = null,
        )
        assertEquals("https://wallabag.example.com/oauth/v2/token", url)
    }

    @Test
    fun `trailing slash on base is normalized`() {
        val url = HostSelectionInterceptor.buildUrlString(
            base = "https://wallabag.example.com/",
            encodedPath = "/api/info.json",
            encodedQuery = null,
        )
        assertEquals("https://wallabag.example.com/api/info.json", url)
    }

    @Test
    fun `preserves sub-path install prefix`() {
        val url = HostSelectionInterceptor.buildUrlString(
            base = "https://example.com/wallabag",
            encodedPath = "/api/entries.json",
            encodedQuery = "archive=0&page=1",
        )
        assertEquals("https://example.com/wallabag/api/entries.json?archive=0&page=1", url)
    }

    @Test
    fun `blank base returns null`() {
        assertNull(HostSelectionInterceptor.buildUrlString("   ", "/api/info.json", null))
    }
}
