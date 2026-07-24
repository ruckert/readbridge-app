package com.readbridge.app.di

import android.content.Context
import androidx.room.Room
import com.readbridge.app.data.local.db.EntryDao
import com.readbridge.app.data.local.db.ReadBridgeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReadBridgeDatabase =
        Room.databaseBuilder(context, ReadBridgeDatabase::class.java, ReadBridgeDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideEntryDao(database: ReadBridgeDatabase): EntryDao = database.entryDao()
}
