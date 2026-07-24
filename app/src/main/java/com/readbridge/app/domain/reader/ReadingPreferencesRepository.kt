package com.readbridge.app.domain.reader

import com.readbridge.app.domain.reader.model.ReadingPreferences
import kotlinx.coroutines.flow.Flow

/** Global, persisted reading preferences (PLAN §6-A.4). */
interface ReadingPreferencesRepository {
    val preferences: Flow<ReadingPreferences>

    suspend fun update(preferences: ReadingPreferences)
}
