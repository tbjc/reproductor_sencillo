package com.musica.player.data.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.musica.player.domain.settings.ThemeMode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprueba la persistencia y los valores por defecto de las preferencias. Usa
 * un archivo propio para no tocar los ajustes reales de la app.
 */
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "musica_settings_test"

    @After
    fun tearDown() {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun losValoresPorDefectoRespetanElComportamientoActual() {
        val repository = SettingsRepositoryImpl(context, name)

        assertFalse(repository.onlineArtworkEnabled.value)
        assertEquals(ThemeMode.SYSTEM, repository.themeMode.value)
        assertTrue(repository.dynamicColorEnabled.value)
    }

    @Test
    fun persisteElValorEntreInstancias() = runTest {
        SettingsRepositoryImpl(context, name).setOnlineArtworkEnabled(true)

        val reopened = SettingsRepositoryImpl(context, name)

        assertTrue(reopened.onlineArtworkEnabled.value)
    }

    @Test
    fun persisteElTemaYElColorDinamico() = runTest {
        SettingsRepositoryImpl(context, name).apply {
            setThemeMode(ThemeMode.DARK)
            setDynamicColorEnabled(false)
        }

        val reopened = SettingsRepositoryImpl(context, name)

        assertEquals(ThemeMode.DARK, reopened.themeMode.value)
        assertFalse(reopened.dynamicColorEnabled.value)
    }

    @Test
    fun unValorDeTemaDesconocidoVuelveAlSistema() {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", "nonsense")
            .commit()

        val repository = SettingsRepositoryImpl(context, name)

        assertEquals(ThemeMode.SYSTEM, repository.themeMode.value)
    }
}