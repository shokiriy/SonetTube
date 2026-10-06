package com.shokirjon.sonettube.app

import android.app.Application
import androidx.room.Room
import com.shokirjon.sonettube.data.local.SonetTubeDatabase
import com.shokirjon.sonettube.data.remote.YouTubeApiService
import com.shokirjon.sonettube.repository.FavoriteRepository
import com.shokirjon.sonettube.repository.HistoryRepository
import com.shokirjon.sonettube.repository.YouTubeRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SonetTubeApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(application: Application) {
    private val database = Room.databaseBuilder(
        application,
        SonetTubeDatabase::class.java,
        "sonettube.db",
    ).build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(YouTubeApiService::class.java)

    val youtubeRepository = YouTubeRepository(apiService)
    val favoriteRepository = FavoriteRepository(database.favoriteVideoDao())
    val historyRepository = HistoryRepository(database.recentVideoDao())
}
