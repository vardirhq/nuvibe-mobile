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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
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
import dev.nuvibe.player.data.model.Album
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.SmartMix
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.playback.PlayerUiState
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.NuvibeLogo
import dev.nuvibe.player.ui.components.SectionHeader
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.Gradients
import dev.nuvibe.player.ui.theme.NuvibeTheme
import dev.nuvibe.player.ui.util.greeting

@Composable
fun HomeScreen(
    library: Library,
    playerState: PlayerUiState,
    recentAlbums: List<Album>,
    smartMixes: List<SmartMix>,
    onPlayTrack: (Track, List<Long>) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayMix: (SmartMix) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onOpenSettings: () -> Unit,
    onSeeAllAlbums: () -> Unit,
) {
    val colors = NuvibeTheme.colors
    val featured: Track? = playerState.currentTrackId?.let { library.track(it) } ?: library.tracks.firstOrNull()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NuvibeLogo(size = 34.dp)
                Spacer(Modifier.width(11.dp))
                Column {
                    Text(
                        "COLLECTOR SESSION",
                        color = colors.text3,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        "Nuvibe",
                        fontFamily = Display,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                }
            }
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colors.panel2)
                    .border(1.dp, colors.border, CircleShape)
                    .clickableNoRipple(onOpenSettings),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", fontFamily = Display, fontWeight = FontWeight.SemiBold, color = colors.text)
            }
        }

        // greeting
        Text(
            greeting(),
            fontFamily = Display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            letterSpacing = (-0.9).sp,
            color = colors.text,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp),
        )
        Text(
            "Your room remembers what matters — pick up where you left off.",
            color = colors.text2,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 18.dp),
        )

        if (featured != null) {
            ContinueListeningCard(
                track = featured,
                onPlay = {
                    val context = library.albumsById[featured.albumId]?.trackIds ?: listOf(featured.id)
                    onPlayTrack(featured, context)
                },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Spacer(Modifier.height(26.dp))

        // recently played
        if (recentAlbums.isNotEmpty()) {
            SectionHeader(
                "Recently played",
                actionLabel = "See all",
                onAction = onSeeAllAlbums,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp),
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(recentAlbums, key = { it.id }) { album ->
                    RecentAlbumCard(album = album, onOpen = { onOpenAlbum(album) }, onPlay = { onPlayAlbum(album) })
                }
            }
            Spacer(Modifier.height(26.dp))
        }

        // made for your room
        if (smartMixes.isNotEmpty()) {
            Text(
                "Made for your room",
                fontFamily = Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                letterSpacing = (-0.4).sp,
                color = colors.text,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(smartMixes, key = { it.key }) { mix ->
                    SmartMixCard(mix = mix, onPlay = { onPlayMix(mix) })
                }
            }
            Spacer(Modifier.height(26.dp))
        }

        // stats
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard("${library.tracks.size}", "Tracks", Modifier.weight(1f))
            StatCard("${library.albums.size}", "Albums", Modifier.weight(1f))
            StatCard("${library.artists.size}", "Artists", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ContinueListeningCard(track: Track, onPlay: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NuvibeTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, colors.border, RoundedCornerShape(26.dp))
            .background(colors.panel),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            AlbumArt(
                artUri = track.albumArtUri,
                seed = track.album,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
            )
            Box(
                Modifier
                    .padding(start = 16.dp, top = 14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x6B090913))
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("CONTINUE LISTENING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp)
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(track.title, color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${track.artist} · ${track.album}", color = colors.text2, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(14.dp))
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .clickableNoRipple(onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun RecentAlbumCard(album: Album, onOpen: () -> Unit, onPlay: () -> Unit) {
    val colors = NuvibeTheme.colors
    Column(
        Modifier
            .width(148.dp)
            .clickableNoRipple(onOpen),
    ) {
        Box(Modifier.size(148.dp)) {
            AlbumArt(album.albumArtUri, album.title, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(20.dp))
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
        Text(album.title, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 9.dp))
        Text(album.artist, color = colors.text2, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SmartMixCard(mix: SmartMix, onPlay: () -> Unit) {
    val colors = NuvibeTheme.colors
    Column(
        Modifier
            .width(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickableNoRipple(onPlay),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(Gradients.brush(mix.gradientSeed)),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.panel)
                .padding(horizontal = 13.dp, vertical = 11.dp),
        ) {
            Text(mix.name, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(mix.description, color = colors.text2, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = NuvibeTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.panel)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = colors.text)
        Text(label.uppercase(), color = colors.text3, fontSize = 10.5.sp, letterSpacing = 1.4.sp, modifier = Modifier.padding(top = 3.dp))
    }
}
