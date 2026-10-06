package com.shokirjon.sonettube.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shokirjon.sonettube.R
import com.shokirjon.sonettube.model.Video
import com.shokirjon.sonettube.ui.components.EmptyState
import com.shokirjon.sonettube.ui.components.VideoCard
import com.shokirjon.sonettube.util.YouTubeUrlParser

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenVideo: (Video) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showUrlDialog by rememberSaveable { mutableStateOf(false) }
    val unavailableTitle = stringResource(R.string.video_unavailable)

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    Text(
                        text = "SonetTube",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.welcome_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::updateQuery,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.search_youtube)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = {
                                focusManager.clearFocus()
                                viewModel.search()
                            }) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            viewModel.search()
                        }),
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showUrlDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(stringResource(R.string.open_from_url))
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Text(
                        text = errorMessage(error),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (!state.hasSearched) {
                item {
                    EmptyState(
                        title = stringResource(R.string.welcome_title),
                        message = stringResource(R.string.welcome_message),
                    )
                }
            } else if (state.videos.isEmpty() && state.error == null) {
                item {
                    EmptyState(
                        title = stringResource(R.string.no_results),
                        message = stringResource(R.string.no_results_message),
                    )
                }
            } else {
                items(state.videos, key = { it.videoId }) { video ->
                    VideoCard(video = video, onClick = { onOpenVideo(video) })
                }
            }
        }
    }

    if (showUrlDialog) {
        UrlDialog(
            onDismiss = { showUrlDialog = false },
            onOpenVideo = { videoId ->
                showUrlDialog = false
                onOpenVideo(
                    Video(
                        videoId = videoId,
                        title = unavailableTitle,
                        channelTitle = "YouTube",
                        thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                    ),
                )
            },
        )
    }
}

@Composable
private fun UrlDialog(
    onDismiss: () -> Unit,
    onOpenVideo: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf("") }
    var isInvalid by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.open_from_url)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it; isInvalid = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.paste_youtube_url)) },
                singleLine = true,
                isError = isInvalid,
                supportingText = if (isInvalid) {
                    { Text(stringResource(R.string.invalid_url)) }
                } else {
                    null
                },
            )
        },
        confirmButton = {
            Button(onClick = {
                val videoId = YouTubeUrlParser.extractVideoId(value)
                if (videoId == null) isInvalid = true else onOpenVideo(videoId)
            }) {
                Text(stringResource(R.string.open))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun errorMessage(error: HomeError): String = when (error) {
    HomeError.EMPTY_QUERY -> stringResource(R.string.empty_query)
    HomeError.API_KEY_MISSING -> stringResource(R.string.api_key_missing)
    HomeError.INVALID_KEY_OR_QUOTA -> "The YouTube API key is invalid or its quota has been reached."
    HomeError.NETWORK -> "Unable to reach YouTube. Check your internet connection."
    HomeError.SERVER -> stringResource(R.string.generic_error)
}
