package com.musica.player.artwork

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ArtworkCacheTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun devuelveNullSiNoHayCaratulaCacheada() {
        val cache = ArtworkCache(tmp.newFolder())

        assertNull(cache.read("content://media/external/audio/media/1"))
    }

    @Test
    fun guardaYRecuperaLaCaratulaPorUri() {
        val cache = ArtworkCache(tmp.newFolder())
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

        cache.write("content://media/external/audio/media/1", bytes)

        assertArrayEquals(bytes, cache.read("content://media/external/audio/media/1"))
    }

    @Test
    fun distingueEntreArchivosDeAudioDistintos() {
        val cache = ArtworkCache(tmp.newFolder())

        cache.write("content://media/external/audio/media/1", byteArrayOf(1))

        assertNull(cache.read("content://media/external/audio/media/2"))
    }

    @Test
    fun ignoraCaratulasVacias() {
        val cache = ArtworkCache(tmp.newFolder())

        cache.write("content://media/external/audio/media/1", ByteArray(0))

        assertNull(cache.read("content://media/external/audio/media/1"))
    }
}
