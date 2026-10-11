package com.musica.player.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector
import com.musica.player.R

/**
 * Destinos de nivel superior accesibles desde la barra de navegación inferior.
 *
 * Única fuente de verdad para las rutas de navegación raíz.
 */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Outlined.Home),
    SONGS("songs", R.string.nav_songs, Icons.Outlined.MusicNote),
    ALBUMS("albums", R.string.nav_albums, Icons.Outlined.Album),
    ARTISTS("artists", R.string.nav_artists, Icons.Outlined.Mic),
    PLAYLISTS("playlists", R.string.nav_playlists, Icons.AutoMirrored.Outlined.QueueMusic),
}