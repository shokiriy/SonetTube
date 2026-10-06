package com.shokirjon.sonettube.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("youtube/v3/search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("q") query: String,
        @Query("key") apiKey: String,
    ): YouTubeSearchResponseDto
}
