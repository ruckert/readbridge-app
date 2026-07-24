package com.readbridge.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-scoped Hilt module. Providers for networking (Retrofit/OkHttp),
 * persistence (Room/DataStore) and repositories are added here in later phases.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
