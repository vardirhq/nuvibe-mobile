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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.model.Album
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.Playlist
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.playback.PlayerUiState
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.TrackRow
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme
import dev.nuvibe.player.ui.util.formatLongDuration

enum class LibraryTab(val label: String) { SONGS("Songs"), ALBUMS("Albums"), PLAYLISTS("Playlists") }

@Composable
fun LibraryScreen(
    library: Library,
    playlists: List<Playlist>,
    playerState: PlayerUiState,
    initialTab: LibraryTab,
    onPlayTrack: (Track, List<Long>) -> Unit,
    onShuffleAll: () -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onTrackLongPress: (Track) -> Unit,
) {
    val colors = NuvibeTheme.colors
    var tab by remember(initialTab) { mutableStateOf(initialTab) }
    var showCreate by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 20.dp),
    ) {
        item {
            Text(
                "Library",
                fontFamily = Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                letterSpacing = (-0.4).sp,
                color = colors.text,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            )
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                LibraryTab.entries.forEach { t ->
                    FilterPill(label = t.label, selected = t == tab) { tab = t }
                }
            }
        }

        when (tab) {
            LibraryTab.SONGS -> songsSection(library, playerState, onPlayTrack, onShuffleAll, onTrackLongPress)
            LibraryTab.ALBUMS -> albumsSection(library, onPlayAlbum, onOpenAlbum)
            LibraryTab.PLAYLISTS -> playlistsSection(library, playlists, onPlayPlaylist, onOpenPlaylist) { showCreate = true }
        }
    }

    if (showCreate) {
        CreatePlaylistDialog(
            onDismiss = { showCreate = false },
            onConfirm = { name -> onCreatePlaylist(name); showCreate = false },
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.songsSection(
    library: Library,
    playerState: PlayerUiState,
    onPlayTrack: (Track, List<Long>) -> Unit,
    onShuffleAll: () -> Unit,
    onTrackLongPress: (Track) -> Unit,
) {
    val allIds = library.tracks.map { it.id }
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShuffleAllButton(onShuffleAll)
            Text("${library.tracks.size} songs", color = NuvibeTheme.colors.text3, fontSize = 12.5.sp)
        }
        Spacer(Modifier.height(8.dp))
    }
    items(library.tracks, key = { it.id }) { track ->
        Box(Modifier.padding(horizontal = 20.dp)) {
            TrackRow(
                track = track,
                isCurrent = playerState.currentTrackId == track.id,
                isPlaying = playerState.isPlaying,
                onClick = { onPlayTrack(track, allIds) },
                onLongClick = { onTrackLongPress(track) },
                showDivider = true,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.albumsSection(
    library: Library,
    onPlayAlbum: (Album) -> Unit,
    onOpenAlbum: (Album) -> Unit,
) {
    val rows = library.albums.chunked(2)
    items(rows.size) { rowIndex ->
        val row = rows[rowIndex]
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            row.forEach { album ->
                AlbumGridCell(album, Modifier.weight(1f), onOpen = { onOpenAlbum(album) }, onPlay = { onPlayAlbum(album) })
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.playlistsSection(
    library: Library,
    playlists: List<Playlist>,
    onPlayPlaylist: (Playlist) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreate: () -> Unit,
) {
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickableNoRipple(onCreate)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NuvibeTheme.colors.panel2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, "New playlist", tint = NuvibeTheme.colors.accent, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text("New playlist", color = NuvibeTheme.colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    items(playlists, key = { it.id }) { playlist ->
        val totalMs = library.tracksFor(playlist.trackIds).sumOf { it.durationMs }
        PlaylistRow(
            playlist = playlist,
            seed = playlist.name,
            description = "${playlist.trackIds.size} songs · ${formatLongDuration(totalMs)}",
            onOpen = { onOpenPlaylist(playlist) },
            onPlay = { onPlayPlaylist(playlist) },
        )
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = NuvibeTheme.colors
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.accent else colors.panel)
            .border(1.dp, if (selected) colors.accent else colors.border, RoundedCornerShape(999.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            color = if (selected) Color.White else colors.text2,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ShuffleAllButton(onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(NuvibeTheme.colors.accent)
            .clickableNoRipple(onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Shuffle, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Shuffle all", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AlbumGridCell(album: Album, modifier: Modifier = Modifier, onOpen: () -> Unit, onPlay: () -> Unit) {
    val colors = NuvibeTheme.colors
    Column(modifier.clickableNoRipple(onOpen)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            AlbumArt(album.albumArtUri, album.title, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(18.dp))
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(9.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x80090913))
                    .clickableNoRipple(onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Text(album.title, color = colors.text, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 9.dp))
        Text("${album.artist} · ${album.year.takeIf { it > 0 } ?: ""}".trimEnd(' ', '·', ' '), color = colors.text2, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, seed: String, description: String, onOpen: () -> Unit, onPlay: () -> Unit) {
    val colors = NuvibeTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickableNoRipple(onOpen)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(null, seed, modifier = Modifier.size(60.dp), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(playlist.name, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, color = colors.text2, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(
            Icons.Rounded.PlayArrow, "Play",
            tint = colors.text2,
            modifier = Modifier
                .size(26.dp)
                .clickableNoRipple(onPlay),
        )
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Rounded.ChevronRight, null, tint = colors.text3, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Playlist name") },
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
