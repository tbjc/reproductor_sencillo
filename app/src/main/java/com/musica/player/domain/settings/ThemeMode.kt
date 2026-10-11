package com.musica.player.domain.settings

/**
 * Modo de tema elegido por el usuario.
 *
 * [storageKey] es el valor con el que se persiste; se mantiene estable aunque se
 * renombre la constante del enum.
 */
enum class ThemeMode(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        val default = SYSTEM

        /** Reconstruye el modo desde lo guardado; si falta o no se reconoce, usa el de por defecto. */
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageKey == value } ?: default
    }
}
