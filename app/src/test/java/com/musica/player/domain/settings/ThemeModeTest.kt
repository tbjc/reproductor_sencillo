package com.musica.player.domain.settings

import org.junit.Assert.assertEquals
import org.junit.Test

/** Comprueba el mapeo entre el modo de tema y el valor que se guarda. */
class ThemeModeTest {

    @Test
    fun usaElTemaDelSistemaPorDefecto() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.default)
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(null))
    }

    @Test
    fun leeLosValoresGuardados() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromStorage("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("dark"))
    }

    @Test
    fun unValorDesconocidoVuelveAlSistema() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("purple"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(""))
    }

    @Test
    fun cadaModoSeGuardaYSeRecuperaSinPerderInformacion() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStorage(mode.storageKey))
        }
    }
}