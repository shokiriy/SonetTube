package com.shokirjon.sonettube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shokirjon.sonettube.model.Video

@Composable
fun VideoThumbnail(
    video: Video,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF30343B)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = video.thumbnailUrl.takeIf { it.isNotBlank() },
            contentDescription = video.title,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Icon(
            imageVector = Icons.Default.PlayCircleOutline,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
        )
    }
}
