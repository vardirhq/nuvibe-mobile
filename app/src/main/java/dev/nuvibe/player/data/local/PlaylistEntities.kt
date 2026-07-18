package dev.nuvibe.player.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "position"],
    indices = [Index("playlistId")],
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,
)

data class PlaylistWithTracks(
    @Embedded val playlist: PlaylistEntity,
    @Relation(parentColumn = "id", entityColumn = "playlistId")
    val tracks: List<PlaylistTrackEntity>,
)
