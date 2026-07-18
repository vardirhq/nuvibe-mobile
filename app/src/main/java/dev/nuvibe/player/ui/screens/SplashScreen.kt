package dev.nuvibe.player.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.ui.components.EqualizerBars
import dev.nuvibe.player.ui.components.NuvibeLogo
import dev.nuvibe.player.ui.theme.Display
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var exiting by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (exiting) 0f else 1f, tween(500), label = "splashAlpha")
    val scale by animateFloatAsState(if (exiting) 1.06f else 1f, tween(500), label = "splashScale")

    LaunchedEffect(Unit) {
        delay(2200)
        exiting = true
        delay(520)
        onFinished()
    }

    val ringSpin = rememberInfiniteTransition(label = "ring")
    val ringAngle by ringSpin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing)),
        label = "ringAngle",
    )

    Box(
        Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(
                Brush.linearGradient(
                    0f to Color(0xFF070810),
                    0.55f to Color(0xFF0A0B14),
                    1f to Color(0xFF090913),
                ),
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x386366F1), Color.Transparent),
                    center = Offset(0.22f * 1200f, 0.12f * 1200f),
                    radius = 900f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .scale(scale)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    Modifier
                        .size(190.dp)
                        .rotate(ringAngle),
                ) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = size.minDimension / 2f * 0.94f,
                        style = Stroke(
                            width = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f),
                        ),
                    )
                }
                NuvibeLogo(size = 128.dp)
            }
            Text(
                "Nuvibe",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color.White, Color(0xFFC7D2FE), Color(0xFFF9A8D4), Color.White),
                    ),
                ),
                fontFamily = Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 56.sp,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                "LISTENING ROOM",
                color = Color(0xFFA1A1AA),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
            Spacer(Modifier.height(34.dp))
            EqualizerBars(
                playing = true,
                color = Color(0xFF818CF8),
                barCount = 7,
                barWidth = 4.dp,
                barHeight = 24.dp,
            )
            Text(
                "Warming up the room…",
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}
