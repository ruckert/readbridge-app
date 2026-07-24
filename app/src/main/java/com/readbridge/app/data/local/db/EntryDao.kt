package com.readbridge.app.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Upsert
    suspend fun upsertAll(entries: List<EntryEntity>)

    @Query("SELECT * FROM entries ORDER BY updatedAtEpoch DESC, id DESC")
    fun pagingAll(): PagingSource<Int, EntryEntity>

    @Query("SELECT * FROM entries WHERE isArchived = 0 ORDER BY updatedAtEpoch DESC, id DESC")
    fun pagingUnread(): PagingSource<Int, EntryEntity>

    @Query("SELECT * FROM entries WHERE isStarred = 1 ORDER BY updatedAtEpoch DESC, id DESC")
    fun pagingStarred(): PagingSource<Int, EntryEntity>

    @Query("SELECT * FROM entries WHERE isArchived = 1 ORDER BY updatedAtEpoch DESC, id DESC")
    fun pagingArchived(): PagingSource<Int, EntryEntity>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun findById(id: Long): EntryEntity?

    @Query("SELECT * FROM entries WHERE id = :id")
    fun observeById(id: Long): Flow<EntryEntity?>

    @Query("UPDATE entries SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Query("UPDATE entries SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun count(): Int

    @Query("DELETE FROM entries")
    suspend fun clear()
}
