package dev.nuvibe.player.ui.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.playback.PlayerUiState
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.EqualizerBars
import dev.nuvibe.player.ui.components.NuvibeBackground
import dev.nuvibe.player.ui.components.TrackRow
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme
import dev.nuvibe.player.ui.util.formatDuration
import kotlin.math.abs
import kotlin.math.sin

private enum class NpTab(val label: String) { BARS("Bars"), WAVEFORM("Waveform"), LYRICS("Lyrics") }

@Composable
fun NowPlayingScreen(
    library: Library,
    playerState: PlayerUiState,
    track: Track,
    onClose: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeekToQueueIndex: (Int) -> Unit,
) {
    val colors = NuvibeTheme.colors
    var tab by remember { mutableStateOf(NpTab.BARS) }
    var showQueue by remember { mutableStateOf(false) }

    NuvibeBackground {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            // header
            Column(Modifier.padding(top = 12.dp)) {
                Box(
                    Modifier
                        .padding(bottom = 12.dp)
                        .size(width = 38.dp, height = 4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.text3.copy(alpha = 0.6f))
                        .align(Alignment.CenterHorizontally),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleIcon(Icons.Rounded.KeyboardArrowDown, "Close", onClose)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FROM ALBUM", color = colors.text3, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                        Text("Now Playing", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    CircleIcon(Icons.AutoMirrored.Rounded.QueueMusic, "Queue", { showQueue = true })
                }
            }

            // view tabs
            Row(
                Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.panel)
                    .border(1.dp, colors.border, RoundedCornerShape(999.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NpTab.entries.forEach { t ->
                    val selected = t == tab
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) colors.accentAlpha(0.28f) else Color.Transparent)
                            .clickableNoRipple { tab = t }
                            .padding(horizontal = 18.dp, vertical = 7.dp),
                    ) {
                        Text(t.label, color = if (selected) colors.text else colors.text2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // stage
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (tab) {
                    NpTab.BARS -> BarsStage(track, playerState.isPlaying)
                    NpTab.WAVEFORM -> WaveformStage(track, playerState.progress)
                    NpTab.LYRICS -> LyricsStage()
                }
            }

            // track info + controls
            Column(Modifier.padding(start = 26.dp, end = 26.dp, bottom = 26.dp)) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(track.title, color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${track.artist} · ${track.album}", color = colors.text2, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
                }
                Spacer(Modifier.height(12.dp))
                SeekBar(playerState, onSeek)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ControlCircle(Icons.Rounded.Shuffle, "Shuffle", 44.dp, iconSize = 21.dp, active = playerState.shuffle, onClick = onToggleShuffle)
                    Icon(Icons.Rounded.SkipPrevious, "Previous", tint = colors.text, modifier = Modifier.size(38.dp).clickableNoRipple(onPrevious))
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickableNoRipple(onPlayPause),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Icon(Icons.Rounded.SkipNext, "Next", tint = colors.text, modifier = Modifier.size(38.dp).clickableNoRipple(onNext))
                    val repeatIcon = if (playerState.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                    ControlCircle(repeatIcon, "Repeat", 44.dp, iconSize = 21.dp, active = playerState.repeatMode != Player.REPEAT_MODE_OFF, onClick = onCycleRepeat)
                }
            }
        }

        if (showQueue) {
            QueueOverlay(
                library = library,
                playerState = playerState,
                onClose = { showQueue = false },
                onSelect = { index -> onSeekToQueueIndex(index) },
            )
        }
    }
}

@Composable
private fun SeekBar(playerState: PlayerUiState, onSeek: (Long) -> Unit) {
    val colors = NuvibeTheme.colors
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableStateOf(0f) }
    val duration = playerState.durationMs.coerceAtLeast(1L).toFloat()
    val value = if (scrubbing) scrubValue else playerState.positionMs.toFloat().coerceIn(0f, duration)

    Slider(
        value = value,
        onValueChange = { scrubbing = true; scrubValue = it },
        onValueChangeFinished = { onSeek(scrubValue.toLong()); scrubbing = false },
        valueRange = 0f..duration,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = colors.accent,
            inactiveTrackColor = colors.bar,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(formatDuration(value.toLong()), color = colors.text3, fontSize = 11.5.sp)
        Text(formatDuration(playerState.durationMs), color = colors.text3, fontSize = 11.5.sp)
    }
}

@Composable
private fun BarsStage(track: Track, playing: Boolean) {
    Box(
        Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        AlbumArt(track.albumArtUri, track.album, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(28.dp))
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            EqualizerBars(
                playing = playing,
                color = Color.White,
                barCount = 24,
                barWidth = 4.dp,
                barHeight = 90.dp,
            )
        }
    }
}

@Composable
private fun WaveformStage(track: Track, progress: Float) {
    val colors = NuvibeTheme.colors
    val bars = remember(track.id) {
        val seed = track.id
        List(56) { i ->
            val v = abs(sin(i * 0.55 + seed % 7) * 0.7 + sin(i * 0.21) * 0.3)
            (0.18 + v * 0.74).toFloat().coerceIn(0.12f, 1f)
        }
    }
    Box(
        Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp)),
    ) {
        AlbumArt(track.albumArtUri, track.album, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(28.dp))
        Box(Modifier.fillMaxSize().background(Color(0xB3060710)))
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            bars.forEachIndexed { i, h ->
                val played = i.toFloat() / bars.size <= progress
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(h)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (played) colors.accent else Color.White.copy(alpha = 0.28f)),
                )
            }
        }
    }
}

@Composable
private fun LyricsStage() {
    val colors = NuvibeTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("♪", fontSize = 40.sp, color = colors.text3)
        Spacer(Modifier.height(12.dp))
        Text(
            "No lyrics for this track yet.",
            color = colors.text2,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            "Lyrics embedded in your files will appear here.",
            color = colors.text3,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun QueueOverlay(
    library: Library,
    playerState: PlayerUiState,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = NuvibeTheme.colors
    val tracks = playerState.queueTrackIds.mapNotNull { library.track(it) }
    NuvibeBackground {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("UP NEXT", color = colors.text3, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    Text("Queue", fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = colors.text)
                }
                CircleIcon(Icons.Rounded.Close, "Close", onClose)
            }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
                    val isCurrent = playerState.currentTrackId == track.id
                    TrackRow(
                        track = track,
                        isCurrent = isCurrent,
                        isPlaying = playerState.isPlaying,
                        onClick = { onSelect(index) },
                        subtitle = track.artist,
                        showDivider = true,
                        trailing = if (isCurrent) {
                            { EqualizerBars(playing = playerState.isPlaying, color = colors.accent) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = NuvibeTheme.colors
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(colors.panel2)
            .border(1.dp, colors.border, CircleShape)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = colors.text, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ControlCircle(icon: ImageVector, label: String, size: androidx.compose.ui.unit.Dp, iconSize: androidx.compose.ui.unit.Dp, active: Boolean, onClick: () -> Unit) {
    val colors = NuvibeTheme.colors
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (active) colors.accentAlpha(0.16f) else Color.Transparent)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (active) colors.accent else colors.text2, modifier = Modifier.size(iconSize))
    }
}
