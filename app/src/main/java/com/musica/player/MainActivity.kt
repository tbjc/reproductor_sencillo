package com.musica.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musica.player.domain.settings.ThemeMode
import com.musica.player.navigation.MusicaApp
import com.musica.player.ui.theme.MusicaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settings = (application as MusicaApp).container.settingsRepository
        setContent {
            val themeMode by settings.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by settings.dynamicColorEnabled.collectAsStateWithLifecycle()
            MusicaTheme(
                darkTheme = themeMode.resolveDarkTheme(),
                dynamicColor = dynamicColor,
            ) {
                MusicaApp()
            }
        }
    }
}

/** Traduce el modo elegido a si toca pintar en oscuro. */
@Composable
private fun ThemeMode.resolveDarkTheme(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
