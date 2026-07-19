package dev.nuvibe.player.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.nuvibe.player.data.ScanState
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.ui.components.MiniPlayer
import dev.nuvibe.player.ui.components.NuvibeBackground
import dev.nuvibe.player.ui.components.NuvibeBottomBar
import dev.nuvibe.player.ui.components.NuvibeLogo
import dev.nuvibe.player.ui.components.Tab
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.player.NowPlayingScreen
import dev.nuvibe.player.ui.player.TrackActionsSheet
import dev.nuvibe.player.ui.screens.AlbumDetailScreen
import dev.nuvibe.player.ui.screens.HomeScreen
import dev.nuvibe.player.ui.screens.LibraryScreen
import dev.nuvibe.player.ui.screens.MetadataEditScreen
import dev.nuvibe.player.ui.screens.LibraryTab
import dev.nuvibe.player.ui.screens.SearchScreen
import dev.nuvibe.player.ui.screens.SettingsScreen
import dev.nuvibe.player.ui.screens.SplashScreen
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NuvibeApp(vm: NuvibeViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    NuvibeTheme(themeMode = settings.themeMode, accent = settings.accent) {
        val view = androidx.compose.ui.platform.LocalView.current
        if (!view.isInEditMode) {
            val light = settings.themeMode == dev.nuvibe.player.data.settings.ThemeMode.LIGHT
            androidx.compose.runtime.SideEffect {
                val window = (view.context as android.app.Activity).window
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = light
                controller.isAppearanceLightNavigationBars = light
            }
        }

        val library by vm.library.collectAsStateWithLifecycle()
        val rawLibrary by vm.rawLibrary.collectAsStateWithLifecycle()
        val hiddenFolders by vm.hiddenFolders.collectAsStateWithLifecycle()
        val hiddenTrackIds by vm.hiddenTrackIds.collectAsStateWithLifecycle()
        val scanState by vm.scanState.collectAsStateWithLifecycle()
        val playerState by vm.playerState.collectAsStateWithLifecycle()
        val playlists by vm.playlists.collectAsStateWithLifecycle()
        val recentAlbumIds by vm.recentAlbumIds.collectAsStateWithLifecycle()

        val audioPermissionName = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val audioPermission = rememberPermissionState(audioPermissionName)
        val notificationsPermission = if (Build.VERSION.SDK_INT >= 33) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            null
        }

        LaunchedEffect(audioPermission.status.isGranted) {
            if (audioPermission.status.isGranted) {
                vm.onPermissionGranted()
                notificationsPermission?.let { if (!it.status.isGranted) it.launchPermissionRequest() }
            }
        }

        var showSplash by remember { mutableStateOf(true) }
        var tab by remember { mutableStateOf(Tab.HOME) }
        var libInitialTab by remember { mutableStateOf(LibraryTab.SONGS) }
        var showPlayer by remember { mutableStateOf(false) }
        var openAlbumId by remember { mutableStateOf<Long?>(null) }
        var actionsTrack by remember { mutableStateOf<Track?>(null) }
        var editTrack by remember { mutableStateOf<Track?>(null) }

        // Route the system/gesture back button through the in-app navigation
        // stack instead of letting it close the app on the first press.
        BackHandler(
            enabled = editTrack != null || showPlayer || openAlbumId != null || tab != Tab.HOME,
        ) {
            when {
                editTrack != null -> editTrack = null
                showPlayer -> showPlayer = false
                openAlbumId != null -> openAlbumId = null
                tab != Tab.HOME -> tab = Tab.HOME
            }
        }

        // Resolve against the raw library so a currently-playing track keeps
        // showing even if the user has hidden it or its folder.
        val currentTrack = playerState.currentTrackId?.let { library.track(it) ?: rawLibrary.track(it) }
        val smartMixes = remember(library) { vm.smartMixes(library) }
        val recentAlbums = remember(recentAlbumIds, library) {
            val fromHistory = recentAlbumIds.mapNotNull { library.albumsById[it] }
            (fromHistory + library.albums.sortedByDescending { al ->
                library.tracksFor(al.trackIds).maxOfOrNull { it.dateAddedSec } ?: 0L
            }).distinctBy { it.id }.take(10)
        }

        Box(Modifier.fillMaxSize()) {
            NuvibeBackground {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when {
                            !audioPermission.status.isGranted -> PermissionGate { audioPermission.launchPermissionRequest() }
                            library.isEmpty && scanState == ScanState.READY -> EmptyLibrary(onRescan = vm::rescan)
                            else -> when (tab) {
                                Tab.HOME -> HomeScreen(
                                    library = library,
                                    playerState = playerState,
                                    recentAlbums = recentAlbums,
                                    smartMixes = smartMixes,
                                    onPlayTrack = { t, ctx -> vm.playTrackId(t.id, ctx) },
                                    onPlayAlbum = vm::playAlbum,
                                    onPlayMix = vm::playMix,
                                    onOpenAlbum = { openAlbumId = it.id },
                                    onOpenSettings = { tab = Tab.SETTINGS },
                                    onSeeAllAlbums = { libInitialTab = LibraryTab.ALBUMS; tab = Tab.LIBRARY },
                                )
                                Tab.SEARCH -> SearchScreen(
                                    library = library,
                                    playerState = playerState,
                                    onPlayTrack = { t, ctx -> vm.playTrackId(t.id, ctx) },
                                    onOpenAlbum = { openAlbumId = it.id },
                                    onTrackLongPress = { actionsTrack = it },
                                )
                                Tab.LIBRARY -> LibraryScreen(
                                    library = library,
                                    playlists = playlists,
                                    playerState = playerState,
                                    initialTab = libInitialTab,
                                    onPlayTrack = { t, ctx -> vm.playTrackId(t.id, ctx) },
                                    onShuffleAll = vm::shuffleAll,
                                    onPlayAlbum = vm::playAlbum,
                                    onOpenAlbum = { openAlbumId = it.id },
                                    onPlayPlaylist = vm::playPlaylist,
                                    onOpenPlaylist = { vm.playPlaylist(it); showPlayer = true },
                                    onCreatePlaylist = { vm.createPlaylist(it) },
                                    onTrackLongPress = { actionsTrack = it },
                                )
                                Tab.SETTINGS -> SettingsScreen(
                                    settings = settings,
                                    songCount = library.tracks.size,
                                    isScanning = scanState == ScanState.SCANNING,
                                    folders = rawLibrary.folders,
                                    hiddenFolders = hiddenFolders,
                                    hiddenTracks = rawLibrary.tracksFor(hiddenTrackIds.toList()),
                                    onSetTheme = vm::setTheme,
                                    onSetAccent = vm::setAccent,
                                    onSetSkipSilence = vm::setSkipSilence,
                                    onSetPauseOnDisconnect = vm::setPauseOnDisconnect,
                                    onSetHandleAudioFocus = vm::setHandleAudioFocus,
                                    onSetFolderHidden = vm::setFolderHidden,
                                    onUnhideTrack = { vm.setTrackHidden(it, false) },
                                    onRescan = vm::rescan,
                                )
                            }
                        }
                    }

                    if (audioPermission.status.isGranted) {
                        Column {
                            if (currentTrack != null) {
                                MiniPlayer(
                                    track = currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    progress = playerState.progress,
                                    onTap = { showPlayer = true },
                                    onPlayPause = vm::togglePlayPause,
                                    onNext = vm::next,
                                    onPrevious = vm::previous,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                )
                            }
                            NuvibeBottomBar(selected = tab, onSelect = { tab = it })
                        }
                    }
                }
            }

            // Album detail overlay (retain last album so the exit animation is smooth)
            val detailAlbum = remember { mutableStateOf<dev.nuvibe.player.data.model.Album?>(null) }
            LaunchedEffect(openAlbumId, library) {
                openAlbumId?.let { detailAlbum.value = library.albumsById[it] }
            }
            AnimatedVisibility(
                visible = openAlbumId != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                val album = detailAlbum.value
                if (album != null) {
                    AlbumDetailScreen(
                        album = album,
                        tracks = library.tracksFor(album.trackIds),
                        playerState = playerState,
                        onBack = { openAlbumId = null },
                        onPlayTrack = { vm.playTrackId(it.id, album.trackIds) },
                        onPlayAlbum = { vm.playAlbum(album) },
                        onShuffleAlbum = {
                            val q = album.trackIds.shuffled()
                            if (q.isNotEmpty()) vm.playTrackId(q.first(), q)
                        },
                        onTrackLongPress = { actionsTrack = it },
                    )
                }
            }

            // Now Playing sheet
            AnimatedVisibility(
                visible = showPlayer && currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                currentTrack?.let { track ->
                    NowPlayingScreen(
                        library = library,
                        playerState = playerState,
                        track = track,
                        onClose = { showPlayer = false },
                        onSeek = vm::seekTo,
                        onPlayPause = vm::togglePlayPause,
                        onNext = vm::next,
                        onPrevious = vm::previous,
                        onToggleShuffle = vm::toggleShuffle,
                        onCycleRepeat = vm::cycleRepeat,
                        onSeekToQueueIndex = vm::seekToQueueIndex,
                    )
                }
            }

            // Track actions
            actionsTrack?.let { track ->
                TrackActionsSheet(
                    track = track,
                    playlists = playlists,
                    onDismiss = { actionsTrack = null },
                    onPlayNext = { vm.playNext(listOf(track.id)) },
                    onAddToQueue = { vm.addToQueue(listOf(track.id)) },
                    onGoToAlbum = { openAlbumId = track.albumId },
                    onAddToPlaylist = { vm.addToPlaylist(it, listOf(track.id)) },
                    onCreatePlaylistWithTrack = { vm.createPlaylist(track.album, listOf(track.id)) },
                    onHideTrack = { vm.setTrackHidden(track.id, true) },
                    onEditMetadata = { editTrack = track },
                )
            }

            // Metadata editor (retain last track so the exit animation is smooth)
            val editingTrack = remember { mutableStateOf<Track?>(null) }
            LaunchedEffect(editTrack) { editTrack?.let { editingTrack.value = it } }
            AnimatedVisibility(
                visible = editTrack != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                editingTrack.value?.let { track ->
                    MetadataEditScreen(
                        track = track,
                        onClose = { editTrack = null },
                        writeMetadata = vm::writeMetadata,
                    )
                }
            }

            // Splash
            AnimatedVisibility(visible = showSplash, enter = fadeIn(), exit = fadeOut()) {
                SplashScreen(onFinished = { showSplash = false })
            }
        }
    }
}

@Composable
private fun PermissionGate(onRequest: () -> Unit) {
    val colors = NuvibeTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        NuvibeLogo(size = 84.dp)
        Text(
            "Nuvibe",
            fontFamily = Display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 34.sp,
            color = colors.text,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            "Your local listening room. Nuvibe needs access to the music on your device to build your library.",
            color = colors.text2,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Box(
            Modifier
                .padding(top = 28.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.accent)
                .clickableNoRipple(onRequest)
                .padding(horizontal = 28.dp, vertical = 14.dp),
        ) {
            Text("Grant access", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun EmptyLibrary(onRescan: () -> Unit) {
    val colors = NuvibeTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        NuvibeLogo(size = 64.dp)
        Text("No music found", fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = colors.text, modifier = Modifier.padding(top = 16.dp))
        Text(
            "Add some audio files to your device, then rescan to fill your room.",
            color = colors.text2,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )
        Box(
            Modifier
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.panel2)
                .clickableNoRipple(onRescan)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text("Rescan", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
