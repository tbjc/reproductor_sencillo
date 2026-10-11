package com.musica.player.ui.player

import androidx.lifecycle.ViewModel
import com.musica.player.domain.library.Song
import com.musica.player.domain.playback.PlaybackState
import com.musica.player.domain.playback.PlayerController
import kotlinx.coroutines.flow.StateFlow

/**
 * Expone el estado de reproducción del [PlayerController] a la UI y traduce las
 * acciones de los controles. Como el controlador es un singleton, varias
 * instancias de este ViewModel (mini reproductor y pantalla completa) observan
 * la misma reproducción.
 */
class PlayerViewModel(private val controller: PlayerController) : ViewModel() {

    val state: StateFlow<PlaybackState> = controller.state

    fun play(songs: List<Song>, startIndex: Int) = controller.play(songs, startIndex)

    fun togglePlayPause() = controller.togglePlayPause()

    fun next() = controller.next()

    fun previous() = controller.previous()

    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)

    fun setShuffleEnabled(enabled: Boolean) = controller.setShuffleEnabled(enabled)

    fun cycleRepeatMode() = controller.cycleRepeatMode()
}
