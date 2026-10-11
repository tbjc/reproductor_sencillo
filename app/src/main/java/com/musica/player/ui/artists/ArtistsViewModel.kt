package com.musica.player.ui.artists

import com.musica.player.domain.library.Artist
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.ui.components.LibraryListViewModel

/**
 * Artistas de la biblioteca.
 */
class ArtistsViewModel(repository: LibraryRepository) :
    LibraryListViewModel<Artist>(repository = repository, items = repository.artists)