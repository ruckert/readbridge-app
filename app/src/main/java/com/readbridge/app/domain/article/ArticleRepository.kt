package com.readbridge.app.domain.article

import androidx.paging.PagingData
import com.readbridge.app.domain.article.model.Article
import com.readbridge.app.domain.article.model.ArticleFilter
import com.readbridge.app.domain.article.model.SyncResult
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first access to the article list. The list is always served from the local
 * cache (Room); [sync] pulls updates from Wallabag into that cache.
 */
interface ArticleRepository {
    /** Paged, reactive stream of cached articles matching [filter]. */
    fun pagingData(filter: ArticleFilter): Flow<PagingData<Article>>

    /**
     * Pull changes from the server into the cache.
     * @param fullRefresh ignore the incremental cursor and re-fetch everything.
     */
    suspend fun sync(fullRefresh: Boolean = false): SyncResult
}
