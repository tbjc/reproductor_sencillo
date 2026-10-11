package com.musica.player.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatsTest {

    @Test
    fun formatDuration_cero() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:00", formatDuration(-500))
    }

    @Test
    fun formatDuration_menosDeUnMinuto() {
        assertEquals("0:59", formatDuration(59_000))
    }

    @Test
    fun formatDuration_unMinuto() {
        assertEquals("1:00", formatDuration(60_000))
        assertEquals("12:34", formatDuration(754_000))
    }

    @Test
    fun formatDuration_conHoras() {
        assertEquals("1:01:01", formatDuration(3_661_000))
    }

    @Test
    fun displayName_reemplazaValoresDesconocidos() {
        assertEquals("Desconocido", displayName("<unknown>", "Desconocido"))
        assertEquals("Desconocido", displayName("", "Desconocido"))
        assertEquals("Desconocido", displayName("   ", "Desconocido"))
        assertEquals("AC/DC", displayName("AC/DC", "Desconocido"))
    }
}