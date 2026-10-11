package com.musica.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.musica.player.R
import com.musica.player.domain.library.Artwork
import com.musica.player.domain.library.Song

/**
 * Fila de canción reutilizada por las listas de canciones (pantalla Canciones
 * y Recientes de Inicio). [onClick] inicia la reproducción de la canción.
 *
 * [trailingAction] permite sustituir el nombre del álbum de la derecha por una
 * acción (por ejemplo, quitar de una playlist o marcar con una casilla).
 */
@Composable
fun SongRow(
    song: Song,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val artist = displayName(song.artist, stringResource(R.string.unknown_artist))
    val album = displayName(song.album, stringResource(R.string.unknown_album))
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            ArtworkImage(
                artwork = Artwork(song.contentUri, song.album, song.artist),
                modifier = Modifier.size(48.dp),
                contentDescription = song.title.ifBlank { artist },
            )
        },
        headlineContent = {
            Text(
                text = song.title.ifBlank { artist },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                
            )
        },
        supportingContent = {
            Text(
                text = "$artist · ${formatDuration(song.durationMs)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                
            )
        },
        trailingContent = {
            if (trailingAction != null) {
                trailingAction()
            } else {
                Text(
                    text = album,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    
                )
            }
        },
    )
}