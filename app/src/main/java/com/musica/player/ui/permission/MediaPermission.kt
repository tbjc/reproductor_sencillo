package com.musica.player.ui.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.musica.player.R
import com.musica.player.data.medialibrary.MediaPermissions

/**
 * Puerta de permiso de lectura de audio.
 *
 * Bloquea el contenido hasta que el permiso esté concedido y muestra una
 * explicación con botón de solicitud. Si el usuario lo bloquea de forma
 * permanente, ofrece abrir los ajustes del sistema. Al volver de los ajustes
 * (o tras conceder el permiso), el estado se re-evalúa en ON_RESUME.
 */
@Composable
fun MediaPermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val permission = MediaPermissions.requiredPermission()

    var granted by remember { mutableStateOf(MediaPermissions.isGranted(context)) }

    LifecycleResumeEffect(Unit) {
        granted = MediaPermissions.isGranted(context)
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { /* El resultado real se re-lee en ON_RESUME */ },
    )

    if (granted) {
        // Se aplica el `modifier` (padding del Scaffold raíz: barra inferior y
        // mini reproductor) para que el contenido —y los FAB de las pantallas—
        // no quede por debajo de la barra de navegación.
        Box(modifier = modifier) {
            content()
        }
    } else {
        var askedBefore by rememberSaveable { mutableStateOf(false) }
        val canShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: false
        // Tras pedir el permiso, la única forma de que el sistema no ofrezca
        // justificación es que el usuario haya marcado "no volver a preguntar".
        val permanentlyDenied = askedBefore && !canShowRationale

        MediaPermissionScreen(
            modifier = modifier,
            permanentlyDenied = permanentlyDenied,
            onRequestPermission = {
                askedBefore = true
                permissionLauncher.launch(MediaPermissions.requiredPermissions())
            },
            onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
                context.startActivity(intent)
            },
        )
    }
}

@Composable
private fun MediaPermissionScreen(
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LibraryMusic,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.media_permission_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.media_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (permanentlyDenied) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.media_permission_permanently_denied),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            enabled = !permanentlyDenied,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.media_permission_request))
        }
        TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.media_permission_open_settings))
        }
    }
}