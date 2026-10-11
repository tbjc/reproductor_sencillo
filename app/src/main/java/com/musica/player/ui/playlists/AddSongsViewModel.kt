package com.musica.player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import com.musica.player.domain.playlist.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddSongsUiState(
    val loaded: Boolean = false,
    val songs: List<Song> = emptyList(),
    val inPlaylist: Set<Long> = emptySet(),
    val selected: Set<Long> = emptySet(),
) {
    val canAdd: Boolean
        get() = selected.isNotEmpty()
}

/**
 * Selector de canciones para añadir a una playlist. Marca las que ya están en
 * la lista (no se pueden volver a añadir) y mantiene la selección del usuario
 * mientras navega por la biblioteca.
 */
class AddSongsViewModel(
    private val playlistRepository: PlaylistRepository,
    libraryRepository: LibraryRepository,
    private val playlistId: Long,
) : ViewModel() {

    private val selected = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<AddSongsUiState> = combine(
        libraryRepository.songs,
        playlistRepository.observeSongIds(playlistId),
        selected,
    ) { songs, songIds, selection ->
        val inPlaylist = songIds.toSet()
        AddSongsUiState(
            loaded = true,
            songs = songs,
            inPlaylist = inPlaylist,
            // Si una canción pasa a estar en la playlist, deja de seleccionarse.
            selected = selection - inPlaylist,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddSongsUiState(),
    )

    init {
        if (!libraryRepository.loaded.value) {
            viewModelScope.launch { runCatching { libraryRepository.refresh() } }
        }
    }

    fun toggle(songId: Long) {
        if (songId in uiState.value.inPlaylist) return
        selected.value = selected.value.let { current ->
            if (songId in current) current - songId else current + songId
        }
    }

    /** Añade las canciones marcadas y avisa cuando termina mediante [onDone]. */
    fun addSelected(onDone: () -> Unit) {
        val toAdd = selected.value.toList()
        viewModelScope.launch {
            if (toAdd.isNotEmpty()) {
                runCatching { playlistRepository.addSongs(playlistId, toAdd) }
            }
            selected.value = emptySet()
            onDone()
        }
    }
}
