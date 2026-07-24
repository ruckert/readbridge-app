package com.readbridge.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.readbridge.app.data.local.db.EntryDao
import com.readbridge.app.data.local.sync.SyncStateStore
import com.readbridge.app.data.mapper.toArticle
import com.readbridge.app.data.mapper.toEntity
import com.readbridge.app.data.remote.api.WallabagApi
import com.readbridge.app.domain.article.ArticleRepository
import com.readbridge.app.domain.article.model.Article
import com.readbridge.app.domain.article.model.ArticleFilter
import com.readbridge.app.domain.article.model.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val api: WallabagApi,
    private val dao: EntryDao,
    private val syncStore: SyncStateStore,
) : ArticleRepository {

    override fun pagingData(filter: ArticleFilter): Flow<PagingData<Article>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                when (filter) {
                    ArticleFilter.All -> dao.pagingAll()
                    ArticleFilter.Unread -> dao.pagingUnread()
                    ArticleFilter.Starred -> dao.pagingStarred()
                    ArticleFilter.Archived -> dao.pagingArchived()
                }
            },
        ).flow.map { pagingData -> pagingData.map { it.toArticle() } }

    override suspend fun sync(fullRefresh: Boolean): SyncResult {
        return try {
            val startEpochSeconds = System.currentTimeMillis() / 1_000
            val since = if (fullRefresh) null else syncStore.lastSince()

            var page = 1
            var totalPages = 1
            var syncedCount = 0
            do {
                val response = api.getEntries(
                    sort = "updated",
                    order = if (since == null) "desc" else "asc",
                    page = page,
                    perPage = PAGE_SIZE,
                    since = since,
                    detail = "full",
                )
                totalPages = response.pages.coerceAtLeast(1)
                val entities = response.embedded.items.map { it.toEntity() }
                if (entities.isNotEmpty()) {
                    dao.upsertAll(entities)
                    syncedCount += entities.size
                }
                page++
            } while (page <= totalPages)

            // Advance the cursor to the moment sync started, so nothing updated during the
            // run is skipped next time.
            syncStore.setLastSince(startEpochSeconds)
            SyncResult.Success(syncedCount)
        } catch (e: IOException) {
            SyncResult.Error("Sem conexão com o servidor.")
        } catch (e: HttpException) {
            SyncResult.Error("Erro do servidor (HTTP ${e.code()}).")
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Falha ao sincronizar.")
        }
    }

    private companion object {
        const val PAGE_SIZE = 30
    }
}
