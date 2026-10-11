package com.musica.player.data.playlist

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos local de Musica. La versión 1 solo almacena playlists; el
 * esquema se exporta a `app/schemas` para poder escribir migraciones.
 */
@Database(
    entities = [PlaylistEntity::class, PlaylistSongEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MusicaDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
