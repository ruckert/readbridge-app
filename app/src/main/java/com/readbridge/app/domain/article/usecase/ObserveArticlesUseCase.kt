package com.readbridge.app.domain.article.usecase

import androidx.paging.PagingData
import com.readbridge.app.domain.article.ArticleRepository
import com.readbridge.app.domain.article.model.Article
import com.readbridge.app.domain.article.model.ArticleFilter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository,
) {
    operator fun invoke(filter: ArticleFilter): Flow<PagingData<Article>> =
        repository.pagingData(filter)
}
