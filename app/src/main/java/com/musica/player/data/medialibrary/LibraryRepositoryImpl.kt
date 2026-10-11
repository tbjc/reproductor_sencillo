package com.musica.player.data.medialibrary

import android.util.Log
import com.musica.player.domain.library.Album
import com.musica.player.domain.library.Artist
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Implementación de [LibraryRepository] que guarda el estado de la biblioteca
 * en memoria y lo mantiene al día re-escaneando MediaStore cuando cambia el
 * contenido (observer de MediaStore) o cuando se invoca [refresh].
 *
 * La carga es perezosa: no se escanea al construir el repositorio, sino cuando
 * la interfaz lo solicita. Así se evita leer sin permiso (lo que devolvería una
 * biblioteca vacía) antes de que el usuario lo conceda.
 */
class LibraryRepositoryImpl(
    private val dataSource: MediaStoreDataSource,
    scope: CoroutineScope,
) : LibraryRepository {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    override val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    override val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    override val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    override val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        // Actualización automática cuando cambia el contenido de MediaStore.
        scope.launch {
            dataSource.mediaChanges().collect {
                runCatching { refresh() }
                    .onFailure { logError("actualización automática", it) }
            }
        }
    }

    override suspend fun refresh() {
        val loadedSongs = dataSource.querySongs()
        _songs.value = loadedSongs
        _albums.value = buildAlbums(loadedSongs)
        _artists.value = buildArtists(loadedSongs)
        _loaded.value = true
    }

    private fun logError(origin: String, error: Throwable) {
        Log.e(TAG, "Error en $origin de la biblioteca", error)
    }

    private companion object {
        const val TAG = "LibraryRepository"
    }
}