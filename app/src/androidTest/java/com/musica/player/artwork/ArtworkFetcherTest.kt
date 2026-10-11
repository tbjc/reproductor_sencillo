package com.musica.player.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.musica.player.artwork.online.NetworkMonitor
import com.musica.player.artwork.online.OnlineArtwork
import com.musica.player.artwork.online.OnlineArtworkCache
import com.musica.player.artwork.online.OnlineArtworkResolver
import com.musica.player.artwork.online.OnlineArtworkSource
import com.musica.player.domain.library.Artwork
import com.musica.player.domain.settings.SettingsRepository
import com.musica.player.domain.settings.ThemeMode
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprueba de punta a punta que [ArtworkFetcher] extrae la carátula embebida de
 * un MP3 real y que, cuando el archivo no la trae, la carga termina en error
 * (lo que en la UI deja ver el marcador de `ArtworkImage`).
 */
@RunWith(AndroidJUnit4::class)
class ArtworkFetcherTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun extraeLaCaratulaEmbebida() = runBlocking {
        val file = writeMp3(withArtwork = true)
        clearArtworkCache()

        val result = load(requestFor(file))

        assertTrue("Se esperaba SuccessResult, fue $result", result is SuccessResult)
        assertNotNull((result as SuccessResult).image)
        assertTrue("La carátula debería quedar cacheada en disco", artworkCacheFiles().isNotEmpty())
    }

    @Test
    fun sinCaratulaDevuelveError() = runBlocking {
        val file = writeMp3(withArtwork = false)

        val result = load(requestFor(file))

        assertTrue("Se esperaba ErrorResult, fue $result", result is ErrorResult)
    }

    @Test
    fun reutilizaLaCaratulaCacheadaEnDisco() = runBlocking {
        val file = writeMp3(withArtwork = true)
        clearArtworkCache()
        // Primera carga: extrae del MP3 y escribe la caché.
        load(requestFor(file))

        // Segunda carga con un cargador limpio: debe resolverse desde la caché.
        val result = load(requestFor(file))

        assertTrue("Se esperaba SuccessResult, fue $result", result is SuccessResult)
    }

    private suspend fun load(request: ImageRequest): coil3.request.ImageResult {
        val loader = newArtworkImageLoader(context, FakeSettings(online = false))
        return try {
            loader.execute(request)
        } finally {
            loader.shutdown()
        }
    }

    @Test
    fun usaLaCaratulaOnlineCuandoElAudioNoTiene() = runBlocking {
        val file = writeMp3(withArtwork = false)
        val embeddedDir = File(context.cacheDir, "artwork_test_embedded").apply { deleteRecursively() }
        val onlineDir = File(context.cacheDir, "artwork_test_online").apply { deleteRecursively() }
        val source = FakeOnlineSource(OnlineArtwork.Found(pngBytes()))
        val settings = FakeSettings(online = true)
        val resolver = OnlineArtworkResolver(
            source = source,
            cache = OnlineArtworkCache(onlineDir),
            network = AlwaysOnline,
            settings = settings,
        )
        val loader = ImageLoader.Builder(context)
            .components {
                add(ArtworkFetcher.Factory(ArtworkCache(embeddedDir), resolver))
                add(ArtworkKeyer(settings))
            }
            .build()
        val request = ImageRequest.Builder(context)
            .data(Artwork(Uri.fromFile(file).toString(), album = "Álbum X", artist = "Artista Y"))
            .size(128)
            .build()

        val result = try {
            loader.execute(request)
        } finally {
            loader.shutdown()
        }

        assertTrue("Se esperaba SuccessResult, fue $result", result is SuccessResult)
        assertEquals(1, source.calls)
        assertTrue("La carátula online debería quedar cacheada", onlineDir.listFiles().orEmpty().isNotEmpty())
    }

    private fun artworkCacheDir(): File = File(context.cacheDir, "artwork")

    private fun artworkCacheFiles(): List<File> = artworkCacheDir().listFiles().orEmpty().toList()

    private fun clearArtworkCache() {
        artworkCacheDir().deleteRecursively()
    }

    private fun requestFor(file: File): ImageRequest =
        ImageRequest.Builder(context)
            .data(Artwork(Uri.fromFile(file).toString()))
            .size(128)
            .build()

    private fun writeMp3(withArtwork: Boolean): File {
        val file = File(context.cacheDir, if (withArtwork) "art_embedded.mp3" else "art_missing.mp3")
        file.outputStream().use { out ->
            if (withArtwork) out.write(id3TagWithApic(pngBytes()))
            out.write(mpegFrames(count = 50))
        }
        return file
    }

    private fun pngBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }

    /** Tag ID3v2.3 con un único frame APIC (carátula frontal). */
    private fun id3TagWithApic(picture: ByteArray): ByteArray {
        val apicBody = ByteArrayOutputStream().apply {
            write(0x00) // Codificación ISO-8859-1
            write("image/png".toByteArray(Charsets.US_ASCII))
            write(0x00) // Fin del MIME type
            write(0x03) // Tipo: carátula frontal
            write(0x00) // Descripción vacía
            write(picture)
        }.toByteArray()

        val apicFrame = ByteArrayOutputStream().apply {
            write("APIC".toByteArray(Charsets.US_ASCII))
            write(bigEndian(apicBody.size))
            write(byteArrayOf(0x00, 0x00)) // Flags del frame
            write(apicBody)
        }.toByteArray()

        return ByteArrayOutputStream().apply {
            write("ID3".toByteArray(Charsets.US_ASCII))
            write(byteArrayOf(0x03, 0x00, 0x00)) // Versión 2.3.0, sin flags
            write(synchsafe(apicFrame.size))
            write(apicFrame)
        }.toByteArray()
    }

    /** Tramas MPEG-1 Layer III de 128 kbps a 44,1 kHz (cabecera válida + relleno). */
    private fun mpegFrames(count: Int): ByteArray {
        val frame = ByteArray(FRAME_SIZE)
        frame[0] = 0xFF.toByte()
        frame[1] = 0xFB.toByte()
        frame[2] = 0x90.toByte()
        frame[3] = 0x00
        val all = ByteArray(FRAME_SIZE * count)
        for (i in 0 until count) {
            frame.copyInto(all, i * FRAME_SIZE)
        }
        return all
    }

    private fun bigEndian(value: Int): ByteArray = byteArrayOf(
        (value ushr 24).toByte(),
        (value ushr 16).toByte(),
        (value ushr 8).toByte(),
        value.toByte(),
    )

    private fun synchsafe(value: Int): ByteArray = byteArrayOf(
        ((value ushr 21) and 0x7F).toByte(),
        ((value ushr 14) and 0x7F).toByte(),
        ((value ushr 7) and 0x7F).toByte(),
        (value and 0x7F).toByte(),
    )

    private class FakeOnlineSource(private val result: OnlineArtwork) : OnlineArtworkSource {
        var calls = 0

        override suspend fun findAlbumArtwork(album: String, artist: String): OnlineArtwork {
            calls++
            return result
        }
    }

    private object AlwaysOnline : NetworkMonitor {
        override fun isOnline(): Boolean = true
    }

    private class FakeSettings(online: Boolean) : SettingsRepository {
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

    private companion object {
        const val FRAME_SIZE = 417
    }
}
