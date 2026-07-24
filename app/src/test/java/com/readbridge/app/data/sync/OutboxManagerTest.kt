package com.readbridge.app.data.sync

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class OutboxManagerTest {

    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("text/plain".toMediaType())))

    @Test
    fun `network errors are retried`() {
        assertEquals(FailureHandling.Retry, OutboxManager.classifyFailure(IOException("offline")))
    }

    @Test
    fun `client errors drop the poison action`() {
        assertEquals(FailureHandling.Drop, OutboxManager.classifyFailure(httpException(404)))
        assertEquals(FailureHandling.Drop, OutboxManager.classifyFailure(httpException(400)))
    }

    @Test
    fun `server errors are retried`() {
        assertEquals(FailureHandling.Retry, OutboxManager.classifyFailure(httpException(500)))
    }

    @Test
    fun `unexpected errors are retried`() {
        assertEquals(FailureHandling.Retry, OutboxManager.classifyFailure(RuntimeException("?")))
    }
}
