package dev.nuvibe.player.data.settings

/** Two-way theme switch, mirroring the design's Dark / Light control. */
enum class ThemeMode { DARK, LIGHT }

/**
 * Accent palette from the design. [rgb] is kept as a comma triple for the
 * translucent "rgba(...)" glows used across the UI.
 */
enum class AccentColor(val hex: Long, val r: Int, val g: Int, val b: Int) {
    PURPLE(0xFF6366F1, 99, 102, 241),
    BLUE(0xFF3B82F6, 59, 130, 246),
    PINK(0xFFEC4899, 236, 72, 153),
    GREEN(0xFF10B981, 16, 185, 129);
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accent: AccentColor = AccentColor.PURPLE,
    val skipSilence: Boolean = false,
    val pauseOnDisconnect: Boolean = true,
    val handleAudioFocus: Boolean = true,
)
