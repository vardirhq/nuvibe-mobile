package dev.nuvibe.player.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import dev.nuvibe.player.ui.theme.Gradients

/**
 * Album artwork with a graceful gradient fallback. Real embedded/MediaStore art
 * is shown when available; otherwise a deterministic gradient (plus the design's
 * top-left highlight) stands in.
 */
@Composable
fun AlbumArt(
    artUri: Uri?,
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Gradients.brush(seed)),
    ) {
        // Top-left highlight (size-independent approximation of the mock's radial glow).
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    ),
                ),
        )
        if (artUri != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                // On error, render nothing so the gradient beneath shows through.
                error = {},
                loading = {},
            )
        }
    }
}
