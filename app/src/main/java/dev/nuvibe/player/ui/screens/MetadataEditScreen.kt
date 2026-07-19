package dev.nuvibe.player.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nuvibe.player.data.mediastore.MetadataWriteResult
import dev.nuvibe.player.data.mediastore.TrackEdit
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.ui.components.AlbumArt
import dev.nuvibe.player.ui.components.NuvibeBackground
import dev.nuvibe.player.ui.components.OverlineLabel
import dev.nuvibe.player.ui.components.clickableNoRipple
import dev.nuvibe.player.ui.theme.Display
import dev.nuvibe.player.ui.theme.NuvibeTheme
import kotlinx.coroutines.launch

/**
 * A full-screen editor for a track's tags, mirroring the desktop app's metadata
 * editing. Saving may need the user's consent (scoped storage) — that consent
 * intent is launched here and the write retried once it is granted.
 */
@Composable
fun MetadataEditScreen(
    track: Track,
    onClose: () -> Unit,
    writeMetadata: suspend (Track, TrackEdit) -> MetadataWriteResult,
) {
    val colors = NuvibeTheme.colors
    val scope = rememberCoroutineScope()

    var title by remember(track.id) { mutableStateOf(track.title) }
    var artist by remember(track.id) { mutableStateOf(track.artist) }
    var album by remember(track.id) { mutableStateOf(track.album) }
    var albumArtist by remember(track.id) { mutableStateOf(track.albumArtist) }
    var genre by remember(track.id) { mutableStateOf(track.genre) }
    var year by remember(track.id) { mutableStateOf(if (track.year > 0) track.year.toString() else "") }
    var trackNo by remember(track.id) { mutableStateOf(if (track.trackNumber > 0) track.trackNumber.toString() else "") }

    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun currentEdit() = TrackEdit(
        title = title,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        year = year.trim().toIntOrNull() ?: 0,
        trackNumber = trackNo.trim().toIntOrNull() ?: 0,
        genre = genre,
    )

    // The launcher and the write reference each other, so park the "launch the
    // consent prompt" action in a plain holder the write reads once it's set.
    val launchConsent = remember { arrayOfNulls<(android.content.IntentSender) -> Unit>(1) }

    // Try to write the tags. Scoped storage rejects the first attempt for files
    // Nuvibe doesn't own, handing back a consent intent; we launch it and, once
    // the user allows, retry with [afterConsent] set so a still-denied write ends
    // in a clear error instead of silently looping.
    fun attemptWrite(afterConsent: Boolean) {
        scope.launch {
            when (val result = writeMetadata(track, currentEdit())) {
                is MetadataWriteResult.Success -> onClose()
                is MetadataWriteResult.Error -> {
                    saving = false
                    message = result.message
                }
                is MetadataWriteResult.NeedsConsent -> {
                    if (afterConsent) {
                        saving = false
                        message = "Nuvibe couldn't get permission to edit this file."
                    } else {
                        launchConsent[0]?.invoke(result.intentSender)
                    }
                }
            }
        }
    }

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Consent granted — apply the edit for real.
            attemptWrite(afterConsent = true)
        } else {
            saving = false
            message = "Editing needs your permission to change this file."
        }
    }
    launchConsent[0] = { sender ->
        consentLauncher.launch(IntentSenderRequest.Builder(sender).build())
    }

    fun save() {
        if (saving) return
        saving = true
        message = null
        attemptWrite(afterConsent = false)
    }

    NuvibeBackground {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickableNoRipple(onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.text, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "Edit details",
                    fontFamily = Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = colors.text,
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AlbumArt(track.albumArtUri, track.album, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(14.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(track.title, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(track.folder.substringAfterLast('/'), color = colors.text3, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                EditField("Title", title, { title = it })
                EditField("Artist", artist, { artist = it })
                EditField("Album", album, { album = it })
                EditField("Album artist", albumArtist, { albumArtist = it })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.weight(1f)) {
                        EditField("Year", year, { year = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    }
                    Box(Modifier.weight(1f)) {
                        EditField("Track #", trackNo, { trackNo = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number)
                    }
                }
                EditField("Genre", genre, { genre = it })

                message?.let {
                    Text(it, color = colors.accent, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
                }
                Spacer(Modifier.height(16.dp))
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (title.isBlank() || saving) colors.panel2 else colors.accent)
                        .clickableNoRipple { if (title.isNotBlank()) save() }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (saving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.text, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        if (saving) "Saving…" else "Save",
                        color = if (title.isBlank() || saving) colors.text2 else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = NuvibeTheme.colors
    Column(Modifier.padding(bottom = 14.dp)) {
        OverlineLabel(label, modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                cursorColor = colors.accent,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.panel,
                unfocusedContainerColor = colors.panel,
            ),
        )
    }
}
