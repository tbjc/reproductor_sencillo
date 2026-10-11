package com.musica.player.artwork

import java.io.File
import java.security.MessageDigest

/**
 * Utilidades compartidas por las cachés de carátulas: cálculo de la clave y
 * escritura atómica de los archivos.
 */

/** Devuelve el hash SHA-256 de [value] en hexadecimal, usado como nombre de archivo. */
internal fun sha256Hex(value: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString(separator = "") { "%02x".format(it) }

/**
 * Escribe [bytes] en [file] de forma atómica: primero en un archivo temporal y
 * luego lo renombra, para no dejar una carátula a medias si el proceso muere
 * durante la escritura.
 */
internal fun writeAtomically(file: File, bytes: ByteArray) {
    file.parentFile?.mkdirs()
    val tmp = File(file.parentFile, "${file.name}.tmp")
    tmp.writeBytes(bytes)
    if (!tmp.renameTo(file)) {
        file.writeBytes(bytes)
        tmp.delete()
    }
}
