package com.musica.player.domain.playback

import com.musica.player.domain.library.Song

/** Modo de repetición de la cola de reproducción. */
enum class RepeatMode {
    OFF,
    ALL,
    ONE,
    ;

    /** Siguiente modo al pulsar el control: OFF → ALL → ONE → OFF. */
    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}

/**
 * Estado observable de la reproducción.
 *
 * [queue] es la lista de canciones que se está reproduciendo y
 * [currentSong] la que suena ahora mismo (null si no hay nada cargado).
 */
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
) {
    val isEmpty: Boolean
        get() = currentSong == null
}
