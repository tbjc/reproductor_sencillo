package com.musica.player.domain.playlist

import kotlinx.coroutines.flow.Flow

/**
 * Persistencia de las playlists del usuario (Room).
 *
 * Todas las lecturas son flujos reactivos: la interfaz se actualiza sola al
 * crear, renombrar, vaciar o borrar una lista. Las escrituras son `suspend`.
 */
interface PlaylistRepository {

    /** Playlists ordenadas por nombre, con el número de canciones de cada una. */
    fun observePlaylists(): Flow<List<Playlist>>

    /** Nombre de la playlist, o `null` si ya no existe. */
    fun observeName(playlistId: Long): Flow<String?>

    /** Identificadores de las canciones de la playlist, en su orden. */
    fun observeSongIds(playlistId: Long): Flow<List<Long>>

    /** Crea una playlist y devuelve su identificador. */
    suspend fun create(name: String): Long

    suspend fun rename(playlistId: Long, name: String)

    suspend fun delete(playlistId: Long)

    /** Añade las canciones indicadas al final de la playlist (sin duplicados). */
    suspend fun addSongs(playlistId: Long, songIds: List<Long>)

    suspend fun removeSong(playlistId: Long, songId: Long)
}
