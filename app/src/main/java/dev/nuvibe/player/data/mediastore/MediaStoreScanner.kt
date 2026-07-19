package dev.nuvibe.player.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.Track
import dev.nuvibe.player.data.model.buildLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the device's audio collection from [MediaStore]. This is the single
 * source of truth for songs, albums and artists — there is no mock data.
 */
class MediaStoreScanner(private val context: Context) {

    private val albumArtBase: Uri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scan(): Library = withContext(Dispatchers.IO) {
        buildLibrary(queryTracks())
    }

    private fun queryTracks(): List<Track> {
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
            "${MediaStore.Audio.Media.DURATION} > 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val out = ArrayList<Track>()
        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val albumId = c.getLong(albumIdCol)
                val artist = c.getString(artistCol).clean("Unknown artist")
                val rawTrack = if (c.isNull(trackCol)) 0 else c.getInt(trackCol)
                out += Track(
                    id = id,
                    title = c.getString(titleCol).clean("Unknown title"),
                    artist = artist,
                    album = c.getString(albumCol).clean("Unknown album"),
                    albumId = albumId,
                    // ARTIST_ID isn't reliably queryable below API 30; derive a stable id from the name.
                    artistId = artist.lowercase().hashCode().toLong(),
                    durationMs = c.getLong(durCol),
                    // TRACK can encode disc*1000 + track; keep the low three digits.
                    trackNumber = if (rawTrack > 1000) rawTrack % 1000 else rawTrack,
                    year = if (c.isNull(yearCol)) 0 else c.getInt(yearCol),
                    uri = ContentUris.withAppendedId(collection, id),
                    albumArtUri = ContentUris.withAppendedId(albumArtBase, albumId),
                    dateAddedSec = c.getLong(dateCol),
                    folder = folderOf(c.getString(dataCol)),
                )
            }
        }
        return out
    }

    /** MediaStore uses "<unknown>" / blank for missing tags. */
    private fun String?.clean(fallback: String): String =
        if (this == null || isBlank() || this == "<unknown>") fallback else this

    /** Parent directory of a track's absolute path (DATA), or "" if unknown. */
    private fun folderOf(data: String?): String =
        data?.substringBeforeLast('/', "")?.takeIf { it.isNotBlank() } ?: ""
}
