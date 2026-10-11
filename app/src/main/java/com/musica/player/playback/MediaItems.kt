package com.musica.player.playback

import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.musica.player.domain.library.Song

private const val EXTRA_ARTIST_ID = "com.musica.player.extra.ARTIST_ID"
private const val EXTRA_ALBUM_ID = "com.musica.player.extra.ALBUM_ID"
private const val EXTRA_DURATION_MS = "com.musica.player.extra.DURATION_MS"

/**
 * Convierte una canción de la biblioteca en un elemento reproducible de Media3,
 * con los metadatos que consumen la notificación y la pantalla de bloqueo.
 *
 * Los campos que no son necesarios para reproducir (tamaño, mime, número de
 * pista...) no viajan en el elemento; [toSong] los deja con valores por
 * defecto.
 */
fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(contentUri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(
                bundleOf(
                    EXTRA_ARTIST_ID to artistId,
                    EXTRA_ALBUM_ID to albumId,
                    EXTRA_DURATION_MS to durationMs,
                ),
            )
            .build(),
    )
    .build()

/**
 * Reconstruye una [Song] a partir de un elemento de la cola del reproductor.
 *
 * El reproductor es la fuente de verdad de la cola, así que el estado de la UI
 * se reconstruye desde sus elementos. Solo se conservan los datos que necesita
 * la reproducción; el resto queda con valores por defecto.
 */
fun MediaItem.toSong(): Song {
    val metadata = mediaMetadata
    val extras = metadata.extras
    return Song(
        id = mediaId.toLongOrNull() ?: -1L,
        title = metadata.title?.toString().orEmpty(),
        artist = metadata.artist?.toString().orEmpty(),
        artistId = extras?.getLong(EXTRA_ARTIST_ID, -1L) ?: -1L,
        album = metadata.albumTitle?.toString().orEmpty(),
        albumId = extras?.getLong(EXTRA_ALBUM_ID, -1L) ?: -1L,
        genre = null,
        durationMs = extras?.getLong(EXTRA_DURATION_MS, 0L) ?: 0L,
        size = 0L,
        mimeType = null,
        trackNumber = null,
        discNumber = null,
        year = null,
        dateModified = 0L,
        contentUri = localConfiguration?.uri?.toString().orEmpty(),
    )
}
