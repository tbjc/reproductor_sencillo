package com.musica.player.playback

import android.net.Uri
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.media3.exoplayer.ExoPlayer
import com.musica.player.domain.library.Song
import com.musica.player.domain.playback.PlaybackState
import com.musica.player.domain.playback.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Pruebas instrumentadas del reproductor: verifican que Media3 prepara y
 * reproduce un archivo real y que el estado publicado es coherente.
 *
 * El audio de prueba se genera en la caché del dispositivo, así que no hace
 * falta incluir ningún archivo binario en el repositorio ni en el APK.
 */
@RunWith(AndroidJUnit4::class)
class Media3PlayerControllerTest {

    private lateinit var controller: Media3PlayerController
    private lateinit var player: ExoPlayer
    private lateinit var scope: CoroutineScope
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Before
    fun setUp() {
        val context = instrumentation.targetContext
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        instrumentation.runOnMainSync {
            player = buildMusicExoPlayer(context)
            controller = Media3PlayerController(scope)
            controller.connect(player)
        }
    }

    @After
    fun tearDown() {
        // Desconectar el sondeo antes de liberar el reproductor para que el
        // ticker no acceda a un ExoPlayer ya liberado.
        instrumentation.runOnMainSync {
            controller.disconnect()
            player.release()
        }
        scope.cancel()
    }

    @Test
    fun reproduceUnArchivoYActualizaElEstado() {
        val song = testSong(id = 1L)

        play(song)

        val state = await { it.currentSong?.id == 1L && it.isPlaying }
        assertEquals("Test tone", state.currentSong?.title)
        assertTrue(state.isPlaying)
    }

    @Test
    fun siguienteAvanzaALaSiguienteCancionDeLaCola() {
        val first = testSong(id = 1L)
        val second = testSong(id = 2L)

        play(first, second)

        await { it.currentSong?.id == 1L }

        instrumentation.runOnMainSync { controller.next() }

        val state = await { it.currentSong?.id == 2L }
        assertEquals(2L, state.currentSong?.id)
    }

    @Test
    fun elModoDeRepeticionCiclaTrasPulsarElControl() {
        play(testSong(id = 1L))
        await { it.currentSong != null }

        instrumentation.runOnMainSync { controller.cycleRepeatMode() }
        assertEquals(RepeatMode.ALL, await { it.repeatMode == RepeatMode.ALL }.repeatMode)

        instrumentation.runOnMainSync { controller.cycleRepeatMode() }
        assertEquals(RepeatMode.ONE, await { it.repeatMode == RepeatMode.ONE }.repeatMode)

        instrumentation.runOnMainSync { controller.cycleRepeatMode() }
        assertEquals(RepeatMode.OFF, await { it.repeatMode == RepeatMode.OFF }.repeatMode)
    }

    private fun play(vararg songs: Song) {
        instrumentation.runOnMainSync { controller.play(songs.toList(), 0) }
    }

    private fun await(timeoutMs: Long = 5_000, predicate: (PlaybackState) -> Boolean): PlaybackState {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val state = controller.state.value
            if (predicate(state)) return state
            Thread.sleep(50)
        }
        throw AssertionError("Tiempo de espera agotado. Estado actual: ${controller.state.value}")
    }

    private fun testSong(id: Long): Song {
        val file = writeTestWav()
        return Song(
            id = id,
            title = "Test tone",
            artist = "Test artist",
            artistId = 1L,
            album = "Test album",
            albumId = 1L,
            genre = null,
            durationMs = 1_000L,
            size = file.length(),
            mimeType = "audio/wav",
            trackNumber = null,
            discNumber = null,
            year = null,
            dateModified = 0L,
            contentUri = Uri.fromFile(file).toString(),
        )
    }

    private fun writeTestWav(): File {
        val file = File(instrumentation.targetContext.cacheDir, "test_tone.wav")
        val sampleRate = 22_050
        val samples = sampleRate // 1 segundo
        val dataSize = samples * 2
        val stream = BufferedOutputStream(file.outputStream())

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)
        header.putShort(1)
        header.putShort(1)
        header.putInt(sampleRate)
        header.putInt(sampleRate * 2)
        header.putShort(2)
        header.putShort(16)
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataSize)
        stream.write(header.array())

        val samplesBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until samples) {
            val value = (6_000 * kotlin.math.sin(2 * Math.PI * 440.0 * i / sampleRate)).toInt().toShort()
            samplesBuffer.putShort(value)
        }
        stream.write(samplesBuffer.array())
        stream.close()
        return file
    }
}
