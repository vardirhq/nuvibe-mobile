package dev.nuvibe.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nuvibe.player.ui.NuvibeApp
import dev.nuvibe.player.ui.NuvibeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val vm: NuvibeViewModel = viewModel(factory = NuvibeViewModel.Factory)
            NuvibeApp(vm)
        }
    }
}
