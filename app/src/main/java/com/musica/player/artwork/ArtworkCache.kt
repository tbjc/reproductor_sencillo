package com.musica.player.artwork

import java.io.File

/**
 * Caché en disco de las carátulas embebidas extraídas de los archivos de audio.
 *
 * Extraer la carátula con `MediaMetadataRetriever` obliga a abrir el archivo de
 * audio, así que el resultado se guarda como una imagen (una por archivo) y se
 * reutiliza en los siguientes arranques. Coil no cachea en disco las imágenes
 * locales, así que esta caché la gestiona la app.
 *
 * La clave es un hash de la URI del audio; el directorio (dentro de la caché de
 * la app) puede vaciarlo el sistema bajo presión de memoria.
 */
internal class ArtworkCache(private val directory: File) {

    /** Devuelve la carátula cacheada de [audioUri], o null si no existe. */
    fun read(audioUri: String): ByteArray? {
        val file = fileFor(audioUri)
        if (!file.isFile || file.length() == 0L) return null
        return runCatching { file.readBytes() }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    /** Guarda [bytes] como carátula de [audioUri]. */
    fun write(audioUri: String, bytes: ByteArray) {
        if (bytes.isEmpty()) return
        runCatching { writeAtomically(fileFor(audioUri), bytes) }
    }

    private fun fileFor(audioUri: String): File = File(directory, "${sha256Hex(audioUri)}.img")
}
