package dev.nuvibe.player.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.data.settings.AccentColor
import dev.nuvibe.player.data.settings.AppSettings
import dev.nuvibe.player.data.settings.ThemeMode
import dev.nuvibe.player.ui.components.NuvibeLogo
import dev.nuvibe.player.ui.components.OverlineLabel
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme
import dev.nuvibe.player.ui.theme.color

@Composable
fun SettingsScreen(
    settings: AppSettings,
    songCount: Int,
    isScanning: Boolean,
    folders: List<Pair<String, Int>>,
    hiddenFolders: Set<String>,
    hiddenTracks: List<Track>,
    onSetTheme: (ThemeMode) -> Unit,
    onSetAccent: (AccentColor) -> Unit,
    onSetSkipSilence: (Boolean) -> Unit,
    onSetPauseOnDisconnect: (Boolean) -> Unit,
    onSetHandleAudioFocus: (Boolean) -> Unit,
    onSetFolderHidden: (String, Boolean) -> Unit,
    onUnhideTrack: (Long) -> Unit,
    onRescan: () -> Unit,
) {
    val colors = NuvibeTheme.colors
    val context = LocalContext.current
    var showFolders by remember { mutableStateOf(false) }
    var showHidden by remember { mutableStateOf(false) }
    val dark = settings.themeMode == ThemeMode.DARK

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            "Settings",
            fontFamily = Display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            letterSpacing = (-0.4).sp,
            color = colors.text,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
        )

        OverlineLabel("Appearance", Modifier.padding(bottom = 11.dp))
        Card {
            RowBetween(
                title = "Theme",
                subtitle = if (dark) "Dark · easy on late nights" else "Light · warm paper tones",
                divider = true,
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.panel2)
                        .padding(3.dp),
                ) {
                    SegChip("Dark", dark) { onSetTheme(ThemeMode.DARK) }
                    SegChip("Light", !dark) { onSetTheme(ThemeMode.LIGHT) }
                }
            }
            Column(Modifier.padding(16.dp)) {
                Text("Accent color", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AccentColor.entries.forEach { accent ->
                        val selected = settings.accent == accent
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accent.color())
                                .border(2.5.dp, if (selected) Color.White else Color.Transparent, CircleShape)
                                .clickableNoRipple { onSetAccent(accent) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        OverlineLabel("Playback", Modifier.padding(bottom = 11.dp))
        Card {
            ToggleRow("Skip silence", "Trim quiet gaps between and inside tracks", settings.skipSilence, divider = true, onToggle = onSetSkipSilence)
            ToggleRow("Pause on disconnect", "Stop when headphones or Bluetooth unplug", settings.pauseOnDisconnect, divider = true, onToggle = onSetPauseOnDisconnect)
            ToggleRow("Respect other apps", "Pause or duck for calls, alarms and assistants", settings.handleAudioFocus, divider = false, onToggle = onSetHandleAudioFocus)
        }

        Spacer(Modifier.height(22.dp))
        OverlineLabel("Library", Modifier.padding(bottom = 11.dp))
        Card {
            val spin = rememberInfiniteTransition(label = "rescan")
            val angle by spin.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                label = "rescanAngle",
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickableNoRipple {
                        if (!isScanning) {
                            onRescan()
                            Toast.makeText(context, "Rescanning your library…", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Refresh, null,
                    tint = colors.accent,
                    modifier = Modifier
                        .size(20.dp)
                        .then(if (isScanning) Modifier.rotate(angle) else Modifier),
                )
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text("Rescan library", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (isScanning) "Scanning your device…" else "$songCount tracks indexed · tap to refresh",
                        color = colors.text2,
                        fontSize = 12.sp,
                    )
                }
            }
            Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.border))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickableNoRipple { if (folders.isNotEmpty()) showFolders = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Folder, null, tint = colors.text2, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text("Music folders", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    val hiddenCount = folders.count { it.first in hiddenFolders }
                    Text(
                        when {
                            folders.isEmpty() -> "No folders indexed yet"
                            hiddenCount > 0 -> "${folders.size} folders · $hiddenCount hidden · tap to manage"
                            folders.size == 1 -> "1 folder · tap to manage"
                            else -> "${folders.size} folders · tap to manage"
                        },
                        color = colors.text2,
                        fontSize = 12.sp,
                    )
                }
            }
            if (hiddenTracks.isNotEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.border))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { showHidden = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.VisibilityOff, null, tint = colors.text2, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hidden tracks", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (hiddenTracks.size == 1) "1 track hidden · tap to manage"
                            else "${hiddenTracks.size} tracks hidden · tap to manage",
                            color = colors.text2,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NuvibeLogo(size = 30.dp)
            Spacer(Modifier.height(8.dp))
            Text("Nuvibe for Android", fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.text)
            Text("Version 1.0 · Local-first listening room", color = colors.text3, fontSize = 12.sp)
        }
        Spacer(Modifier.height(20.dp))
    }

    if (showFolders) {
        FoldersDialog(
            folders = folders,
            hiddenFolders = hiddenFolders,
            onSetFolderHidden = onSetFolderHidden,
            onDismiss = { showFolders = false },
        )
    }

    if (showHidden) {
        HiddenTracksDialog(
            tracks = hiddenTracks,
            onUnhide = onUnhideTrack,
            onDismiss = { showHidden = false },
        )
    }
}

