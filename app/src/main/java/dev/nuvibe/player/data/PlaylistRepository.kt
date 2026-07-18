package dev.nuvibe.player.data

import dev.nuvibe.player.data.local.PlaylistDao
import dev.nuvibe.player.data.local.PlaylistEntity
import dev.nuvibe.player.data.local.PlaylistTrackEntity
import dev.nuvibe.player.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepository(private val dao: PlaylistDao) {

    val playlists: Flow<List<Playlist>> = dao.observePlaylists().map { rows ->
        rows.map { row ->
            Playlist(
                id = row.playlist.id,
                name = row.playlist.name,
                createdAt = row.playlist.createdAt,
                trackIds = row.tracks.sortedBy { it.position }.map { it.trackId },
            )
        }
    }

    suspend fun create(name: String): Long =
        dao.createPlaylist(PlaylistEntity(name = name.trim().ifBlank { "Untitled" }, createdAt = System.currentTimeMillis()))

    suspend fun createWith(name: String, trackIds: List<Long>): Long {
        val id = create(name)
        if (trackIds.isNotEmpty()) addTracks(id, trackIds)
        return id
    }

    suspend fun rename(id: Long, name: String) = dao.rename(id, name.trim().ifBlank { "Untitled" })
    suspend fun delete(id: Long) = dao.deletePlaylist(id)
    suspend fun removeTrack(id: Long, trackId: Long) = dao.removeTrack(id, trackId)

    suspend fun addTracks(id: Long, trackIds: List<Long>) {
        var pos = dao.maxPosition(id) + 1
        dao.insertTracks(trackIds.map { PlaylistTrackEntity(playlistId = id, trackId = it, position = pos++) })
    }
}
