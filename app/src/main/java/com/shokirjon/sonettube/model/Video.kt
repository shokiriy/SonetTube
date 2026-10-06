package com.shokirjon.sonettube.model

data class Video(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val publishedAt: String? = null,
)
