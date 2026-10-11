package com.musica.player.domain.library

/**
 * Una canción de la biblioteca local detectada vía MediaStore.
 *
 * [contentUri] es la URI `content://` de la que se pueden leer los datos de
 * audio (la consume Media3 en fases posteriores). Se guarda como cadena para
 * mantener el dominio independiente de `android.net.Uri` y facilitar la
 * persistencia en Room (Fase 5).
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long,
    val album: String,
    val albumId: Long,
    val genre: String?,
    val durationMs: Long,
    val size: Long,
    val mimeType: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val dateModified: Long,
    val contentUri: String,
)