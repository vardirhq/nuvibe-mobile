package dev.nuvibe.player.playback

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dev.nuvibe.player.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Background playback host. A [MediaSessionService] means playback continues
 * when the UI is gone and the system media notification / lock-screen controls
 * are managed automatically by Media3.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        mediaSession = MediaSession.Builder(this, player).build()

        observeSettings(audioAttributes)
    }

    /** Apply real, user-controlled playback behaviours to the engine. */
    private fun observeSettings(audioAttributes: AudioAttributes) {
        val settings = SettingsRepository(applicationContext).settings
        settings
            .map { Triple(it.skipSilence, it.pauseOnDisconnect, it.handleAudioFocus) }
            .distinctUntilChanged()
            .onEach { (skipSilence, pauseOnDisconnect, handleAudioFocus) ->
                player.skipSilenceEnabled = skipSilence
                player.setHandleAudioBecomingNoisy(pauseOnDisconnect)
                player.setAudioAttributes(audioAttributes, handleAudioFocus)
            }
            .launchIn(serviceScope)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        // Stop the service if the user swipes the app away while paused.
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
