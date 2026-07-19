package dev.nuvibe.player.data.mediastore

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The editable tag fields Nuvibe exposes, mirroring what the desktop app edits. */
data class TrackEdit(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val year: Int,
    val trackNumber: Int,
    val genre: String,
)

/** Outcome of a metadata write. */
sealed interface MetadataWriteResult {
    data object Success : MetadataWriteResult

    /**
     * The OS needs the user to grant write access to this file before the edit
     * can be applied. Launch [intentSender] and, on OK, retry the write.
     */
    data class NeedsConsent(val intentSender: IntentSender) : MetadataWriteResult

    data class Error(val message: String) : MetadataWriteResult
}

/**
 * Writes user-edited tags back to a MediaStore audio item.
 *
 * Scoped storage means an app may not modify media it didn't create without the
 * user's consent: on API 30+ we ask up front with [createWriteRequest]; on API
 * 29 the update throws a [RecoverableSecurityException] we turn into a consent
 * prompt; below that a plain `WRITE_EXTERNAL_STORAGE` grant is enough.
 */
class MetadataWriter(private val context: Context) {

    /**
     * On API 30+, an [IntentSender] that asks the user to allow editing [uri].
     * Null on older versions, where the recoverable-exception flow is used.
     */
    fun createWriteRequest(uri: Uri): IntentSender? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createWriteRequest(context.contentResolver, listOf(uri)).intentSender
        } else {
            null
        }

    suspend fun write(uri: Uri, edit: TrackEdit): MetadataWriteResult =
        withContext(Dispatchers.IO) {
            try {
                val rows = context.contentResolver.update(uri, edit.toContentValues(), null, null)
                if (rows > 0) {
                    MetadataWriteResult.Success
                } else {
                    MetadataWriteResult.Error("Couldn't save changes to this file.")
                }
            } catch (e: SecurityException) {
                // Isolate the API 29 class reference in its own method so the
                // dex verifier doesn't touch it on API 26–28.
                val consent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    recoverableIntent(e)
                } else {
                    null
                }
                if (consent != null) {
                    MetadataWriteResult.NeedsConsent(consent)
                } else {
                    MetadataWriteResult.Error("Nuvibe doesn't have permission to edit this file.")
                }
            } catch (e: Exception) {
                MetadataWriteResult.Error(e.message ?: "Couldn't save changes to this file.")
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun recoverableIntent(e: SecurityException): IntentSender? =
        (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender

    private fun TrackEdit.toContentValues(): ContentValues = ContentValues().apply {
        put(MediaStore.Audio.Media.TITLE, title.trim())
        put(MediaStore.Audio.Media.ARTIST, artist.trim())
        put(MediaStore.Audio.Media.ALBUM, album.trim())
        put(MediaStore.Audio.Media.YEAR, year)
        put(MediaStore.Audio.Media.TRACK, trackNumber)
        // ALBUM_ARTIST and GENRE columns are only writable from API 30 onwards.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            put(MediaStore.Audio.Media.ALBUM_ARTIST, albumArtist.trim())
            put(MediaStore.Audio.Media.GENRE, genre.trim())
        }
    }
}
