package com.readbridge.app.data.sync

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PendingActionDao {

    @Insert
    suspend fun insert(action: PendingActionEntity)

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC, id ASC")
    suspend fun getAll(): List<PendingActionEntity>

    @Delete
    suspend fun delete(action: PendingActionEntity)

    @Query("DELETE FROM pending_actions")
    suspend fun clear()
}
