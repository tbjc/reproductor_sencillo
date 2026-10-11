package com.musica.player.artwork.online

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OnlineArtworkCacheTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun cache() = OnlineArtworkCache(tmp.root)

    @Test
    fun devuelveNullCuandoNoSeHaConsultado() {
        assertNull(cache().read("Discovery", "Daft Punk"))
    }

    @Test
    fun guardaYRecuperaLaCaratula() {
        val bytes = byteArrayOf(1, 2, 3)
        val cache = cache()
        cache.writeFound("Discovery", "Daft Punk", bytes)

        val entry = cache.read("Discovery", "Daft Punk")

        assertTrue(entry is OnlineArtworkCache.Entry.Found)
        assertArrayEquals(bytes, (entry as OnlineArtworkCache.Entry.Found).bytes)
    }

    @Test
    fun recuerdaLosAlbumesSinCaratula() {
        val cache = cache()
        cache.writeMissing("Discovery", "Daft Punk")

        assertEquals(OnlineArtworkCache.Entry.Missing, cache.read("Discovery", "Daft Punk"))
    }

    @Test
    fun laCaratulaSustituyeAlMarcadorDeAusencia() {
        val cache = cache()
        cache.writeMissing("Discovery", "Daft Punk")
        cache.writeFound("Discovery", "Daft Punk", byteArrayOf(9))

        assertTrue(cache.read("Discovery", "Daft Punk") is OnlineArtworkCache.Entry.Found)
    }

    @Test
    fun distingueEntreAlbumesDistintos() {
        val cache = cache()
        cache.writeFound("Discovery", "Daft Punk", byteArrayOf(1))

        assertEquals(null, cache.read("Homework", "Daft Punk"))
    }

    @Test
    fun ignoraImagenesVacias() {
        val cache = cache()
        cache.writeFound("Discovery", "Daft Punk", ByteArray(0))

        assertEquals(null, cache.read("Discovery", "Daft Punk"))
    }
}
