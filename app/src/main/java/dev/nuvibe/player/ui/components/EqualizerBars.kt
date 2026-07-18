package dev.nuvibe.player.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Small dancing equalizer used to mark the currently-playing row. */
@Composable
fun EqualizerBars(
    playing: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 3,
    barWidth: Dp = 2.5.dp,
    barHeight: Dp = 15.dp,
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val durations = listOf(600, 780, 500, 700)
    Row(
        modifier = modifier.height(barHeight),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(barCount) { i ->
            val anim by transition.animateFloat(
                initialValue = 0.28f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durations[i % durations.size], easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar$i",
            )
            val scale = if (playing) anim else 0.4f
            androidx.compose.foundation.layout.Box(
                Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .graphicsLayer {
                        scaleY = scale
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                    .background(color, RoundedCornerShape(2.dp)),
            )
        }
    }
}
