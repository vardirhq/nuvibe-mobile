package dev.nuvibe.player

import android.app.Application
import android.content.Context
import dev.nuvibe.player.data.MusicRepository
import dev.nuvibe.player.data.PlaylistRepository
import dev.nuvibe.player.data.local.NuvibeDatabase
import dev.nuvibe.player.data.settings.SettingsRepository
import dev.nuvibe.player.playback.PlayerController

/** Poor-man's DI container — one instance of each repository/controller. */
class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)
    val musicRepository = MusicRepository(context)
    val playlistRepository = PlaylistRepository(NuvibeDatabase.get(context).playlistDao())
    val playerController = PlayerController(context, musicRepository, settingsRepository)
}

class NuvibeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.playerController.initialize()
    }
}
