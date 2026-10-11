package com.musica.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Estructura base de las pantallas principales: barra superior con título y
 * contenido debajo. Evita duplicar el esqueleto en cada pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}