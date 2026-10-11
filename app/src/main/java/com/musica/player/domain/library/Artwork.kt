package com.musica.player.domain.library

/**
 * Carátula de una canción o álbum.
 *
 * Normalmente el tema lleva la carátula embebida en el propio archivo de audio,
 * así que [audioUri] es la URI `content://` de la que se extrae (Fase 6). Si el
 * archivo no trae ninguna, [album] y [artist] se usan para buscarla en Internet
 * (Fase 7) y, una vez descargada, se guarda en la caché en disco. Se guardan
 * como cadenas para mantener el dominio independiente de `android.net.Uri`.
 */
data class Artwork(
    val audioUri: String,
    val album: String = "",
    val artist: String = "",
)
