# Arquitectura de la app móvil · Desafío Sports

> **Estado:** base establecida con la spec [MOV-0001](specs/MOV-0001-estructura-base.md) (8 nov 2026).
> **Qué es este documento:** la fuente de verdad de *cómo* se construye la app. Si el código contradice algo de aquí, se corrige uno de los dos en la misma spec, nunca se deja la diferencia.
> **Origen:** sigue la estructura que recomienda el profesor de la materia (proyecto de clase *StreamFlix*). La diferencia es que Firebase se cambia por Supabase Auth y por BackendCodeCup. El detalle está en el §12.

---

## 1. Qué es la app y con qué habla

Desafío Sports es la app Android multideporte de los torneos universitarios. Empieza solo con fútbol sala.

**Tres modos sobre la misma cuenta:**
- **Jugador**;
- **Árbitro**;
- **Creador**: administra solo los torneos que crea. No hay administradores globales.

Además está el **Visitante**, que entra sin sesión.

| Necesidad | Quién la resuelve | Cómo |
|---|---|---|
| Sesión: correo y contraseña, Google, recuperar contraseña | **Supabase Auth** (proyecto Supabase #1) | `supabase-kt`, solo su módulo de Auth |
| Datos: torneos, equipos, partidos, multas… | **BackendCodeCup** (Spring Boot, puerto 8082) | REST con Retrofit. Cada petición lleva `Authorization: Bearer <JWT de Supabase>` |
| Archivos (comprobantes de pago) | Supabase Storage, a través del backend | El backend firma una URL y la app sube el archivo con `PUT` |

**Reglas que no se negocian:**
1. **La app nunca lee ni escribe la base de datos directamente.** Todo pasa por el backend, que es donde viven las reglas: roles por torneo, multas, aprobaciones y bloqueos. La web funciona igual. Si la app se lo saltara, habría que duplicar esas reglas y quedarían huecos de seguridad.
2. **Solo la clave publicable** (`sb_publishable_…`) entra a la app, vía `local.properties` → `BuildConfig`. La clave secreta (`sb_secret_…`) nunca.
3. **La web (CodeCup) y esta app comparten el backend.** Lo que se pruebe bien aquí se lleva después a la web: el móvil manda.

---

## 2. Stack y versiones

| Pieza | Elección | Versión |
|---|---|---|
| Lenguaje | Kotlin (solo Kotlin, sin Java) | 2.2.10 |
| Build | Android Gradle Plugin + Gradle (wrapper), catálogo `gradle/libs.versions.toml` | AGP 9.1.0 · Gradle 9.3.1 · JDK 21 (toolchain del daemon) |
| SDK | `minSdk` 26 · `targetSdk` 36 · `compileSdk` 36.1 | — |
| UI | Jetpack Compose + Material 3 | BOM 2026.06.01 (Compose 1.11 · Material 3 1.4) |
| Navegación | Navigation Compose con rutas tipadas (`@Serializable`) | 2.9.8 |
| Estado y ViewModel | AndroidX Lifecycle | 2.10.0 |
| Sesión | supabase-kt (Auth) con motor Ktor OkHttp | 3.2.6 · Ktor 3.3.3 |
| API REST | Retrofit + OkHttp + kotlinx.serialization | Retrofit 3.0.0 · OkHttp 5.3.2 · serialization 1.9.0 |
| Corrutinas | kotlinx.coroutines | 1.10.2 |
| Tests | JUnit 4 · kotlinx-coroutines-test · MockWebServer | 4.13.2 · 1.10.2 · 5.3.2 |

**No se usan:** Firebase, Hilt, Room, AppCompat, vistas XML ni viewBinding. La lista de lo que entrará y en qué spec está en el §11.

### 2.1 Por qué estas versiones y no las últimas

- **Base del profesor.** AGP 9.1.0, Gradle 9.3.1 y Kotlin 2.2.10 son los de StreamFlix. Así el proyecto abre en el Android Studio de la clase (2026.1 o posterior).
- **Kotlin 2.2 solo lee librerías compiladas con Kotlin 2.3 o anterior.** Por eso:
  - supabase-kt queda en 3.2.6, porque la 3.7 o posterior pide Kotlin 2.4;
  - Ktor en 3.3.3, serialization en 1.9.0 y coroutines en 1.10.2.
- **`compileSdk` 36.** Quedan fuera las librerías que ya exigen 37:
  - la BOM de Compose 2026.09 o posterior;
  - `core` 1.19 o posterior;
  - `navigation-compose` 2.10 o posterior;
  - `lifecycle` 2.11 o posterior (sus variantes de Compose).

### 2.2 Cómo actualizar una versión

Una actualización es **su propia spec** (tipo `chore`), nunca un cambio de paso dentro de otra. Antes de subir una librería:
1. Revisa qué `kotlin-stdlib` declara en su POM o en su archivo `.module`.
2. Revisa qué `minCompileSdk` y qué AGP mínimo declara en `aar-metadata.properties` (viene dentro del AAR).
3. Corre toda la suite, la prueba de humo (§10) y abre la app en el emulador.

---

## 3. Capas y paquetes

Se mantiene la estructura de tres capas que recomienda el profesor: un solo módulo `:app`, con paquetes por capa y, dentro de cada capa, por módulo del backend.

```
app/src/main/java/co/edu/ufps/desafiosports/
├── DesafioSportsApp.kt        # Application: crea el AppContainer una vez
├── MainActivity.kt            # única Activity: tema + AppNavigation
├── di/
│   └── AppContainer.kt        # inyección manual: el único lugar que conecta las capas
├── domain/                    # Kotlin puro: sin Android, Retrofit ni Supabase
│   ├── error/AppError.kt      # errores tipados
│   ├── model/                 # Cuenta, Rol, EstadoSesion, Configuracion…
│   ├── repository/            # interfaces: SesionRepository, CuentaRepository…
│   └── use_case/
│       ├── identidad/         # IniciarSesion, CerrarSesion, ObtenerCuenta…
│       ├── sistema/           # ComprobarServidor
│       ├── competicion/       # (próximas specs) torneos, equipos, partidos
│       └── finanzas/          # (próximas specs) multas, comprobantes
├── data/
│   ├── auth/                  # supabase-kt: ProveedorTokenSupabase, ErroresSupabase
│   ├── remote/
│   │   ├── api/               # interfaces Retrofit por módulo del backend: AuthApi, SistemaApi…
│   │   ├── dto/               # @Serializable, idénticos al JSON del backend
│   │   └── red/               # ClienteHttp, interceptores, TokenAuthenticator, ErroresHttp, llamarApi
│   └── repository/            # …RepositoryImpl: llaman a la API o a Supabase y convierten DTO → modelo
├── presentacion/
│   ├── navigation/            # Rutas (tipadas) y AppNavigation
│   └── diagnostico/           # pantalla temporal de MOV-0001 (Screen + ViewModel + State)
└── ui/
    └── theme/                 # Color, Type, Shape y Theme con los tokens del Design System
```

**Hacia dónde apuntan las dependencias:**

```
presentacion ───▶ domain ◀─── data
      ▲                          ▲
      └────────── di ────────────┘
```

- `domain` no conoce a nadie: es Kotlin puro y se prueba en la JVM.
- `data` implementa las interfaces de `domain`.
- `presentacion` solo usa casos de uso de `domain`.
- `di` arma todo. Solo la `Factory` de cada ViewModel lo toca.
- **Los DTO no salen de `data`.** El repositorio los convierte a modelos de dominio.

**Por módulo del backend:** `identidad`, `competicion` y `finanzas` llevan el mismo nombre en BackendCodeCup, en `src/api/*.js` de la web y aquí. Un endpoint se busca por el mismo nombre en los tres repos.

---

## 4. Cómo fluye una acción

Ejemplo: iniciar sesión y consultar la cuenta.

```
DiagnosticoScreen ─evento─▶ DiagnosticoViewModel ─▶ IniciarSesionUseCase ─▶ SesionRepository ──▶ supabase-kt (Auth)
        ▲                          │                ObtenerCuentaUseCase ─▶ CuentaRepository ──▶ Retrofit: GET /api/auth/me
        └──── DiagnosticoState ◀───┘                                          (interfaces de domain, implementadas en data)
```

1. La pantalla llama a `viewModel.iniciarSesion()`.
2. El ViewModel invoca el caso de uso, que **valida primero** (correo válido y contraseña de 8 o más caracteres) y después llama al repositorio.
3. El repositorio habla con Supabase y devuelve `Result<Unit>`.
4. El ViewModel actualiza su estado con `copy()` y la pantalla se redibuja sola.
5. Para `/api/auth/me`, el `AuthInterceptor` pone el Bearer. Si llega un 401, `TokenAuthenticator` renueva la sesión y reintenta una vez.

---

## 5. Convenciones

### 5.1 Nombres

| Pieza | Nombre | Ejemplo |
|---|---|---|
| Pantalla con estado | `XxxScreen` | `DiagnosticoScreen` |
| Pantalla sin estado (previews y tests de UI) | `XxxContenido` | `DiagnosticoContenido` |
| Estado de pantalla | `XxxState` (data class inmutable) | `DiagnosticoState` |
| ViewModel | `XxxViewModel`, con `companion object { val Factory }` | `DiagnosticoViewModel` |
| Caso de uso | `VerboSustantivoUseCase`, con `operator fun invoke` | `IniciarSesionUseCase` |
| Repositorio | `XxxRepository` (interfaz en domain) / `XxxRepositoryImpl` (data) | `CuentaRepositoryImpl` |
| API Retrofit | `XxxApi`, una por módulo del backend | `AuthApi` |
| DTO | `XxxDto`, igual al JSON del backend | `MeResponseDto` |
| Test | ``fun `CA-n qué se espera`()``, con el criterio de la spec | `` `CA-8 ante 401 renueva la sesion una vez…` `` |

**Idioma:**
- El dominio va en español (`Cuenta`, `iniciarSesion`, `EstadoSesion`).
- Las piezas técnicas de Android van en inglés (`Screen`, `ViewModel`, `UseCase`, `Repository`), como en el proyecto del profesor.

### 5.2 Reglas de código

1. **ViewModel → caso de uso → repositorio, siempre.** Vale también para las lecturas simples: cuesta poco y deja una sola forma de hacer las cosas.
2. **Estado:** `var state by mutableStateOf(XxxState()) private set` y cambios con `state = state.copy(...)`. Es el patrón de `LoginViewModelCompose` del profesor.
3. **El ViewModel se pide con `viewModel(factory = XxxViewModel.Factory)`.** Nunca se crea a mano en la Activity: en StreamFlix eso hacía que el estado se perdiera al girar el teléfono.
4. **La `Factory` toma las dependencias del `AppContainer`** con `this[APPLICATION_KEY] as DesafioSportsApp`.
5. **Cada pantalla tiene dos funciones:** una con estado (`XxxScreen`) y otra sin estado (`XxxContenido`), con `@Preview` en claro y en oscuro.
6. **Los textos van en `res/values/strings.xml`.** Los colores, la tipografía y las formas salen de `MaterialTheme`. Nunca `Color(0x…)` ni tamaños fijos dentro de una pantalla.
7. **`testTag` en todo control interactivo**, en `snake_case`. La raíz de la pantalla lleva `Modifier.semantics { testTagsAsResourceId = true }`, para poder automatizar con `adb` y uiautomator.
8. **Neutral al deporte.** "Gol", "tarjeta" o "portero" no se escriben como texto fijo de dominio. Los tipos de evento se pintan con un mapeo por código en `ui/deporte/`, que se crea con la primera pantalla de partidos.
9. **Sin datos personales en vistas públicas**, porque hay menores. Se usa el nombre visible "Nombres A.".
10. **Sesión y permisos:** el modo es solo la interfaz; el backend decide los permisos. La app no deduce permisos a partir del nombre de un rol.

---

## 6. Errores

Los errores viajan en `Result<T>`, como en el proyecto del profesor, pero el fallo siempre es un `AppError` tipado. La pantalla decide según el **tipo** y muestra `error.mensaje`.

| Origen | Señal | `AppError` | Qué hace la interfaz |
|---|---|---|---|
| Validación local | correo mal escrito, contraseña corta | `Validacion` | Muestra el mensaje junto al campo |
| Red | sin conexión, servidor apagado, tiempo agotado | `SinConexion` | Aviso más botón de reintentar |
| Supabase | `invalid_credentials` | `CredencialesInvalidas` | "Correo o contraseña incorrectos." |
| Supabase | `email_not_confirmed` | `CorreoNoConfirmado` | Pide confirmar el correo |
| Backend 401 | `NO_AUTENTICADO` | `NoAutenticado` | Llevar al login (la sesión ya se intentó renovar) |
| Backend 404 | `CUENTA_NO_REGISTRADA` | `CuentaNoRegistrada` | Llevar a "Completar registro" |
| Backend 403 | `CUENTA_BLOQUEADA` | `CuentaBloqueada` | La sesión local ya se cerró: pantalla de bloqueo |
| Backend 403 | `ACCESO_DENEGADO` | `AccesoDenegado` | Mensaje, sin cerrar sesión |
| Backend | otro `code` (por ejemplo `DOCUMENTO_REGISTRADO`) | `Negocio(codigo, mensaje)` | Mensaje del backend. Se decide por `codigo` si hace falta |
| Configuración | faltan claves en `local.properties` | `ConfiguracionIncompleta` | El diagnóstico lo avisa |

- El backend responde siempre `application/problem+json` con `code` (estable, para decidir) y `mensaje` (para mostrar). La traducción está en `data/remote/red/ErroresHttp.kt`.
- Los errores de supabase-kt se traducen en `data/auth/ErroresSupabase.kt`.

---

## 7. Red y sesión

El cliente HTTP (`data/remote/red/ClienteHttp.kt`) **se comporta igual que `src/api/http.js` de la web**:

1. **`AuthInterceptor`** pone `Authorization: Bearer <token>` si hay sesión.
2. **`TokenAuthenticator`**, ante un 401 de una petición que llevaba token:
   - renueva la sesión **una sola vez** y reintenta;
   - si no se puede renovar, o si el reintento vuelve a dar 401, cierra la sesión local;
   - si varias peticiones fallan a la vez, solo renueva una de ellas.
3. **`CuentaBloqueadaInterceptor`**: ante un 403 `CUENTA_BLOQUEADA` de cualquier endpoint, cierra la sesión local.
4. **Registro de tráfico** solo en `debug`, a nivel `BASIC` (método, URL y status) y con el `Authorization` oculto.
5. Tiempo máximo de 15 s para conectar y 20 s para leer.

**Sesión:**
- supabase-kt guarda la sesión en el teléfono y la renueva sola antes de que venza.
- El manifiesto lleva `allowBackup="false"` para que los tokens no terminen en respaldos en la nube.

---

## 8. Navegación

- **Una sola Activity** con Navigation Compose y rutas tipadas (`presentacion/navigation/Rutas.kt`).
- **Hoy** solo existe `Diagnostico`, una pantalla temporal.
- **Plan**, que llega con las specs de acceso y modos:

```
Arranque ──▶ Acceso (login, registro, revisa tu correo, completar registro, recuperar, bloqueada)
         └─▶ Elegir modo ──▶ Grafo del modo (Jugador | Árbitro | Creador), cada uno con su barra inferior
                              └─ pantallas compartidas: detalle de torneo, de partido y perfil
```

- El botón Atrás del sistema siempre funciona; nunca se intercepta para atrapar al usuario.

---

## 9. Configuración y entornos

**`local.properties`** (nunca entra a git; la plantilla es `local.properties.ejemplo`):

| Clave | Valor | Por defecto |
|---|---|---|
| `SUPABASE_URL` | `https://<proyecto>.supabase.co` | — (obligatoria) |
| `SUPABASE_PUBLISHABLE_KEY` | `sb_publishable_…` | — (obligatoria) |
| `API_BASE_URL` | URL del backend, termina en `/` | `http://10.0.2.2:8082/` |

- Gradle copia estos valores a `BuildConfig` y `AppContainer` los lee. Si falta alguno, la app abre igual y el diagnóstico avisa qué falta.
- **Emulador:** `10.0.2.2` es el `localhost` del PC.
- **Teléfono por USB:** `adb reverse tcp:8082 tcp:8082` y `API_BASE_URL=http://localhost:8082/`.
- **HTTP sin cifrar** se permite solo en `debug` y solo hacia `10.0.2.2`, `localhost` y `127.0.0.1`. El resto, incluido Supabase, va por HTTPS. Está en `src/debug/res/xml/network_security_config.xml`.
- **Backend:** se levanta desde el Spring Boot Dashboard de VS Code, sin Docker, y queda en el puerto 8082.

---

## 10. Pruebas

| Nivel | Dónde | Herramientas | Para qué |
|---|---|---|---|
| Dominio | `test/…/domain` | JUnit 4 + coroutines-test + *fakes* | Validaciones y reglas de los casos de uso |
| ViewModel | `test/…/presentacion` | `MainDispatcherRule` + *fakes* | Qué estado ve la pantalla en cada caso |
| Red y repositorios | `test/…/data` | MockWebServer | Contrato con el backend, traducción de errores, 401 y 403 |
| Humo (backend real) | `test/…/humo` | Se activa con `DESAFIO_HUMO=1` | Comprobar el entorno de punta a punta: backend, Supabase y `/api/auth/me` |
| UI | `androidTest/` (llega con la primera pantalla real) | Compose UI Test | Flujos críticos |
| Verificación visual | emulador | `adb exec-out screencap` | Capturas para la spec y para Impeccable: claro, oscuro y letra al 130 % |

- Se usan **implementaciones falsas** (`test/…/testutil/Fakes.kt`) en vez de librerías de mocks. Con inyección manual basta con pasar otra implementación.

**Comandos** (en Windows, `gradlew.bat`):

```bash
./gradlew testDebugUnitTest        # toda la suite (la prueba de humo se salta sola)
./gradlew assembleDebug            # compila el APK de debug
./gradlew installDebug             # instala en el emulador o teléfono conectado

# Prueba de humo contra el backend real (backend levantado en :8082):
DESAFIO_HUMO=1 HUMO_CLAVE=<SEED_PASSWORD del .env del backend> \
  ./gradlew testDebugUnitTest --tests "*BackendRealHumoTest*" --rerun
```

---

## 11. Lo que aún no está, y en qué spec entra

| Pieza | Para qué | Entra con |
|---|---|---|
| Credential Manager + `googleid` | Iniciar sesión con Google nativo | Spec de acceso (F0). Antes hay que crear el cliente OAuth Android en GCP |
| DataStore | Guardar el modo elegido y el torneo seleccionado | Spec de elegir modo |
| `core-splashscreen` | Pantalla de arranque mientras se resuelve la sesión | Spec de acceso |
| Coil | Escudos y fotos | Primera pantalla con imágenes |
| Compose UI Test (`androidTest`) | Tests de UI | Primera pantalla real |
| Room | Planilla del árbitro sin conexión | Solo si las pruebas en cancha lo piden (F2) |
| Firebase Cloud Messaging | Notificaciones de multas | Cuando el backend tenga notificaciones. Solo FCM: la sesión sigue en Supabase |

---

## 12. Diferencias con StreamFlix (estructura del profesor)

| Se conserva | Se ajusta | Por qué |
|---|---|---|
| Un módulo `:app` con `domain` / `data` / `presentacion` / `ui` | — | Es la estructura recomendada |
| Casos de uso con la validación adentro | `invocar()` pasa a `operator fun invoke()` | Idiomático en Kotlin: se llama `iniciarSesion(correo, clave)` |
| `XxxState` + `mutableStateOf` + `copy()` | — | Patrón de `LoginViewModelCompose` |
| Inyección manual con `viewModelFactory { initializer { } }` | Centralizada en `AppContainer` | En StreamFlix el ViewModel se creaba en `onCreate` y se perdía al girar |
| `Result<T>` para errores | El fallo es un `AppError` tipado | Decidir por tipo y por `code`, no por texto |
| Credential Manager + `googleid` para Google | Entrega el token a Supabase (`signInWith(IDToken)`), no a Firebase | Supabase reemplaza a Firebase Auth |
| Firestore como fuente de datos | BackendCodeCup por REST | Las reglas viven en el backend |
| — | `android.util.Patterns` sale del dominio | Con él, los tests fallaban fuera del teléfono |
| — | Kotlin 100 %, sin vistas XML | StreamFlix mezclaba Java y XML (restos de clase) |
| — | `minSdk` 26 en vez de 29 | Para que funcione en teléfonos más viejos de estudiantes |
| — | Sin color dinámico (Material You) | Taparía los colores de la marca |
| `google-services.json` dentro del proyecto | Claves en `local.properties`, fuera de git | No subir claves al repositorio |
