package dev.nuvibe.player.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.model.Album
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.playback.PlayerUiState
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.EqualizerBars
import dev.nuvibe.player.ui.components.NuvibeBackground
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme
import dev.nuvibe.player.ui.util.formatDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumDetailScreen(
    album: Album,
    tracks: List<Track>,
    playerState: PlayerUiState,
    onBack: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayAlbum: () -> Unit,
    onShuffleAlbum: () -> Unit,
    onTrackLongPress: (Track) -> Unit,
) {
    val colors = NuvibeTheme.colors
    NuvibeBackground {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickableNoRipple(onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.text, modifier = Modifier.size(24.dp))
                    }
                }
            }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AlbumArt(album.albumArtUri, album.title, modifier = Modifier.size(200.dp), shape = RoundedCornerShape(24.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(album.title, fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = colors.text, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        buildString {
                            append(album.artist)
                            if (album.year > 0) append(" · ${album.year}")
                            append(" · ${album.trackCount} songs")
                        },
                        color = colors.text2,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.accent)
                                .clickableNoRipple(onPlayAlbum)
                                .padding(horizontal = 24.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.panel2)
                                .clickableNoRipple(onShuffleAlbum)
                                .padding(horizontal = 24.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Shuffle, null, tint = colors.text, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Shuffle", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
            itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
                val isCurrent = playerState.currentTrackId == track.id
                Row(
                    Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onPlayTrack(track) },
                            onLongClick = { onTrackLongPress(track) },
                        )
                        .padding(horizontal = 20.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        if (isCurrent) {
                            EqualizerBars(playing = playerState.isPlaying, color = colors.accent)
                        } else {
                            Text("${index + 1}", color = colors.text3, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(track.title, color = if (isCurrent) colors.accent else colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(track.artist, color = colors.text2, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(formatDuration(track.durationMs), color = colors.text3, fontSize = 12.sp)
                }
            }
        }
    }
}
