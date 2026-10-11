package com.musica.player.data.medialibrary

import com.musica.player.domain.library.Artwork
import com.musica.player.domain.library.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryAggregatorTest {

    private fun song(
        id: Long,
        title: String,
        artist: String,
        artistId: Long,
        album: String,
        albumId: Long,
        year: Int? = null,
    ): Song = Song(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        album = album,
        albumId = albumId,
        genre = null,
        durationMs = 0,
        size = 0,
        mimeType = null,
        trackNumber = null,
        discNumber = null,
        year = year,
        dateModified = 0,
        contentUri = "content://media/external/audio/media/$id",
    )

    @Test
    fun buildAlbums_agrupaPorAlbumYOrdenaPorNombre() {
        val songs = listOf(
            song(1, "C", "Artista A", 10, "zeta", 300),
            song(2, "A", "Artista B", 20, "Alfa", 200),
            song(3, "B", "Artista A", 10, "zeta", 300),
        )

        val albums = buildAlbums(songs)

        assertEquals(listOf("Alfa", "zeta"), albums.map { it.name })
        assertEquals(2, albums[1].songCount)
    }

    @Test
    fun buildAlbums_conteosYPrimerAnioDisponible() {
        val songs = listOf(
            song(1, "A", "Artista", 10, "Álbum", 100, year = 2001),
            song(2, "B", "Artista", 10, "Álbum", 100, year = null),
            song(3, "C", "Artista", 10, "Otro", 200, year = 2010),
        )

        val albums = buildAlbums(songs)

        val primero = albums.first { it.id == 100L }
        assertEquals(2001, primero.year)
        assertEquals(2, primero.songCount)
        val segundo = albums.first { it.id == 200L }
        assertEquals(2010, segundo.year)
        assertEquals(1, segundo.songCount)
    }

    @Test
    fun buildAlbums_caraturaSaleDelPrimerTemaDelAlbum() {
        val songs = listOf(
            song(1, "A", "Artista", 10, "Álbum", 100),
            song(2, "B", "Artista", 10, "Álbum", 100),
        )

        val albums = buildAlbums(songs)

        assertEquals(
            Artwork("content://media/external/audio/media/1", "Álbum", "Artista"),
            albums.first { it.id == 100L }.artwork,
        )
    }

    @Test
    fun buildArtists_agrupaPorArtistaYCuentaAlbumesDistintos() {
        val songs = listOf(
            song(1, "A", "Artista X", 10, "Álbum 1", 100),
            song(2, "B", "Artista X", 10, "Álbum 1", 100),
            song(3, "C", "Artista X", 10, "Álbum 2", 200),
            song(4, "D", "Artista Y", 20, "Álbum 3", 300),
        )

        val artists = buildArtists(songs)

        val artistaX = artists.first { it.id == 10L }
        assertEquals("Artista X", artistaX.name)
        assertEquals(3, artistaX.songCount)
        assertEquals(2, artistaX.albumCount)
        assertEquals(1, artists.first { it.id == 20L }.albumCount)
    }

    @Test
    fun buildArtists_artistaDesconocidoSeAgrupaYSigueElOrden() {
        val songs = listOf(
            song(1, "A", "zeta", 10, "Álbum", 100),
            song(2, "B", "Alfa", 20, "Álbum", 100),
            song(3, "C", "<unknown>", -1, "Álbum", 100),
        )

        val artists = buildArtists(songs)

        // Orden insensible a mayúsculas: "<" precede a las letras.
        assertEquals(listOf("<unknown>", "Alfa", "zeta"), artists.map { it.name })
        assertEquals(1, artists.first { it.id == -1L }.songCount)
    }
}