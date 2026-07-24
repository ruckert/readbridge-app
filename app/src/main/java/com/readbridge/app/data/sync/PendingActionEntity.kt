package com.readbridge.app.data.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A queued action to replay against the server (the offline outbox). */
@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val entryId: Long?,
    val url: String?,
    val flag: Boolean?,
    val createdAt: Long,
)

enum class PendingActionType { SetStarred, SetArchived, AddUrl }
