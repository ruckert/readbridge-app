package com.readbridge.app.data.sync

import com.readbridge.app.data.local.db.EntryDao
import com.readbridge.app.data.mapper.toEntity
import com.readbridge.app.data.remote.api.WallabagApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Result of draining the outbox once. */
enum class OutboxResult { Empty, Processed, Retry }

/** How a failed replay should be handled. */
enum class FailureHandling { Drop, Retry }

/**
 * The offline action outbox: queues star/archive/add-url actions and replays them against
 * the server. Actions are applied optimistically to Room by the callers; this only pushes
 * them. A permanent (4xx) failure drops the poison action; a transient one asks for retry.
 */
@Singleton
class OutboxManager @Inject constructor(
    private val pendingActionDao: PendingActionDao,
    private val entryDao: EntryDao,
    private val api: WallabagApi,
) {

    suspend fun enqueueSetStarred(entryId: Long, starred: Boolean) =
        enqueue(PendingActionType.SetStarred, entryId = entryId, flag = starred)

    suspend fun enqueueSetArchived(entryId: Long, archived: Boolean) =
        enqueue(PendingActionType.SetArchived, entryId = entryId, flag = archived)

    suspend fun enqueueAddUrl(url: String) =
        enqueue(PendingActionType.AddUrl, url = url)

    private suspend fun enqueue(
        type: PendingActionType,
        entryId: Long? = null,
        url: String? = null,
        flag: Boolean? = null,
    ) {
        pendingActionDao.insert(
            PendingActionEntity(
                type = type.name,
                entryId = entryId,
                url = url,
                flag = flag,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    /** Replay queued actions oldest-first. Stops at the first transient failure. */
    suspend fun process(): OutboxResult {
        val actions = pendingActionDao.getAll()
        if (actions.isEmpty()) return OutboxResult.Empty

        for (action in actions) {
            try {
                execute(action)
                pendingActionDao.delete(action)
            } catch (e: Exception) {
                when (classifyFailure(e)) {
                    FailureHandling.Drop -> pendingActionDao.delete(action)
                    FailureHandling.Retry -> return OutboxResult.Retry
                }
            }
        }
        return OutboxResult.Processed
    }

    private suspend fun execute(action: PendingActionEntity) {
        when (PendingActionType.valueOf(action.type)) {
            PendingActionType.SetStarred ->
                api.updateEntry(id = action.entryId!!, starred = action.flag!!.toFlag())
            PendingActionType.SetArchived ->
                api.updateEntry(id = action.entryId!!, archive = action.flag!!.toFlag())
            PendingActionType.AddUrl -> {
                val entry = api.addEntry(action.url!!)
                entryDao.upsertAll(listOf(entry.toEntity()))
            }
        }
    }

    private fun Boolean.toFlag(): Int = if (this) 1 else 0

    companion object {
        /**
         * Pure failure classification (unit-tested): 4xx responses are permanent → drop the
         * action; network errors and 5xx are transient → retry later.
         */
        fun classifyFailure(error: Throwable): FailureHandling = when (error) {
            is IOException -> FailureHandling.Retry
            is HttpException -> if (error.code() in 400..499) FailureHandling.Drop else FailureHandling.Retry
            else -> FailureHandling.Retry
        }
    }
}
