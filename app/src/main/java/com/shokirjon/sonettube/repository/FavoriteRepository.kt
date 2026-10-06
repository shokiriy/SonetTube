package com.shokirjon.sonettube.repository

import com.shokirjon.sonettube.data.local.FavoriteVideoDao
import com.shokirjon.sonettube.data.local.toFavoriteEntity
import com.shokirjon.sonettube.data.local.toVideo
import com.shokirjon.sonettube.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(private val dao: FavoriteVideoDao) {
    val favorites: Flow<List<Video>> = dao.observeAll().map { entities ->
        entities.map { it.toVideo() }
    }

    suspend fun isFavorite(videoId: String): Boolean = dao.findById(videoId) != null

    suspend fun findById(videoId: String): Video? = dao.findById(videoId)?.toVideo()

    suspend fun save(video: Video) {
        dao.upsert(video.toFavoriteEntity(System.currentTimeMillis()))
    }

    suspend fun remove(videoId: String) {
        dao.deleteById(videoId)
    }
}
