package com.musica.player.ui.playlists

import com.musica.player.domain.library.Album
import com.musica.player.domain.library.Artist
import com.musica.player.domain.library.LibraryRepository
import com.musica.player.domain.library.Song
import com.musica.player.domain.playlist.Playlist
import com.musica.player.domain.playlist.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Dobles de prueba compartidos por los tests de los ViewModels de playlists.
 * Evitan Android y Room para que los tests se ejecuten en la JVM.
 */
internal fun testSong(id: Long): Song = Song(
    id = id,
    title = "Canción $id",
    artist = "Artista",
    artistId = 1,
    album = "Álbum",
    albumId = 1,
    genre = null,
    durationMs = 1000,
    size = 0,
    mimeType = null,
    trackNumber = null,
    discNumber = null,
    year = null,
    dateModified = 0,
    contentUri = "content://media/external/audio/media/$id",
)

internal class FakeLibraryRepository : LibraryRepository {
    override val songs = MutableStateFlow<List<Song>>(emptyList())
    override val albums = MutableStateFlow<List<Album>>(emptyList())
    override val artists = MutableStateFlow<List<Artist>>(emptyList())
    override val loaded = MutableStateFlow(false)

    override suspend fun refresh() {
        loaded.value = true
    }
}

internal class FakePlaylistRepository : PlaylistRepository {
    val playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val names = MutableStateFlow<Map<Long, String>>(emptyMap())
    val songsByPlaylist = MutableStateFlow<Map<Long, List<Long>>>(emptyMap())

    val addCalls = mutableListOf<Pair<Long, List<Long>>>()
    val removeCalls = mutableListOf<Pair<Long, Long>>()

    override fun observePlaylists(): Flow<List<Playlist>> = playlists

    override fun observeName(playlistId: Long): Flow<String?> = names.map { it[playlistId] }

    override fun observeSongIds(playlistId: Long): Flow<List<Long>> =
        songsByPlaylist.map { it[playlistId].orEmpty() }

    override suspend fun create(name: String): Long {
        val id = (names.value.keys.maxOrNull() ?: 0L) + 1
        names.value = names.value + (id to name)
        return id
    }

    override suspend fun rename(playlistId: Long, name: String) {
        names.value = names.value + (playlistId to name)
    }

    override suspend fun delete(playlistId: Long) {
        names.value = names.value - playlistId
        songsByPlaylist.value = songsByPlaylist.value - playlistId
    }

    override suspend fun addSongs(playlistId: Long, songIds: List<Long>) {
        addCalls += playlistId to songIds
    }

    override suspend fun removeSong(playlistId: Long, songId: Long) {
        removeCalls += playlistId to songId
    }
}
