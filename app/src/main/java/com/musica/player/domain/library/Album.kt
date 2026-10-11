package com.musica.player.domain.library

/**
 * Álbum derivado de las canciones de la biblioteca.
 *
 * [artwork] apunta al archivo del primer tema del álbum, del que se extrae la
 * carátula embebida (Fase 6).
 */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val year: Int?,
    val songCount: Int,
    val artwork: Artwork,
)
