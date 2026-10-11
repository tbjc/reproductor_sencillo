package com.musica.player.ui.songs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
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
import com.musica.player.ui.components.ErrorState
import com.musica.player.ui.components.LibraryUiState
import com.musica.player.ui.components.LoadingState
import com.musica.player.ui.components.SongRow
import com.musica.player.ui.components.TabScreenScaffold
import com.musica.player.ui.player.PlayerViewModel

/**
 * Pantalla de canciones: listado completo de la biblioteca.
 */
@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: SongsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    playerViewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TabScreenScaffold(
        title = stringResource(R.string.nav_songs),
        modifier = modifier,
    ) {
        when (val state = uiState) {
            LibraryUiState.Loading -> LoadingState()
            LibraryUiState.Error -> ErrorState(
                message = stringResource(R.string.library_load_error),
                onRetry = viewModel::refresh,
            )

            is LibraryUiState.Ready ->
                if (state.items.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.songs_empty_title),
                        message = stringResource(R.string.songs_empty_message),
                        icon = Icons.Outlined.MusicNote,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        itemsIndexed(state.items, key = { _, song -> song.id }) { index, song ->
                            SongRow(
                                song = song,
                                onClick = { playerViewModel.play(state.items, index) },
                            )
                        }
                    }
                }
        }
    }
}