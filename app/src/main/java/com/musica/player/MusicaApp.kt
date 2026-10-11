package com.musica.player

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.musica.player.artwork.newArtworkImageLoader
import com.musica.player.di.AppContainer

/**
 * Punto de entrada de la aplicación. Aloja el contenedor de dependencias
 * ([AppContainer]) con los repositorios y fuentes de datos de la app.
 *
 * Implementa [SingletonImageLoader.Factory] para que Coil use el cargador de
 * carátulas locales (artwork embebido) al margen de dónde se invoque la carga.
 */
class MusicaApp : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        newArtworkImageLoader(context, container.settingsRepository)
}
