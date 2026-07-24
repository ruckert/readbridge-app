package com.readbridge.app.data.local.db

import androidx.room.TypeConverter

/** Room converters. Tags are joined with the ASCII unit-separator to avoid clashing with tag text. */
class Converters {
    @TypeConverter
    fun fromTags(tags: List<String>): String = tags.joinToString(SEPARATOR)

    @TypeConverter
    fun toTags(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        // ASCII unit separator (0x1F) — will not appear in normal tag labels.
        private val SEPARATOR: String = 0x1F.toChar().toString()
    }
}
