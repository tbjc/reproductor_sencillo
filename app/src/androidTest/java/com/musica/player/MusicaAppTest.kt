package com.musica.player

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas de humo de la interfaz. Conceden el permiso de audio antes de lanzar
 * la Activity y resuelven los textos desde los recursos de la app, de forma que
 * funcionan en cualquier idioma del dispositivo.
 */
class MusicaAppTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val targetContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun laAppLanzaConTodosLosDestinos() {
        listOf(
            R.string.nav_home,
            R.string.nav_songs,
            R.string.nav_albums,
            R.string.nav_artists,
            R.string.nav_playlists,
        ).forEach { res ->
            // Home aparece a la vez en la barra y en la barra superior.
            composeRule.onAllNodesWithText(targetContext.getString(res))
                .onFirst()
                .assertIsDisplayed()
        }
    }

    @Test
    fun alPulsarUnaPestanaSeAbreSuPantalla() {
        // Se usa Playlists porque no aparece en las estadísticas de Inicio.
        val playlists = targetContext.getString(R.string.nav_playlists)
        // En Inicio, "Playlists" solo está en la barra inferior.
        composeRule.onAllNodesWithText(playlists).assertCountEquals(1)

        composeRule.onNodeWithText(playlists).performClick()

        // Ya en su pantalla, el texto aparece en la barra y en el título superior.
        composeRule.onAllNodesWithText(playlists).assertCountEquals(2)
    }

    @Test
    fun crearPlaylistLaMuestraEnLaLista() {
        composeRule.onNodeWithText(targetContext.getString(R.string.nav_playlists)).performClick()

        composeRule
            .onNodeWithContentDescription(targetContext.getString(R.string.playlists_create))
            .performClick()

        val name = "Lista ${System.currentTimeMillis()}"
        composeRule.onNode(hasSetTextAction()).performTextInput(name)
        composeRule.onNodeWithText(targetContext.getString(R.string.action_create)).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()
        }
    }

    companion object {
        /**
         * `@BeforeClass` se ejecuta antes de aplicar las reglas, por lo que el
         * permiso queda concedido antes de que se lance la Activity.
         */
        @BeforeClass
        @JvmStatic
        fun grantMediaPermission() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName,
                permission,
            )
        }
    }
}