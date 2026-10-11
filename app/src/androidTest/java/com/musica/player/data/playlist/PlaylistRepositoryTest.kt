package com.musica.player.data.playlist

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.musica.player.domain.playlist.PlaylistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de la persistencia de playlists sobre una base de datos Room en
 * memoria. Verifican el CRUD completo, el orden y la eliminación en cascada.
 */
@RunWith(AndroidJUnit4::class)
class PlaylistRepositoryTest {

    private lateinit var database: MusicaDatabase
    private lateinit var repository: PlaylistRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MusicaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PlaylistRepositoryImpl(database.playlistDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun crearYListarOrdenaPorNombreSinDistinguirMayusculas() = runTest {
        repository.create("zeta")
        repository.create("Alfa")
        repository.create("beta")

        val names = repository.observePlaylists().first().map { it.name }

        assertEquals(listOf("Alfa", "beta", "zeta"), names)
    }

    @Test
    fun crearGuardaElNombreRecortado() = runTest {
        val id = repository.create("  Favoritas  ")

        assertEquals("Favoritas", repository.observeName(id).first())
    }

    @Test
    fun anadirCancionesRespetaElOrdenYEvitaDuplicados() = runTest {
        val id = repository.create("Lista")

        repository.addSongs(id, listOf(10L, 20L))
        repository.addSongs(id, listOf(30L, 10L))

        assertEquals(listOf(10L, 20L, 30L), repository.observeSongIds(id).first())
        val playlist = repository.observePlaylists().first().single { it.id == id }
        assertEquals(3, playlist.songCount)
    }

    @Test
    fun quitarCancionLaEliminaDeLaLista() = runTest {
        val id = repository.create("Lista")
        repository.addSongs(id, listOf(1L, 2L))

        repository.removeSong(id, 1L)

        assertEquals(listOf(2L), repository.observeSongIds(id).first())
    }

    @Test
    fun renombrarCambiaElNombre() = runTest {
        val id = repository.create("Antes")

        repository.rename(id, "Después")

        assertEquals("Después", repository.observeName(id).first())
    }

    @Test
    fun borrarEliminaLaPlaylistYSusCancionesEnCascada() = runTest {
        val id = repository.create("Lista")
        repository.addSongs(id, listOf(1L, 2L))

        repository.delete(id)

        assertNull(repository.observeName(id).first())
        assertEquals(emptyList<Long>(), repository.observeSongIds(id).first())
        assertTrue(repository.observePlaylists().first().isEmpty())
    }
}
