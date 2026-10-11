package com.musica.player.ui.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musica.player.R
import com.musica.player.di.AppViewModelProvider
import com.musica.player.domain.library.Album
import com.musica.player.ui.components.ArtworkImage
import com.musica.player.ui.components.EmptyState
import com.musica.player.ui.components.ErrorState
import com.musica.player.ui.components.LibraryUiState
import com.musica.player.ui.components.LoadingState
import com.musica.player.ui.components.TabScreenScaffold
import com.musica.player.ui.components.displayName

/**
 * Pantalla de álbumes: cuadrícula con todos los álbumes de la biblioteca.
 */
@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TabScreenScaffold(
        title = stringResource(R.string.nav_albums),
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
                        title = stringResource(R.string.albums_empty_title),
                        message = stringResource(R.string.albums_empty_message),
                        icon = Icons.Outlined.Album,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(state.items, key = { it.id }) { album ->
                            AlbumCard(album)
                        }
                    }
                }
        }
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    modifier: Modifier = Modifier,
) {
    val name = displayName(album.name, stringResource(R.string.unknown_album))
    val artist = displayName(album.artist, stringResource(R.string.unknown_artist))

    Column(modifier = modifier.fillMaxWidth()) {
        ArtworkImage(
            artwork = album.artwork,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            iconSize = 48.dp,
            shape = RoundedCornerShape(12.dp),
            contentDescription = name,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}