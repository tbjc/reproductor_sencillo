package com.musica.player.ui.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musica.player.domain.library.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Base de las pantallas que listan un tipo de la biblioteca (canciones,
 * álbumes, artistas).
 *
 * El estado se **deriva** siempre de los flujos del repositorio mediante
 * `combine`, en lugar de escribirse a mano. Así se evita una carrera: si se
 * fijara `Loading` al iniciar un re-escaneo y el repositorio devolviera los
 * mismos valores, el `StateFlow` no re-emitiría y la pantalla quedaría
 * atascada en carga.
 *
 * Mientras [LibraryRepository.loaded] sea `false` se muestra carga; si un
 * re-escaneo lanza excepción se muestra error con reintento.
 */
abstract class LibraryListViewModel<T>(
    private val repository: LibraryRepository,
    items: StateFlow<List<T>>,
) : ViewModel() {

    private val hasError = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState<T>> = combine(
        items,
        repository.loaded,
        hasError,
    ) { items, loaded, error ->
        when {
            error -> LibraryUiState.Error
            loaded -> LibraryUiState.Ready(items)
            else -> LibraryUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState.Loading,
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
        const val TAG = "LibraryListViewModel"
    }
}