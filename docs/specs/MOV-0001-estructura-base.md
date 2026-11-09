---
id: MOV-0001
titulo: Estructura base del proyecto y diagnóstico de conexión
tipo: chore
estado: hecha
prioridad: P0
fase: Base
modo: Transversal
responsable: David
depende_de: [backend GET /api/auth/me, backend GET /actuator/health]
rama: mov-0001-estructura-base
---

# MOV-0001 · Estructura base del proyecto y diagnóstico de conexión

## 1. Contexto

La app Android de Desafío Sports arranca desde cero. Antes de diseñar pantallas hace falta una base que el equipo pueda usar con confianza:
- la estructura: Clean Architecture con `domain` / `data` / `presentacion`, MVVM e inyección manual;
- en lugar de Firebase, **Supabase Auth** para la sesión y **BackendCodeCup** por REST para los datos;
- un **corte vertical que funcione de verdad**: iniciar sesión y consultar un endpoint protegido, para saber que el entorno de cada persona está bien.

Las pantallas reales **no** son parte de esta spec: se diseñan con Impeccable ([`plan-ui-impeccable.md`](../plan-ui-impeccable.md)).

## 2. Alcance

- Proyecto Gradle: wrapper 9.3.1, catálogo de versiones, módulo `:app`, `local.properties` → `BuildConfig`.
- Paquetes `domain`, `data`, `presentacion`, `ui` y `di` con sus reglas ([`arquitectura.md`](../arquitectura.md) §3).
- Red hacia el backend:
  - Retrofit + OkHttp;
  - Bearer, reintento único ante 401 y cierre de sesión ante 403 `CUENTA_BLOQUEADA`;
  - errores leídos por `code`.
- Sesión con supabase-kt (correo y contraseña).
- Tema provisional con los tokens del Design System y un ícono provisional.
- **Pantalla temporal "Diagnóstico de conexión"** (sin diseño):
  - comprueba `GET /actuator/health`;
  - inicia sesión;
  - consulta `GET /api/auth/me` y muestra la cuenta.
- Tests en la JVM, una prueba de humo opcional contra el backend real y la documentación (`docs/`).

## 3. Diseño

Sin diseño: es una pantalla técnica y temporal. Usa Material 3 por defecto con el tema provisional, textos en `strings.xml` y `testTag` en cada control.

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde | Errores esperados → AppError |
|---|---|---|---|---|
| `/actuator/health` | GET | — (público) | `{"status":"UP"}` | sin red → `SinConexion` |
| `/api/auth/me` | GET | `Authorization: Bearer <JWT de Supabase>` | `MeResponseDto`: nombres, apellidos, nombre, correo, estado, roles y roles pendientes | 401 `NO_AUTENTICADO` → `NoAutenticado` · 404 `CUENTA_NO_REGISTRADA` → `CuentaNoRegistrada` · 403 `CUENTA_BLOQUEADA` → `CuentaBloqueada` |
| Supabase Auth `signInWithPassword` | — | correo y contraseña | sesión | `invalid_credentials` → `CredencialesInvalidas` · `email_not_confirmed` → `CorreoNoConfirmado` |

## 5. Criterios de aceptación

1. **CA-1** · **Dado** el proyecto recién clonado con `local.properties` · **cuando** se corre `assembleDebug` · **entonces** compila con la base (AGP 9.1.0, Gradle 9.3.1, Kotlin 2.2.10), sin Firebase.
2. **CA-2** · **Dado** el código · **cuando** se revisan los paquetes · **entonces** sigue `domain` / `data` / `presentacion` / `ui` / `di`, y `domain` no depende de Android, Retrofit ni Supabase.
3. **CA-3** · **Dado** que faltan claves en `local.properties` · **cuando** se abre la app · **entonces** no se cierra: el diagnóstico dice qué falta y no intenta usar la sesión.
4. **CA-4** · **Dado** el backend encendido (o apagado) · **cuando** se abre el diagnóstico · **entonces** muestra "En línea (UP)" (o el mensaje de sin conexión) y se puede reintentar.
5. **CA-5** · **Dado** un correo mal escrito o una contraseña de menos de 8 caracteres · **cuando** se intenta iniciar sesión · **entonces** falla **sin** llamar a Supabase. Si Supabase rechaza las credenciales, el error llega tipado y con un mensaje claro.
6. **CA-6** · **Dado** un usuario del seed · **cuando** inicia sesión · **entonces** la app consulta `GET /api/auth/me` y muestra nombre, correo, estado y roles.
7. **CA-7** · **Dado** que hay sesión · **cuando** la app llama al backend · **entonces** cada petición lleva `Authorization: Bearer <token>`. Sin sesión no se envía.
8. **CA-8** · **Dado** un 401 en una petición con token · **cuando** llega · **entonces** se renueva la sesión **una vez** y se reintenta. Si no se puede renovar, o el reintento vuelve a dar 401, se cierra la sesión local, sin bucles.
9. **CA-9** · **Dado** un 403 `CUENTA_BLOQUEADA` en cualquier endpoint · **cuando** llega · **entonces** se cierra la sesión local. Un 403 de otro tipo no la cierra.
10. **CA-10** · **Dado** un error del backend · **cuando** llega · **entonces** se decide por su `code` y se muestra su `mensaje`.
11. **CA-11** · **Dado** que el servidor no responde · **cuando** se llama · **entonces** el resultado es `SinConexion` y la app no se cierra.
12. **CA-12** · **Dado** la cuenta ya consultada · **cuando** se gira el teléfono · **entonces** el estado se conserva.
13. **CA-13** · **Dado** el build `debug` · **cuando** se conecta al backend local por HTTP · **entonces** funciona hacia `10.0.2.2` y `localhost`. En `release` solo se permite HTTPS.

