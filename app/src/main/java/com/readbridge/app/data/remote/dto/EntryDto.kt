package com.readbridge.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single Wallabag entry (`/api/entries` item). Booleans are 0/1 integers on the wire. */
@Serializable
data class EntryDto(
    val id: Long,
    val title: String? = null,
    val url: String? = null,
    @SerialName("domain_name") val domainName: String? = null,
    val content: String? = null,
    @SerialName("preview_picture") val previewPicture: String? = null,
    @SerialName("reading_time") val readingTime: Int = 0,
    @SerialName("is_archived") val isArchived: Int = 0,
    @SerialName("is_starred") val isStarred: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val tags: List<TagDto> = emptyList(),
)

@Serializable
data class TagDto(
    val id: Long,
    val label: String,
    val slug: String? = null,
)

/** HAL-style paginated response of `GET /api/entries.json`. */
@Serializable
data class EntriesResponseDto(
    val page: Int = 1,
    val limit: Int = 0,
    val pages: Int = 1,
    val total: Int = 0,
    @SerialName("_embedded") val embedded: EmbeddedEntriesDto = EmbeddedEntriesDto(),
)

@Serializable
data class EmbeddedEntriesDto(
    val items: List<EntryDto> = emptyList(),
)
