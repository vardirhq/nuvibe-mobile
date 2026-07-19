package dev.nuvibe.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import dev.nuvibe.player.data.settings.AccentColor
import dev.nuvibe.player.data.settings.ThemeMode

@Composable
fun NuvibeTheme(
    themeMode: ThemeMode,
    accent: AccentColor,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val accentColor = accent.color()

    val nuvibeColors = if (dark) {
        NuvibeColors(
            isDark = true, accent = accentColor, accentColor = accent,
            text = DarkText, text2 = DarkText2, text3 = DarkText3,
            panel = DarkPanel, panel2 = DarkPanel2, border = DarkBorder, bar = DarkBar,
            miniBg = DarkMiniBg, tabBg = DarkTabBg,
            bg0 = DarkBg0, bg1 = DarkBg1, bg2 = DarkBg2,
        )
    } else {
        NuvibeColors(
            isDark = false, accent = accentColor, accentColor = accent,
            text = LightText, text2 = LightText2, text3 = LightText3,
            panel = LightPanel, panel2 = LightPanel2, border = LightBorder, bar = LightBar,
            miniBg = LightMiniBg, tabBg = LightTabBg,
            bg0 = LightBg0, bg1 = LightBg1, bg2 = LightBg2,
        )
    }

    val colorScheme = if (dark) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            background = DarkBg0,
            surface = DarkBg1,
            onBackground = DarkText,
            onSurface = DarkText,
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            background = LightBg0,
            surface = LightBg1,
            onBackground = LightText,
            onSurface = LightText,
        )
    }

    CompositionLocalProvider(LocalNuvibeColors provides nuvibeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NuvibeTypography,
            content = content,
        )
    }
}

/** Convenience accessor mirroring `MaterialTheme.colorScheme`. */
object NuvibeTheme {
    val colors: NuvibeColors
        @Composable get() = LocalNuvibeColors.current
}
