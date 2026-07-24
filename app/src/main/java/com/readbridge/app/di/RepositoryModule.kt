package com.readbridge.app.di

import com.readbridge.app.data.repository.ArticleRepositoryImpl
import com.readbridge.app.data.repository.AuthRepositoryImpl
import com.readbridge.app.data.repository.ReaderRepositoryImpl
import com.readbridge.app.data.repository.ReadingPreferencesRepositoryImpl
import com.readbridge.app.domain.article.ArticleRepository
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.data.sync.WorkManagerSyncScheduler
import com.readbridge.app.domain.reader.ReaderRepository
import com.readbridge.app.domain.reader.ReadingPreferencesRepository
import com.readbridge.app.domain.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindReaderRepository(impl: ReaderRepositoryImpl): ReaderRepository

    @Binds
    @Singleton
    abstract fun bindReadingPreferencesRepository(
        impl: ReadingPreferencesRepositoryImpl,
    ): ReadingPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler
}
