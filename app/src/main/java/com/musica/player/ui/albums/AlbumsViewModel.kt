package com.musica.player.ui.albums

import com.musica.player.domain.library.Album
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.ui.components.LibraryListViewModel

/**
 * Álbumes de la biblioteca.
 */
class AlbumsViewModel(repository: LibraryRepository) :
    LibraryListViewModel<Album>(repository = repository, items = repository.albums)