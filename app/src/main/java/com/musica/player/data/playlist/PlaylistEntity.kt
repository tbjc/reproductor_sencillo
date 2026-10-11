package com.musica.player.data.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla de playlists. [id] es autogenerado por Room; [createdAt] permite un
 * orden estable si en el futuro se ofrece ordenar por fecha.
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)
