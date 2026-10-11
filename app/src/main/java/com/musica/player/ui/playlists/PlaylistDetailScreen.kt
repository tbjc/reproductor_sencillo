package com.musica.player.ui.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musica.player.R
import com.musica.player.di.AppViewModelProvider
import com.musica.player.ui.components.EmptyState
import com.musica.player.ui.components.LoadingState
import com.musica.player.ui.components.ScreenScaffold
import com.musica.player.ui.components.SongRow
import com.musica.player.ui.player.PlayerViewModel

/**
 * Detalle de una playlist: sus canciones, con acciones para reproducir,
 * quitar canciones, renombrar, eliminar y añadir más.
 *
 * [playlistId] llega como argumento de navegación; con él se construye su
 * propio ViewModel mediante una fábrica.
 */
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBack: () -> Unit,
    onAddSongs: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = viewModel(
        factory = AppViewModelProvider.playlistDetailFactory(playlistId),
    ),
    playerViewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Si la playlist deja de existir (recién eliminada, o enlace a una que ya
    // no está), se vuelve atrás.
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    ScreenScaffold(
        title = uiState.name.orEmpty(),
        onBack = onBack,
        modifier = modifier,
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.playlist_options),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.playlist_rename)) },
                    onClick = {
                        menuExpanded = false
                        showRenameDialog = true
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.playlist_delete)) },
                    onClick = {
                        menuExpanded = false
                        showDeleteDialog = true
                    },
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSongs,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.playlist_add_songs)) },
            )
        },
    ) { padding ->
        when {
            !uiState.loaded -> LoadingState(modifier = Modifier.padding(padding))
            uiState.songs.isEmpty() -> Box(modifier = Modifier.padding(padding)) {
                EmptyState(
                    title = stringResource(R.string.playlist_empty_title),
                    message = stringResource(R.string.playlist_empty_message),
                    icon = Icons.AutoMirrored.Outlined.QueueMusic,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Espacio extra abajo para que el FAB no tape la última fila.
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + FAB_SPACE,
                ),
            ) {
                itemsIndexed(uiState.songs, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        onClick = { playerViewModel.play(uiState.songs, index) },
                        trailingAction = {
                            IconButton(onClick = { viewModel.removeSong(song.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.playlist_remove_song),
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        PlaylistNameDialog(
            title = stringResource(R.string.playlist_rename_title),
            confirmLabel = stringResource(R.string.action_save),
            initialName = uiState.name.orEmpty(),
            onConfirm = { name ->
                viewModel.rename(name)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.playlist_delete_title)) },
            text = { Text(stringResource(R.string.playlist_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Hueco inferior para que el FAB no tape la última canción de la lista. */
private val FAB_SPACE = 88.dp
