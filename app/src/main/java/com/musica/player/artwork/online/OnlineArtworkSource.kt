package com.musica.player.artwork.online

/** Resultado de buscar la carátula de un álbum en Internet. */
sealed interface OnlineArtwork {

    /** Se encontró y descargó la imagen. */
    class Found(val bytes: ByteArray) : OnlineArtwork

    /** La búsqueda funcionó pero el álbum no tiene carátula disponible. */
    data object NotFound : OnlineArtwork

    /**
     * No se pudo completar la búsqueda por un fallo de red o del servidor. No se
     * debe cachear como "sin carátula" para poder reintentarlo.
     */
    data object Error : OnlineArtwork
}

/**
 * Fuente de carátulas online. Se abstrae para poder simularla en los tests y
 * cambiar de proveedor sin tocar el resto del código.
 */
interface OnlineArtworkSource {
    /** Busca y descarga la carátula del álbum [album] del artista [artist]. */
    suspend fun findAlbumArtwork(album: String, artist: String): OnlineArtwork
}
