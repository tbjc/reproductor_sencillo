package com.musica.player.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musica.player.R
import com.musica.player.di.AppViewModelProvider
import com.musica.player.domain.playlist.Playlist
import com.musica.player.ui.components.EmptyState
import com.musica.player.ui.components.LoadingState
import com.musica.player.ui.components.ScreenScaffold

/**
 * Lista de playlists del usuario. Permite crear una nueva con el FAB y abrir
 * cualquiera de ellas para editar sus canciones.
 */
@Composable
fun PlaylistsScreen(
    onOpenPlaylist: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold(
        title = stringResource(R.string.nav_playlists),
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.playlists_create),
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                !uiState.loaded -> LoadingState()
                uiState.playlists.isEmpty() -> EmptyState(
                    title = stringResource(R.string.playlists_empty_title),
                    message = stringResource(R.string.playlists_empty_message),
                    icon = Icons.AutoMirrored.Outlined.QueueMusic,
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.playlists, key = { it.id }) { playlist ->
                        PlaylistRow(
                            playlist = playlist,
                            onClick = { onOpenPlaylist(playlist.id) },
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PlaylistNameDialog(
            title = stringResource(R.string.playlist_create_title),
            confirmLabel = stringResource(R.string.action_create),
            initialName = "",
            onConfirm = { name ->
                viewModel.create(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
        },
        supportingContent = {
            Text(
                text = pluralStringResource(
                    R.plurals.library_song_count,
                    playlist.songCount,
                    playlist.songCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
