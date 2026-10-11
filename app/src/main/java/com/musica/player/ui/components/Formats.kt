package com.musica.player.ui.components

import java.util.Locale

/** Formatea una duración en milisegundos como `m:ss` o `h:mm:ss`. */
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}

/**
 * MediaStore devuelve `"<unknown>"` para artistas/álbumes sin etiqueta.
 * Sustituye ese valor (o texto vacío) por el texto traducible de la UI.
 */
fun displayName(name: String, fallback: String): String =
    if (name.isBlank() || name.equals("<unknown>", ignoreCase = true)) fallback else name