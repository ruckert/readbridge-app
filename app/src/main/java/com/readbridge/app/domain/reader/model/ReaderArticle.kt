package com.readbridge.app.domain.reader.model

/** Full article as shown in the reader, including cached HTML [contentHtml]. */
data class ReaderArticle(
    val id: Long,
    val title: String?,
    val url: String?,
    val domainName: String?,
    val contentHtml: String,
    val isStarred: Boolean,
    val isArchived: Boolean,
)
