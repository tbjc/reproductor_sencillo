package com.musica.player.artwork

import coil3.ImageLoader
import coil3.PlatformContext
import com.musica.player.artwork.online.ConnectivityNetworkMonitor
import com.musica.player.artwork.online.DeezerArtworkSource
import com.musica.player.artwork.online.OnlineArtworkCache
import com.musica.player.artwork.online.OnlineArtworkResolver
import com.musica.player.artwork.online.UrlConnectionHttpClient
import com.musica.player.domain.settings.SettingsRepository
import java.io.File

/** Subdirectorios de la caché de la app donde se guardan las carátulas. */
private const val EMBEDDED_CACHE_DIR = "artwork"
private const val ONLINE_CACHE_DIR = "artwork_online"

/** Identificación de la app ante el servicio de carátulas. */
private const val USER_AGENT = "Musica/0.1.0 (Android)"

/**
 * Crea el [ImageLoader] de la aplicación con el componente que obtiene la
 * carátula de un tema (embebida o, si falta, online) junto con las cachés en
 * disco propias, ya que Coil no cachea en disco las imágenes locales.
 *
 * La búsqueda online solo se realiza si [settings] la tiene activada (por
 * defecto, no).
 */
fun newArtworkImageLoader(
    context: PlatformContext,
    settings: SettingsRepository,
): ImageLoader {
    val embeddedCache = ArtworkCache(File(context.cacheDir, EMBEDDED_CACHE_DIR))
    val onlineResolver = OnlineArtworkResolver(
        source = DeezerArtworkSource(UrlConnectionHttpClient(USER_AGENT)),
        cache = OnlineArtworkCache(File(context.cacheDir, ONLINE_CACHE_DIR)),
        network = ConnectivityNetworkMonitor(context),
        settings = settings,
    )
    return ImageLoader.Builder(context)
        .components {
            add(ArtworkFetcher.Factory(embeddedCache, onlineResolver))
            add(ArtworkKeyer(settings))
        }
        .build()
}
