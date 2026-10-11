package com.musica.player.domain.settings

import kotlinx.coroutines.flow.StateFlow

/**
 * Preferencias internas de la aplicación (Fase 8).
 *
 * Son la fuente de verdad para la apariencia y el comportamiento opcional. Se
 * exponen como [StateFlow] porque hay consumidores que necesitan leer el valor
 * actual de forma síncrona (p. ej. el cargador de imágenes de Coil).
 *
 * Valores por defecto, elegidos para no sorprender y respetar el modo
 * offline-first: tema del sistema, color dinámico activado (Android 12+) y
 * búsqueda de carátulas online **desactivada**.
 */
interface SettingsRepository {

    /** Si está activo, se buscan en Internet las carátulas que falten. */
    val onlineArtworkEnabled: StateFlow<Boolean>

    /** Claro, oscuro o el que siga el sistema. */
    val themeMode: StateFlow<ThemeMode>

    /** Si está activo (y el sistema lo permite), se usan los colores de Material You. */
    val dynamicColorEnabled: StateFlow<Boolean>

    suspend fun setOnlineArtworkEnabled(enabled: Boolean)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDynamicColorEnabled(enabled: Boolean)
}
