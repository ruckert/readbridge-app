package com.readbridge.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EntryEntity::class, ReadingProgressEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ReadBridgeDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun readingProgressDao(): ReadingProgressDao

    companion object {
        const val NAME = "readbridge.db"
    }
}
