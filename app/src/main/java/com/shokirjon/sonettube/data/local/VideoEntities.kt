package com.shokirjon.sonettube.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shokirjon.sonettube.model.Video

@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val addedAt: Long,
)

@Entity(tableName = "recent_videos")
data class RecentVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val watchedAt: Long,
)

fun FavoriteVideoEntity.toVideo() = Video(
    videoId = videoId,
    title = title,
    channelTitle = channelTitle,
    thumbnailUrl = thumbnailUrl,
)

fun RecentVideoEntity.toVideo() = Video(
    videoId = videoId,
    title = title,
    channelTitle = channelTitle,
    thumbnailUrl = thumbnailUrl,
)

fun Video.toFavoriteEntity(now: Long) = FavoriteVideoEntity(
    videoId = videoId,
    title = title,
    channelTitle = channelTitle,
    thumbnailUrl = thumbnailUrl,
    addedAt = now,
)

fun Video.toRecentEntity(now: Long) = RecentVideoEntity(
    videoId = videoId,
    title = title,
    channelTitle = channelTitle,
    thumbnailUrl = thumbnailUrl,
    watchedAt = now,
)
