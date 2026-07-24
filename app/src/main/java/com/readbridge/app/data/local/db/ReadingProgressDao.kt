package com.readbridge.app.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ReadingProgressDao {

    @Query("SELECT scrollRatio FROM reading_progress WHERE entryId = :entryId")
    suspend fun getRatio(entryId: Long): Float?

    @Upsert
    suspend fun upsert(progress: ReadingProgressEntity)
}
