package com.readbridge.app.domain.reader

import com.readbridge.app.domain.reader.model.ReaderArticle
import kotlinx.coroutines.flow.Flow

/** Reader-specific access: article content, starred/archived actions, reading progress. */
interface ReaderRepository {

    /** Observe a single cached article (reflects optimistic star/archive changes live). */
    fun observeArticle(id: Long): Flow<ReaderArticle?>

    /** Set starred: optimistic local update + queued for sync (survives offline). */
    suspend fun setStarred(id: Long, starred: Boolean)

    /** Set archived: optimistic local update + queued for sync (survives offline). */
    suspend fun setArchived(id: Long, archived: Boolean)

    /** Last saved scroll ratio (0f..1f) for [id], or 0 if none. */
    suspend fun getProgress(id: Long): Float

    /** Persist scroll [ratio] (0f..1f) for [id]. */
    suspend fun saveProgress(id: Long, ratio: Float)
}
