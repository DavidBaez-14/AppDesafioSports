---
id: MOV-0004
titulo: Inicio (jugador), navegación principal y Perfil
tipo: feature
estado: en-revision
prioridad: P1
fase: F1
modo: Jugador
responsable: David
depende_de: [MOV-0003, MOV-0005, MOV-0010, backend 0026, backend 0029]
rama: mov-0004-inicio-y-perfil
---

# MOV-0004 · Inicio (jugador), navegación principal y Perfil

> **Para quien la implementa:** esta spec se puede trabajar sin preguntar nada más. Todo el backend que usa **ya existe** en `main` de BackendCodeCup. Lee antes [`../sdd.md`](../sdd.md) (cómo se trabaja) y [`../arquitectura.md`](../arquitectura.md) (capas, nombres y reglas de código). El diseño de referencia es el prototipo de roles: `docs/prototipo-roles/index.html` (ábrelo en el navegador) o su copia publicada, <https://claude.ai/artifact/2cGBE6e4ynfh2HNvPUcqHy>.

## 1. Contexto

- **Hoy, después de entrar, la app abre el Inicio provisional de MOV-0010:** el saludo y "Tus secciones", sin barra inferior, sin equipos y sin Perfil. Esta spec pone el Inicio real y **conserva los bloques de sección de MOV-0010** (§11.5).
- **Estructura decidida** (`PRODUCT.md`, 8-nov-2026): todos entran como **jugador**. **Creador** y **Árbitro** no son modos: son **secciones** a las que se entra desde Inicio, y solo aparecen si la persona las tiene.
- **Inicio es de donde cuelga todo lo demás.** Las secciones Creador y Árbitro, Crear torneo y Explorar ya están en `main` (MOV-0005, 0007, 0008, 0009 y 0010): esta spec las enlaza con las rutas de §11.1.

## 2. Alcance

### 2.1 Navegación principal

- **Barra inferior** de Material 3 con tres destinos: **Inicio · Explorar · Perfil**.
- **Explorar** abre la pantalla real de MOV-0005, sin la flecha de volver (decisión del 14-nov, §11.5). El visitante sin cuenta sigue entrando a Explorar desde el login, con su flecha.
- **Después de entrar, crear cuenta o completar registro**, la app abre Inicio y no el diagnóstico.
- **Atrás:** desde Explorar o Perfil vuelve a Inicio; desde Inicio sale de la app.

### 2.2 Inicio

De arriba hacia abajo, como en la pantalla `inicio` del prototipo (estructura "Jugador de base"):

0. **El saludo** ("Hola, Carlos"), como en MOV-0010.
1. **"Tus secciones".** Un bloque por cada sección que la persona tiene según `secciones` de `/api/auth/me`. Si no tiene ninguna, "Tus secciones" no aparece.
   - Los bloques son los de MOV-0010, sin cambios: la cabecera abre la sección y dentro va lo urgente (§3.3).
   - **Creador:** el torneo que más espera revisión abre sus inscripciones.
   - **Árbitro:** el partido que sigue abre su planilla; si es hoy, la hora y el chip "Planilla" van en naranja.
2. **"Tu próximo partido".** El partido más cercano entre todos sus equipos. Si no hay ninguno, el bloque no aparece. Abre el torneo.
3. **"Mis equipos".** Cada equipo con su torneo, si la persona **juega** o **delega**, y el estado del trámite en un chip (§3.3). Cada fila abre el torneo.
4. **Botón flotante "Crear torneo".** Abre el flujo de crear torneo.
5. **Estado vacío** para una cuenta sin equipos: "Aún no juegas en ningún torneo", con el botón "Ir a Explorar".

**Se actualiza** al deslizar hacia abajo y cada vez que la pantalla vuelve a verse (por ejemplo, al regresar de una sección).

### 2.3 Perfil

Como en la pantalla `perfil` del prototipo:

1. **Encabezado:** iniciales, nombre completo y "Así te ven en lo público: *Nombres A.*".
2. **Cifras:** partidos, goles y títulos. No se muestran si las tres están en cero.
3. **"Trayectoria":** un renglón por equipo y torneo. Si ese equipo ganó un puesto en ese torneo, lleva el chip con el puesto.
4. **"Sanciones":** las multas con su estado y un renglón con el total de tarjetas. El bloque no aparece si no hay multas ni tarjetas.
5. **"Cuenta":**
   - "Perfil deportivo", **solo lectura**: posición, pierna hábil y altura, o "Sin completar";
   - "Diagnóstico de conexión", **solo en la versión debug**;
   - "Cerrar sesión", que vuelve al login.

### 2.4 Arranque con sesión guardada

