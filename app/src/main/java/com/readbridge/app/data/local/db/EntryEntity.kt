package com.readbridge.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached Wallabag entry. [content] holds the reader HTML so articles open offline. */
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: Long,
    val title: String?,
    val url: String?,
    val domainName: String?,
    val content: String?,
    val previewPicture: String?,
    val readingTime: Int,
    val isArchived: Boolean,
    val isStarred: Boolean,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    val tags: List<String>,
)
