package com.musica.player.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musica.player.R
import com.musica.player.di.AppViewModelProvider
import com.musica.player.domain.library.Artwork
import com.musica.player.domain.library.Song
import com.musica.player.domain.playback.PlaybackState
import com.musica.player.domain.playback.RepeatMode
import com.musica.player.ui.components.ArtworkImage
import com.musica.player.ui.components.displayName
import com.musica.player.ui.components.formatDuration

/**
 * Pantalla de reproducción completa: carátula, datos de la canción, barra de
 * progreso y controles (aleatorio, anterior, reproducir/pausar, siguiente,
 * repetir).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_now_playing)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.player_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        val song = state.currentSong
        if (song == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.player_nothing_playing),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            NowPlayingContent(
                state = state,
                song = song,
                onTogglePlayPause = viewModel::togglePlayPause,
                onPrevious = viewModel::previous,
                onNext = viewModel::next,
                onSeek = viewModel::seekTo,
                onToggleShuffle = { viewModel.setShuffleEnabled(!state.shuffleEnabled) },
                onCycleRepeat = viewModel::cycleRepeatMode,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun NowPlayingContent(
    state: PlaybackState,
    song: Song,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = displayName(song.artist, stringResource(R.string.unknown_artist))
    val album = displayName(song.album, stringResource(R.string.unknown_album))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArtworkImage(
            artwork = Artwork(song.contentUri, song.album, song.artist),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            iconSize = 96.dp,
            contentDescription = song.title.ifBlank { artist },
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = song.title.ifBlank { artist },
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,

        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,

        )
        Text(
            text = album,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,

        )

        Spacer(modifier = Modifier.weight(1f))

        SeekBar(
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            songId = song.id,
            onSeek = onSeek,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PlayerControls(
            isPlaying = state.isPlaying,
            shuffleEnabled = state.shuffleEnabled,
            repeatMode = state.repeatMode,
            onTogglePlayPause = onTogglePlayPause,
            onPrevious = onPrevious,
            onNext = onNext,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    songId: Long,
    onSeek: (Long) -> Unit,
) {
    // Mientras se arrastra se usa la posición local para no pelear con el
    // sondeo del reproductor; al soltar se aplica el salto.
    var dragPosition by remember(songId) { mutableStateOf<Float?>(null) }
    val duration = durationMs.coerceAtLeast(0L).toFloat()
    val value = dragPosition ?: positionMs.toFloat()

    Slider(
        value = value.coerceIn(0f, duration),
        onValueChange = { dragPosition = it },
        onValueChangeFinished = {
            dragPosition?.let { onSeek(it.toLong()) }
            dragPosition = null
        },
        valueRange = 0f..duration.coerceAtLeast(1f),
        enabled = durationMs > 0L,
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = formatDuration(positionMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = formatDuration(durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleShuffle) {
            Icon(
                imageVector = Icons.Outlined.Shuffle,
                contentDescription = stringResource(R.string.player_shuffle),
                tint = accent(shuffleEnabled),
            )
        }
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.player_previous),
                modifier = Modifier.size(40.dp),
            )
        }
        FilledIconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier.size(72.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.player_pause else R.string.player_play,
                ),
                modifier = Modifier.size(40.dp),
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.player_next),
                modifier = Modifier.size(40.dp),
            )
        }
        IconButton(onClick = onCycleRepeat) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) {
                    Icons.Outlined.RepeatOne
                } else {
                    Icons.Outlined.Repeat
                },
                contentDescription = stringResource(
                    when (repeatMode) {
                        RepeatMode.OFF -> R.string.player_repeat_off
                        RepeatMode.ALL -> R.string.player_repeat_all
                        RepeatMode.ONE -> R.string.player_repeat_one
                    },
                ),
                tint = accent(repeatMode != RepeatMode.OFF),
            )
        }
    }
}

@Composable
private fun accent(active: Boolean): Color =
    if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
