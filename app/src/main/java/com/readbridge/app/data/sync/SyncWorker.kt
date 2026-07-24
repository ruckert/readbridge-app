package com.readbridge.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.readbridge.app.domain.article.ArticleRepository
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.model.AuthState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Drains the offline outbox, then pulls incremental updates. Runs one-off (after an action)
 * and periodically. Returns retry() when the outbox has transient failures so WorkManager
 * backs off and tries again.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val outboxManager: OutboxManager,
    private val articleRepository: ArticleRepository,
    private val authRepository: AuthRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Nothing to do (and nothing to retry) when logged out.
        if (authRepository.authState.value != AuthState.Authenticated) {
            return Result.success()
        }

        val outboxResult = outboxManager.process()
        // Pull the latest regardless; ignore its outcome here (list already shows cached data).
        articleRepository.sync(fullRefresh = false)

        return if (outboxResult == OutboxResult.Retry) Result.retry() else Result.success()
    }
}
