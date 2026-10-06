package com.shokirjon.sonettube.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteVideoDao {
    @Query("SELECT * FROM favorite_videos ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteVideoEntity>>

    @Query("SELECT * FROM favorite_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun findById(videoId: String): FavoriteVideoEntity?

    @Upsert
    suspend fun upsert(video: FavoriteVideoEntity)

    @Query("DELETE FROM favorite_videos WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)
}

@Dao
interface RecentVideoDao {
    @Query("SELECT * FROM recent_videos ORDER BY watchedAt DESC")
    fun observeAll(): Flow<List<RecentVideoEntity>>

    @Query("SELECT * FROM recent_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun findById(videoId: String): RecentVideoEntity?

    @Upsert
    suspend fun upsert(video: RecentVideoEntity)

    @Query("DELETE FROM recent_videos WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)

    @Query("DELETE FROM recent_videos")
    suspend fun deleteAll()

    @Query(
        "DELETE FROM recent_videos WHERE videoId NOT IN " +
            "(SELECT videoId FROM recent_videos ORDER BY watchedAt DESC LIMIT :maxRecords)",
    )
    suspend fun trimTo(maxRecords: Int)
}
