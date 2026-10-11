package com.musica.player.artwork

import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.key.Keyer
import coil3.request.Options
import com.musica.player.artwork.online.OnlineArtworkResolver
import com.musica.player.domain.library.Artwork
import com.musica.player.domain.settings.SettingsRepository
import okio.Buffer

/**
 * Obtiene la carátula de un tema o álbum ([Artwork]) para Coil.
 *
 * Primero intenta extraer la carátula embebida del archivo de audio con
 * [MediaMetadataRetriever] (Fase 6) y, si el archivo no trae ninguna, la busca
 * en Internet a partir del álbum y el artista (Fase 7). La extracción es
 * costosa, así que el resultado se guarda en [ArtworkCache] (disco) y Coil
 * además lo mantiene en memoria; la carátula online la gestiona
 * [OnlineArtworkResolver] con su propia caché. Si no se encuentra nada devuelve
 * null y la UI dibuja el marcador.
 */
internal class ArtworkFetcher(
    private val data: Artwork,
    private val options: Options,
    private val cache: ArtworkCache,
    private val online: OnlineArtworkResolver,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val bytes = readEmbedded() ?: readOnline() ?: return null

        return SourceFetchResult(
            source = ImageSource(
                source = Buffer().write(bytes),
                fileSystem = options.fileSystem,
            ),
            mimeType = null,
            dataSource = DataSource.DISK,
        )
    }

    private fun readEmbedded(): ByteArray? =
        cache.read(data.audioUri)
            ?: extractEmbeddedPicture(data.audioUri)
                ?.takeIf { it.isNotEmpty() }
                ?.also { cache.write(data.audioUri, it) }

    private suspend fun readOnline(): ByteArray? = online.resolve(data.album, data.artist)

    // `release()` está obsoleto desde API 34 en favor de `close()`, pero `close()`
    // solo existe a partir de API 29 y aquí se soporta desde API 26.
    @Suppress("DEPRECATION")
    private fun extractEmbeddedPicture(audioUri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(options.context, audioUri.toUri())
            retriever.embeddedPicture
        } catch (e: RuntimeException) {
            // Archivo ilegible o formato sin carátula: Coil dibuja el marcador.
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    class Factory(
        private val cache: ArtworkCache,
        private val online: OnlineArtworkResolver,
    ) : Fetcher.Factory<Artwork> {
        override fun create(data: Artwork, options: Options, imageLoader: ImageLoader): Fetcher =
            ArtworkFetcher(data, options, cache, online)
    }
}

/**
 * Clave de la caché en memoria de Coil para [Artwork]: la URI del audio. Aunque
 * la carátula online se busque por álbum, el modelo que pinta la UI sigue siendo
 * el de la canción, así que su URI es una clave estable.
 *
 * Se añade el estado de "carátulas online" para que, al activarlo o
 * desactivarlo en los ajustes, Coil no sirva desde memoria una imagen de la
 * política anterior y vuelva a resolver la carátula.
 */
internal class ArtworkKeyer(private val settings: SettingsRepository) : Keyer<Artwork> {
    override fun key(data: Artwork, options: Options): String =
        "${artworkKey(data.audioUri)}:online=${settings.onlineArtworkEnabled.value}"
}

/** Clave estable de la caché en memoria para una URI de audio. */
internal fun artworkKey(uri: String): String = "artwork:$uri"
