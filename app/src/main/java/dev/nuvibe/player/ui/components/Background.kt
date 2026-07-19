package dev.nuvibe.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.nuvibe.player.ui.theme.NuvibeTheme
import dev.nuvibe.player.ui.theme.PinkGlow

/** The layered ambient gradient behind every screen, driven by the accent. */
@Composable
fun NuvibeBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = NuvibeTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        0f to colors.bg0,
                        0.5f to colors.bg1,
                        1f to colors.bg2,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(colors.accentAlpha(if (colors.isDark) 0.20f else 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.15f, -size.height * 0.05f),
                        radius = size.maxDimension * 0.9f,
                    ),
                )
                if (colors.isDark) {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(PinkGlow.copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(size.width, 0f),
                            radius = size.maxDimension * 0.75f,
                        ),
                    )
                }
            },
        content = content,
    )
}
