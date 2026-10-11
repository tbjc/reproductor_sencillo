package com.musica.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.musica.player.domain.library.Artwork

/**
 * Carátula local de una canción o álbum.
 *
 * Extrae el artwork embebido del archivo de audio ([artwork]) con Coil y lo
 * dibuja recortado sobre el [modifier]. El icono de marcador se pinta siempre
 * por debajo: mientras carga y si la carga falla o el tema no trae carátula,
 * queda a la vista.
 */
@Composable
fun ArtworkImage(
    artwork: Artwork?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (artwork != null) {
            AsyncImage(
                model = artwork,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
