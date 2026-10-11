# Arquitectura

## Decisiones técnicas

### Versiones base (Fase 2)

| Componente | Versión | Notas |
|---|---|---|
| Android Gradle Plugin | 9.4.1 | Requiere Gradle 9.6 y soporta API 37 |
| Gradle | 9.6.0 | Wrapper incluido |
| Kotlin (KGP) | 2.4.21 | Compose compiler plugin 2.4.21 |
| Compose BOM | 2026.09.00 | Compose UI 1.12.1 (requiere compileSdk 37) |
| compileSdk / minSdk / targetSdk | 37 / 26 / 37 | API 26+ (Android 8.0+) |
| Navigation Compose | 2.10.2 | |
| activity-compose | 1.13.0 | |
| core-ktx | 1.19.1 | |
| lifecycle-* | 2.11.0 | runtime-compose y viewmodel-compose |
| kotlinx-coroutines | 1.11.0 | coroutines-android; coroutines-test en tests unitarios |
| Media3 | 1.11.1 | media3-exoplayer + media3-session (Fases 3-4) |
| Room | 2.8.5 | room-runtime + room-compiler (Fase 5) |
| KSP | 2.3.12 | com.google.devtools.ksp, para el procesador de Room |
| Coil | 3.6.3 | coil-compose (Fase 6: carátulas locales) |
| org.json | 20250107 | solo `testImplementation`: en Android ya forma parte de la plataforma (Fase 7) |

### AGP 9: Kotlin integrado (built-in Kotlin)

AGP 9.0+ sustituye al plugin `org.jetbrains.kotlin.android`. No se aplica ese
plugin; el compilador Compose se activa con `org.jetbrains.kotlin.plugin.compose`
versionado con Kotlin. En la Fase 5 se añade KSP
(`com.google.devtools.ksp`) para generar el código de Room; KSP soporta el
Kotlin integrado de AGP 9 (arreglado en 2.3.6+) y funciona con Kotlin 2.4.x.
Solo se usan `room-runtime` y `room-compiler`: las extensiones de corrutinas y
`Flow` ya vienen en el runtime, así que no se añade `room-ktx`.

### Módulo único

Se usa un único módulo `:app`. Cuando un módulo adicional aporte un beneficio
real (reutilización, tiempos de build), se dividirá. Regla: no añadir
complejidad arquitectónica sin razón.

### Inyección de dependencias

Sin framework de DI por ahora. A partir de la Fase 2 se usará un
`AppContainer` manual alojado en `MusicaApp` (Application). Si el grafo crece,
se evaluará Hilt.

## Organización de paquetes (real, tras Fase 9)

```
com.musica.player
├── navigation/          → rutas (TopLevelDestination, Routes) y MusicaApp
├── ui/
│   ├── home/            → pantalla Inicio + ViewModel
│   ├── songs/           → pantalla Canciones + ViewModel
│   ├── albums/          → pantalla Álbumes + ViewModel
│   ├── artists/         → pantalla Artistas + ViewModel
│   ├── playlists/       → pantallas Playlists, detalle y añadir canciones + ViewModels
│   ├── player/          → mini reproductor, pantalla Reproduciendo, ViewModel
│   ├── permission/      → puerta de permisos de audio
│   ├── settings/        → pantalla de ajustes (apariencia y carátulas
│   │                       online) + ViewModel
│   ├── components/      → EmptyState, estados de carga/error, filas,
│   │                       Scaffold de pantalla y LibraryListViewModel
│   └── theme/           → Material 3
├── domain/library/      → modelos Song/Album/Artist/Artwork y LibraryRepository
├── domain/playback/     → PlaybackState, RepeatMode y PlayerController
├── domain/playlist/     → modelo Playlist, PlaylistRepository y resolución
│   │                       de canciones (función pura)
├── domain/settings/     → SettingsRepository y ThemeMode (preferencias internas)
├── data/medialibrary/   → MediaStoreDataSource, agregación, repositorio
├── data/playlist/       → Room: entidades, DAO, base de datos y repositorio
├── playback/            → PlaybackService (MediaSession), adaptador del
│   │                       Player, mapeo Song↔MediaItem y fábrica de ExoPlayer
├── di/                  → AppContainer y fábrica de ViewModels (DI manual)
├── artwork/             → extracción y caché en disco de carátulas locales,
│   │                       componentes de Coil (Fase 6)
│   └── online/          → búsqueda de carátulas en Internet (Deezer), caché
│                           online y resolución (Fase 7)
└── data/settings/       → SettingsRepositoryImpl (SharedPreferences; sin deps)
```

