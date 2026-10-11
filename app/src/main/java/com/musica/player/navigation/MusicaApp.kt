package com.musica.player.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.musica.player.di.AppViewModelProvider
import com.musica.player.ui.albums.AlbumsScreen
import com.musica.player.ui.artists.ArtistsScreen
import com.musica.player.ui.home.HomeScreen
import com.musica.player.ui.permission.MediaPermissionGate
import com.musica.player.ui.player.MiniPlayer
import com.musica.player.ui.player.NowPlayingScreen
import com.musica.player.ui.player.PlayerViewModel
import com.musica.player.ui.playlists.AddSongsScreen
import com.musica.player.ui.playlists.PlaylistDetailScreen
import com.musica.player.ui.playlists.PlaylistsScreen
import com.musica.player.ui.settings.SettingsScreen
import com.musica.player.ui.songs.SongsScreen

/**
 * Raíz de la interfaz: `Scaffold` con barra de navegación inferior (y mini
 * reproductor cuando suena algo) y el grafo de navegación de la aplicación.
 */
@Composable
fun MusicaApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val onNowPlaying = currentDestination?.route == NOW_PLAYING_ROUTE

    val playerViewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val playbackState by playerViewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // La pantalla de reproducción ocupa toda la pantalla.
            if (!onNowPlaying) {
                Column {
                    if (playbackState.currentSong != null) {
                        MiniPlayer(
                            state = playbackState,
                            onTogglePlayPause = playerViewModel::togglePlayPause,
                            onNext = playerViewModel::next,
                            onOpenNowPlaying = {
                                navController.navigate(NOW_PLAYING_ROUTE) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    NavigationBar {
                        TopLevelDestination.entries.forEach { destination ->
                            val selected = currentDestination
                                ?.hierarchy
                                ?.any { it.route == destination.route } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        MediaPermissionGate(
            modifier = Modifier.padding(innerPadding),
        ) {
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.HOME.route) {
            HomeScreen(
                onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
            )
        }
        composable(TopLevelDestination.SONGS.route) {
            SongsScreen()
        }
        composable(TopLevelDestination.ALBUMS.route) {
            AlbumsScreen()
        }
        composable(TopLevelDestination.ARTISTS.route) {
            ArtistsScreen()
        }
        composable(TopLevelDestination.PLAYLISTS.route) {
            PlaylistsScreen(
                onOpenPlaylist = { playlistId ->
                    navController.navigate(playlistDetailRoute(playlistId))
                },
            )
        }
        composable(
            route = PLAYLIST_DETAIL_ROUTE,
            arguments = listOf(navArgument(ARG_PLAYLIST_ID) { type = NavType.LongType }),
        ) { entry ->
            val playlistId = entry.arguments?.getLong(ARG_PLAYLIST_ID) ?: 0L
            PlaylistDetailScreen(
                playlistId = playlistId,
                onBack = { navController.popBackStack() },
                onAddSongs = { navController.navigate(playlistAddSongsRoute(playlistId)) },
            )
        }
        composable(
            route = PLAYLIST_ADD_SONGS_ROUTE,
            arguments = listOf(navArgument(ARG_PLAYLIST_ID) { type = NavType.LongType }),
        ) { entry ->
            val playlistId = entry.arguments?.getLong(ARG_PLAYLIST_ID) ?: 0L
            AddSongsScreen(
                playlistId = playlistId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(NOW_PLAYING_ROUTE) {
            NowPlayingScreen(onBack = { navController.popBackStack() })
        }
    }
}
