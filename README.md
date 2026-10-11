# Musica

Reproductor de música local para Android: nativo, gratuito, sin publicidad
y sin cuentas obligatorias.

## Estado del proyecto

Desarrollo por fases según `AGENTS.md`.

| Fase | Contenido | Estado |
|------|-----------|--------|
| 1 | Estructura, Gradle, Compose, Material 3, navegación, arquitectura base | ✅ |
| 2 | MediaStore, biblioteca (canciones, artistas, álbumes) | ✅ |
| 3 | Media3, reproductor, cola, mini-player | ✅ |
| 4 | Reproducción en segundo plano, MediaSession, notificación, Bluetooth | ✅ |
| 5 | Room, playlists | ✅ |
| 6 | Artwork local | ✅ |
| 7 | Artwork online | ✅ |
| 8 | Personalización y configuración | ✅ |
| 9 | Optimización, testing, accesibilidad | ✅ |
| 10 | Release | ⏳ |

> La Fase 8 añade la pantalla **Ajustes** (icono de la barra superior de Inicio)
> con la sección **Apariencia** (tema: sistema/claro/oscuro y color dinámico de
> Material You) y la sección **Biblioteca** ("Buscar carátulas online",
> **desactivada por defecto**).

## Compilar

```bash
# Con JDK 17+ (se recomienda el JBR de Android Studio)
./gradlew :app:assembleDebug
```

Requiere tener instalado el Android SDK (path en `local.properties`).

## Stack

- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- Android Gradle Plugin 9.x (Kotlin integrado)
- Media3 / ExoPlayer (Fases 3-4)
- Room (Fase 5)
- Coil (Fase 6+)
- Artwork online con `HttpURLConnection` + `org.json` (Fase 7; sin librerías
  nuevas en la app)
- Ajustes con `SharedPreferences` (Fase 8; sin librerías nuevas)

Consulta `docs/architecture.md` para las decisiones técnicas.