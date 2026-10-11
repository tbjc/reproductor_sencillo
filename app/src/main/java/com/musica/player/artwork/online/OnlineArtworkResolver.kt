package com.musica.player.artwork.online

import com.musica.player.domain.settings.SettingsRepository

/**
 * Resuelve la carátula online de un álbum combinando la caché en disco y la
 * fuente de Internet.
 *
 * Solo se usa si el usuario ha activado "buscar carátulas online" en los
 * ajustes ([SettingsRepository]); por defecto está desactivado y la app no sale
 * a Internet. Si la carátula ya está cacheada (o consta como ausente) no toca la
 * red. Si no hay conectividad devuelve null sin cachear nada, para poder
 * reintentarlo más tarde. El fallo de la búsqueda tampoco se cachea.
 *
 * Se exige álbum y artista válidos (no vacíos ni el marcador de MediaStore):
 * buscar solo por álbum devolvería carátulas de otro artista con facilidad.
 */
internal class OnlineArtworkResolver(
    private val source: OnlineArtworkSource,
    private val cache: OnlineArtworkCache,
    private val network: NetworkMonitor,
    private val settings: SettingsRepository,
) {

    suspend fun resolve(album: String, artist: String): ByteArray? {
        if (!settings.onlineArtworkEnabled.value) return null

        val cleanAlbum = album.usableTag() ?: return null
        val cleanArtist = artist.usableTag() ?: return null

        when (val cached = cache.read(cleanAlbum, cleanArtist)) {
            is OnlineArtworkCache.Entry.Found -> return cached.bytes
            OnlineArtworkCache.Entry.Missing -> return null
            null -> Unit
        }
        if (!network.isOnline()) return null

        val result = runCatching { source.findAlbumArtwork(cleanAlbum, cleanArtist) }
            .getOrElse { OnlineArtwork.Error }
        return when (result) {
            is OnlineArtwork.Found -> {
                cache.writeFound(cleanAlbum, cleanArtist, result.bytes)
                result.bytes
            }

            OnlineArtwork.NotFound -> {
                cache.writeMissing(cleanAlbum, cleanArtist)
                null
            }

            OnlineArtwork.Error -> null
        }
    }
}

/** Descarta etiquetas vacías y el marcador con el que MediaStore marca "desconocido". */
private fun String.usableTag(): String? =
    trim().takeIf { it.isNotEmpty() && !it.equals("<unknown>", ignoreCase = true) }
