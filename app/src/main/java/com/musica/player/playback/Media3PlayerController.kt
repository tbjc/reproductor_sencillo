package com.musica.player.playback

import androidx.media3.common.C
import androidx.media3.common.Player
import com.musica.player.domain.library.Song
import com.musica.player.domain.playback.PlaybackState
import com.musica.player.domain.playback.PlayerController
import com.musica.player.domain.playback.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Adapta un `Player` de Media3 a la interfaz [PlayerController] del dominio.
 *
 * El mismo adaptador sirve para el `ExoPlayer` que vive en el servicio (en los
 * tests) y para el `MediaController` con el que la app se comunica con la
 * sesión. **No posee** el reproductor: quien lo entrega a [connect] es
 * responsable de liberarlo.
 *
 * La cola del estado se **reconstruye** desde los elementos del reproductor en
 * cada publicación, de modo que siempre refleja la lista real de la sesión
 * (por ejemplo, al reconectar con el servicio).
 *
 * Todos los métodos deben invocarse desde el hilo principal (el hilo de
 * aplicación de Media3).
 */
class Media3PlayerController(
    private val scope: CoroutineScope,
) : PlayerController {

    private var player: Player? = null
    private var positionJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish()
        }
    }

    /** Empieza a observar y controlar [newPlayer]. */
    fun connect(newPlayer: Player) {
        disconnect()
        player = newPlayer
        newPlayer.addListener(listener)
        publish()
        // ExoPlayer no emite eventos al avanzar la posición: se sondea mientras
        // suena música para que la barra de progreso se actualice.
        positionJob = scope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                if (player?.isPlaying == true) publishPosition()
                delay(POSITION_POLL_MS)
            }
        }
    }

    /** Deja de observar el reproductor actual. No lo libera. */
    fun disconnect() {
        positionJob?.cancel()
        positionJob = null
        player?.removeListener(listener)
        player = null
        _state.value = PlaybackState()
    }

    override fun play(songs: List<Song>, startIndex: Int) {
        val player = player ?: return
        if (songs.isEmpty()) return
        val items = songs.map { it.toMediaItem() }
        val start = startIndex.coerceIn(0, songs.lastIndex)
        player.setMediaItems(items, start, 0L)
        player.prepare()
        player.play()
        publish()
    }

    override fun togglePlayPause() {
        val player = player ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            // Si la cola terminó, al reanudar se vuelve a empezar por el inicio.
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekToDefaultPosition()
            }
            player.play()
        }
    }

    override fun next() {
        val player = player ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    override fun previous() {
        val player = player ?: return
        // Convención habitual: reiniciar la canción si ya avanzó unos segundos.
        if (player.currentPosition > RESTART_THRESHOLD_MS) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }
    }

    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        publishPosition()
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        player?.shuffleModeEnabled = enabled
    }

    override fun cycleRepeatMode() {
        val player = player ?: return
        player.repeatMode = player.repeatMode.toDomainMode().next().toPlayerMode()
    }

    private fun publish() {
        val player = player ?: return
        val queue = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).toSong() }
        val duration = player.duration.let { if (it == C.TIME_UNSET) 0L else it }
        _state.value = PlaybackState(
            currentSong = queue.getOrNull(player.currentMediaItemIndex),
            isPlaying = player.isPlaying,
            hasPrevious = player.hasPreviousMediaItem(),
            hasNext = player.hasNextMediaItem(),
            positionMs = player.currentPosition,
            durationMs = duration,
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode.toDomainMode(),
            queue = queue,
        )
    }

    private fun publishPosition() {
        val player = player ?: return
        _state.value = _state.value.copy(
            positionMs = player.currentPosition,
            isPlaying = player.isPlaying,
        )
    }

    private fun Int.toDomainMode(): RepeatMode = when (this) {
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        else -> RepeatMode.OFF
    }

    private fun RepeatMode.toPlayerMode(): Int = when (this) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
    }

    private companion object {
        const val POSITION_POLL_MS = 500L

        /** Si ya sonó más de esto, "anterior" reinicia la canción actual. */
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