@Composable
private fun FoldersDialog(
    folders: List<Pair<String, Int>>,
    hiddenFolders: Set<String>,
    onSetFolderHidden: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = NuvibeTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Music folders") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    "Turn a folder off to hide its tracks from your library. Discovery still finds them — nothing is deleted.",
                    color = colors.text3,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                folders.forEach { (path, count) ->
                    val hidden = path in hiddenFolders
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Folder, null, tint = colors.text3, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                path.substringAfterLast('/').ifBlank { path },
                                color = if (hidden) colors.text3 else colors.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${prettyPath(path)} · $count",
                                color = colors.text3,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        NuvibeToggle(checked = !hidden) { onSetFolderHidden(path, !hidden) }
                    }
                }
            }
        },
    )
}

@Composable
private fun HiddenTracksDialog(
    tracks: List<Track>,
    onUnhide: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = NuvibeTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Hidden tracks") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (tracks.isEmpty()) {
                    Text("No hidden tracks.", color = colors.text3, fontSize = 13.sp)
                }
                tracks.forEach { track ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                track.title,
                                color = colors.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${track.artist} · ${track.album}",
                                color = colors.text3,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        TextButton(onClick = { onUnhide(track.id) }) { Text("Unhide") }
                    }
                }
            }
        },
    )
}

/** Trim the storage-root prefix so folder paths read cleanly. */
private fun prettyPath(path: String): String = path
    .removePrefix("/storage/emulated/0/")
    .removePrefix("/storage/")
    .ifBlank { "Internal storage" }

@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val colors = NuvibeTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.panel)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp)),
        content = content,
    )
}

@Composable
private fun RowBetween(title: String, subtitle: String, divider: Boolean, trailing: @Composable () -> Unit) {
    val colors = NuvibeTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = colors.text2, fontSize = 12.sp)
        }
        trailing()
    }
    if (divider) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.border))
}

@Composable
private fun SegChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = NuvibeTheme.colors
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.accent else Color.Transparent)
            .clickableNoRipple(onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(label, color = if (selected) Color.White else colors.text2, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, divider: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = NuvibeTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = colors.text2, fontSize = 12.sp)
        }
        NuvibeToggle(checked) { onToggle(!checked) }
    }
    if (divider) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.border))
}

@Composable
private fun NuvibeToggle(checked: Boolean, onClick: () -> Unit) {
    val colors = NuvibeTheme.colors
    val track by animateColorAsState(if (checked) colors.accent else colors.panel2, label = "track")
    val knobOffset by animateDpAsState(if (checked) 23.dp else 3.dp, label = "knob")
    Box(
        Modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(track)
            .clickableNoRipple(onClick),
    ) {
        Box(
            Modifier
                .offset(x = knobOffset, y = 3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
