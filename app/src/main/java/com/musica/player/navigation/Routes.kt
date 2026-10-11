package com.musica.player.navigation

/**
 * Ruta de la pantalla de reproducción completa. No es un destino de la barra
 * inferior: se abre pulsando el mini reproductor.
 */
const val NOW_PLAYING_ROUTE = "now_playing"

/**
 * Ruta de la pantalla de ajustes. Tampoco es un destino de la barra inferior:
 * se abre desde el icono de la barra superior de Inicio.
 */
const val SETTINGS_ROUTE = "settings"

/** Clave del argumento con el identificador de la playlist. */
const val ARG_PLAYLIST_ID = "playlistId"

/** Detalle de una playlist. */
const val PLAYLIST_DETAIL_ROUTE = "playlist/{$ARG_PLAYLIST_ID}"

/** Selector para añadir canciones a una playlist. */
const val PLAYLIST_ADD_SONGS_ROUTE = "playlist/{$ARG_PLAYLIST_ID}/add"

fun playlistDetailRoute(playlistId: Long): String = "playlist/$playlistId"

fun playlistAddSongsRoute(playlistId: Long): String = "playlist/$playlistId/add"
