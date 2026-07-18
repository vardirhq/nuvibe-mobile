package dev.nuvibe.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.model.Album
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.playback.PlayerUiState
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.OverlineLabel
import dev.nuvibe.player.ui.components.TrackRow
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme

@Composable
fun SearchScreen(
    library: Library,
    playerState: PlayerUiState,
    onPlayTrack: (Track, List<Long>) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onTrackLongPress: (Track) -> Unit,
) {
    val colors = NuvibeTheme.colors
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val results = remember(q, library) {
        if (q.isEmpty()) emptyList() else library.tracks.filter {
            it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
        }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
    ) {
        item {
            Text(
                "Search",
                fontFamily = Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                letterSpacing = (-0.4).sp,
                color = colors.text,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.panel2)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(horizontal = 15.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Search, null, tint = colors.text3, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Songs, albums, artists", color = colors.text3, fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.merge(TextStyle(color = colors.text, fontSize = 15.sp)),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close, "Clear",
                        tint = colors.text3,
                        modifier = Modifier
                            .size(18.dp)
                            .clickableNoRipple { query = "" },
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
        }

        if (q.isEmpty()) {
            item { OverlineLabel("Browse your collection", Modifier.padding(bottom = 12.dp)) }
            val rows = library.albums.chunked(2)
            items(rows.size) { rowIndex ->
                val row = rows[rowIndex]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { album ->
                        BrowseTile(album, Modifier.weight(1f)) { onOpenAlbum(album) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            item {
                OverlineLabel(
                    "${results.size} result${if (results.size == 1) "" else "s"}",
                    Modifier.padding(bottom = 4.dp),
                )
            }
            items(results, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isCurrent = playerState.currentTrackId == track.id,
                    isPlaying = playerState.isPlaying,
                    onClick = { onPlayTrack(track, results.map { it.id }) },
                    onLongClick = { onTrackLongPress(track) },
                )
            }
            if (results.isEmpty()) {
                item {
                    Text(
                        "No matches in your room for \"$query\".",
                        color = colors.text3,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowseTile(album: Album, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(92.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickableNoRipple(onClick),
    ) {
        AlbumArt(album.albumArtUri, album.title, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(18.dp))
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x66060710)),
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 10.dp, bottom = 10.dp),
        ) {
            Text(album.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(album.artist, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
