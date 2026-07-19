package dev.nuvibe.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.nuvibe.player.NuvibeApplication
import dev.nuvibe.player.data.MusicRepository
import dev.nuvibe.player.data.PlaylistRepository
import dev.nuvibe.player.data.ScanState
import dev.nuvibe.player.data.mediastore.MetadataWriteResult
import dev.nuvibe.player.data.mediastore.TrackEdit
import dev.nuvibe.player.data.model.Album
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.Playlist
import dev.nuvibe.player.data.model.SmartMix
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.data.settings.AccentColor
import dev.nuvibe.player.data.settings.AppSettings
import dev.nuvibe.player.data.settings.SettingsRepository
import dev.nuvibe.player.data.settings.ThemeMode
import dev.nuvibe.player.playback.PlayerController
import dev.nuvibe.player.playback.PlayerUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NuvibeViewModel(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository,
    private val player: PlayerController,
) : ViewModel() {

    /** The full on-device library, before the user's hide filters are applied. */
    val rawLibrary: StateFlow<Library> = musicRepository.library
    val scanState: StateFlow<ScanState> = musicRepository.scanState
    val playerState: StateFlow<PlayerUiState> = player.state

    val hiddenFolders: StateFlow<Set<String>> = settingsRepository.hiddenFolders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val hiddenTrackIds: StateFlow<Set<Long>> = settingsRepository.hiddenTrackIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    /** The library the rest of the app browses: discovery minus the user's hides. */
    val library: StateFlow<Library> = combine(
        musicRepository.library,
        settingsRepository.hiddenFolders,
        settingsRepository.hiddenTrackIds,
    ) { lib, folders, trackIds -> lib.filtered(folders, trackIds) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Library())

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentAlbumIds: StateFlow<List<Long>> = settingsRepository.recentAlbumIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var restored = false

    // ---- library ----------------------------------------------------------

    fun onPermissionGranted() {
        viewModelScope.launch {
            musicRepository.refresh()
            restoreLastSession()
        }
    }

    fun rescan() {
        viewModelScope.launch { musicRepository.refresh() }
    }

    private suspend fun restoreLastSession() {
        if (restored) return
        restored = true
        if (player.state.value.hasCurrent) return
        val lib = musicRepository.library.value
        val lastId = settingsRepository.lastTrackId.first() ?: return
        val track = lib.track(lastId) ?: return
        val position = settingsRepository.lastPositionMs.first()
        val album = lib.albumsById[track.albumId]
        val queue = album?.trackIds ?: lib.tracks.map { it.id }
        player.preparePaused(queue, track.id, position)
    }

    fun smartMixes(lib: Library): List<SmartMix> = musicRepository.smartMixes(lib)

    // ---- playback ---------------------------------------------------------

    fun playTrack(track: Track, context: List<Long>) = player.play(context, track.id)
    fun playTrackId(id: Long, context: List<Long>) = player.play(context, id)

    fun playAlbum(album: Album) = player.play(album.trackIds, album.trackIds.firstOrNull())

    fun playPlaylist(playlist: Playlist) = player.play(playlist.trackIds, playlist.trackIds.firstOrNull())

    fun playMix(mix: SmartMix) = player.play(mix.trackIds, mix.trackIds.firstOrNull())

    fun shuffleAll() {
        val ids = library.value.tracks.map { it.id }.shuffled()
        if (!player.state.value.shuffle) player.toggleShuffle()
        player.play(ids, ids.firstOrNull())
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seekTo(ms: Long) = player.seekTo(ms)
    fun seekToQueueIndex(index: Int) = player.seekToQueueIndex(index)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeat() = player.cycleRepeat()
    fun playNext(ids: List<Long>) = player.playNext(ids)
    fun addToQueue(ids: List<Long>) = player.addToQueue(ids)
    fun removeFromQueue(index: Int) = player.removeFromQueue(index)

    // ---- settings ---------------------------------------------------------

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setTheme(mode) }
    fun setAccent(accent: AccentColor) = viewModelScope.launch { settingsRepository.setAccent(accent) }
    fun setSkipSilence(v: Boolean) = viewModelScope.launch { settingsRepository.setSkipSilence(v) }
    fun setPauseOnDisconnect(v: Boolean) = viewModelScope.launch { settingsRepository.setPauseOnDisconnect(v) }
    fun setHandleAudioFocus(v: Boolean) = viewModelScope.launch { settingsRepository.setHandleAudioFocus(v) }

    fun setFolderHidden(path: String, hidden: Boolean) =
        viewModelScope.launch { settingsRepository.setFolderHidden(path, hidden) }

    fun setTrackHidden(trackId: Long, hidden: Boolean) =
        viewModelScope.launch { settingsRepository.setTrackHidden(trackId, hidden) }

    // ---- metadata ---------------------------------------------------------

    /** Writes edited tags for [track]; the library rescans on success. */
    suspend fun writeMetadata(track: Track, edit: TrackEdit): MetadataWriteResult =
        musicRepository.writeMetadata(track.uri, edit)

    // ---- playlists --------------------------------------------------------

    fun createPlaylist(name: String, trackIds: List<Long> = emptyList()) =
        viewModelScope.launch { playlistRepository.createWith(name, trackIds) }

    fun renamePlaylist(id: Long, name: String) = viewModelScope.launch { playlistRepository.rename(id, name) }
    fun deletePlaylist(id: Long) = viewModelScope.launch { playlistRepository.delete(id) }
    fun addToPlaylist(id: Long, trackIds: List<Long>) = viewModelScope.launch { playlistRepository.addTracks(id, trackIds) }
    fun removeFromPlaylist(id: Long, trackId: Long) = viewModelScope.launch { playlistRepository.removeTrack(id, trackId) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as NuvibeApplication
                val c = app.container
                NuvibeViewModel(c.musicRepository, c.playlistRepository, c.settingsRepository, c.playerController)
            }
        }
    }
}