Inicio consulta `/api/auth/me` al abrir. Con eso se cierra un hueco de MOV-0002, que con sesión guardada iba al diagnóstico sin comprobar la cuenta:

- **cuenta sin registro** (404 `CUENTA_NO_REGISTRADA`) → va a Completar registro;
- **cuenta bloqueada** (403 `CUENTA_BLOQUEADA`) o **sesión vencida** (401) → vuelve al login. Esto ya lo hacen los interceptores y `RaizViewModel`; aquí solo se comprueba que siga funcionando.

## 3. Diseño

### 3.1 Referencia

- **Prototipo:** pantallas `inicio` y `perfil`, con la estructura "Jugador de base" seleccionada. Úsalo para el orden, la jerarquía y los textos. **No copies su HTML ni sus colores a mano.**
- **Tokens:** [`../../DESIGN.md`](../../DESIGN.md) y `ui/theme/`. Colores, tipografía y formas salen de `MaterialTheme`.
- **Componentes de Material 3:** `Scaffold`, `NavigationBar`, `ExtendedFloatingActionButton`, `ListItem`, `Badge`, `AssistChip` o `SuggestionChip` y `PullToRefreshBox`.
- **Iconos:** vectores `res/drawable/ic_*.xml` de Material Symbols, como los que ya hay. **No se agrega** la librería de iconos extendidos.
- **El naranja** se usa solo para "Crear torneo" y para lo que pasa hoy (el chip "Planilla").

### 3.2 Estados de cada pantalla

| Pantalla | Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|---|
| Inicio | Esqueleto de las listas, con la barra y el botón flotante ya visibles | "Aún no juegas en ningún torneo" + "Ir a Explorar". El botón flotante sigue | Mensaje del error + "Reintentar" | "Sin conexión. Revisa tu internet." + "Reintentar" | Bloques de §2.2 |
| Perfil | Esqueleto; el encabezado se pinta en cuanto llega la cuenta | "Tu trayectoria empieza con tu primer partido" | Mensaje + "Reintentar". "Cerrar sesión" **siempre** visible | Igual que el error, con el texto de sin conexión | Bloques de §2.3 |
| Explorar | Los de MOV-0005 | | | | |

- **Si falla la actualización** y ya había datos en pantalla, se conservan y el error sale en un `Snackbar`.

### 3.3 Textos y reglas de presentación

Todos van a `strings.xml`. Los que llevan número usan `plurals`.

**Tus secciones** (los bloques de MOV-0010; aquí solo cambia cuándo aparecen)

| Caso | Cabecera | Dentro del bloque |
|---|---|---|
| Creador, algo por revisar | "Creador" · "2 torneos" · chip naranja "3 por revisar" | "*Torneo* te espera" con sus inscripciones y comprobantes: abre las inscripciones |
| Creador, nada por revisar | "Creador" · "2 torneos" | "Nada espera tu revisión." |
| Árbitro, con partidos por jugar | "Árbitro" · "2 por jugar" | El partido que sigue, con el chip "Planilla" (naranja si es hoy): abre la planilla |
| Árbitro, sin partidos por jugar | "Árbitro" | "No tienes partidos por arbitrar. Los jugados siguen en la sección." |

- **Cuándo aparece cada bloque:** lo decide `secciones` de `/api/auth/me` (`creador` o `arbitro` distinto de `null`). Antes, Creador salía siempre y Árbitro solo con partidos.
- **Mientras llega el detalle** de un bloque, la cabecera de Creador ya muestra cuántos torneos tiene, con el dato de `secciones`.
- **Si falla el detalle de un bloque,** el error sale dentro de ese bloque con su "Reintentar" y el resto de Inicio se ve igual (MOV-0010 · CA-5).

**Tu próximo partido**

- **Cuál:** entre los equipos de `yo/equipos`, el `proximoPartido` con la `fechaHora` más temprana. Los que no tienen fecha no cuentan. Un partido atrasado que sigue sin jugarse cuenta: es el que el backend manda como próximo.
- **Título:** "Local vs Visitante". Si `local` es `true`, el equipo propio va primero. Si `rival` es `null`, "Rival por definir".
- **Texto:** "*torneo* · *lugar*". Sin lugar, solo el torneo.
- **Al inicio:** el día y la hora ("sáb 14" y "4:00 p. m."; "Hoy" o "Mañana" cuando aplique).

**Mis equipos**

- **Título:** el nombre del equipo. **Texto:** "Juegas · *torneo*" o "Delegas · *torneo*", según `rol`.
- **Icono:** según el código de `deporte` (`FUTSAL`, `VOLEIBOL`, `BALONCESTO`, `TENIS_MESA`). Un código desconocido usa un icono genérico. El mapeo vive en `ui/deporte/` (regla 8 de la arquitectura).
- **Chip de estado:**

