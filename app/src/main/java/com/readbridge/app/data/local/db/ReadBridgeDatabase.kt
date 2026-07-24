package com.readbridge.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.readbridge.app.data.sync.PendingActionDao
import com.readbridge.app.data.sync.PendingActionEntity

@Database(
    entities = [EntryEntity::class, ReadingProgressEntity::class, PendingActionEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ReadBridgeDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun pendingActionDao(): PendingActionDao

    companion object {
        const val NAME = "readbridge.db"
    }
}
