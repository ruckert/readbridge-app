package com.readbridge.app.data.mapper

import com.readbridge.app.data.local.db.EntryEntity
import com.readbridge.app.data.remote.dto.EntryDto
import com.readbridge.app.data.util.DateParsing
import com.readbridge.app.domain.article.model.Article

fun EntryDto.toEntity(): EntryEntity = EntryEntity(
    id = id,
    title = title,
    url = url,
    domainName = domainName,
    content = content,
    previewPicture = previewPicture?.takeIf { it.isNotBlank() },
    readingTime = readingTime,
    isArchived = isArchived == 1,
    isStarred = isStarred == 1,
    createdAtEpoch = DateParsing.toEpochSeconds(createdAt),
    updatedAtEpoch = DateParsing.toEpochSeconds(updatedAt),
    tags = tags.map { it.label },
)

fun EntryEntity.toArticle(): Article = Article(
    id = id,
    title = title,
    url = url,
    domainName = domainName,
    previewPicture = previewPicture,
    readingTimeMinutes = readingTime,
    isStarred = isStarred,
    isArchived = isArchived,
    tags = tags,
)
