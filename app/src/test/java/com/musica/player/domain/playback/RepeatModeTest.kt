package com.musica.player.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatModeTest {

    @Test
    fun nextCiclaDesactivadoTodoUnaYVuelveADesactivado() {
        assertEquals(RepeatMode.ALL, RepeatMode.OFF.next())
        assertEquals(RepeatMode.ONE, RepeatMode.ALL.next())
        assertEquals(RepeatMode.OFF, RepeatMode.ONE.next())
    }

    @Test
    fun nextEsUnCicloCompletoEnTresPasos() {
        var mode = RepeatMode.OFF
        repeat(3) { mode = mode.next() }
        assertEquals(RepeatMode.OFF, mode)
    }
}
