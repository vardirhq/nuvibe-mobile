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
 * user's consent. Rather than guess up front, we simply attempt the write: on
 * API 29+ the OS rejects it with a [SecurityException], which we turn into a
 * consent prompt ([MetadataWriteResult.NeedsConsent]) for the caller to launch;
 * once the user allows, the caller retries [write] and it goes through. Below
 * API 29 a plain `WRITE_EXTERNAL_STORAGE` grant is enough and no prompt is
 * needed.
 */
class MetadataWriter(private val context: Context) {

    suspend fun write(uri: Uri, edit: TrackEdit): MetadataWriteResult =
        withContext(Dispatchers.IO) {
            try {
                applyUpdate(uri, edit.toContentValues(extended = true))
            } catch (e: SecurityException) {
                consentResult(uri, e)
            } catch (e: IllegalArgumentException) {
                // A column such as ALBUM_ARTIST or GENRE can be read-only on some
                // OS versions/devices; don't let it sink the whole edit — retry
                // with just the core tags that are always writable.
                retryCoreOnly(uri, edit, e)
            } catch (e: UnsupportedOperationException) {
                retryCoreOnly(uri, edit, e)
            } catch (e: Exception) {
                MetadataWriteResult.Error(e.message ?: "Couldn't save changes to this file.")
            }
        }

    private fun applyUpdate(uri: Uri, values: ContentValues): MetadataWriteResult {
        val rows = context.contentResolver.update(uri, values, null, null)
        return if (rows > 0) {
            MetadataWriteResult.Success
        } else {
            MetadataWriteResult.Error("Couldn't save changes to this file.")
        }
    }

    private fun retryCoreOnly(uri: Uri, edit: TrackEdit, cause: Exception): MetadataWriteResult =
        try {
            applyUpdate(uri, edit.toContentValues(extended = false))
        } catch (e: SecurityException) {
            consentResult(uri, e)
        } catch (e: Exception) {
            MetadataWriteResult.Error(cause.message ?: "Couldn't save changes to this file.")
        }

    private fun consentResult(uri: Uri, e: SecurityException): MetadataWriteResult {
        val consent = writeConsentIntent(uri, e)
        return if (consent != null) {
            MetadataWriteResult.NeedsConsent(consent)
        } else {
            MetadataWriteResult.Error("Nuvibe doesn't have permission to edit this file.")
        }
    }

    /**
     * The [IntentSender] to launch so the user can grant write access to [uri].
     * On API 30+ we build a MediaStore write request; on API 29 we pull the
     * recovery action out of the thrown [RecoverableSecurityException].
     */
    private fun writeConsentIntent(uri: Uri, e: SecurityException): IntentSender? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> createWriteRequestIntent(uri)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> recoverableIntent(e)
        else -> null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createWriteRequestIntent(uri: Uri): IntentSender =
        MediaStore.createWriteRequest(context.contentResolver, listOf(uri)).intentSender

    // Isolate the API 29 class reference in its own method so the dex verifier
    // doesn't touch it on API 26–28.
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun recoverableIntent(e: SecurityException): IntentSender? =
        (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender

    private fun TrackEdit.toContentValues(extended: Boolean): ContentValues = ContentValues().apply {
        put(MediaStore.Audio.Media.TITLE, title.trim())
        put(MediaStore.Audio.Media.ARTIST, artist.trim())
        put(MediaStore.Audio.Media.ALBUM, album.trim())
        put(MediaStore.Audio.Media.YEAR, year)
        put(MediaStore.Audio.Media.TRACK, trackNumber)
        // ALBUM_ARTIST and GENRE columns only exist from API 30 onwards, and even
        // there they aren't writable on every device — [extended] lets the caller
        // drop them and retry with just the core tags.
        if (extended && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            put(MediaStore.Audio.Media.ALBUM_ARTIST, albumArtist.trim())
            put(MediaStore.Audio.Media.GENRE, genre.trim())
        }
    }
}