> Nota: la estructura crece según las fases; no se crean clases vacías "para
> que existan". Los paquetes se materializan con su primer tipo con
> comportamiento. Se mantiene el flujo UI → ViewModel → Repository → Data
> Source → MediaStore/Room/Media3.

## Estado por fase

### Fase 1 (completada)

- Molde Gradle con versiones estables verificadas.
- `MusicaTheme` (Material 3, claro/oscuro siguiendo el sistema, color
  dinámico en Android 12+).
- Navegación inferior con 5 destinos raíz (`TopLevelDestination`).
- Tests: unit test de rutas + smoke test instrumentado.

### Fase 2 (completada)

- Permisos de lectura de audio: `READ_MEDIA_AUDIO` (API 33+) y
  `READ_EXTERNAL_STORAGE` con `maxSdkVersion=32` (API 26-32). Puerta de
  permiso con explicación, botón de reintento y acceso a ajustes si se
  bloquea permanentemente.
- Escaneo de `MediaStore`: una consulta única de canciones + mapa de géneros
  en un pase aparte. URI de carátula de álbum cuando existe.
- Modelos `Song`, `Album` y `Artist`; álbumes y artistas derivados de las
  canciones (funciones puras testeadas).
- `LibraryRepository` con caché en memoria (`StateFlow`), **carga perezosa**
  (no escanea al construirse, para no leer sin permiso), auto-actualización por
  observer de MediaStore y `refresh()` explícito. Los ViewModels disparan el
  primer escaneo; así se ejecuta solo tras conceder el permiso de audio.
- Pantallas pobladas: canciones (lista), álbumes (cuadrícula), artistas
  (lista con avatar de inicial), inicio (estadísticas + recientes). Estados
  Loading / listo / error reales.
- DI manual: `AppContainer` en `MusicaApp` + `AppViewModelProvider`.
- `LibraryListViewModel<T>`: base de las pantallas de lista. El estado se
  **deriva** siempre de los flujos del repositorio (`combine`), nunca se fija
  a mano. Esto evita una carrera real detectada en el emulador: si el
  re-escaneo se lanzaba tras el primer `Ready` y el repositorio devolvía los
  mismos valores, el `StateFlow` no re-emitía y la pantalla se quedaba
  atascada en "cargando".
- Tests: agregación de álbumes/artistas, formatos de duración y nombre,
  máquina de estados de `LibraryListViewModel` (incluye la regresión anterior)
  y smoke tests instrumentados.

### Fase 3 (completada)

- Reproducción con Media3 `ExoPlayer` (1.11.1). El reproductor se construye en
  el hilo principal (hilo de aplicación de `ExoPlayer`) y maneja foco de audio
  (`AudioAttributes` de música) y la salida de auriculares
  (`setHandleAudioBecomingNoisy`).
- `domain/playback/`: `PlaybackState` (canción actual, cola, posición,
  duración, aleatorio y repetición), `RepeatMode` (con `next()` puro y
  testeado) y la interfaz `PlayerController`.
- `playback/Media3PlayerController`: implementación sobre `ExoPlayer`. Traduce
  el índice del reproductor al `Song` de la cola en memoria, publica el estado
  como `StateFlow` y sondea la posición cada 500 ms mientras suena (Media3 no
  emite eventos al avanzar). Control de "anterior" con umbral de reinicio.
