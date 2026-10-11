package com.musica.player.data.medialibrary

import com.musica.player.domain.library.Album
import com.musica.player.domain.library.Artist
import com.musica.player.domain.library.Artwork
import com.musica.player.domain.library.Song

/**
 * Deriva álbumes y artistas a partir del listado de canciones, ordenados por
 * nombre ignorando mayúsculas. Funciones puras: testables sin framework.
 */
internal fun buildAlbums(songs: List<Song>): List<Album> =
    songs.groupBy { it.albumId }
        .map { (albumId, group) ->
            val first = group.first()
            Album(
                id = albumId,
                name = first.album,
                artist = first.artist,
                artistId = first.artistId,
                year = group.firstNotNullOfOrNull { it.year },
                songCount = group.size,
                artwork = Artwork(first.contentUri, first.album, first.artist),
            )
        }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

internal fun buildArtists(songs: List<Song>): List<Artist> =
    songs.groupBy { it.artistId }
        .map { (artistId, group) ->
            val first = group.first()
            Artist(
                id = artistId,
                name = first.artist,
                songCount = group.size,
                albumCount = group.map { it.albumId }.distinct().size,
            )
        }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })