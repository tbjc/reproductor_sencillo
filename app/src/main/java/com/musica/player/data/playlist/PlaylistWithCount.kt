package com.musica.player.data.playlist

import androidx.room.Embedded

/**
 * Resultado de consultar una playlist junto al número de canciones que
 * contiene, calculado con una subconsulta en SQL.
 */
data class PlaylistWithCount(
    @Embedded val playlist: PlaylistEntity,
    val songCount: Int,
)
