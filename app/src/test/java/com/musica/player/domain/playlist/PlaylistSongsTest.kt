package com.musica.player.domain.playlist

import com.musica.player.domain.library.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistSongsTest {

    private fun song(id: Long): Song = Song(
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

    @Test
    fun resuelveLasCancionesEnElOrdenDeLaPlaylist() {
        val allSongs = listOf(song(1), song(2), song(3))

        val result = resolvePlaylistSongs(allSongs, listOf(3, 1, 2))

        assertEquals(listOf(3L, 1L, 2L), result.map { it.id })
    }

    @Test
    fun descartaCancionesQueYaNoExistenEnLaBiblioteca() {
        val allSongs = listOf(song(1), song(2))

        val result = resolvePlaylistSongs(allSongs, listOf(2, 99, 1))

        assertEquals(listOf(2L, 1L), result.map { it.id })
    }

    @Test
    fun conListaVaciaDevuelveVacio() {
        assertEquals(emptyList<Song>(), resolvePlaylistSongs(listOf(song(1)), emptyList()))
    }
}
