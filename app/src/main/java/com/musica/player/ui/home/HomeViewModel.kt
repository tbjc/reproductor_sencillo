package com.musica.player.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musica.player.domain.library.Album
import com.musica.player.domain.library.Artist
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val loaded: Boolean = false,
    val hasError: Boolean = false,
) {
    val recentSongs: List<Song>
        get() = songs.sortedByDescending { it.dateModified }.take(MAX_RECENT)

    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty()

    private companion object {
        const val MAX_RECENT = 10
    }
}

/**
 * Estado de la pantalla de inicio: estadísticas de la biblioteca
 * (canciones/álbumes/artistas) y canciones más recientes.
 *
 * Dispara el primer escaneo al crearse (la carga del repositorio es perezosa),
 * de forma que se ejecuta una vez concedido el permiso de audio.
 */
class HomeViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val hasError = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.songs,
        repository.albums,
        repository.artists,
        repository.loaded,
        hasError,
    ) { songs, albums, artists, loaded, error ->
        HomeUiState(
            songs = songs,
            albums = albums,
            artists = artists,
            loaded = loaded,
            hasError = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        refresh()
    }

    /** Re-escanea la biblioteca; también se usa como acción de reintento. */
    fun refresh() {
        viewModelScope.launch {
            hasError.value = false
            runCatching { repository.refresh() }
                .onFailure {
                    Log.e(TAG, "Error al actualizar la biblioteca", it)
                    hasError.value = true
                }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
