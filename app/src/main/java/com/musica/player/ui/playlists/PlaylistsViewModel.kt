package com.musica.player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musica.player.domain.playlist.Playlist
import com.musica.player.domain.playlist.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistsUiState(
    val loaded: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
)

/**
 * Pantalla de playlists. El estado se **deriva** del flujo de Room, de modo que
 * cualquier creación, renombrado o borrado se refleja sin refrescar a mano.
 */
class PlaylistsViewModel(
    private val repository: PlaylistRepository,
) : ViewModel() {

    val uiState: StateFlow<PlaylistsUiState> = repository.observePlaylists()
        .map { playlists -> PlaylistsUiState(loaded = true, playlists = playlists) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaylistsUiState(),
        )

    fun create(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.create(trimmed) }
    }
}