| `estadoInscripcion` | `rol` | `comprobante` | Chip |
|---|---|---|---|
| `APROBADO` | cualquiera | cualquiera | sin chip (flecha) |
| `PENDIENTE_PAGO` | `DELEGADO` | `null` | "Falta comprobante" (advertencia) |
| `PENDIENTE_PAGO` | `DELEGADO` | `PENDIENTE_REVISION` | "En revisión" (informativo) |
| `PENDIENTE_PAGO` | `DELEGADO` | `RECHAZADO` | "Comprobante rechazado" (error) |
| `PENDIENTE_PAGO` | `JUGADOR` | — | "Inscripción pendiente" (advertencia) |
| `RECHAZADO` | cualquiera | — | "Rechazada" (error) |
| `EXPULSADO` | cualquiera | — | "Expulsado" (error) |

- **Las filas de equipo y la de próximo partido abren el detalle del torneo** (`Torneo(torneoId)`, MOV-0005). Decisión del 14-nov (§11.5).

**Perfil**

- **Nombre público:** `nombres` más la inicial de `apellidos` y un punto ("Carlos R.").
- **Trayectoria:** título = `torneo`; texto = `nombre` del equipo. El chip sale de `titulos` cuando coinciden `torneo` y `equipo`: `CAMPEON` → "Campeón" (naranja) · `SUBCAMPEON` → "Subcampeón" · `TERCERO` → "Tercer puesto".
- **Multas:**

| `tipoSancion` | Título |
|---|---|
| `AMARILLA` | "Multa por tarjeta amarilla" |
| `AZUL` | "Multa por tarjeta azul" |
| `ROJA` | "Multa por tarjeta roja" |
| `ACUMULACION_AMARILLAS` | "Multa por acumulación de amarillas" |
| `ROJA_DIRECTA` | "Multa por roja directa" |
| otro | "Multa" |

  - **Texto:** el monto en pesos sin decimales ("$10.000") y la fecha en que se generó.
  - **Chip:** `PENDIENTE` → "Pendiente" · `EN_REVISION` → "En revisión" · `PAGADA` → "Pagada" · `CONDONADA` → "Condonada".
  - **Orden:** primero las pendientes y en revisión; dentro de cada grupo, la más reciente arriba.
- **Tarjetas:** "3 amarillas, 0 azules, 1 roja", de `resumen.tarjetas`.

### 3.4 Accesibilidad

- Objetivos de 48 dp como mínimo y contraste AA.
- Letra al 130 % sin cortes: los textos largos pasan a dos líneas, no se recortan.
- Cada fila se lee completa en TalkBack (título, texto y estado). Los iconos decorativos llevan `contentDescription = null`.
- `testTag` en todo control interactivo (§9 de este documento lista los mínimos).

## 4. Contrato con el backend

**Todo existe.** Se puede revisar en Swagger: `<API_BASE_URL>swagger-ui/index.html`.

| Endpoint | Método | Envía | Responde | Errores esperados → AppError |
|---|---|---|---|---|
| `/api/auth/me` | GET | — | `MeResponseDto`, ahora con `secciones` | 401 → `NoAutenticado` · 403 `CUENTA_BLOQUEADA` → `CuentaBloqueada` · 404 `CUENTA_NO_REGISTRADA` → `CuentaNoRegistrada` |
| `/api/supercopa/yo/equipos` | GET | — | `List<MiEquipoDto>` | los mismos |
| `/api/supercopa/mi-perfil` | GET | — | `PerfilDto` | los mismos |
| `/api/finanzas/multas/mias` | GET | — | `List<MultaDto>` | los mismos |

**Reglas comunes:**
- **Todos los ids son UUID** y en la app son `String`.
- **Las fechas con hora** llegan como `2026-11-12T16:00:00`, sin zona. Las fechas solas, como `2026-11-12`.
- **El backend manda los campos vacíos como `null`.** Los DTO llevan todos sus campos anulables con valor por defecto, como `MeResponseDto`.
- **Un valor de texto que la app no conoce** (un deporte o un estado nuevo) no puede cerrar la app: se muestra con el caso genérico.

### 4.1 `GET /api/auth/me` · campo nuevo `secciones`

El resto de la respuesta no cambia. `secciones` viene siempre; una sección en `null` es una que la cuenta no tiene.

```json
"secciones": {
  "creador": { "torneos": 2, "porRevisar": 3 },
  "arbitro": {
    "partidosAsignados": 2,
    "hoy": {
      "partidoId": "5f0c…", "torneoId": "9a1b…", "torneo": "Liga Relámpago (prueba)",
      "local": "Halcones FC", "visitante": "Tiburones FC",
      "fechaHora": "2026-11-12T16:00:00", "lugar": "Cancha 1", "estado": "PROGRAMADO"
    }
  }
}
```

