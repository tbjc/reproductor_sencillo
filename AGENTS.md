# AGENTS.md

## Proyecto

Nombre: Musica

Tipo: Android Music Player

Descripción:

Musica es un reproductor de música local para Android, gratuito y sin publicidad.

La aplicación reproduce archivos de música almacenados en el dispositivo y proporciona herramientas para organizar la biblioteca, crear playlists, administrar carátulas y personalizar la experiencia.

---

# 1. REGLAS FUNDAMENTALES

Estas reglas tienen prioridad sobre preferencias menores de implementación.

### Regla 1

La aplicación debe ser Android nativa.

### Regla 2

El lenguaje principal es Kotlin.

### Regla 3

La UI debe utilizar Jetpack Compose + Material 3.

### Regla 4

La reproducción debe utilizar Android Media3.

### Regla 5

La persistencia local debe utilizar Room/SQLite.

### Regla 6

La aplicación debe ser offline-first.

### Regla 7

No debe existir publicidad.

### Regla 8

No debe ser necesaria una cuenta para utilizar las funciones principales.

### Regla 9

No recopilar datos innecesarios.

### Regla 10

No introducir complejidad arquitectónica sin una razón clara.

---

# 2. STACK

Preferencias:

- Kotlin
- AndroidX
- Jetpack Compose
- Material 3
- Navigation Compose
- Coroutines
- Flow
- StateFlow
- Room
- Media3
- MediaSession
- WorkManager cuando sea necesario
- Coil cuando sea apropiado

Utilizar versiones estables y compatibles.

No añadir librerías innecesarias.

---

# 3. ARQUITECTURA

Preferir:

```text
UI
 ↓
ViewModel
 ↓
Use Cases
 ↓
Repository
 ↓
Data Source
 ↓
MediaStore / Room / Media3 / Network