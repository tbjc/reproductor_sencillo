package com.musica.player.playback

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.musica.player.domain.library.Song
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica que el mapeo canción → elemento reproducible → canción conserva los
 * datos que necesita la reproducción y la notificación.
 */
@RunWith(AndroidJUnit4::class)
class MediaItemsTest {

    @Test
    fun idaYVueltaConservaLosDatosDeReproduccion() {
        val song = Song(
            id = 42L,
            title = "Una canción",
            artist = "Un artista",
            artistId = 7L,
            album = "Un álbum",
            albumId = 9L,
            genre = "Rock",
            durationMs = 123_456L,
            size = 999L,
            mimeType = "audio/mpeg",
            trackNumber = 3,
            discNumber = 1,
            year = 2020,
            dateModified = 123L,
            contentUri = "content://media/external/audio/media/42",
        )

        val roundTrip = song.toMediaItem().toSong()

        assertEquals(42L, roundTrip.id)
        assertEquals("Una canción", roundTrip.title)
        assertEquals("Un artista", roundTrip.artist)
        assertEquals(7L, roundTrip.artistId)
        assertEquals("Un álbum", roundTrip.album)
        assertEquals(9L, roundTrip.albumId)
        assertEquals(123_456L, roundTrip.durationMs)
        assertEquals("content://media/external/audio/media/42", roundTrip.contentUri)
    }
}
