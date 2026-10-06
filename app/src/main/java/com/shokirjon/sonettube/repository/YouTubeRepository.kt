package com.shokirjon.sonettube.repository

import com.shokirjon.sonettube.BuildConfig
import com.shokirjon.sonettube.data.remote.YouTubeApiService
import com.shokirjon.sonettube.data.remote.toVideoOrNull
import com.shokirjon.sonettube.model.Video
import retrofit2.HttpException
import java.io.IOException

class YouTubeRepository(
    private val apiService: YouTubeApiService,
    private val apiKey: String = BuildConfig.YOUTUBE_API_KEY,
) {
    suspend fun search(query: String): List<Video> {
        if (apiKey.isBlank()) throw ApiKeyMissingException
        return try {
            apiService.search(query = query, apiKey = apiKey)
                .items
                .mapNotNull { it.toVideoOrNull() }
        } catch (error: HttpException) {
            when (error.code()) {
                400, 403 -> throw YouTubeApiException(ApiErrorReason.INVALID_KEY_OR_QUOTA, error)
                else -> throw YouTubeApiException(ApiErrorReason.SERVER, error)
            }
        } catch (error: IOException) {
            throw YouTubeApiException(ApiErrorReason.NETWORK, error)
        }
    }
}

object ApiKeyMissingException : IllegalStateException()

enum class ApiErrorReason {
    INVALID_KEY_OR_QUOTA,
    NETWORK,
    SERVER,
}

class YouTubeApiException(
    val reason: ApiErrorReason,
    cause: Throwable? = null,
) : RuntimeException(cause)
