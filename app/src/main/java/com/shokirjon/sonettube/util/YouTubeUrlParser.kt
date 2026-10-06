package com.shokirjon.sonettube.util

import java.net.URI
import java.net.URLDecoder

object YouTubeUrlParser {
    private val videoIdPattern = Regex("^[A-Za-z0-9_-]{11}$")
    private val supportedHosts = setOf("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be")

    fun extractVideoId(input: String): String? {
        val value = input.trim()
        if (videoIdPattern.matches(value)) return value
        if (value.isBlank()) return null

        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host !in supportedHosts) return null

        val candidate = when {
            host == "youtu.be" -> uri.pathSegments().firstOrNull()
            uri.path.equals("/watch", ignoreCase = true) -> queryParameter(uri.rawQuery, "v")
            uri.pathSegments().firstOrNull()?.lowercase() in setOf("shorts", "embed", "live") ->
                uri.pathSegments().getOrNull(1)
            else -> null
        }
        return candidate?.takeIf { videoIdPattern.matches(it) }
    }

    private fun URI.pathSegments(): List<String> = path
        ?.split('/')
        ?.filter(String::isNotBlank)
        .orEmpty()

    private fun queryParameter(query: String?, key: String): String? = query
        ?.split('&')
        ?.asSequence()
        ?.map { it.split('=', limit = 2) }
        ?.firstOrNull { it.firstOrNull() == key }
        ?.getOrNull(1)
        ?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() }
}