- `creador` va si administra al menos un torneo. `porRevisar` suma las inscripciones pendientes y los comprobantes por revisar de sus torneos.
- `arbitro` va si es árbitro de al menos un torneo, **aunque no tenga partidos** (`partidosAsignados` puede ser 0). `hoy` es su próximo partido del día, o `null`.
- **La app lee de `secciones` solo qué secciones hay y cuántos torneos administra.** El partido del árbitro y lo que espera revisión salen de `GET /api/supercopa/arbitro/partidos` y `GET /api/supercopa/creador/torneos`, que ya usan los bloques de MOV-0010 y solo se piden si la sección existe.
- `roles` puede traer valores que la app no conoce, como `SUPERADMIN`. Ya se ignoran; no cambies eso.

### 4.2 `GET /api/supercopa/yo/equipos`

```json
[{
  "equipoTorneoId": "c2d3…", "equipoId": "a1b2…", "equipo": "Halcones FC",
  "torneoId": "9a1b…", "torneo": "Copa Prueba 2026", "deporte": "FUTSAL",
  "estadoTorneo": "PUBLICADO", "rol": "DELEGADO",
  "estadoInscripcion": "PENDIENTE_PAGO", "motivoRechazo": null,
  "comprobante": "PENDIENTE_REVISION", "numeroCamiseta": 10,
  "proximoPartido": {
    "partidoId": "5f0c…", "rival": "Tiburones FC", "local": true,
    "fechaHora": "2026-11-12T16:00:00", "lugar": "Cancha 1", "estado": "PROGRAMADO"
  }
}]
```

- `rol`: `DELEGADO` (gestiona la inscripción y también juega) o `JUGADOR`.
- `estadoInscripcion`: `PENDIENTE_PAGO`, `APROBADO`, `RECHAZADO` o `EXPULSADO`.
- `comprobante`: solo para el delegado. `PENDIENTE_REVISION`, `APROBADO`, `RECHAZADO`, o `null` si no ha subido ninguno.
- `proximoPartido`: `null` si el equipo no tiene partidos por jugar. Dentro, `rival` y `fechaHora` pueden ser `null`.

### 4.3 `GET /api/supercopa/mi-perfil`

```json
{
  "personaId": "7e8f…", "nombre": "Juan Castro",
  "alturaCm": 175, "piernaHabil": "DERECHA", "posicion": "DELANTERO",
  "equipos": [{ "id": "a1b2…", "nombre": "Halcones FC", "torneo": "Liga Relámpago (prueba)",
                "desde": "2026-11-01", "hasta": null }],
  "resumen": { "partidosJugados": 5, "goles": 4,
               "tarjetas": { "amarillas": 3, "azules": 0, "rojas": 1 }, "titulos": 1 },
  "titulos": [{ "torneo": "Copa Sistemas 2025", "equipo": "Halcones FC", "puesto": "SUBCAMPEON" }],
  "partidos": [{ "id": "5f0c…", "fecha": "2026-11-05T16:00:00", "equipo": "Halcones FC",
                 "rival": "Tiburones FC", "goles": 2, "tarjetas": ["AMARILLA"] }]
}
```

- `alturaCm`, `piernaHabil` y `posicion` son `null` si la persona no los ha indicado.
- `puesto` es un código: `CAMPEON`, `SUBCAMPEON` o `TERCERO`.
- `partidos` no se usa en esta spec.

### 4.4 `GET /api/finanzas/multas/mias`

```json
[{
  "id": "d4e5…", "partidoId": "5f0c…", "equipoTorneoId": "c2d3…", "torneoId": "9a1b…",
  "tipoSancion": "ACUMULACION_AMARILLAS", "monto": 10000.00, "estado": "PENDIENTE",
  "partidosSuspension": 1, "partidosSuspensionRestantes": 1,
  "fechaGeneracion": "2026-11-05T17:40:00", "fechaPago": null
}]
```

- La respuesta trae más campos; solo se usan estos.
- **No trae el nombre del torneo.** Por eso el renglón muestra el monto y la fecha, no el torneo (§11.3).

## 5. Criterios de aceptación

