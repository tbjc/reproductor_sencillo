package com.musica.player.ui.components

import com.musica.player.domain.library.Album
import com.musica.player.domain.library.Artist
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryListViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Regresión: si la biblioteca ya estaba cargada al crear el ViewModel, el
     * estado debe quedar en contenido y no atascarse en carga.
     */
    @Test
    fun conBibliotecaYaCargada_muestraContenido() = runTest(mainDispatcher) {
        val repository = FakeLibraryRepository().apply {
            songs.value = listOf(song(1))
            loaded.value = true
        }
        val viewModel = TestListViewModel(repository)
        collectInBackground(viewModel)

        advanceUntilIdle()

        assertEquals(LibraryUiState.Ready(listOf(song(1))), viewModel.uiState.value)
    }

    @Test
    fun mientrasNoEsteCargada_muestraCarga() = runTest(mainDispatcher) {
        val repository = FakeLibraryRepository().apply {
            // El re-escaneo no completa: loaded se queda en false.
            refreshAction = { }
        }
        val viewModel = TestListViewModel(repository)
        collectInBackground(viewModel)

        advanceUntilIdle()

        assertEquals(LibraryUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun siElReescaneoFalla_muestraError() = runTest(mainDispatcher) {
        val repository = FakeLibraryRepository().apply {
            refreshAction = { throw IllegalStateException("fallo simulado") }
        }
        val viewModel = TestListViewModel(repository)
        collectInBackground(viewModel)

        advanceUntilIdle()

        assertEquals(LibraryUiState.Error, viewModel.uiState.value)
    }

    private fun TestScope.collectInBackground(viewModel: TestListViewModel) {
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    private fun song(id: Long) = Song(
        id = id,
        title = "Canción $id",
        artist = "Artista",
        artistId = 1,
        album = "Álbum",
        albumId = 1,
        genre = null,
        durationMs = 1000,
        size = 0,
        mimeType = null,
        trackNumber = null,
        discNumber = null,
        year = null,
        dateModified = 0,
        contentUri = "content://media/external/audio/media/$id",
    )

    private class TestListViewModel(repository: LibraryRepository) :
        LibraryListViewModel<Song>(repository = repository, items = repository.songs)

    private class FakeLibraryRepository : LibraryRepository {
        override val songs = MutableStateFlow<List<Song>>(emptyList())
        override val albums = MutableStateFlow<List<Album>>(emptyList())
        override val artists = MutableStateFlow<List<Artist>>(emptyList())
        override val loaded = MutableStateFlow(false)

        var refreshAction: () -> Unit = { loaded.value = true }

        override suspend fun refresh() {
            refreshAction()
        }
    }
}