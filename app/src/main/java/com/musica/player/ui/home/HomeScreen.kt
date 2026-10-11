package com.musica.player.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musica.player.R
import com.musica.player.di.AppViewModelProvider
import com.musica.player.ui.components.EmptyState
import com.musica.player.ui.components.ErrorState
import com.musica.player.ui.components.LoadingState
import com.musica.player.ui.components.SongRow
import com.musica.player.ui.components.TabScreenScaffold
import com.musica.player.ui.player.PlayerViewModel

/**
 * Pantalla de inicio: estadísticas de la biblioteca y canciones recientes.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    playerViewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TabScreenScaffold(
        title = stringResource(R.string.nav_home),
        modifier = modifier,
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings_open),
                )
            }
        },
    ) {
        when {
            uiState.hasError -> ErrorState(
                message = stringResource(R.string.library_load_error),
                onRetry = viewModel::refresh,
            )

            !uiState.loaded -> LoadingState()

            uiState.isEmpty -> EmptyState(
                title = stringResource(R.string.home_empty_title),
                message = stringResource(R.string.home_empty_message),
                icon = Icons.Outlined.Home,
            )

            else -> HomeContent(
                uiState = uiState,
                onSongClick = { index -> playerViewModel.play(uiState.recentSongs, index) },
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSongClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    count = uiState.songs.size,
                    label = stringResource(R.string.home_stat_songs),
                    icon = Icons.Outlined.MusicNote,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    count = uiState.albums.size,
                    label = stringResource(R.string.home_stat_albums),
                    icon = Icons.Outlined.Album,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    count = uiState.artists.size,
                    label = stringResource(R.string.home_stat_artists),
                    icon = Icons.Outlined.Mic,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.home_recent),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        itemsIndexed(uiState.recentSongs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                onClick = { onSongClick(index) },
            )
        }
    }
}

@Composable
private fun StatCard(
    count: Int,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}