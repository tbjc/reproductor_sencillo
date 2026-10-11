package com.musica.player.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.musica.player.MusicaApp
import com.musica.player.ui.albums.AlbumsViewModel
import com.musica.player.ui.artists.ArtistsViewModel
import com.musica.player.ui.home.HomeViewModel
import com.musica.player.ui.player.PlayerViewModel
import com.musica.player.ui.playlists.AddSongsViewModel
import com.musica.player.ui.playlists.PlaylistDetailViewModel
import com.musica.player.ui.playlists.PlaylistsViewModel
import com.musica.player.ui.settings.SettingsViewModel
import com.musica.player.ui.songs.SongsViewModel

/**
 * Fábrica única de ViewModels. Cada `initializer` resuelve el repositorio del
 * [AppContainer] de la aplicación, evitando repetir el acceso al contenedor en
 * cada pantalla.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            val container = musicaAppContainer()
            HomeViewModel(container.libraryRepository)
        }
        initializer {
            val container = musicaAppContainer()
            SongsViewModel(container.libraryRepository)
        }
        initializer {
            val container = musicaAppContainer()
            AlbumsViewModel(container.libraryRepository)
        }
        initializer {
            val container = musicaAppContainer()
            ArtistsViewModel(container.libraryRepository)
        }
        initializer {
            val container = musicaAppContainer()
            PlaylistsViewModel(container.playlistRepository)
        }
        initializer {
            val container = musicaAppContainer()
            PlayerViewModel(container.playerController)
        }
        initializer {
            val container = musicaAppContainer()
            SettingsViewModel(container.settingsRepository)
        }
    }

    /** ViewModel del detalle de una playlist, que depende de su identificador. */
    fun playlistDetailFactory(playlistId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val container = musicaAppContainer()
            PlaylistDetailViewModel(
                playlistRepository = container.playlistRepository,
                libraryRepository = container.libraryRepository,
                playlistId = playlistId,
            )
        }
    }

    /** ViewModel del selector de canciones de una playlist. */
    fun addSongsFactory(playlistId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val container = musicaAppContainer()
            AddSongsViewModel(
                playlistRepository = container.playlistRepository,
                libraryRepository = container.libraryRepository,
                playlistId = playlistId,
            )
        }
    }

    private fun CreationExtras.musicaAppContainer(): AppContainer =
        (this[APPLICATION_KEY] as MusicaApp).container
}
