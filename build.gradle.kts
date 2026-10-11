// Configuración de plugins a nivel de proyecto.
//
// AGP 9.0+ incluye soporte de Kotlin integrado (built-in Kotlin): no se aplica
// el plugin org.jetbrains.kotlin.android. El compilador Compose se activa con
// el plugin org.jetbrains.kotlin.plugin.compose, versionado con Kotlin.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}