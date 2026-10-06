package com.shokirjon.sonettube.navigation

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val PLAYER = "player/{videoId}"

    fun player(videoId: String) = "player/$videoId"
}
