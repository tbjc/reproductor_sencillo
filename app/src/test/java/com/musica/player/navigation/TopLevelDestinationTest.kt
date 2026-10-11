package com.musica.player.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verificaciones básicas del grafo de navegación de nivel superior.
 */
class TopLevelDestinationTest {

    @Test
    fun routes_son_unicas() {
        val routes = TopLevelDestination.entries.map { it.route }
        assertEquals("Las rutas de navegación deben ser únicas", routes.size, routes.distinct().size)
    }

    @Test
    fun contiene_rutas_principales() {
        val routes = TopLevelDestination.entries.map { it.route }.toSet()
        assertTrue("Debe existir la pantalla de inicio", "home" in routes)
        assertTrue("Debe existir la pantalla de canciones", "songs" in routes)
        assertTrue("Debe existir la pantalla de playlists", "playlists" in routes)
    }
}