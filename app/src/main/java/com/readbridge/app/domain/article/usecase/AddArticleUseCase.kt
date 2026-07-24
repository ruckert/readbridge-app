package com.readbridge.app.domain.article.usecase

import com.readbridge.app.domain.article.ArticleRepository
import javax.inject.Inject

class AddArticleUseCase @Inject constructor(
    private val repository: ArticleRepository,
) {
    suspend operator fun invoke(url: String) = repository.addUrl(url.trim())
}
