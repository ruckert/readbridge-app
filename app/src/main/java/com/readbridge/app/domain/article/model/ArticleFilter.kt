package com.readbridge.app.domain.article.model

/** Which subset of the cached articles the list shows. */
enum class ArticleFilter {
    All,
    Unread,
    Starred,
    Archived,
}
