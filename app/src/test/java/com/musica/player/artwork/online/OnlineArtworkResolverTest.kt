package com.musica.player.artwork.online

import com.musica.player.domain.settings.SettingsRepository
import com.musica.player.domain.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OnlineArtworkResolverTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun cacheaLaCaratulaYNoRepiteLaBusqueda() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val source = FakeSource(OnlineArtwork.Found(bytes))
        val resolver = resolver(source)

        assertArrayEquals(bytes, resolver.resolve("Discovery", "Daft Punk"))
        assertArrayEquals(bytes, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(1, source.calls)
    }

    @Test
    fun recuerdaLosAlbumesSinCaratula() = runTest {
        val source = FakeSource(OnlineArtwork.NotFound)
        val resolver = resolver(source)

        assertEquals(null, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(null, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(1, source.calls)
    }

    @Test
    fun noConsultaLaRedSiNoHayConexion() = runTest {
        val source = FakeSource(OnlineArtwork.Found(byteArrayOf(1)))
        val network = FakeNetwork(online = false)
        val resolver = resolver(source, network)

        assertEquals(null, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(0, source.calls)

        network.online = true
        assertArrayEquals(byteArrayOf(1), resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(1, source.calls)
    }

    @Test
    fun noCacheaUnErrorDeRed() = runTest {
        val source = FakeSource(OnlineArtwork.Error)
        val resolver = resolver(source)

        assertEquals(null, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(null, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(2, source.calls)
    }

    @Test
    fun descartaEtiquetasDesconocidas() = runTest {
        val source = FakeSource(OnlineArtwork.Found(byteArrayOf(1)))
        val resolver = resolver(source, FakeNetwork(online = true))

        assertEquals(null, resolver.resolve("<unknown>", "<unknown>"))
        assertEquals(null, resolver.resolve("  ", "Daft Punk"))
        assertEquals(null, resolver.resolve("Discovery", "<unknown>"))
        assertEquals(null, resolver.resolve("Discovery", ""))
        assertEquals(0, source.calls)
    }

    @Test
    fun noBuscaSiLaOpcionEstaDesactivada() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val source = FakeSource(OnlineArtwork.Found(bytes))
        val settings = FakeSettings(online = false)
        val resolver = resolver(source, settings = settings)

        assertEquals(null, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(0, source.calls)

        settings.setOnlineArtworkEnabled(true)

        assertArrayEquals(bytes, resolver.resolve("Discovery", "Daft Punk"))
        assertEquals(1, source.calls)
    }

    private fun resolver(
        source: OnlineArtworkSource,
        network: NetworkMonitor = FakeNetwork(online = true),
        settings: SettingsRepository = FakeSettings(online = true),
    ) = OnlineArtworkResolver(source, OnlineArtworkCache(tmp.root), network, settings)

    private class FakeSource(private val result: OnlineArtwork) : OnlineArtworkSource {
        var calls = 0

        override suspend fun findAlbumArtwork(album: String, artist: String): OnlineArtwork {
            calls++
            return result
        }
    }

    private class FakeNetwork(var online: Boolean) : NetworkMonitor {
        override fun isOnline(): Boolean = online
    }

    private class FakeSettings(online: Boolean = true) : SettingsRepository {
        private val state = MutableStateFlow(online)

        override val onlineArtworkEnabled: StateFlow<Boolean> = state.asStateFlow()
        override val themeMode: StateFlow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM).asStateFlow()
        override val dynamicColorEnabled: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

        override suspend fun setOnlineArtworkEnabled(enabled: Boolean) {
            state.value = enabled
        }

        override suspend fun setThemeMode(mode: ThemeMode) = Unit

        override suspend fun setDynamicColorEnabled(enabled: Boolean) = Unit
    }
}