1. **CA-1** · **Dado** que entro con una cuenta registrada, o abro la app con la sesión guardada, **cuando** termina el arranque, **entonces** veo Inicio con la barra Inicio · Explorar · Perfil y no el diagnóstico. Atrás desde Inicio sale de la app; desde Explorar o Perfil vuelve a Inicio.
2. **CA-2** · **Dado** que `secciones` trae `creador` y `arbitro`, **cuando** se pinta Inicio, **entonces** "Tus secciones" muestra los dos bloques y se piden sus detalles. **Dado** que solo viene una, **entonces** solo se muestra y se pide esa. **Dado** que las dos vienen en `null`, **entonces** "Tus secciones" no aparece y no se pide ningún detalle.
3. **CA-3** · **Dado** el bloque Árbitro con su partido que sigue, **cuando** toco el partido, **entonces** se abre `Planilla` con ese `partidoId`; la cabecera abre `SeccionArbitro`. La cabecera de Creador abre `SeccionCreador`, su torneo en espera abre `InscripcionesTorneo` y el botón flotante abre `CrearTorneo`.
4. **CA-4** · **Dado** que `yo/equipos` trae varios equipos con `proximoPartido`, **cuando** se pinta Inicio, **entonces** "Tu próximo partido" muestra el de `fechaHora` más cercana, con el equipo local primero. **Dado** que ninguno tiene partido con fecha, **entonces** el bloque no aparece.
5. **CA-5** · **Dado** un equipo de `yo/equipos`, **cuando** se pinta "Mis equipos", **entonces** muestra "Juegas" o "Delegas" según `rol` y el chip que le toca según la tabla de §3.3.
6. **CA-6** · **Dado** que la cuenta no tiene equipos ni secciones, **cuando** se pinta Inicio, **entonces** veo el estado vacío con "Ir a Explorar" (que cambia a la pestaña Explorar) y el botón flotante "Crear torneo".
7. **CA-7** · **Dado** que abro Perfil, **cuando** responden `mi-perfil` y `multas/mias`, **entonces** veo el nombre, el nombre público, las cifras, la trayectoria con sus títulos, y las multas ordenadas con su chip de estado.
8. **CA-8** · **Dado** que la cuenta no tiene partidos, equipos ni multas, **cuando** abro Perfil, **entonces** veo el estado vacío de trayectoria, y no aparecen las cifras ni el bloque de sanciones.
9. **CA-9** · **Dado** que estoy en Perfil, **cuando** toco "Cerrar sesión", **entonces** vuelvo al login y Atrás no regresa a Perfil.
10. **CA-10** · **Dado** que abro la app con sesión guardada y `/api/auth/me` responde 404 `CUENTA_NO_REGISTRADA`, **entonces** voy a Completar registro. **Dado** que responde 403 `CUENTA_BLOQUEADA`, **entonces** vuelvo al login.
11. **CA-11** · **Dado** que no hay conexión, **cuando** carga Inicio o Perfil, **entonces** veo el aviso de sin conexión con "Reintentar", y al reintentar con conexión se cargan los datos. En Perfil, "Cerrar sesión" sigue disponible.
12. **CA-12** · **Dado** que el backend responde un deporte o un estado que la app no conoce, **entonces** la pantalla se pinta con el caso genérico y la app no se cierra.
13. **CA-13** · **Dado** que vuelvo a Inicio desde otra pantalla, o deslizo hacia abajo, **entonces** los datos se vuelven a pedir.
14. **CA-14** · Inicio y Perfil se ven bien en claro, en oscuro y con la letra al 130 %, sin textos cortados (emulador).
15. **CA-15** · **Dado** un equipo de "Mis equipos" o "Tu próximo partido", **cuando** lo toco, **entonces** se abre `Torneo` con el `torneoId` de ese equipo.
16. **CA-16** · **Dado** que estoy en la pestaña Explorar, **entonces** veo la pantalla de MOV-0005 sin flecha de volver y con la barra inferior; abrir un torneo y volver me deja en Explorar.

## 6. Fuera de alcance / qué NO debe cambiar

