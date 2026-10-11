package com.musica.player.data.playlist

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Relación entre una playlist y una canción de MediaStore.
 *
 * La clave primaria compuesta (playlistId, songId) impide añadir la misma
 * canción dos veces. [position] fija el orden dentro de la lista. Si se borra
 * la playlist, sus filas se eliminan en cascada.
 *
 * Solo se guarda `songId`: los metadatos viven en MediaStore y se resuelven al
 * vuelo, evitando duplicar (y desactualizar) títulos o artistas.
 */
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int,
)