## 6. Fuera de alcance / qué NO debe cambiar

- El diseño final de cualquier pantalla (se hace con Impeccable en MOV-0002 y siguientes).
- Registro, completar registro, recuperar contraseña, Google nativo, elegir modo y modos (specs F0 en adelante).
- DataStore, Coil, splash screen, Room y FCM (ver [`arquitectura.md`](../arquitectura.md) §11).
- El backend: no se tocó nada. Se levantó en local con Flyway apagado, solo para leer.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) o verificación | Nivel |
|---|---|---|
| CA-1 | `./gradlew assembleDebug` en verde | Build |
| CA-2 | Revisión de paquetes + los tests de dominio corren en la JVM sin Android | Revisión |
| CA-3 | `DiagnosticoViewModelTest › CA-3 con configuracion incompleta lo avisa y no toca la sesion` | ViewModel |
| CA-4 | `DiagnosticoViewModelTest › CA-4 al abrir comprueba el servidor…` · `› CA-4 con el servidor caido…` · `BackendRealHumoTest › CA-4 el backend real responde UP…` · captura 01 | ViewModel · Humo · Emulador |
| CA-5 | `IniciarSesionUseCaseTest › CA-5 …` (4 tests) · `DiagnosticoViewModelTest › CA-5 credenciales invalidas no inician sesion` | Dominio · ViewModel |
| CA-6 | `CuentaRepositoryImplTest › CA-6 me devuelve la cuenta…` · `DiagnosticoViewModelTest › CA-6 iniciar sesion consulta la cuenta…` · `BackendRealHumoTest › CA-6 inicia sesion en Supabase y GET api auth me…` · captura 02 | Red · ViewModel · Humo · Emulador |
| CA-7 | `RedSesionTest › CA-7 cada peticion lleva el Bearer…` · `› CA-7 sin sesion no se envia Authorization` | Red |
| CA-8 | `RedSesionTest › CA-8 …` (4 tests). Verificado con una mutación: sin el control del reintento, falla `CA-8 si el reintento vuelve a dar 401…` | Red |
| CA-9 | `RedSesionTest › CA-9 un 403 CUENTA_BLOQUEADA cierra…` · `› CA-9 un 403 ACCESO_DENEGADO no cierra…` | Red |
| CA-10 | `ErroresHttpTest › CA-10 …` (4 tests) · `CuentaRepositoryImplTest › CA-10 …` · `DiagnosticoViewModelTest › CA-10 …` · `BackendRealHumoTest › CA-10 …` | Red · ViewModel · Humo |
| CA-11 | `CuentaRepositoryImplTest › CA-11 sin servidor devuelve SinConexion…` | Red |
| CA-12 | Emulador: girar con la cuenta en pantalla. La tarjeta sigue (captura 03) | Emulador (manual) |
| CA-13 | Emulador (debug) conectado a `http://10.0.2.2:8082/` (captura 01) + revisión de `network_security_config.xml` (main y debug) | Emulador · Revisión |

## 8. Verificación en el emulador

Hecha el 8-nov-2026 en el AVD `Pixel_8` (Android 17, API 37), con BackendCodeCup local en el puerto 8082. Detalle y salidas en [`evidencias/MOV-0001/`](evidencias/MOV-0001/README.md).

1. Abrir la app: el servidor sale "En línea (UP)" y la sesión "Sin sesión" (captura 01).
2. Iniciar sesión con `jugador01@prueba.local`: "Sesión activa" y la tarjeta de `/api/auth/me` con Juan Castro, ACTIVA, JUGADOR (captura 02).
3. Girar el teléfono: la tarjeta de la cuenta sigue ahí (captura 03).
4. Tema oscuro provisional (captura 04).

## 9. Archivos afectados

Todo el repo es nuevo. Los archivos clave:
- **Build:** `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `local.properties.ejemplo`.
- **Red:** `data/remote/red/*` (`ClienteHttp`, `AuthInterceptor`, `TokenAuthenticator`, `CuentaBloqueadaInterceptor`, `ErroresHttp`, `LlamadaApi`).
- **Sesión:** `data/auth/*`, `data/repository/*`.
- **Dominio:** `domain/**`.
- **Inyección:** `di/AppContainer.kt`.
- **Pantalla temporal:** `presentacion/diagnostico/*`, `presentacion/navigation/*`.
- **Tema:** `ui/theme/*`.
- **Tests:** `app/src/test/**`.

## 10. Datos de prueba

- Usuarios del seed del backend: `jugador01@prueba.local`, `admin01@prueba.local`… La contraseña está en `SEED_PASSWORD` del `.env` del backend y nunca se escribe en este repo.

## 11. Notas y decisiones

- **Versiones:** se probaron las más nuevas y se bajaron las que exigen `compileSdk` 37 o Kotlin 2.4 ([`arquitectura.md`](../arquitectura.md) §2.1).
- **El diagnóstico es temporal.** Cuando lleguen las pantallas de acceso (MOV-0004) deja de ser la pantalla de inicio. Se propone conservarlo solo en `debug`, como herramienta para el equipo.
- **El emulador casi no arranca:** el disco C: estaba al 100 % (760 MB libres). Arrancó cuando hubo unos 3 GB libres. Para el trabajo con Impeccable hace falta más espacio (plan de UI §8).

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-08 | Implementada y verificada: 31 tests (28 pasan y 3 de humo se saltan sin `DESAFIO_HUMO`), humo contra el backend real en verde y recorrido en el emulador. Queda `en-revision` hasta el merge a `main`. |
| 2026-11-09 | Integrada a `main` junto con MOV-0002, MOV-0003 y MOV-0006, por pedido del usuario. Pasa a `hecha`. |
