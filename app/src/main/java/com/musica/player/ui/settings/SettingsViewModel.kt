package com.musica.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musica.player.domain.settings.SettingsRepository
import com.musica.player.domain.settings.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de Ajustes: apariencia (tema y color dinámico) y
 * comportamiento de la biblioteca (carátulas online). Persiste cada cambio.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val onlineArtworkEnabled: StateFlow<Boolean> = settingsRepository.onlineArtworkEnabled

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode

    val dynamicColorEnabled: StateFlow<Boolean> = settingsRepository.dynamicColorEnabled

    fun setOnlineArtworkEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setOnlineArtworkEnabled(enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColorEnabled(enabled) }
    }
}
