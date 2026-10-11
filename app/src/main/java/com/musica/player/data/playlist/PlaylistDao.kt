package com.musica.player.data.playlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a las tablas de playlists. Las consultas de lectura devuelven `Flow`
 * para que la interfaz se actualice sola ante cualquier cambio.
 */
@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT playlists.*, (
            SELECT COUNT(*) FROM playlist_songs WHERE playlistId = playlists.id
        ) AS songCount
        FROM playlists
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Query("SELECT name FROM playlists WHERE id = :playlistId")
    fun observeName(playlistId: Long): Flow<String?>

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observeSongIds(playlistId: Long): Flow<List<Long>>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun updateName(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongs(entries: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSong(playlistId: Long, songId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int
}
