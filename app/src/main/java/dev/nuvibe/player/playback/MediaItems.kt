package dev.nuvibe.player.playback

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dev.nuvibe.player.data.model.Track

object MediaItemKeys {
    const val ALBUM_ID = "nuvibe.albumId"
    const val DURATION_MS = "nuvibe.durationMs"
}

/** Build a fully-populated [MediaItem] (uri + metadata) for a library [Track]. */
fun Track.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putLong(MediaItemKeys.ALBUM_ID, albumId)
        putLong(MediaItemKeys.DURATION_MS, durationMs)
    }
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setAlbumArtist(artist)
        .setTrackNumber(trackNumber.takeIf { it > 0 })
        .setReleaseYear(year.takeIf { it > 0 })
        .setArtworkUri(albumArtUri)
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .setExtras(extras)
        .build()

    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}