- **Las pantallas de otras specs:** sección Creador (MOV-0008), sección Árbitro y planilla (MOV-0009), Crear torneo (MOV-0007) y Explorar con el detalle del torneo (MOV-0005). Aquí solo se enlazan (§11.1). De Explorar cambia una sola cosa: la flecha de volver pasa a ser opcional.
- **Los bloques de sección de MOV-0010** (cabecera, partido que sigue, torneo en espera, error por bloque) no cambian de aspecto ni de comportamiento.
- **Editar** el perfil deportivo, los datos personales o la foto.
- **Pagar una multa o subir su comprobante** desde la app. El backend aún no lo ofrece para el jugador.
- **El aviso de "cuenta bloqueada" en el login al arrancar.** Basta con volver al login; el aviso ya sale al intentar entrar (MOV-0002).
- **No se quita el diagnóstico.** Sigue en la pulsación larga del logotipo del login (debug) y se suma su acceso desde Perfil (debug).
- **No cambia** nada del acceso: login, crear cuenta, Google ni recuperar contraseña.
- **No se suben versiones ni se agregan librerías** (`arquitectura.md` §2.2).

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) o verificación | Nivel |
|---|---|---|
| CA-1 | `RaizViewModelTest › CA-1 con sesion el destino es el inicio` y verificación en el emulador (Atrás) | ViewModel + emulador |
| CA-2 | `InicioViewModelTest › MOV-0004 CA-2 …` (las dos, una sola y ninguna) | ViewModel |
| CA-2 | `CuentaRepositoryImplTest › CA-2 lee secciones de me` (con las dos, con una y con ninguna) | Red (MockWebServer) |
| CA-3 | Los de MOV-0010 que ya cubren el partido que sigue y el torneo en espera (`InicioViewModelTest › CA-2 …` y `› CA-4 …`), y verificación en el emulador | ViewModel + emulador |
| CA-4 | `MisEquiposTest › CA-4 el proximo partido es el mas cercano` y `› CA-4 sin fechas no hay proximo partido` | Dominio |
| CA-5 | `EstadoTramiteTest › CA-5 …` (un caso por fila de la tabla de §3.3) | Dominio |
| CA-5 | `MisEquiposRepositoryImplTest › CA-5 convierte la respuesta de yo equipos` | Red (MockWebServer) |
| CA-6 | `InicioViewModelTest › CA-6 cuenta nueva ve el estado vacio` | ViewModel |
| CA-7 | `PerfilViewModelTest › CA-7 arma trayectoria titulos y multas ordenadas` | ViewModel |
| CA-7 | `PerfilRepositoryImplTest › CA-7 …` y `MultasRepositoryImplTest › CA-7 …` | Red (MockWebServer) |
| CA-8 | `PerfilViewModelTest › CA-8 cuenta nueva oculta cifras y sanciones` | ViewModel |
| CA-9 | `PerfilViewModelTest › CA-9 cerrar sesion llama al caso de uso` y verificación en el emulador | ViewModel + emulador |
| CA-10 | `InicioViewModelTest › CA-10 cuenta sin registro pide completar registro` | ViewModel |
| CA-11 | `InicioViewModelTest › CA-11 sin conexion muestra reintentar y se recupera`; lo mismo en `PerfilViewModelTest` | ViewModel |
| CA-12 | `MisEquiposRepositoryImplTest › CA-12 un deporte o estado desconocido no falla` | Red (MockWebServer) |
| CA-13 | `InicioViewModelTest › CA-13 recargar vuelve a pedir los datos` | ViewModel |
| CA-14 | Capturas del emulador | Emulador |
| CA-15 | Verificación en el emulador (tocar un equipo abre su torneo) | Emulador |
| CA-16 | Verificación en el emulador | Emulador |

- Las pruebas usan las implementaciones falsas de `testutil/Fakes.kt`, no librerías de mocks.
- Los JSON de las pruebas de red son los de §4.

## 8. Verificación en el emulador

Con el backend levantado y las cuentas del seed (§10):

1. Entra con `arbitro01`: se ve la fila Árbitro. Tócala.
2. Entra con `admin01`: se ve la fila Creador con sus torneos. Tócala. Toca "Crear torneo".
3. Entra con `delegado01`: "Mis equipos" muestra "Delegas · …" con su chip.
4. Entra con `jugador01`: "Mis equipos" muestra "Juegas · …". Abre Perfil.
5. Crea una cuenta nueva: Inicio y Perfil muestran sus estados vacíos.
6. Activa el modo avión y desliza hacia abajo: aparece el aviso; desactívalo y reintenta.
7. Cierra sesión desde Perfil y pulsa Atrás.

**Capturas** a guardar en `docs/specs/evidencias/MOV-0004/`: Inicio y Perfil en claro, en oscuro y con letra al 130 %; Inicio vacío; Inicio sin conexión; y el caso extremo de §10.

## 9. Archivos afectados

Lista cerrada. Si hace falta tocar algo fuera de aquí, primero se actualiza esta spec.

**Nuevos**

- `domain/model/`: `Secciones.kt`, `MiEquipo.kt` (con el próximo partido y el estado del trámite), `Perfil.kt`, `Multa.kt`
- `domain/repository/`: `MisEquiposRepository.kt`, `PerfilRepository.kt`, `MultasRepository.kt`
- `domain/use_case/competicion/`: `ObtenerMisEquiposUseCase.kt`, `ObtenerMiPerfilUseCase.kt`
- `domain/use_case/finanzas/`: `ObtenerMisMultasUseCase.kt`
- `data/remote/api/`: `CompeticionApi.kt`, `FinanzasApi.kt`
- `data/remote/dto/`: `SeccionesDto.kt`, `MiEquipoDto.kt`, `PerfilDto.kt`, `MultaDto.kt`
- `data/repository/`: `MisEquiposRepositoryImpl.kt`, `PerfilRepositoryImpl.kt`, `MultasRepositoryImpl.kt`
- `presentacion/principal/`: `PrincipalScreen.kt` (el `Scaffold` con la barra inferior y las tres pestañas)
- `presentacion/perfil/`: `PerfilScreen.kt` y `PerfilViewModel.kt` (con su `PerfilState`)
- `res/drawable/ic_*.xml` (los iconos nuevos: inicio, explorar y perfil de la barra)
- Las pruebas de §7, en `app/src/test/…`
- `docs/specs/evidencias/MOV-0004/`

