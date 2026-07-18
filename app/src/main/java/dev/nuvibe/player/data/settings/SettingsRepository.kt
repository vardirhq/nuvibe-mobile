package dev.nuvibe.player.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nuvibe_settings")

/** Persists user preferences and lightweight playback history via DataStore. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent")
        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val PAUSE_DISCONNECT = booleanPreferencesKey("pause_on_disconnect")
        val AUDIO_FOCUS = booleanPreferencesKey("handle_audio_focus")
        val LAST_TRACK = longPreferencesKey("last_track_id")
        val LAST_POSITION = longPreferencesKey("last_position_ms")
        val RECENT_ALBUMS = stringPreferencesKey("recent_album_ids")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.DARK,
            accent = p[Keys.ACCENT]?.let { runCatching { AccentColor.valueOf(it) }.getOrNull() } ?: AccentColor.PURPLE,
            skipSilence = p[Keys.SKIP_SILENCE] ?: false,
            pauseOnDisconnect = p[Keys.PAUSE_DISCONNECT] ?: true,
            handleAudioFocus = p[Keys.AUDIO_FOCUS] ?: true,
        )
    }

    val lastTrackId: Flow<Long?> = context.dataStore.data.map { it[Keys.LAST_TRACK] }
    val lastPositionMs: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_POSITION] ?: 0L }
    val recentAlbumIds: Flow<List<Long>> = context.dataStore.data.map { p ->
        p[Keys.RECENT_ALBUMS]?.split(',')?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setAccent(accent: AccentColor) = context.dataStore.edit { it[Keys.ACCENT] = accent.name }
    suspend fun setSkipSilence(value: Boolean) = context.dataStore.edit { it[Keys.SKIP_SILENCE] = value }
    suspend fun setPauseOnDisconnect(value: Boolean) = context.dataStore.edit { it[Keys.PAUSE_DISCONNECT] = value }
    suspend fun setHandleAudioFocus(value: Boolean) = context.dataStore.edit { it[Keys.AUDIO_FOCUS] = value }

    suspend fun saveNowPlaying(trackId: Long, positionMs: Long) = context.dataStore.edit {
        it[Keys.LAST_TRACK] = trackId
        it[Keys.LAST_POSITION] = positionMs
    }

    /** Push an album to the front of the recently-played list (capped, de-duplicated). */
    suspend fun pushRecentAlbum(albumId: Long) = context.dataStore.edit { p ->
        val current = p[Keys.RECENT_ALBUMS]?.split(',')?.mapNotNull { it.toLongOrNull() } ?: emptyList()
        val updated = (listOf(albumId) + current.filter { it != albumId }).take(12)
        p[Keys.RECENT_ALBUMS] = updated.joinToString(",")
    }
}
