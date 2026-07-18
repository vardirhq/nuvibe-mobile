package dev.nuvibe.player.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.model.Playlist
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.NuvibeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackActionsSheet(
    track: Track,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onGoToAlbum: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onCreatePlaylistWithTrack: () -> Unit,
) {
    val colors = NuvibeTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picking by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bg1,
        dragHandle = null,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp, top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                AlbumArt(track.albumArtUri, track.album, modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.size(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(track.title, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${track.artist} · ${track.album}", color = colors.text2, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).padding(bottom = 0.dp))

            if (!picking) {
                ActionItem(Icons.Rounded.PlayArrow, "Play next") { onPlayNext(); onDismiss() }
                ActionItem(Icons.AutoMirrored.Rounded.QueueMusic, "Add to queue") { onAddToQueue(); onDismiss() }
                ActionItem(Icons.AutoMirrored.Rounded.PlaylistAdd, "Add to playlist") { picking = true }
                ActionItem(Icons.Rounded.Album, "Go to album") { onGoToAlbum(); onDismiss() }
            } else {
                Text("Add to playlist", color = colors.text3, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
                ActionItem(Icons.Rounded.Add, "New playlist with this song") { onCreatePlaylistWithTrack(); onDismiss() }
                playlists.forEach { pl ->
                    ActionItem(Icons.AutoMirrored.Rounded.PlaylistAdd, pl.name) { onAddToPlaylist(pl.id); onDismiss() }
                }
            }
        }
    }
}

@Composable
private fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = NuvibeTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(icon, null, tint = colors.text2, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(16.dp))
        Text(label, color = colors.text, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
