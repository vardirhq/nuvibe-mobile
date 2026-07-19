package dev.nuvibe.player.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

/**
 * Deterministic album gradients. Tracks without embedded artwork get a stable,
 * design-matched gradient derived from a seed (album title / artist), so the
 * library keeps the mock's colourful look while showing real cover art when it
 * exists.
 */
object Gradients {
    private val palettes: List<List<Color>> = listOf(
        listOf(Color(0xFF6366F1), Color(0xFFEC4899)),
        listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)),
        listOf(Color(0xFF10B981), Color(0xFF3B82F6)),
        listOf(Color(0xFFEC4899), Color(0xFFF97316)),
        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)),
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        listOf(Color(0xFF6366F1), Color(0xFF3B82F6)),
        listOf(Color(0xFF10B981), Color(0xFF6366F1)),
        listOf(Color(0xFF14B8A6), Color(0xFF8B5CF6)),
        listOf(Color(0xFFF43F5E), Color(0xFF8B5CF6)),
    )

    fun colorsFor(seed: String): List<Color> {
        val idx = (seed.hashCode().absoluteValue) % palettes.size
        return palettes[idx]
    }

    /** Diagonal (≈135°) gradient, matching the design's album tiles. */
    fun brush(seed: String): Brush = Brush.linearGradient(colorsFor(seed))
}
