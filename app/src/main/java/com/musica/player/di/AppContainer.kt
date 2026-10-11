package com.musica.player.di

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.room.Room
import com.musica.player.data.medialibrary.LibraryRepositoryImpl
import com.musica.player.data.medialibrary.MediaStoreDataSource
import com.musica.player.data.playlist.MusicaDatabase
import com.musica.player.data.playlist.PlaylistRepositoryImpl
import com.musica.player.data.settings.SettingsRepositoryImpl
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.playlist.PlaylistRepository
import com.musica.player.domain.settings.SettingsRepository
import com.musica.player.playback.Media3PlayerController
import com.musica.player.playback.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Contenedor de dependencias manual (sin framework de DI).
 *
 * Se crea una única vez en `MusicaApp` y vive mientras dure el proceso. El
 * repositorio de biblioteca es un singleton en memoria con su propio scope, por
 * lo que mantiene la caché y el observer de MediaStore durante toda la sesión.
 *
 * El reproductor vive en [PlaybackService] (para sonar en segundo plano); aquí
 * se crea el `MediaController` que conecta la app con la sesión de ese servicio
 * y se lo entrega a [Media3PlayerController].
 */
@androidx.annotation.OptIn(UnstableApi::class)
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val libraryRepository: LibraryRepository = LibraryRepositoryImpl(
        dataSource = MediaStoreDataSource(appContext),
        scope = scope,
    )

    private val database: MusicaDatabase = Room.databaseBuilder(
        appContext,
        MusicaDatabase::class.java,
        DATABASE_NAME,
    ).build()

    val playlistRepository: PlaylistRepository = PlaylistRepositoryImpl(database.playlistDao())

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(appContext)

    val playerController = Media3PlayerController(scope)

    init {
        val token = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java),
        )
        val controllerFuture = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }
                    .onSuccess { playerController.connect(it) }
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    private companion object {
        const val DATABASE_NAME = "musica.db"
    }
}
