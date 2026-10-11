package com.musica.player.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musica.player.R
import com.musica.player.di.AppViewModelProvider
import com.musica.player.domain.library.Artist
import com.musica.player.ui.components.EmptyState
import com.musica.player.ui.components.ErrorState
import com.musica.player.ui.components.LibraryUiState
import com.musica.player.ui.components.LoadingState
import com.musica.player.ui.components.TabScreenScaffold
import com.musica.player.ui.components.displayName

/**
 * Pantalla de artistas: listado con inicial del avatar y recuentos.
 */
@Composable
fun ArtistsScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtistsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TabScreenScaffold(
        title = stringResource(R.string.nav_artists),
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
                        title = stringResource(R.string.artists_empty_title),
                        message = stringResource(R.string.artists_empty_message),
                        icon = Icons.Outlined.Mic,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(state.items, key = { it.id }) { artist ->
                            ArtistRow(artist)
                        }
                    }
                }
        }
    }
}

@Composable
private fun ArtistRow(
    artist: Artist,
    modifier: Modifier = Modifier,
) {
    val name = displayName(artist.name, stringResource(R.string.unknown_artist))
    val songCount = pluralStringResource(R.plurals.library_song_count, artist.songCount, artist.songCount)
    val albumCount = pluralStringResource(R.plurals.library_album_count, artist.albumCount, artist.albumCount)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = "$songCount · $albumCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}