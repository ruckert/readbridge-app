package com.readbridge.app.data.local.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_state")

/** Persists the incremental-sync cursor (last `since` timestamp, in epoch seconds). */
@Singleton
class SyncStateStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun lastSince(): Long? =
        context.syncDataStore.data.map { it[KEY_LAST_SINCE] }.first()

    suspend fun setLastSince(epochSeconds: Long) {
        context.syncDataStore.edit { it[KEY_LAST_SINCE] = epochSeconds }
    }

    suspend fun clear() {
        context.syncDataStore.edit { it.remove(KEY_LAST_SINCE) }
    }

    private companion object {
        val KEY_LAST_SINCE = longPreferencesKey("last_since")
    }
}