- `ui/player/`: `PlayerViewModel` (varias instancias comparten el mismo
  controlador singleton), `MiniPlayer` (sobre la barra inferior, con progreso,
  título/artista, reproducir/pausar, siguiente; se pulsa para abrir la pantalla
  completa) y `NowPlayingScreen` (carátula provisional, datos de la canción,
  barra de progreso con salto y controles aleatorio/anterior/play/siguiente/
  repetir). El botón "repetir" cicla OFF → ALL → ONE y cambia de icono.
- Se pulsa una canción en Canciones o en Recientes para reproducir la lista
  como cola desde esa posición. La pantalla Reproduciendo oculta la barra
  inferior; la carátula real llega en la Fase 6 (ahora un marcador).
- Ciclo de vida: el controlador vive a nivel de proceso (no se libera al cerrar
  la Activity, para no cortar la música al rotar). La Fase 4 lo moverá a un
  servicio con `MediaSession` y ahí gestionará su liberación.
- Tests: `RepeatModeTest` (unitario) y `Media3PlayerControllerTest`
  (instrumentado: reproduce un WAV generado en la caché del dispositivo y
  comprueba estado, avance de cola y ciclo de repetición).

### Fase 4 (completada)

- Reproducción en segundo plano con `PlaybackService : MediaSessionService`
  (media3-session 1.11.1). El servicio es dueño del `ExoPlayer` y de la
  `MediaSession`; vive en primer plano mientras suena música, con
  `foregroundServiceType="mediaPlayback"` y los permisos
  `FOREGROUND_SERVICE`/`FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
- La app ya no crea el reproductor. `Media3PlayerController` es ahora un
  adaptador de cualquier `Player` de Media3: en el servicio se usaría un
  `ExoPlayer`, y en la app se le entrega un `MediaController` que conecta con
  la sesión (creado en `AppContainer`). Ya no posee ni libera el reproductor.
- La **cola del estado se reconstruye** desde los elementos del reproductor, de
  modo que refleja siempre la lista real de la sesión (por ejemplo, al
  reconectar). `Song↔MediaItem` se mapean en `playback/MediaItems.kt`: los
  metadatos (título, artista, álbum) alimentan la notificación y la pantalla de
  bloqueo; los campos que no hacen falta para reproducir no viajan.
- Notificación multimedia con canal propio (`playback`) y controles de
  anterior/pausa/siguiente; al pulsarla se abre la app. Se pide
  `POST_NOTIFICATIONS` (API 33+) junto al permiso de audio (su denegación no
  bloquea la app). Los botones multimedia (Bluetooth, auriculares) los atiende
  la propia `MediaSession`.
- El manifiesto declara el servicio como exportado (según documenta Media3) para
  que el sistema y otros controladores puedan conectarse; el chequeo de lint
  `ExportedService` se silencia con justificación.

### Fase 5 (completada)

- Persistencia con Room 2.8.5 (KSP 2.3.12). Base de datos `musica.db` con
  `exportSchema = true`; el esquema se exporta a `app/schemas` para poder
  escribir migraciones en fases futuras.
- Modelo de datos mínimo: `PlaylistEntity` (id autogenerado, nombre, fecha de
  creación) y `PlaylistSongEntity` con **clave primaria compuesta**
  `(playlistId, songId)`, `position` para el orden y clave foránea a la playlist
  con `ON DELETE CASCADE`. La PK compuesta evita canciones duplicadas y el
  borrado de la playlist limpia sus filas sin código extra.
- **La playlist guarda solo el `songId`** (identificador de MediaStore), no los
  metadatos. Las canciones se resuelven contra la biblioteca en memoria con
  `resolvePlaylistSongs` (función pura y testeada); si una canción se borra del
  dispositivo, su entrada desaparece sola de la playlist. Así no se duplica ni
  queda obsoleta la información.
- `domain/playlist/PlaylistRepository` expone flujos de solo lectura
  (`observePlaylists`, `observeName`, `observeSongIds`) y escrituras `suspend`
  (`create`, `rename`, `delete`, `addSongs`, `removeSong`). El listado usa una
  proyección `PlaylistWithCount` con el conteo por subconsulta SQL.
- Interfaz: la pestaña **Playlists** lista las listas (orden por nombre sin
  distinguir mayúsculas) con su número de canciones y un FAB para crear. El
  **detalle** muestra las canciones, permite reproducirlas (la playlist se usa
  como cola desde la canción pulsada), quitarlas, renombrar, eliminar (con
  confirmación) y un FAB para añadir canciones. La pantalla **Añadir canciones**
  marca con casillas las que se van a añadir y señala las que ya están.
- El estado de la UI se **deriva** de los flujos (`combine` de Room y la
  biblioteca), igual que en fases anteriores; no se fija a mano.
- Los ViewModels de detalle y de añadir canciones dependen del identificador de
  la playlist, que se entrega por una **fábrica con el id capturado**
  (`playlistDetailFactory(id)` / `addSongsFactory(id)`). Las rutas usan
  `navArgument` de tipo `LongType`.
- **Corrección de un bug latente**: `MediaPermissionGate` ignoraba el `modifier`
  (que lleva el padding de la barra inferior) cuando el permiso estaba
  concedido, por lo que el contenido se dibujaba por debajo de la barra de
  navegación y los FAB quedaban ocultos e inaccesibles. Ahora el contenido se
  inserta correctamente.
- Tests: `PlaylistSongsTest` (resolución/orden), `PlaylistDetailViewModelTest` y
  `AddSongsViewModelTest` (con dobles de prueba), `PlaylistRepositoryTest`
  (Room en memoria: CRUD, orden, duplicados y cascada) y el test de humo de la
  app extendido para crear una playlist de punta a punta.

### Fase 6 (completada)

- Carátulas locales con **Coil 3.6.3** (`coil-compose`). El `ImageLoader`
  singleton se configura en `MusicaApp` implementando
  `SingletonImageLoader.Factory`, así que cualquier `AsyncImage` de la app usa
  estos componentes sin pasar el cargador a mano.
- `domain/library/Artwork`: referencia la **URI del audio**, no un archivo de
  imagen, porque la carátula va embebida en el propio tema. `Song` no almacena
  la carátula (se deriva de su `contentUri`) y `Album.artwork` toma el primer
  tema del álbum, que en un álbum real comparte la misma carátula. Se elimina
  el antiguo `coverArtUri` (proveedor `albumart` de MediaStore, obsoleto) y el
  código muerto `MediaUris`.
- `artwork/ArtworkFetcher`: `Fetcher` de Coil que extrae la carátula embebida con
  `MediaMetadataRetriever.getEmbeddedPicture()` (una sola ruta, API 26+). Si el
  archivo no tiene carátula (o no se puede leer) devuelve null y la UI muestra
  el marcador. Un `Keyer` propio aporta la clave de la caché en memoria.
- `artwork/ArtworkCache`: **caché en disco propia** (`cache/artwork/<sha256>.img`).
  La caché en disco de Coil 3 está pensada para imágenes descargadas de Internet,
  no para orígenes locales, así que la extracción se guarda aparte y se reutiliza
  entre arranques. La escritura es atómica (temporal + rename) y el directorio de
  caché lo puede purgar Android bajo presión.
- `ui/components/ArtworkImage`: composable compartido que dibuja el icono de
  marcador como capa de fondo y encima el `AsyncImage` recortado; mientras carga
  o si falla la extracción, queda a la vista el marcador. Se usa en `SongRow`
  (miniatura de 48 dp en las listas), `MiniPlayer`, `NowPlayingScreen` (cuadrado)
  y `AlbumsScreen`. Desaparece el placeholder `PlayerArtwork`.
- Tests: `ArtworkKeyerTest` y `ArtworkCacheTest` (unitarios) y
  `ArtworkFetcherTest` (instrumentado: genera en el dispositivo un MP3 con una
  carátula PNG embebida y comprueba la extracción, la reutilización desde la
  caché en disco y el caso sin carátula).

### Fase 7 (completada)

- Carátulas online con la API pública de **Deezer** (`api.deezer.com/search/album`),
  sin autenticación. **Sin dependencias nuevas**: se usa `HttpURLConnection` y
  `org.json`, que ya forman parte de Android. Se añaden los permisos `INTERNET` y
  `ACCESS_NETWORK_STATE`.
- Se busca **por álbum** (texto libre "artista álbum") y, entre los resultados,
  se elige el que mejor coincide: el nombre del álbum debe aparecer y, si hay
  artista, su nombre debe solaparse; los nombres exactos puntúan más que una
  simple contención, de modo que "Album" gana a "Album (Deluxe)". Así se evita
  mostrar una carátula equivocada cuando el nombre es ambiguo; si no hay
  coincidencia no se inventa nada. Se descarga `cover_xl` (1000×1000).
- `artwork/online/`, manteniendo el modo offline-first y sin bloquear la UI:
  - `HttpClient` + `UrlConnectionHttpClient`: cliente mínimo e inyectable (con
    timeouts de conexión/lectura), para poder probar sin tocar la red.
  - `OnlineArtworkSource` (interfaz) + `DeezerArtworkSource`: la fuente se
    abstrae para simularla en tests y cambiar de proveedor sin tocar el resto.
    El resultado es `Found` / `NotFound` / `Error`.
  - `OnlineArtworkCache`: caché en disco con **memoria de los "no encontrado"**
    (`<sha256>.img` / `<sha256>.none`), para no repetir la búsqueda en cada
    arranque. Comparte con `ArtworkCache` las utilidades `sha256Hex` y
    `writeAtomically` (`artwork/ArtworkFiles.kt`).
  - `OnlineArtworkResolver`: orden caché → conectividad → fuente. **Si no hay red
    no consulta ni cachea** (se reintentará más tarde); un error del servicio
    tampoco se cachea; solo se guarda el "no encontrado" cuando la búsqueda se
    completó de verdad.
  - `NetworkMonitor` + `ConnectivityNetworkMonitor`: evita esperar a un timeout
    cuando el dispositivo está sin conexión.
- `Artwork` gana `album` y `artist` (opcionales) para poder buscar; se rellenan
  desde `Song`/`Album` en `LibraryAggregator` y en las pantallas (`SongRow`,
  `MiniPlayer`, `NowPlayingScreen`). Se descartan las etiquetas vacías y el
  marcador `<unknown>` de MediaStore, y se **exige artista** además de álbum:
  buscar solo por un álbum genérico (p. ej. una carpeta llamada "Music")
  devolvería arte de otro artista.
- `ArtworkFetcher` encadena ambas vías: carátula embebida (Fase 6) y, si falta,
  online (Fase 7). Si no se encuentra por ninguna, sigue viéndose el marcador.
- **Ajuste interno (Fase 8 adelantada)**: `domain/settings/SettingsRepository` +
  `data/settings/SettingsRepositoryImpl` sobre `SharedPreferences` (sin
  dependencias nuevas) con la opción **"Buscar carátulas online", desactivada
  por defecto**. `OnlineArtworkResolver` comprueba el ajuste antes de tocar la
  red y `ArtworkKeyer` lo incluye en la clave de memoria de Coil, de modo que al
  activarlo o desactivarlo las carátulas se resuelven al instante (sin
  reiniciar). Se abre desde el icono de la barra superior de Inicio
  (`ui/settings/SettingsScreen`, un interruptor). Con la opción apagada no se
  descarga nada ni se lee la caché online: se ve el marcador.
- Tests: `DeezerArtworkSourceTest` (cliente falso con JSON enlatado: elección del
  mejor resultado, sin resultados, error del servicio y fallo de la imagen),
  `OnlineArtworkCacheTest` y `OnlineArtworkResolverTest` (caché, sin conexión,
  error no cacheado, etiquetas desconocidas y opción desactivada). `org.json`
  pasa a ser `testImplementation` porque en los tests JVM el stub de Android no
  sirve. `ArtworkFetcherTest` (instrumentado) añade la ruta online con una fuente
  falsa, y `SettingsRepositoryImplTest` (instrumentado) comprueba el valor por
  defecto y la persistencia.

### Fase 8 (completada)

- **Personalización**: pantalla de Ajustes ampliada (se abre con el icono de
  engranaje de la barra superior de Inicio) organizada en secciones.
  - **Apariencia**:
    - **Tema** (Sistema / Claro / Oscuro) mediante un diálogo de opciones. El
      valor por defecto es **Sistema** (el comportamiento anterior).
    - **Color dinámico** (Android 12+): interruptor que activa o desactiva los
      colores de Material You del fondo de pantalla. Solo se muestra donde el
      sistema lo soporta; por defecto **activado** (el comportamiento anterior).
  - **Biblioteca**: "Buscar carátulas online" (de la Fase 7), **desactivado por
    defecto**.
- Las preferencias se guardan con `SharedPreferences` (sin dependencias nuevas)
  detrás de `SettingsRepository`, que ahora expone tres `StateFlow`: carátulas
  online, tema y color dinámico. `ThemeMode` es un enum del dominio con
  `storageKey` y `fromStorage(value)` (los valores desconocidos vuelven a
  `Sistema`), de modo que el mapeo es puro y testeable; el texto de cada modo
  vive en la UI, que es la única que conoce recursos.
- `MainActivity` actúa como raíz de composición: recolecta `themeMode` y
  `dynamicColorEnabled` del repositorio y los aplica a `MusicaTheme`
  (`darkTheme` + `dynamicColor`). Como son `StateFlow`, el cambio se ve **al
  instante**, sin reiniciar. La traducción `ThemeMode → oscuro` es una función
  `@Composable` que consulta `isSystemInDarkTheme()` para el modo Sistema.
- Se mantiene la **regla de dependencias**: el dominio (`ThemeMode`) no conoce
  recursos Android; la etiqueta de cada modo se resuelve en la UI
  (`ThemeMode.labelRes()`).
- Tests: `ThemeModeTest` (unitario: valor por defecto, lectura de los valores
  guardados, valor desconocido y round-trip) y `SettingsRepositoryImplTest`
  (instrumentado) ampliado con los valores por defecto de tema/color dinámico,
  su persistencia y el saneamiento de un valor corrupto. Los dobles de test
  (`FakeSettings` en `OnlineArtworkResolverTest` y `ArtworkFetcherTest`) se
  actualizan a la nueva interfaz.

### Verificación (emulador Medium Phone API 36.1)

- `assembleDebug`, `testDebugUnitTest`, `lintDebug` y
  `assembleDebugAndroidTest` correctos. Lint: 0 errores y solo el aviso
  aceptado de versión de Gradle.
- Unit tests: 53/53. Tests instrumentados (`connectedDebugAndroidTest`): 21/21.
- Comprobación manual de punta a punta con audio real: se subieron WAV a
  `/sdcard/Music`, se forzó el escaneo y la app pobló Inicio (estadísticas +
  recientes), Canciones, Álbumes y Artistas.
- Flujo de permiso verificado de verdad en el emulador: se revocó el permiso,
  se lanzó la app (pantalla de explicación), se pulsó "Allow access" y se
  concedió en el diálogo del sistema; la biblioteca se cargó automáticamente
  después. Bug corregido: antes la biblioteca quedaba vacía porque el escaneo
  ocurría al arrancar sin permiso y no se repetía al concederlo.
- Reproducción verificada en el emulador: al pulsar una canción aparece el
  mini reproductor con su título/artista y el botón Pausar; `dumpsys audio`
  confirma que la app solicita el foco (`USAGE_MEDIA`/`CONTENT_TYPE_MUSIC`) y
  que hay un `AudioTrack` activo en `PlaybackActivityMonitor` (suena de
  verdad). Se comprobó además que la cola avanza sola, que "siguiente"
  funciona, que play/pausar reanuda (incluso tras terminar la cola) y que el
  modo de repetición cicla OFF → ALL → ONE.
- Segundo plano verificado: con música sonando se pulsó Inicio; `dumpsys
  media_session` muestra la sesión de `com.musica.player` en estado `PLAYING`
  y `dumpsys notification` muestra la notificación del canal **playback** con
  el título/artista y las acciones anterior/pausar/siguiente. Se enviaron
  `KEYCODE_MEDIA_PLAY_PAUSE` (85) y `KEYCODE_MEDIA_NEXT` (87) por `adb` (como
  haría un dispositivo Bluetooth) y el estado de la sesión cambió en
  consecuencia. Al volver la app a primer plano, el mini reproductor refleja la
  reproducción en curso.
- Los tests instrumentados conceden el permiso en `@BeforeClass` (antes de que
  la regla lance la Activity) y resuelven sus textos desde los recursos de la
  app, por lo que funcionan en cualquier idioma del dispositivo.
- Playlists verificadas de punta a punta en el emulador con `adb`: se creó una
  playlist desde el FAB, se abrió su detalle, se añadieron canciones con
  casillas, se reprodujo desde la playlist (aparece el mini reproductor y la
  sesión de Media3 responde), se renombró (diálogo) y se eliminó con la
  confirmación, volviendo a la lista vacía. La base de datos persiste entre
  reinicios de la app.
- Carátulas verificadas en el emulador: se generó un MP3 con una carátula PNG
  embebida y el análisis de píxeles del `screencap` confirma que la miniatura de
  la lista muestra la imagen (colores saturados), mientras las filas de WAV sin
  carátula quedan en el marcador gris. `run-as com.musica.player ls cache/artwork`
  muestra el archivo extraído, y al relanzar la app no se vuelve a extraer.
- Ajuste de carátulas online verificado en el emulador con audio real
  (`online_test.mp3`, sin carátula): recién instalada (valor por defecto) no se
  crea `cache/artwork_online` y la tarjeta del álbum muestra el marcador; al
  activar el interruptor en Ajustes, la tarjeta pasa a la carátula de Deezer
  (el 93,2% de los píxeles de su región cambian) y aparece
  `cache/artwork_online/<sha256>.img`; al desactivarlo vuelve al marcador (0%
  de cambio). La tarjeta del álbum "Music" (artista `<unknown>`) no se modifica
  en ningún caso (0%).
- Ajustes de la Fase 8 verificados en el emulador: la pantalla muestra la
  sección **Apariencia** (Tema = "System", Color dinámico activado) y la sección
  **Biblioteca** (carátulas online desactivadas). Al elegir "Oscuro" el brillo
  medio del `screencap` cae a ~19; al elegir "Claro" sube a ~244 (medido con
  Pillow), y el valor queda escrito en `shared_prefs/musica_settings.xml`. Tras
  `am force-stop` y relanzar, el tema se restaura en claro y los interruptores
  conservan su estado (color dinámico desactivado, carátulas online activadas);
  con estas activadas se descarga la carátula de Deezer de "Discovery"
  (`cache/artwork_online/<sha256>.img`).

### Fase 9 (completada)

- **Accesibilidad**: se añaden descripciones de contenido adecuadas a las
  carátulas (`ArtworkImage`) en las listas de canciones, la pantalla del
  mini reproductor, la pantalla de reproducción completa y las tarjetas de
  álbumes, mejorando la experiencia con lectores de pantalla. Los iconos
  decorativos mantienen `contentDescription = null`.
- **Robustez y consistencia de UI**: `ErrorState` acepta un icono personalizable
  y mantiene el comportamiento existente.
- **Cobertura de tests**: se conservan los 53 tests unitarios y 21 tests
  instrumentados, todos en verde. Lint sin errores.

### Próxima fase (10)

- Release: preparación de una build firmada de release, limpieza final,
  comprobación de minificación/reestricción de recursos, verificación de
  permisos en release y checklist de publicación (Play Store o APK distribuible).