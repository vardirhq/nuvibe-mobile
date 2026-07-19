package dev.nuvibe.player.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dev.nuvibe.player.data.MusicRepository
import dev.nuvibe.player.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Immutable snapshot of everything the UI needs to render playback. */
data class PlayerUiState(
    val hasCurrent: Boolean = false,
    val currentTrackId: Long? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 1L,
    val bufferedMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queueTrackIds: List<Long> = emptyList(),
) {
    val progress: Float get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

/**
 * App-scoped bridge to the [PlaybackService]. Connects a [MediaController],
 * mirrors its state into a [StateFlow], and exposes intent-style actions.
 */
class PlayerController(
    private val context: Context,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var lastPersistedTrackId: Long? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState()
        }
    }

    fun initialize() {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            syncState()
            startPositionLoop()
        }, ContextCompat.getMainExecutor(context))
    }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    // ---- actions ----------------------------------------------------------

    /** Play [trackIds] as a fresh queue, starting on [startId]. */
    fun play(trackIds: List<Long>, startId: Long? = trackIds.firstOrNull()) {
        val c = controller ?: return
        if (trackIds.isEmpty() || startId == null) return
        val lib = musicRepository.library.value
        val items = trackIds.mapNotNull { lib.track(it)?.toMediaItem() }
        if (items.isEmpty()) return
        val startIndex = trackIds.indexOf(startId).coerceIn(0, items.lastIndex)
        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.play()
    }

    /** Load a queue paused at [positionMs] — used to restore "Continue listening". */
    fun preparePaused(trackIds: List<Long>, startId: Long, positionMs: Long) {
        val c = controller ?: return
        if (c.mediaItemCount > 0) return
        val items = resolve(trackIds)
        if (items.isEmpty()) return
        val startIndex = trackIds.indexOf(startId).coerceIn(0, items.lastIndex)
        c.setMediaItems(items, startIndex, positionMs.coerceAtLeast(0L))
        c.prepare() // playWhenReady stays false, so it stays paused
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE || c.playbackState == Player.STATE_ENDED) c.prepare()
            c.play()
        }
    }

    fun next() { controller?.seekToNext() }

    fun previous() { controller?.seekToPrevious() }

    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }

    /** Jump directly to a queue entry (used by the Queue list & lyrics). */
    fun seekToQueueIndex(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) {
            c.seekToDefaultPosition(index)
            c.play()
        }
    }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    /** Insert tracks right after the current item. */
    fun playNext(trackIds: List<Long>) {
        val c = controller ?: return
        val items = resolve(trackIds)
        if (items.isEmpty()) return
        val at = (c.currentMediaItemIndex + 1).coerceIn(0, c.mediaItemCount)
        if (c.mediaItemCount == 0) play(trackIds) else c.addMediaItems(at, items)
    }

    fun addToQueue(trackIds: List<Long>) {
        val c = controller ?: return
        val items = resolve(trackIds)
        if (items.isEmpty()) return
        if (c.mediaItemCount == 0) play(trackIds) else c.addMediaItems(items)
    }

    fun removeFromQueue(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) c.removeMediaItem(index)
    }

    private fun resolve(trackIds: List<Long>): List<MediaItem> {
        val lib = musicRepository.library.value
        return trackIds.mapNotNull { lib.track(it)?.toMediaItem() }
    }

    // ---- state mirroring --------------------------------------------------

    private fun syncState() {
        val c = controller ?: return
        val current = c.currentMediaItem
        val trackId = current?.mediaId?.toLongOrNull()
        val fallbackDuration = current?.mediaMetadata?.extras?.getLong(MediaItemKeys.DURATION_MS) ?: 1L
        val duration = if (c.duration > 0) c.duration else fallbackDuration
        val queue = (0 until c.mediaItemCount).mapNotNull { c.getMediaItemAt(it).mediaId.toLongOrNull() }

        _state.value = _state.value.copy(
            hasCurrent = current != null,
            currentTrackId = trackId,
            isPlaying = c.isPlaying,
            durationMs = duration,
            bufferedMs = c.bufferedPosition,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            shuffle = c.shuffleModeEnabled,
            repeatMode = c.repeatMode,
            queueTrackIds = queue,
        )
        persist(trackId)
    }

    private fun startPositionLoop() {
        scope.launch {
            while (true) {
                val c = controller
                if (c != null && c.isPlaying) {
                    _state.value = _state.value.copy(
                        positionMs = c.currentPosition.coerceAtLeast(0L),
                        bufferedMs = c.bufferedPosition,
                    )
                    if ((c.currentPosition / 1000) % 5 == 0L) persist(_state.value.currentTrackId)
                }
                delay(500)
            }
        }
    }

    private fun persist(trackId: Long?) {
        trackId ?: return
        val position = controller?.currentPosition ?: 0L
        scope.launch {
            settingsRepository.saveNowPlaying(trackId, position)
            if (trackId != lastPersistedTrackId) {
                lastPersistedTrackId = trackId
                musicRepository.library.value.track(trackId)?.let {
                    settingsRepository.pushRecentAlbum(it.albumId)
                }
            }
        }
    }
}
