package dev.nuvibe.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.ui.theme.NuvibeTheme

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Float,
    onTap: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NuvibeTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.miniBg)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .clickableNoRipple(onTap),
    ) {
        // progress rail
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(colors.bar),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(colors.accent),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(
                artUri = track.albumArtUri,
                seed = track.album,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(11.dp),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    track.title,
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    track.artist,
                    color = colors.text2,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconGhost(Icons.Rounded.SkipPrevious, "Previous", onPrevious, tint = colors.text2)
            Box(
                Modifier
                    .size(42.dp)
                    .padding(start = 4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.accent)
                    .clickableNoRipple(onPlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconGhost(Icons.Rounded.SkipNext, "Next", onNext, tint = colors.text2)
        }
    }
}

@Composable
private fun IconGhost(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color,
) {
    Box(
        Modifier
            .size(34.dp)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
    }
}
