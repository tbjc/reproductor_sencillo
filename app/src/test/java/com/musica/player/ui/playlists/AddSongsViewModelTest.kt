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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AddSongsViewModelTest {

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
    fun lasCancionesYaAnadidasNoSePuedenSeleccionar() = runTest(mainDispatcher) {
        val playlists = FakePlaylistRepository().apply {
            songsByPlaylist.value = mapOf(PLAYLIST_ID to listOf(2L))
        }
        val viewModel = newViewModel(playlists)
        collectInBackground(viewModel)
        advanceUntilIdle()

        viewModel.toggle(2L) // ya está en la playlist: se ignora
        viewModel.toggle(1L)
        advanceUntilIdle()

        assertEquals(setOf(1L), viewModel.uiState.value.selected)
        assertTrue(viewModel.uiState.value.canAdd)
    }

    @Test
    fun addSelectedAnadeLasMarcadasYVaciaLaSeleccion() = runTest(mainDispatcher) {
        val playlists = FakePlaylistRepository()
        val viewModel = newViewModel(playlists)
        collectInBackground(viewModel)
        advanceUntilIdle()

        viewModel.toggle(3L)
        advanceUntilIdle()

        var done = false
        viewModel.addSelected(onDone = { done = true })
        advanceUntilIdle()

        assertEquals(listOf(PLAYLIST_ID to listOf(3L)), playlists.addCalls)
        assertTrue(done)
        assertEquals(emptySet<Long>(), viewModel.uiState.value.selected)
        assertFalse(viewModel.uiState.value.canAdd)
    }

    private fun TestScope.newViewModel(playlists: FakePlaylistRepository): AddSongsViewModel {
        val library = FakeLibraryRepository().apply {
            songs.value = listOf(testSong(1), testSong(2), testSong(3))
            loaded.value = true
        }
        return AddSongsViewModel(playlists, library, PLAYLIST_ID)
    }

    private fun TestScope.collectInBackground(viewModel: AddSongsViewModel) {
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    private companion object {
        const val PLAYLIST_ID = 7L
    }
}
