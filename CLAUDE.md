# Desafío Sports · guía para agentes de IA

App Android (Kotlin + Jetpack Compose) que consume BackendCodeCup (REST, puerto 8082) y Supabase Auth.

**Antes de tocar código, lee:**
- [`docs/arquitectura.md`](docs/arquitectura.md): stack, capas, convenciones y errores;
- [`docs/sdd.md`](docs/sdd.md): cómo se trabaja.

## Reglas

- **Todo cambio nace de una spec** en `docs/specs/` (`MOV-NNNN`) y trae tests.
  - Los nombres de los tests empiezan por el criterio que cubren: ``fun `CA-3 …`()``.
- **Capas:**
  - `domain` es Kotlin puro: sin Android, Retrofit ni Supabase;
  - `data` implementa las interfaces;
  - `presentacion` llama solo a casos de uso;
  - `di/AppContainer.kt` conecta todo.
- **Siempre ViewModel → caso de uso → repositorio.**
  - El estado va en `var state by mutableStateOf(XxxState()) private set` y se cambia con `copy()`.
  - El ViewModel se pide con `viewModel(factory = XxxViewModel.Factory)`.
- **Errores:** `Result<T>` cuyo fallo es un `AppError`. Los del backend se deciden por `code` y se muestra su `mensaje`.
- **Pantallas:**
  - `XxxScreen` (con estado) más `XxxContenido` (sin estado, con `@Preview` claro y oscuro);
  - textos en `strings.xml` y colores desde `MaterialTheme`;
  - `testTag` en los controles.
- **Versiones:** no subas librerías sin una spec propia. Kotlin 2.2 solo lee librerías compiladas con Kotlin 2.3 o anterior, y `compileSdk` es 36. Ver `docs/arquitectura.md` §2.
- **Nunca:**
  - Firebase, Hilt ni vistas XML;
  - claves en el código ni `sb_secret_…` en la app;
  - acceso directo a la base de datos;
  - borrar pantallas o comportamientos fuera del alcance de la spec.
- **Antes de dar algo por terminado:**
  - `./gradlew testDebugUnitTest assembleDebug` en verde;
  - si hay pantalla, capturas del emulador (`adb exec-out screencap -p`) en `docs/specs/evidencias/MOV-NNNN/`.
- **Diseño:** skill Impeccable sobre el Code Cup Design System, siguiendo [`docs/plan-ui-impeccable.md`](docs/plan-ui-impeccable.md). En Android manda Material 3 y la verificación es en el emulador, no en el navegador.
- **Idioma:** español en la interfaz, en la documentación y en los nombres del dominio, tuteando.
