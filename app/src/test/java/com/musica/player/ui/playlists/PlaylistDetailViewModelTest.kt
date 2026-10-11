package com.musica.player.ui.playlists

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun muestraElNombreYLasCancionesEnElOrdenDeLaPlaylist() = runTest(mainDispatcher) {
        val playlists = FakePlaylistRepository().apply {
            names.value = mapOf(PLAYLIST_ID to "Favoritas")
            songsByPlaylist.value = mapOf(PLAYLIST_ID to listOf(3L, 1L))
        }
        val library = FakeLibraryRepository().apply {
            songs.value = listOf(testSong(1), testSong(2), testSong(3))
            loaded.value = true
        }
        val viewModel = PlaylistDetailViewModel(playlists, library, PLAYLIST_ID)
        collectInBackground(viewModel)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.loaded)
        assertEquals("Favoritas", state.name)
        assertEquals(listOf(3L, 1L), state.songs.map { it.id })
        assertFalse(state.deleted)
    }

    @Test
    fun cuandoLaPlaylistNoExiste_marcaDeleted() = runTest(mainDispatcher) {
        val playlists = FakePlaylistRepository()
        val library = FakeLibraryRepository().apply { loaded.value = true }
        val viewModel = PlaylistDetailViewModel(playlists, library, PLAYLIST_ID)
        collectInBackground(viewModel)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.deleted)
    }

    @Test
    fun removeSongSeDelegaEnElRepositorio() = runTest(mainDispatcher) {
        val playlists = FakePlaylistRepository().apply {
            names.value = mapOf(PLAYLIST_ID to "Favoritas")
            songsByPlaylist.value = mapOf(PLAYLIST_ID to listOf(1L))
        }
        val library = FakeLibraryRepository().apply { loaded.value = true }
        val viewModel = PlaylistDetailViewModel(playlists, library, PLAYLIST_ID)
        collectInBackground(viewModel)
        advanceUntilIdle()

        viewModel.removeSong(1L)
        advanceUntilIdle()

        assertEquals(listOf(PLAYLIST_ID to 1L), playlists.removeCalls)
    }

    private fun TestScope.collectInBackground(viewModel: PlaylistDetailViewModel) {
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    private companion object {
        const val PLAYLIST_ID = 7L
    }
}
