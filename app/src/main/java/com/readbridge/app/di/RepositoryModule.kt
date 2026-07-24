package com.readbridge.app.di

import com.readbridge.app.data.repository.ArticleRepositoryImpl
import com.readbridge.app.data.repository.AuthRepositoryImpl
import com.readbridge.app.domain.article.ArticleRepository
import com.readbridge.app.domain.auth.AuthRepository
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
}
