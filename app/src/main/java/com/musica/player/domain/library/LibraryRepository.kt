package com.musica.player.domain.library

import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente única de la biblioteca local.
 *
 * Expone flujos en memoria con el estado actual de canciones, álbumes y
 * artistas, ya derivados y ordenados. [loaded] indica si ya se completó al
 * menos un escaneo correcto (permite distinguir "todavía cargando" de
 * "biblioteca vacía").
 */
interface LibraryRepository {

    val songs: StateFlow<List<Song>>
    val albums: StateFlow<List<Album>>
    val artists: StateFlow<List<Artist>>
    val loaded: StateFlow<Boolean>

    /**
     * Re-escanea MediaStore y actualiza los flujos. Lanza excepción si el
     * escaneo falla; a partir de entonces [loaded] permanece en false hasta
     * una invocación correcta.
     */
    suspend fun refresh()
}