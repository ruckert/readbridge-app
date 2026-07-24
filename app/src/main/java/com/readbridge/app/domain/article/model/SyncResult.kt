package com.readbridge.app.domain.article.model

/** Outcome of a sync pass against the Wallabag server. */
sealed interface SyncResult {
    data class Success(val syncedCount: Int) : SyncResult
    data class Error(val message: String) : SyncResult
}