**Modificados**

- `presentacion/inicio/InicioScreen.kt` e `InicioViewModel.kt` (ya existían por MOV-0010): se suman la carga de la cuenta con sus secciones, "Tu próximo partido", "Mis equipos" y el estado vacío; salen la lupa y el menú de la cuenta (pasan a la barra y a Perfil)
- `presentacion/explorar/ExplorarScreen.kt`: `onVolver` opcional (sin flecha dentro de la pestaña)
- `presentacion/comun/Componentes.kt`: el esqueleto de carga
- `data/remote/dto/MeResponseDto.kt` y `domain/model/Cuenta.kt`: se agrega `secciones`
- `data/repository/CuentaRepositoryImpl.kt`: convierte `secciones`
- `di/AppContainer.kt`: las API, los repositorios y los casos de uso nuevos
- `presentacion/navigation/Rutas.kt` y `AppNavigation.kt`: la ruta `Inicio` pasa a llamarse `Principal`; después de entrar se abre `Principal`
- `presentacion/navigation/RaizViewModel.kt`: solo el comentario de `DestinoRaiz.INICIO`
- `res/values/strings.xml`
- `app/src/test/…/testutil/Fakes.kt`, `RaizViewModelTest.kt`, `CuentaRepositoryImplTest.kt` e `InicioViewModelTest.kt` (los casos de MOV-0010 se conservan; cambian los dos que decían cuándo aparece una sección)
- `docs/arquitectura.md`: §1 (secciones en lugar de modos), §3 (los paquetes nuevos) y §8 (la navegación real)
- `docs/specs/README.md` y esta spec (estado y registro)

**`testTag` mínimos:** `nav_inicio`, `nav_explorar`, `nav_perfil`, `inicio_creador`, `inicio_arbitro` e `inicio_crear_torneo` (los de MOV-0010), `inicio_proximo_partido_equipo`, `inicio_equipo`, `inicio_ir_a_explorar`, `inicio_reintentar`, `perfil_reintentar`, `perfil_diagnostico` y `perfil_cerrar_sesion`.

## 10. Datos de prueba

**Entorno:** el backend de desarrollo, con el `local.properties` que entregó David. Las cuentas existen después de correr el seed una vez; la contraseña es la misma para todas y la da David.

| Cuenta | Qué debe verse |
|---|---|
| `admin01@prueba.local` | Fila Creador con 2 torneos ("Copa Prueba 2026" y "Liga Relámpago (prueba)") |
| `arbitro01@prueba.local` | Fila Árbitro. El seed le programa un partido para **el día siguiente** a correrlo, a las 4:00 p. m. |
| `delegado01@prueba.local` | "Halcones FC" como "Delegas", con su chip de trámite |
| `jugador01@prueba.local` | "Halcones FC" como "Juegas" |
| Una cuenta nueva | Estados vacíos de Inicio y de Perfil |

- **Para ver el chip "Planilla"** hace falta que el partido del árbitro sea hoy. Se logra corriendo el seed el día anterior, o reprogramando el partido con `admin01` en Swagger: `PUT /api/supercopa/admin/partidos/{partidoId}/programacion`.
- **Casos extremos:**
  - un equipo con nombre de 40 caracteres o más y un torneo con nombre largo, con letra al 130 %;
  - una persona en 6 equipos o más (la lista se desplaza y el botón flotante no tapa la última fila);
  - `porRevisar` de dos y tres cifras en el `Badge`;
  - `proximoPartido` con `rival` en `null`.

  Los que el seed no trae se cubren en las pruebas de red y de ViewModel con JSON armado a mano.

## 11. Notas y decisiones

### 11.1 Rutas compartidas con las otras specs

Las rutas de las otras pantallas ya existen en `Rutas.kt` con estos nombres (las fijó esta spec en su borrador y las llenaron MOV-0007 a MOV-0009):

| Ruta | Qué abre | La llena |
|---|---|---|
| `Principal` | La barra inferior con Inicio, Explorar y Perfil | **Esta spec** |
| `SeccionCreador` | Sección Creador | MOV-0008 |
| `SeccionArbitro` | Sección Árbitro | MOV-0009 |
| `Planilla(partidoId: String)` | Planilla de un partido | MOV-0009 |
| `CrearTorneo` | Crear torneo en tres pasos | MOV-0007 |

