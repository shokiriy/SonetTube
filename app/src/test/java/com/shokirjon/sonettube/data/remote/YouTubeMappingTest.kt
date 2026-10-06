package com.shokirjon.sonettube.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeMappingTest {
    @Test
    fun mapsHighestAvailableThumbnailAndMetadata() {
        val item = YouTubeSearchItemDto(
            id = YouTubeVideoIdDto("dQw4w9WgXcQ"),
            snippet = YouTubeSnippetDto(
                title = "A title",
                channelTitle = "A channel",
                publishedAt = "2024-01-01T00:00:00Z",
                thumbnails = YouTubeThumbnailsDto(
                    medium = YouTubeThumbnailDto("medium"),
                    high = YouTubeThumbnailDto("high"),
                    default = YouTubeThumbnailDto("default"),
                ),
            ),
        )

        val video = item.toVideoOrNull()

        assertEquals("dQw4w9WgXcQ", video?.videoId)
        assertEquals("high", video?.thumbnailUrl)
        assertEquals("A channel", video?.channelTitle)
    }

    @Test
    fun dropsItemsWithoutVideoIdOrSnippet() {
        assertNull(YouTubeSearchItemDto(null, null).toVideoOrNull())
        assertNull(YouTubeSearchItemDto(YouTubeVideoIdDto(null), YouTubeSnippetDto(null, null, null, null)).toVideoOrNull())
    }
}
