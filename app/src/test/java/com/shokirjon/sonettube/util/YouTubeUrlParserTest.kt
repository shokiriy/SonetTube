package com.shokirjon.sonettube.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUrlParserTest {
    private val videoId = "dQw4w9WgXcQ"

    @Test
    fun extractsFromStandardWatchUrl() {
        assertEquals(videoId, YouTubeUrlParser.extractVideoId("https://www.youtube.com/watch?v=$videoId"))
    }

    @Test
    fun extractsFromShortUrlWithExtraQueryParameters() {
        assertEquals(videoId, YouTubeUrlParser.extractVideoId("https://youtu.be/$videoId?t=42"))
    }

    @Test
    fun extractsFromShortsAndEmbedUrls() {
        assertEquals(videoId, YouTubeUrlParser.extractVideoId("https://youtube.com/shorts/$videoId"))
        assertEquals(videoId, YouTubeUrlParser.extractVideoId("https://www.youtube.com/embed/$videoId"))
    }

    @Test
    fun acceptsRawVideoId() {
        assertEquals(videoId, YouTubeUrlParser.extractVideoId("  $videoId "))
    }

    @Test
    fun rejectsOtherHostsAndMalformedIds() {
        assertNull(YouTubeUrlParser.extractVideoId("https://example.com/watch?v=$videoId"))
        assertNull(YouTubeUrlParser.extractVideoId("https://youtube.com/watch?v=too-short"))
        assertNull(YouTubeUrlParser.extractVideoId(""))
    }
}
