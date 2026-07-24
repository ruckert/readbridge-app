package com.readbridge.app.data.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Parses Wallabag timestamps to epoch seconds. Wallabag emits ISO-8601 with a numeric
 * offset like `2023-01-15T12:34:56+0100`; we also accept the colon-offset variant.
 * Returns 0 when absent/unparseable (used only for local ordering, never as a sync cursor).
 */
object DateParsing {

    private val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
    )

    fun toEpochSeconds(value: String?): Long {
        if (value.isNullOrBlank()) return 0
        for (formatter in formatters) {
            try {
                return OffsetDateTime.parse(value, formatter).toEpochSecond()
            } catch (_: Exception) {
                // try next format
            }
        }
        return 0
    }
}
