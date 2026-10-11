package com.musica.player.domain.playlist

/**
 * Una lista de reproducción creada por el usuario.
 *
 * No guarda las canciones en sí: solo su nombre y el número de canciones que
 * contiene. Las canciones se resuelven contra la biblioteca de MediaStore por
 * su identificador (ver [resolvePlaylistSongs]), de modo que si una canción se
 * elimina del dispositivo su entrada deja de mostrarse automáticamente.
 */
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
)
