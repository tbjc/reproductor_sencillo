package com.musica.player.data.playlist

import com.musica.player.domain.playlist.Playlist
import com.musica.player.domain.playlist.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [PlaylistRepository] sobre Room.
 *
 * Traduce entre las entidades de la base de datos y el modelo de dominio
 * [Playlist], y asigna las posiciones de las canciones al añadirlas.
 */
class PlaylistRepositoryImpl(
    private val dao: PlaylistDao,
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> =
        dao.observePlaylists().map { rows ->
            rows.map { Playlist(it.playlist.id, it.playlist.name, it.songCount) }
        }

    override fun observeName(playlistId: Long): Flow<String?> = dao.observeName(playlistId)

    override fun observeSongIds(playlistId: Long): Flow<List<Long>> =
        dao.observeSongIds(playlistId)

    override suspend fun create(name: String): Long =
        dao.insertPlaylist(PlaylistEntity(name = name.trim(), createdAt = System.currentTimeMillis()))

    override suspend fun rename(playlistId: Long, name: String) =
        dao.updateName(playlistId, name.trim())

    override suspend fun delete(playlistId: Long) = dao.deletePlaylist(playlistId)

    override suspend fun addSongs(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        var position = dao.maxPosition(playlistId) + 1
        val entries = songIds.map { songId ->
            PlaylistSongEntity(playlistId = playlistId, songId = songId, position = position++)
        }
        dao.insertSongs(entries)
    }

    override suspend fun removeSong(playlistId: Long, songId: Long) =
        dao.deleteSong(playlistId, songId)
}
