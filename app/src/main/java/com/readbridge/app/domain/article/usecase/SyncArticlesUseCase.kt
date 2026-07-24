package com.readbridge.app.domain.article.usecase

import com.readbridge.app.domain.article.ArticleRepository
import com.readbridge.app.domain.article.model.SyncResult
import javax.inject.Inject

class SyncArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository,
) {
    suspend operator fun invoke(fullRefresh: Boolean = false): SyncResult =
        repository.sync(fullRefresh)
}
