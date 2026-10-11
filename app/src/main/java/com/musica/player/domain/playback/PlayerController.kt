package com.musica.player.domain.playback

import com.musica.player.domain.library.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Control de reproducción de la aplicación. La única implementación se apoya
 * en Media3 (`ExoPlayer`).
 *
 * La interfaz expone el estado como [state] y una serie de acciones. La
 * implementación vive a nivel de aplicación, por lo que cualquier pantalla
 * observa y controla la misma reproducción.
 */
interface PlayerController {

    val state: StateFlow<PlaybackState>

    /** Reproduce [songs] empezando por la posición [startIndex]. */
    fun play(songs: List<Song>, startIndex: Int)

    fun togglePlayPause()

    fun next()

    fun previous()

    fun seekTo(positionMs: Long)

    fun setShuffleEnabled(enabled: Boolean)

    fun cycleRepeatMode()
}
