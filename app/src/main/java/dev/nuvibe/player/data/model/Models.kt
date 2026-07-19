package dev.nuvibe.player.data.model

import android.net.Uri

/** A single playable audio file discovered from MediaStore. */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val year: Int,
    val uri: Uri,
    val albumArtUri: Uri?,
    val dateAddedSec: Long,
    val folder: String,
) {
    /** Stable key used as the MediaItem mediaId. */
    val mediaId: String get() = id.toString()
}

/** An album aggregated from its tracks. */
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val year: Int,
    val trackCount: Int,
    val albumArtUri: Uri?,
    val trackIds: List<Long>,
)

/** An artist aggregated from albums/tracks. */
data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
)

/** A user-created playlist (persisted in Room). */
data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val trackIds: List<Long>,
)

/** A generated "smart" mix built from the real library — not persisted. */
data class SmartMix(
    val key: String,
    val name: String,
    val description: String,
    val trackIds: List<Long>,
    val gradientSeed: String,
)

/** The full in-memory snapshot of the on-device library. */
data class Library(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
) {
    val byId: Map<Long, Track> = tracks.associateBy { it.id }
    val albumsById: Map<Long, Album> = albums.associateBy { it.id }

    /** Distinct source folders with their track counts, most-populated first. */
    val folders: List<Pair<String, Int>> = tracks
        .filter { it.folder.isNotBlank() }
        .groupingBy { it.folder }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

    fun track(id: Long): Track? = byId[id]
    fun tracksFor(ids: List<Long>): List<Track> = ids.mapNotNull { byId[it] }

    val isEmpty: Boolean get() = tracks.isEmpty()
}
