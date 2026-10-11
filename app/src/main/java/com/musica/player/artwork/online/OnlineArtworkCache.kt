package com.musica.player.artwork.online

import com.musica.player.artwork.sha256Hex
import com.musica.player.artwork.writeAtomically
import java.io.File

/**
 * Caché en disco de las carátulas online, con memoria de los "no encontrado".
 *
 * Cada consulta se identifica por (álbum, artista). Se guarda la imagen
 * descargada (`<clave>.img`) o un marcador vacío (`<clave>.none`) cuando el
 * álbum no tiene carátula, para no repetir la búsqueda en cada arranque. Vive
 * dentro de la caché de la app, que Android puede purgar bajo presión.
 */
internal class OnlineArtworkCache(private val directory: File) {

    sealed interface Entry {
        /** Carátula encontrada y cacheada en disco. */
        class Found(val bytes: ByteArray) : Entry

        /** Ya se buscó y no había carátula disponible. */
        data object Missing : Entry
    }

    /** Devuelve la entrada cacheada, o null si nunca se consultó. */
    fun read(album: String, artist: String): Entry? {
        val base = keyFor(album, artist)
        val image = File(directory, "$base.img")
        if (image.isFile && image.length() > 0L) {
            val bytes = runCatching { image.readBytes() }.getOrNull()
            if (bytes != null && bytes.isNotEmpty()) return Entry.Found(bytes)
        }
        if (File(directory, "$base.none").isFile) return Entry.Missing
        return null
    }

    /** Guarda [bytes] como carátula del álbum y borra un posible marcador. */
    fun writeFound(album: String, artist: String, bytes: ByteArray) {
        if (bytes.isEmpty()) return
        runCatching {
            val base = keyFor(album, artist)
            writeAtomically(File(directory, "$base.img"), bytes)
            File(directory, "$base.none").delete()
        }
    }

    /** Marca que el álbum no tiene carátula, para no volver a consultarlo. */
    fun writeMissing(album: String, artist: String) {
        runCatching {
            directory.mkdirs()
            File(directory, "${keyFor(album, artist)}.none").writeBytes(ByteArray(0))
        }
    }

    private fun keyFor(album: String, artist: String): String =
        sha256Hex("v1|${normalize(album)}|${normalize(artist)}")

    private fun normalize(value: String): String = value.trim().lowercase()
}
