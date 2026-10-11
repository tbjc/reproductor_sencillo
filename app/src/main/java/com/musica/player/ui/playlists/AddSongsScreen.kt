package com.musica.player.ui.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * Selector de canciones de la biblioteca para añadir a una playlist. Las que ya
 * están en la lista se muestran marcadas y no se pueden volver a añadir.
 */
@Composable
fun AddSongsScreen(
    playlistId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddSongsViewModel = viewModel(
        factory = AppViewModelProvider.addSongsFactory(playlistId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = stringResource(R.string.playlist_add_songs),
        onBack = onBack,
        modifier = modifier,
        actions = {
            TextButton(
                onClick = { viewModel.addSelected(onDone = onBack) },
                enabled = uiState.canAdd,
            ) {
                Text(stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        when {
            !uiState.loaded -> LoadingState(Modifier.padding(padding))
            uiState.songs.isEmpty() -> Box(modifier = Modifier.padding(padding)) {
                EmptyState(
                    title = stringResource(R.string.songs_empty_title),
                    message = stringResource(R.string.songs_empty_message),
                    icon = Icons.Outlined.MusicNote,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding(),
                ),
            ) {
                items(uiState.songs, key = { it.id }) { song ->
                    val inPlaylist = song.id in uiState.inPlaylist
                    SongRow(
                        song = song,
                        onClick = { viewModel.toggle(song.id) },
                        trailingAction = {
                            if (inPlaylist) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = stringResource(R.string.playlist_already_added),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Checkbox(
                                    checked = song.id in uiState.selected,
                                    onCheckedChange = null,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
