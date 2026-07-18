package dev.nuvibe.player.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.nuvibe.player.data.settings.AccentColor

// Dark palette (design defaults)
val DarkText = Color(0xFFFFFFFF)
val DarkText2 = Color(0xFFA1A1AA)
val DarkText3 = Color(0xFF6B7280)
val DarkPanel = Color(0x0AFFFFFF)   // rgba(255,255,255,.04)
val DarkPanel2 = Color(0x12FFFFFF)  // rgba(255,255,255,.07)
val DarkBorder = Color(0x14FFFFFF)  // rgba(255,255,255,.08)
val DarkBar = Color(0x1AFFFFFF)     // rgba(255,255,255,.10)
val DarkMiniBg = Color(0xDB1A1C2B)  // rgba(26,28,43,.86)
val DarkTabBg = Color(0xB8090913)   // rgba(9,9,19,.72)
val DarkBg0 = Color(0xFF070810)
val DarkBg1 = Color(0xFF0A0B14)
val DarkBg2 = Color(0xFF090913)

// Light palette ("warm paper tones")
val LightText = Color(0xFF1E1C19)
val LightText2 = Color(0xFF5F5A54)
val LightText3 = Color(0xFF8A837A)
val LightPanel = Color(0x0D4C4235)
val LightPanel2 = Color(0x174C4235)
val LightBorder = Color(0x1F4C4235)
val LightBar = Color(0x244C4235)
val LightMiniBg = Color(0xE6FFFCF7)
val LightTabBg = Color(0xD1FFFAF2)
val LightBg0 = Color(0xFFF8F3EA)
val LightBg1 = Color(0xFFEEE6D8)
val LightBg2 = Color(0xFFE6DDCF)

val PinkGlow = Color(0xFFEC4899)

fun AccentColor.color(): Color = Color(hex)
fun AccentColor.rgba(alpha: Float): Color = Color(r / 255f, g / 255f, b / 255f, alpha)

/** Design-specific tokens layered on top of Material's ColorScheme. */
@Immutable
data class NuvibeColors(
    val isDark: Boolean,
    val accent: Color,
    val accentColor: AccentColor,
    val text: Color,
    val text2: Color,
    val text3: Color,
    val panel: Color,
    val panel2: Color,
    val border: Color,
    val bar: Color,
    val miniBg: Color,
    val tabBg: Color,
    val bg0: Color,
    val bg1: Color,
    val bg2: Color,
) {
    fun accentAlpha(alpha: Float): Color = accent.copy(alpha = alpha)
}

val LocalNuvibeColors = staticCompositionLocalOf<NuvibeColors> {
    error("NuvibeColors not provided")
}
