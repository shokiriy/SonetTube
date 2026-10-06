package com.shokirjon.sonettube.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteVideoEntity::class, RecentVideoEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SonetTubeDatabase : RoomDatabase() {
    abstract fun favoriteVideoDao(): FavoriteVideoDao
    abstract fun recentVideoDao(): RecentVideoDao
}
