package com.musica.player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import com.musica.player.domain.playlist.PlaylistRepository
import com.musica.player.domain.playlist.resolvePlaylistSongs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistDetailUiState(
    val loaded: Boolean = false,
    val name: String? = null,
    val songs: List<Song> = emptyList(),
) {
    /** La playlist ya no existe (por ejemplo, se acaba de eliminar). */
    val deleted: Boolean
        get() = loaded && name == null
}

/**
 * Detalle de una playlist: su nombre y sus canciones, resueltas contra la
 * biblioteca de MediaStore. Escucha Room y la biblioteca a la vez para
 * mantenerse al día sin recargar manualmente.
 */
class PlaylistDetailViewModel(
    private val playlistRepository: PlaylistRepository,
    private val libraryRepository: LibraryRepository,
    private val playlistId: Long,
) : ViewModel() {

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        playlistRepository.observeName(playlistId),
        playlistRepository.observeSongIds(playlistId),
        libraryRepository.songs,
    ) { name, songIds, songs ->
        PlaylistDetailUiState(
            loaded = true,
            name = name,
            songs = resolvePlaylistSongs(songs, songIds),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlaylistDetailUiState(),
    )

    init {
        // La biblioteca se carga de forma perezosa; si aún no lo está (por
        // ejemplo, navegación directa al detalle), se pide un escaneo.
        if (!libraryRepository.loaded.value) {
            viewModelScope.launch { runCatching { libraryRepository.refresh() } }
        }
    }

    fun rename(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { playlistRepository.rename(playlistId, trimmed) }
    }

    fun delete() {
        viewModelScope.launch { playlistRepository.delete(playlistId) }
    }

    fun removeSong(songId: Long) {
        viewModelScope.launch { playlistRepository.removeSong(playlistId, songId) }
    }
}
