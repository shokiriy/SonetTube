package com.shokirjon.sonettube.ui.favorites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shokirjon.sonettube.R
import com.shokirjon.sonettube.model.Video
import com.shokirjon.sonettube.ui.components.EmptyState
import com.shokirjon.sonettube.ui.components.VideoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onOpenVideo: (Video) -> Unit,
) {
    val videos by viewModel.favorites.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.favorites)) }) },
    ) { padding ->
        if (videos.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_favorites),
                message = stringResource(R.string.no_favorites_message),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 24.dp),
            ) {
                items(videos, key = { it.videoId }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onOpenVideo(video) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.remove(video.videoId) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = stringResource(R.string.remove),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
