package com.musica.player.data.medialibrary

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.musica.player.domain.library.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Lee la biblioteca de audio directamente de MediaStore.
 *
 * La consulta de canciones es una única pasada al proveedor; los géneros se
 * resuelven en un pase aparte (tabla de géneros + miembros) para no hacer una
 * consulta por canción.
 */
class MediaStoreDataSource(context: Context) {

    private val appContext = context.applicationContext
    private val resolver get() = appContext.contentResolver

    /** Canciones ordenadas por título (insensible a mayúsculas). Vacía si no hay permiso. */
    suspend fun querySongs(): List<Song> = withContext(Dispatchers.IO) {
        if (!MediaPermissions.isGranted(appContext)) {
            return@withContext emptyList()
        }
        val genreByAudioId = queryGenreByAudioId()
        queryAudioSongs(genreByAudioId)
    }

    /**
     * Flujo que emite una señal cada vez que MediaStore notifica cambios en
     * el contenido de audio (archivos añadidos, renombrados o eliminados).
     */
    fun mediaChanges(): Flow<Unit> = callbackFlow {
        val handler = Handler(Looper.getMainLooper())
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer,
        )
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    private fun queryAudioSongs(genreByAudioId: Map<Long, String>): List<Song> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DISC_NUMBER,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val result = mutableListOf<Song>()

        resolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeType = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val track = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val disc = cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
            val year = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateModified = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val songId = cursor.getLong(id)
                result += Song(
                    id = songId,
                    title = cursor.getString(title).orEmpty(),
                    artist = cursor.getString(artist).orEmpty(),
                    artistId = if (cursor.isNull(artistId)) -1L else cursor.getLong(artistId),
                    album = cursor.getString(album).orEmpty(),
                    albumId = if (cursor.isNull(albumId)) -1L else cursor.getLong(albumId),
                    genre = genreByAudioId[songId],
                    durationMs = cursor.getLong(duration),
                    size = cursor.getLong(size),
                    mimeType = cursor.getString(mimeType)?.takeIf { it.isNotBlank() },
                    trackNumber = if (cursor.isNull(track)) null else cursor.getInt(track),
                    discNumber = if (disc >= 0 && !cursor.isNull(disc)) cursor.getInt(disc) else null,
                    year = if (cursor.isNull(year)) null else cursor.getInt(year),
                    dateModified = cursor.getLong(dateModified),
                    contentUri = ContentUris.withAppendedId(collection, songId).toString(),
                )
            }
        }
        return result
    }

    /**
     * Mapa `audioId -> género`. Recorre la tabla de géneros y, por cada una,
     * su lista de canciones miembro. Normalmente hay pocos géneros.
     */
    private fun queryGenreByAudioId(): Map<Long, String> {
        val result = mutableMapOf<Long, String>()
        val genresUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val genresProjection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        val membersProjection = arrayOf(MediaStore.Audio.Media._ID)

        resolver.query(genresUri, genresProjection, null, null, null)?.use { genres ->
            val idCol = genres.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            val nameCol = genres.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
            while (genres.moveToNext()) {
                val genreId = genres.getLong(idCol)
                val name = genres.getString(nameCol)?.takeIf { it.isNotBlank() } ?: continue
                val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                resolver.query(membersUri, membersProjection, null, null, null)?.use { members ->
                    val songIdCol = members.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    while (members.moveToNext()) {
                        result[members.getLong(songIdCol)] = name
                    }
                }
            }
        }
        return result
    }
}