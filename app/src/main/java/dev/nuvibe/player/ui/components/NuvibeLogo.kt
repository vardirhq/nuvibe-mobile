package dev.nuvibe.player.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nuvibe.player.R

@Composable
fun NuvibeLogo(modifier: Modifier = Modifier, size: Dp = 34.dp) {
    Image(
        painter = painterResource(R.drawable.ic_nuvibe_logo),
        contentDescription = "Nuvibe",
        modifier = modifier.size(size),
    )
}
