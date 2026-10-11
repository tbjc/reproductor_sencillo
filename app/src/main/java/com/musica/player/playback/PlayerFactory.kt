package com.musica.player.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

/**
 * Crea el `ExoPlayer` de música con foco de audio y pausa automática al
 * desconectar auriculares.
 *
 * Lo usa el servicio de reproducción; los tests instrumentados también lo
 * reutilizan para no repetir la configuración.
 */
fun buildMusicExoPlayer(context: Context): ExoPlayer =
    ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()
