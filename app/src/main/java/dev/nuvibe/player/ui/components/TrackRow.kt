package dev.nuvibe.player.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.nuvibe.player.ui.util.formatDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    subtitle: String = "${track.artist} · ${track.album}",
    showDivider: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = NuvibeTheme.colors
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(46.dp)) {
                AlbumArt(
                    artUri = track.albumArtUri,
                    seed = track.album,
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(11.dp),
                )
                if (isCurrent) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0x8C090913)),
                        contentAlignment = Alignment.Center,
                    ) {
                        EqualizerBars(playing = isPlaying, color = Color.White, barHeight = 14.dp)
                    }
                }
            }
            Spacer(Modifier.size(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isCurrent) colors.accent else colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = colors.text2,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.size(10.dp))
            if (trailing != null) {
                trailing()
            } else {
                Text(
                    text = formatDuration(track.durationMs),
                    color = colors.text3,
                    fontSize = 12.sp,
                )
            }
        }
        if (showDivider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(1.dp)
                    .background(colors.border),
            )
        }
    }
}
