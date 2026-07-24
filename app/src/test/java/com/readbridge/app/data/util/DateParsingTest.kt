package com.readbridge.app.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DateParsingTest {

    @Test
    fun `parses numeric-offset timestamp (Wallabag format)`() {
        val utc = DateParsing.toEpochSeconds("2023-01-15T12:34:56+0000")
        val plusOne = DateParsing.toEpochSeconds("2023-01-15T12:34:56+0100")
        // +0100 is one hour ahead of UTC, so the same wall time is 3600s earlier in epoch.
        assertEquals(3600L, utc - plusOne)
    }

    @Test
    fun `parses colon-offset variant equivalently`() {
        assertEquals(
            DateParsing.toEpochSeconds("2023-01-15T12:34:56+0000"),
            DateParsing.toEpochSeconds("2023-01-15T12:34:56+00:00"),
        )
    }

    @Test
    fun `returns zero for null blank or garbage`() {
        assertEquals(0L, DateParsing.toEpochSeconds(null))
        assertEquals(0L, DateParsing.toEpochSeconds("   "))
        assertEquals(0L, DateParsing.toEpochSeconds("not-a-date"))
    }
}
