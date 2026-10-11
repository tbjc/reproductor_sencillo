package com.musica.player.domain.library

/**
 * Artista derivado de las canciones de la biblioteca.
 */
data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int,
)