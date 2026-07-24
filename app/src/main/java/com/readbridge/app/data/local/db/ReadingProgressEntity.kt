package com.readbridge.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-article reading position. Kept in its own table (not on [EntryEntity]) so the
 * entries upsert during sync never overwrites the user's progress.
 */
@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val entryId: Long,
    val scrollRatio: Float,
    val updatedAt: Long,
)
