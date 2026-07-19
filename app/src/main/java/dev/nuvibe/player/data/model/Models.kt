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
    val albumArtist: String = "",
    val genre: String = "",
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

    /**
     * A copy of this library with tracks in [hiddenFolders] or with an id in
     * [hiddenTrackIds] removed, and its albums/artists rebuilt so they never
     * reference filtered-out tracks. Discovery still finds everything; this is
     * only the user's "don't show me these" view.
     */
    fun filtered(hiddenFolders: Set<String>, hiddenTrackIds: Set<Long>): Library {
        if (hiddenFolders.isEmpty() && hiddenTrackIds.isEmpty()) return this
        val kept = tracks.filter { it.folder !in hiddenFolders && it.id !in hiddenTrackIds }
        if (kept.size == tracks.size) return this
        return buildLibrary(kept)
    }
}

/** Aggregates a flat list of [Track]s into a full [Library] (albums + artists). */
fun buildLibrary(tracks: List<Track>): Library =
    Library(tracks = tracks, albums = buildAlbums(tracks), artists = buildArtists(tracks))

private fun buildAlbums(tracks: List<Track>): List<Album> =
    tracks.groupBy { it.albumId }
        .map { (albumId, group) ->
            val ordered = group.sortedWith(
                compareBy({ it.trackNumber == 0 }, { it.trackNumber }, { it.title })
            )
            val head = group.first()
            Album(
                id = albumId,
                title = head.album,
                artist = group.dominantArtist(),
                year = group.maxOf { it.year },
                trackCount = group.size,
                albumArtUri = head.albumArtUri,
                trackIds = ordered.map { it.id },
            )
        }
        .sortedBy { it.title.lowercase() }

private fun buildArtists(tracks: List<Track>): List<Artist> =
    tracks.groupBy { it.artistId }
        .map { (artistId, group) ->
            Artist(
                id = artistId,
                name = group.dominantArtist(),
                albumCount = group.map { it.albumId }.distinct().size,
                trackCount = group.size,
            )
        }
        .sortedBy { it.name.lowercase() }

private fun List<Track>.dominantArtist(): String =
    groupingBy { it.artist }.eachCount().maxByOrNull { it.value }?.key ?: "Unknown artist"
