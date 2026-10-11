package com.musica.player.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp
import com.musica.player.R
import com.musica.player.domain.library.Artwork
import com.musica.player.domain.playback.PlaybackState
import com.musica.player.ui.components.ArtworkImage
import com.musica.player.ui.components.displayName

/**
 * Mini reproductor que se muestra sobre la barra de navegación mientras hay
 * algo en reproducción. Al pulsarlo se abre la pantalla completa.
 */
@Composable
fun MiniPlayer(
    state: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong ?: return
    val artist = displayName(song.artist, stringResource(R.string.unknown_artist))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenNowPlaying),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Column {
            if (state.durationMs > 0L) {
                LinearProgressIndicator(
                    progress = {
                        (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    artwork = Artwork(song.contentUri, song.album, song.artist),
                    modifier = Modifier.size(44.dp),
                    contentDescription = song.title.ifBlank { artist },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title.ifBlank { artist },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (state.isPlaying) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (state.isPlaying) R.string.player_pause else R.string.player_play,
                        ),
                    )
                }
                IconButton(onClick = onNext, enabled = state.hasNext) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.player_next),
                    )
                }
            }
        }
    }
}