- **Las cuatro tienen ya su pantalla real,** así que no hace falta ninguna pantalla "en construcción".
- **`InicioScreen` no conoce las rutas.** Recibe funciones (`onAbrirCreador`, `onAbrirArbitro`, `onAbrirPlanilla(partidoId)`, `onAbrirInscripciones(torneo)`, `onAbrirTorneo(torneoId)`, `onCrearTorneo`, `onExplorar`, `onCompletarRegistro`) y `AppNavigation` las conecta.

### 11.2 Las tres pestañas

- `Principal` es **una sola ruta**. La pestaña elegida se guarda con `rememberSaveable` dentro de `PrincipalScreen`; no hay un `NavHost` anidado.
- Cada pestaña conserva su posición al cambiar de pestaña (`SaveableStateHolder`).
- Cada pestaña pide su ViewModel con `viewModel(factory = …)`, como dice la arquitectura.

### 11.3 Diferencias con el prototipo, a propósito

- **Cifras del Perfil:** el prototipo muestra partidos, torneos, títulos y deportes. Se muestran **partidos, goles y títulos**, que es lo que entrega `mi-perfil`.
- **Multas:** el prototipo muestra el torneo y el botón "Subir pago". El backend no manda el nombre del torneo en la multa ni permite todavía que el jugador suba el comprobante. Se muestran el monto, la fecha y el estado.
- **Fila Creador:** el prototipo dice "3 inscripciones esperan tu aprobación". `porRevisar` también cuenta comprobantes, así que el texto es "3 por revisar".
- **Creador y Árbitro en Perfil:** no van. En la estructura decidida se entra a las secciones desde Inicio.

### 11.4 Pendientes que no bloquean

- Tocar "Tu próximo partido" abre el torneo, no el partido: todavía no hay pantalla de partido para el jugador.
- Que la multa traiga el nombre del torneo: se pide como spec del backend si hace falta.
- Editar el perfil deportivo: spec propia. `PUT /api/supercopa/mi-perfil` ya existe.

### 11.5 Decisiones del 14-nov-2026 (el código adelantó a la spec)

La spec se escribió el 9-nov. Entre el 10 y el 11 llegaron a `main` Explorar y el detalle del torneo (MOV-0005), las secciones (MOV-0007 a 0009) y el Inicio provisional con su diseño (MOV-0010). David aprobó estos tres ajustes:

1. **La pestaña Explorar abre la pantalla real,** no un estado vacío.
2. **"Mis equipos" y "Tu próximo partido" abren el detalle del torneo,** que ya existe.
3. **No hay pantallas "en construcción"** y el Inicio provisional se reemplaza por este, **conservando el diseño de sus bloques de sección.** Por eso "Tus secciones" no usa las filas simples del borrador (§3.3): quitar el partido que sigue o el torneo en espera habría sido perder algo que ya funciona.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
| 2026-11-09 | Reescrita con el contrato real del backend (0026 y 0029 ya están en `main`): `secciones` en `/me`, `yo/equipos`, `mi-perfil` y `multas/mias`. Se agregan las rutas compartidas, los estados, los textos, el plan de pruebas y la lista de archivos |
| 2026-11-14 | Aprobada por David con los tres ajustes de §11.5 y pasa a `en-curso`. Se actualizan el alcance, CA-2, CA-3, CA-15, CA-16, el plan de pruebas y la lista de archivos |
| 2026-11-14 | Implementada. 283 tests, 0 fallos (`evidencias/MOV-0004/02-green.txt`). Las pruebas se escribieron junto con el código, no antes: en lugar de un RED previo se hizo la prueba de dientes de `sdd.md` §6, rompiendo el código a propósito (`01-dientes.txt`, 6 pruebas fallan). El contrato se comprobó contra el backend desplegado con las cuentas del seed. Dentro del bloque Árbitro, "Tu próximo partido" pasa a decir "Próximo que arbitras", para no repetir el título del bloque del jugador. Pasa a `en-revision` |
| 2026-11-14 | Verificada en el emulador (Pixel 8) contra el backend desplegado, con `arbitro02`, `jugador01` y `admin01`: Inicio con sus secciones, el partido de hoy en naranja, "Tu próximo partido", "Mis equipos" (abre el torneo), el estado vacío, Explorar en su pestaña sin flecha, Atrás, Perfil y cerrar sesión. Capturas 03 a 12 en `evidencias/MOV-0004/` (oscuro, claro y letra al 130 %). **Sin verificar en el emulador:** el modo avión (CA-11, cubierto por pruebas), el giro del teléfono y una cuenta recién creada |
