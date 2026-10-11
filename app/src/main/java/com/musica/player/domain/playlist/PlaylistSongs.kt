package com.musica.player.domain.playlist

import com.musica.player.domain.library.Song

/**
 * Resuelve las canciones de una playlist a partir de sus identificadores.
 *
 * Respeta el orden de [songIds] y descarta los identificadores que ya no estén
 * en [allSongs] (por ejemplo, canciones borradas del dispositivo). Es una
 * función pura para poder probarla sin Android.
 */
fun resolvePlaylistSongs(allSongs: List<Song>, songIds: List<Long>): List<Song> {
    if (songIds.isEmpty()) return emptyList()
    val byId = allSongs.associateBy { it.id }
    return songIds.mapNotNull { byId[it] }
}
