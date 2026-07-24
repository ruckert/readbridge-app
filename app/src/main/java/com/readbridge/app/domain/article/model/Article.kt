package com.readbridge.app.domain.article.model

/** An article as shown in the list. Full HTML [content] is cached for the reader (Phase 3). */
data class Article(
    val id: Long,
    val title: String?,
    val url: String?,
    val domainName: String?,
    val previewPicture: String?,
    val readingTimeMinutes: Int,
    val isStarred: Boolean,
    val isArchived: Boolean,
    val tags: List<String>,
)
