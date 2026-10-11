package com.musica.player.artwork.online

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeezerArtworkSourceTest {

    @Test
    fun descargaLaCaratulaDelAlbumQueMasCoincide() = runTest {
        val image = byteArrayOf(1, 2, 3, 4)
        val client = fakeClient(image)

        val result = DeezerArtworkSource(client).findAlbumArtwork("Discovery", "Daft Punk")

        val found = result as OnlineArtwork.Found
        assertArrayEquals(image, found.bytes)
        assertTrue(client.requested[0].startsWith("https://api.deezer.com/search/album?q="))
        assertEquals("https://img/discovery.jpg", client.requested[1])
    }

    @Test
    fun sinResultadosDevuelveNotFound() = runTest {
        val client = FakeHttpClient { HttpResponse(200, """{"data":[],"total":0}""".toByteArray()) }

        val result = DeezerArtworkSource(client).findAlbumArtwork("Nada", "Nadie")

        assertEquals(OnlineArtwork.NotFound, result)
    }

    @Test
    fun ignoraCoincidenciasDeOtrosAlbumes() = runTest {
        val body = """{"data":[{"title":"Otra Cosa","artist":{"name":"Otro"},"cover_xl":"https://img/otra.jpg"}]}"""
        val client = FakeHttpClient { HttpResponse(200, body.toByteArray()) }

        val result = DeezerArtworkSource(client).findAlbumArtwork("Discovery", "Daft Punk")

        assertEquals(OnlineArtwork.NotFound, result)
        assertEquals(1, client.requested.size)
    }

    @Test
    fun unErrorDelServicioDevuelveError() = runTest {
        val client = FakeHttpClient { HttpResponse(500, ByteArray(0)) }

        val result = DeezerArtworkSource(client).findAlbumArtwork("Discovery", "Daft Punk")

        assertEquals(OnlineArtwork.Error, result)
    }

    @Test
    fun unFalloAlDescargarLaImagenDevuelveError() = runTest {
        val client = FakeHttpClient { url ->
            if (url.startsWith("https://api.deezer.com/search/album")) {
                HttpResponse(200, singleResultJson().toByteArray())
            } else {
                HttpResponse(404, ByteArray(0))
            }
        }

        val result = DeezerArtworkSource(client).findAlbumArtwork("Discovery", "Daft Punk")

        assertEquals(OnlineArtwork.Error, result)
    }

    @Test
    fun elAlbumVacioNoConsultaLaRed() = runTest {
        val client = FakeHttpClient { throw AssertionError("no debería salir a la red") }

        val result = DeezerArtworkSource(client).findAlbumArtwork("", "Daft Punk")

        assertEquals(OnlineArtwork.NotFound, result)
        assertTrue(client.requested.isEmpty())
    }

    private fun fakeClient(image: ByteArray): FakeHttpClient = FakeHttpClient { url ->
        when {
            url.startsWith("https://api.deezer.com/search/album") ->
                HttpResponse(200, twoResultsJson().toByteArray())

            url == "https://img/discovery.jpg" -> HttpResponse(200, image)
            else -> HttpResponse(404, ByteArray(0))
        }
    }

    private fun twoResultsJson(): String =
        """
        {"data":[
          {"title":"Other","artist":{"name":"Someone"},"cover_xl":"https://img/other.jpg"},
          {"title":"Discovery","artist":{"name":"Daft Punk"},"cover_xl":"https://img/discovery.jpg"}
        ]}
        """.trimIndent()

    private fun singleResultJson(): String =
        """{"data":[{"title":"Discovery","artist":{"name":"Daft Punk"},"cover_xl":"https://img/discovery.jpg"}]}"""

    private class FakeHttpClient(private val handler: (String) -> HttpResponse) : HttpClient {
        val requested = mutableListOf<String>()

        override suspend fun get(url: String): HttpResponse {
            requested += url
            return handler(url)
        }
    }
}
