package dev.nuvibe.player.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistWithTracks>>

    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :id")
    suspend fun clearTracks(id: Long)

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_tracks WHERE playlistId = :id")
    suspend fun maxPosition(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(ref: PlaylistTrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(refs: List<PlaylistTrackEntity>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :id AND trackId = :trackId")
    suspend fun removeTrack(id: Long, trackId: Long)
}
