package com.musica.player.data.settings

import android.content.Context
import androidx.core.content.edit
import com.musica.player.domain.settings.SettingsRepository
import com.musica.player.domain.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias persistentes con `SharedPreferences` (no añade dependencias).
 *
 * Los valores se mantienen en memoria para que la UI y el cargador de imágenes
 * los lean sin bloquear. [preferencesName] es inyectable para que los tests usen
 * un archivo propio y no toquen las preferencias reales de la app.
 */
class SettingsRepositoryImpl(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME,
) : SettingsRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    private val onlineArtwork = MutableStateFlow(prefs.getBoolean(KEY_ONLINE_ARTWORK, false))
    private val theme = MutableStateFlow(ThemeMode.fromStorage(prefs.getString(KEY_THEME_MODE, null)))
    private val dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))

    override val onlineArtworkEnabled: StateFlow<Boolean> = onlineArtwork.asStateFlow()
    override val themeMode: StateFlow<ThemeMode> = theme.asStateFlow()
    override val dynamicColorEnabled: StateFlow<Boolean> = dynamicColor.asStateFlow()

    override suspend fun setOnlineArtworkEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ONLINE_ARTWORK, enabled) }
        onlineArtwork.value = enabled
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.storageKey) }
        theme.value = mode
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
        dynamicColor.value = enabled
    }

    companion object {
        const val DEFAULT_PREFERENCES_NAME = "musica_settings"

        private const val KEY_ONLINE_ARTWORK = "online_artwork"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }
}
