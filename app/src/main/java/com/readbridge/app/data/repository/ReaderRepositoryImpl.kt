package com.readbridge.app.data.repository

import com.readbridge.app.data.local.db.EntryDao
import com.readbridge.app.data.local.db.ReadingProgressDao
import com.readbridge.app.data.local.db.ReadingProgressEntity
import com.readbridge.app.data.mapper.toReaderArticle
import com.readbridge.app.data.sync.OutboxManager
import com.readbridge.app.domain.reader.ReaderRepository
import com.readbridge.app.domain.reader.model.ReaderArticle
import com.readbridge.app.domain.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao,
    private val progressDao: ReadingProgressDao,
    private val outboxManager: OutboxManager,
    private val syncScheduler: SyncScheduler,
) : ReaderRepository {

    override fun observeArticle(id: Long): Flow<ReaderArticle?> =
        entryDao.observeById(id).map { it?.toReaderArticle() }

    override suspend fun setStarred(id: Long, starred: Boolean) {
        entryDao.setStarred(id, starred)          // optimistic
        outboxManager.enqueueSetStarred(id, starred)
        syncScheduler.requestSync()
    }

    override suspend fun setArchived(id: Long, archived: Boolean) {
        entryDao.setArchived(id, archived)        // optimistic
        outboxManager.enqueueSetArchived(id, archived)
        syncScheduler.requestSync()
    }

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
}
