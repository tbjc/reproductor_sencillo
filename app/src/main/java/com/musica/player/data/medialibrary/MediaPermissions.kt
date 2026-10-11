package com.musica.player.data.medialibrary

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Permisos necesarios para leer la biblioteca de audio, según la versión de
 * Android:
 * - API 33+ (Android 13+): `READ_MEDIA_AUDIO`.
 * - API 26-32: `READ_EXTERNAL_STORAGE`.
 *
 * En API 33+ se añade `POST_NOTIFICATIONS` para poder mostrar la notificación
 * del reproductor. Su concesión no es obligatoria para usar la app.
 */
object MediaPermissions {

    fun requiredPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    /** Permisos que se solicitan de una vez al pedir el acceso a la música. */
    fun requiredPermissions(): Array<String> = buildList {
        add(requiredPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, requiredPermission()) ==
            PackageManager.PERMISSION_GRANTED
}