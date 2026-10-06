package com.shokirjon.sonettube.repository

import com.shokirjon.sonettube.data.local.RecentVideoDao
import com.shokirjon.sonettube.data.local.toRecentEntity
import com.shokirjon.sonettube.data.local.toVideo
import com.shokirjon.sonettube.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(private val dao: RecentVideoDao) {
    val history: Flow<List<Video>> = dao.observeAll().map { entities ->
        entities.map { it.toVideo() }
    }

    suspend fun findById(videoId: String): Video? = dao.findById(videoId)?.toVideo()

    suspend fun record(video: Video) {
        dao.upsert(video.toRecentEntity(System.currentTimeMillis()))
        dao.trimTo(MAX_HISTORY_SIZE)
    }

    suspend fun remove(videoId: String) {
        dao.deleteById(videoId)
    }

    suspend fun clear() {
        dao.deleteAll()
    }

    private companion object {
        const val MAX_HISTORY_SIZE = 100
    }
}
