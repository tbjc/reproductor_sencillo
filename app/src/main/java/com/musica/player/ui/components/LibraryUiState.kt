package com.musica.player.ui.components

/**
 * Estado de una pantalla de biblioteca: cargando, lista lista o error.
 * El mensaje de error lo renderiza la UI con textos traducidos.
 */
sealed interface LibraryUiState<out T> {
    data object Loading : LibraryUiState<Nothing>
    data class Ready<T>(val items: List<T>) : LibraryUiState<T>
    data object Error : LibraryUiState<Nothing>
}