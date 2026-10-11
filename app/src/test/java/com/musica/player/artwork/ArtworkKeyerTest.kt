package com.musica.player.artwork

import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkKeyerTest {

    @Test
    fun artworkKey_usaLaUriDelAudioComoClaveEstable() {
        val uri = "content://media/external/audio/media/7"

        assertEquals("artwork:$uri", artworkKey(uri))
    }

    @Test
    fun artworkKey_esIgualParaLaMismaUri() {
        val uri = "content://media/external/audio/media/7"

        assertEquals(artworkKey(uri), artworkKey(uri))
    }
}
