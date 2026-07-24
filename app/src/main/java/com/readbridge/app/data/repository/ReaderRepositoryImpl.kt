package com.readbridge.app.data.repository

import com.readbridge.app.data.local.db.EntryDao
import com.readbridge.app.data.local.db.ReadingProgressDao
import com.readbridge.app.data.local.db.ReadingProgressEntity
import com.readbridge.app.data.mapper.toReaderArticle
import com.readbridge.app.data.remote.api.WallabagApi
import com.readbridge.app.domain.reader.ReaderRepository
import com.readbridge.app.domain.reader.model.ReaderArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderRepositoryImpl @Inject constructor(
    private val api: WallabagApi,
    private val entryDao: EntryDao,
    private val progressDao: ReadingProgressDao,
) : ReaderRepository {

    override fun observeArticle(id: Long): Flow<ReaderArticle?> =
        entryDao.observeById(id).map { it?.toReaderArticle() }

    override suspend fun setStarred(id: Long, starred: Boolean): Boolean =
        applyFlag(
            optimistic = { entryDao.setStarred(id, starred) },
            revert = { entryDao.setStarred(id, !starred) },
            remote = { api.updateEntry(id = id, starred = starred.toFlag()) },
        )

    override suspend fun setArchived(id: Long, archived: Boolean): Boolean =
        applyFlag(
            optimistic = { entryDao.setArchived(id, archived) },
            revert = { entryDao.setArchived(id, !archived) },
            remote = { api.updateEntry(id = id, archive = archived.toFlag()) },
        )

    override suspend fun getProgress(id: Long): Float = progressDao.getRatio(id) ?: 0f

    override suspend fun saveProgress(id: Long, ratio: Float) {
        progressDao.upsert(
            ReadingProgressEntity(
                entryId = id,
                scrollRatio = ratio.coerceIn(0f, 1f),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    /** Optimistic local write, then remote; revert locally if the server rejects it. */
    private suspend inline fun applyFlag(
        optimistic: () -> Unit,
        revert: () -> Unit,
        remote: () -> Unit,
    ): Boolean {
        optimistic()
        return try {
            remote()
            true
        } catch (e: Exception) {
            revert()
            false
        }
    }

    private fun Boolean.toFlag(): Int = if (this) 1 else 0
}
