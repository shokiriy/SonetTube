package com.shokirjon.sonettube.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shokirjon.sonettube.R
import com.shokirjon.sonettube.app.LocalAppContainer
import com.shokirjon.sonettube.model.Video
import com.shokirjon.sonettube.ui.favorites.FavoritesScreen
import com.shokirjon.sonettube.ui.favorites.FavoritesViewModel
import com.shokirjon.sonettube.ui.history.HistoryScreen
import com.shokirjon.sonettube.ui.history.HistoryViewModel
import com.shokirjon.sonettube.ui.home.HomeScreen
import com.shokirjon.sonettube.ui.home.HomeViewModel
import com.shokirjon.sonettube.ui.player.PlayerScreen
import com.shokirjon.sonettube.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
fun SonetTubeNavigation() {
    val container = LocalAppContainer.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val destinations = listOf(
        TopLevelDestination(Routes.HOME, R.string.home, Icons.Default.Home),
        TopLevelDestination(Routes.FAVORITES, R.string.favorites, Icons.Default.FavoriteBorder),
        TopLevelDestination(Routes.HISTORY, R.string.history, Icons.Default.History),
    )
    val showBottomBar = currentRoute in destinations.map { it.route }

    fun openVideo(video: Video) {
        scope.launch {
            container.historyRepository.record(video)
            navController.navigate(Routes.player(Uri.encode(video.videoId)))
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        AppNavHost(
            navController = navController,
            padding = padding,
            container = container,
            onOpenVideo = ::openVideo,
        )
    }
}

@Composable
private fun AppNavHost(
    navController: androidx.navigation.NavHostController,
    padding: PaddingValues,
    container: com.shokirjon.sonettube.app.AppContainer,
    onOpenVideo: (Video) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.padding(padding),
    ) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(factory = factory {
                HomeViewModel(container.youtubeRepository)
            })
            HomeScreen(viewModel = viewModel, onOpenVideo = onOpenVideo)
        }
        composable(Routes.FAVORITES) {
            val viewModel: FavoritesViewModel = viewModel(factory = factory {
                FavoritesViewModel(container.favoriteRepository)
            })
            FavoritesScreen(viewModel = viewModel, onOpenVideo = onOpenVideo)
        }
        composable(Routes.HISTORY) {
            val viewModel: HistoryViewModel = viewModel(factory = factory {
                HistoryViewModel(container.historyRepository)
            })
            HistoryScreen(viewModel = viewModel, onOpenVideo = onOpenVideo)
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
        ) { entry ->
            val videoId = entry.arguments?.getString("videoId") ?: return@composable
            val viewModel: PlayerViewModel = viewModel(factory = factory {
                PlayerViewModel(
                    videoId = Uri.decode(videoId),
                    favoriteRepository = container.favoriteRepository,
                    historyRepository = container.historyRepository,
                )
            })
            PlayerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}

private inline fun <reified T : ViewModel> factory(crossinline create: () -> T) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
