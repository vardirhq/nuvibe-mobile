package dev.nuvibe.player.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = NuvibeTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontFamily = Display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            letterSpacing = (-0.4).sp,
            color = colors.text,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickableNoRipple(onAction),
            )
        }
    }
}

@Composable
fun OverlineLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = NuvibeTheme.colors.text3,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.8.sp,
        modifier = modifier,
    )
}
