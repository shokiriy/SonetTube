package com.shokirjon.sonettube.data.remote

import com.shokirjon.sonettube.model.Video

data class YouTubeSearchResponseDto(
    val items: List<YouTubeSearchItemDto> = emptyList(),
)

data class YouTubeSearchItemDto(
    val id: YouTubeVideoIdDto?,
    val snippet: YouTubeSnippetDto?,
)

data class YouTubeVideoIdDto(
    val videoId: String?,
)

data class YouTubeSnippetDto(
    val title: String?,
    val channelTitle: String?,
    val publishedAt: String?,
    val thumbnails: YouTubeThumbnailsDto?,
)

data class YouTubeThumbnailsDto(
    val medium: YouTubeThumbnailDto?,
    val high: YouTubeThumbnailDto?,
    val default: YouTubeThumbnailDto?,
)

data class YouTubeThumbnailDto(
    val url: String?,
)

fun YouTubeSearchItemDto.toVideoOrNull(): Video? {
    val videoId = id?.videoId?.takeIf { it.isNotBlank() } ?: return null
    val snippet = snippet ?: return null
    return Video(
        videoId = videoId,
        title = snippet.title.orEmpty(),
        channelTitle = snippet.channelTitle.orEmpty(),
        thumbnailUrl = snippet.thumbnails?.high?.url
            ?: snippet.thumbnails?.medium?.url
            ?: snippet.thumbnails?.default?.url
            .orEmpty(),
        publishedAt = snippet.publishedAt,
    )
}
