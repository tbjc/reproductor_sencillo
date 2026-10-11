package com.musica.player.ui.songs

import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import com.musica.player.ui.components.LibraryListViewModel

/**
 * Canciones de la biblioteca.
 */
class SongsViewModel(repository: LibraryRepository) :
    LibraryListViewModel<Song>(repository = repository, items = repository.songs)