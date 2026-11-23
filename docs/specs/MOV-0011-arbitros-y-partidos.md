---
id: MOV-0011
titulo: Gestión del torneo, árbitros y partidos automáticos
tipo: feature
estado: hecha
prioridad: P1
fase: F3
modo: Creador
responsable: David
depende_de: [MOV-0004, MOV-0008, MOV-0009, backend 0024, backend 0025]
rama: mov-0011-arbitros-y-partidos
---

# MOV-0011 · Gestión del torneo, árbitros y partidos automáticos

> **Para quien la implementa:** todo el backend que usa **ya existe** en `main` de BackendCodeCup (specs 0024 y 0025). Lee antes [`../sdd.md`](../sdd.md) y [`../arquitectura.md`](../arquitectura.md).

## 1. Contexto

- **Hoy un torneo creado en la app no se puede poner a jugar desde la app.** El organizador crea el torneo (MOV-0007) y aprueba inscripciones (MOV-0008), pero para generar los partidos, invitar árbitros y asignarlos tiene que usar Swagger. La sección Árbitro (MOV-0009) existe, pero nadie llega a ella sin ese paso.
- **Esta spec cierra el ciclo** crear torneo → inscribir → **generar partidos y poner árbitros** → planilla.
- **Pedido de David (16-nov-2026):** "asignar árbitros y fixture automático".
- **Vocabulario:** en la interfaz se dice **"partidos"**, no "fixture".

## 2. Alcance

### 2.1 Gestión del torneo

- Pantalla nueva `GestionTorneo`, con tres filas: **Inscripciones** (MOV-0008), **Árbitros** y **Partidos**.
- **Se entra** al tocar un torneo de "Mis torneos" en la sección Creador y desde la franja "Organizas este torneo" del detalle del torneo. Antes, las dos abrían las inscripciones.
- **"Por revisar"** (sección Creador e Inicio) sigue abriendo las inscripciones directo: es el atajo a lo urgente.

### 2.2 Árbitros del torneo

- **Lista** de los árbitros del torneo, con su nombre y su correo.
- **"Arbitra todos los partidos":** un interruptor por árbitro. Con él puede operar la planilla de cualquier partido del torneo sin que se le asigne uno por uno.
- **Quitar** a un árbitro, con confirmación: pierde también los partidos que tenía asignados.
- **"Invitar árbitro":** un diálogo con el correo, **opcional**.
  - Con correo, solo esa cuenta puede aceptar. Si esa persona todavía no tiene cuenta, la invitación se aplica sola cuando se registre con ese correo.
  - Sin correo, la acepta la primera persona que abra el enlace.
  - Al crearla se abre la hoja de compartir de Android con el enlace.
- **Invitaciones pendientes:** cada una con su correo (o "Cualquiera con el enlace"), "Compartir" y "Cancelar".

### 2.3 Partidos del torneo

- **Sin partidos:** la pantalla dice cuántos equipos aprobados hay y ofrece **"Generar partidos"**.
  - Hacen falta **2 equipos aprobados** como mínimo. Con menos, el botón está deshabilitado y la pantalla dice por qué.
  - Si el torneo tiene las inscripciones abiertas, generar los partidos **lo inicia**: un diálogo avisa que el torneo empieza y deja de recibir inscripciones.
  - Los partidos salen según el formato del torneo (liga, eliminación directa o grupos y eliminatorias), con fechas de partida que pone el backend.
- **Con partidos:** agrupados por fase y jornada. Cada uno muestra los equipos, la fecha, y sus árbitros o el chip "Sin árbitro".
- **Asignar árbitros:** tocar un partido abre una hoja con los árbitros del torneo. Marcar asigna, desmarcar quita.
  - Quien arbitra todos los partidos aparece marcado y fijo.
  - Si el torneo no tiene árbitros, la hoja ofrece "Invitar árbitros".
  - Un partido ya cerrado no se puede tocar.

### 2.4 Aceptar una invitación

- En **Perfil → Cuenta**, la fila **"Tengo una invitación"** abre la pantalla `Invitacion`.
- La persona **pega el enlace** (o el código) que recibió. La app muestra qué es la invitación (rol, torneo y quién invita) y el botón "Aceptar".
- Al aceptar, la pantalla muestra la confirmación con "Listo", que vuelve a Perfil. Inicio muestra la sección nueva la próxima vez que se ve.

## 3. Diseño

### 3.1 Referencia

- **Tokens y componentes:** [`../../DESIGN.md`](../../DESIGN.md) y los de `presentacion/comun/` (`Fila`, `EncabezadoBloque`, `ChipEstado`, `EstadoVacio`, `EstadoError`, `BotonPrimario`, `BotonContorno`). Mismo lenguaje que `InscripcionesScreen`.
- **El naranja** se usa solo para la acción principal de cada pantalla: "Invitar árbitro", "Generar partidos" y "Aceptar".

### 3.2 Estados de cada pantalla

| Pantalla | Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|---|
| Árbitros | Indicador | "Aún no hay árbitros" + "Invitar árbitro" | Mensaje + "Reintentar"; en un torneo ajeno, "Volver" | Texto de sin conexión + "Reintentar" | Árbitros e invitaciones |
| Partidos | Indicador | "Aún no hay partidos" + equipos aprobados + "Generar partidos" | Igual que Árbitros | Igual | Partidos por fase y jornada |
| Invitación | "Buscando…" en el botón | El campo para pegar el enlace | El mensaje bajo el campo | El mensaje bajo el campo | Vista previa + "Aceptar" |

- **Si falla una acción** (invitar, quitar, asignar, generar), lo que había en pantalla se conserva y el mensaje del backend sale en un `Snackbar`.

### 3.3 Textos y reglas de presentación

Todos van a `strings.xml`.

- **Encabezado de cada grupo de partidos:**

| `fase` | `grupo` | Encabezado |
|---|---|---|
| `GRUPOS` | `null` | "Jornada *n*" |
| `GRUPOS` | "A" | "Grupo A · Jornada *n*" |
| `REPECHAJE` | — | "Repechaje" |
| `OCTAVOS` · `CUARTOS` · `SEMIS` | — | "Octavos de final" · "Cuartos de final" · "Semifinales" |
| `TERCER_PUESTO` · `FINAL` | — | "Tercer puesto" · "Final" |
| otro | — | el código legible ("Fase nueva") |

- **Orden de los grupos:** `GRUPOS` (por jornada y luego por grupo), `REPECHAJE`, `OCTAVOS`, `CUARTOS`, `SEMIS`, `TERCER_PUESTO`, `FINAL`. Dentro de cada grupo, por fecha.
- **Equipos:** "Local vs Visitante". Un equipo que aún no se conoce (cruces de eliminatoria) es "Por definir".
- **Estado del partido:** `FINALIZADO` muestra el marcador; `WO`, "W.O."; `EN_CURSO`, "En juego"; `APLAZADO`, "Aplazado"; `DESCANSO`, "Descansa".
- **Motivo por el que no se pueden generar los partidos:**

| Caso | Texto |
|---|---|
| Menos de 2 equipos aprobados | "Necesitas al menos 2 equipos aprobados. Tienes *n*." |
| Torneo `BORRADOR` | "Publica el torneo antes de generar los partidos." |
| Torneo `FINALIZADO` | "El torneo ya terminó." |

### 3.4 Accesibilidad

- Objetivos de 48 dp como mínimo y contraste AA. Letra al 130 % sin cortes.
- Cada fila se lee completa en TalkBack. El interruptor dice a quién pertenece.
- `testTag` en todo control interactivo (§9).

## 4. Contrato con el backend

**Todo existe.** Las rutas `admin/torneos/{torneoId}/…` y `admin/partidos/{partidoId}/…` las autoriza el rol ADMIN de ese torneo: en un torneo ajeno responden 403 `NO_ADMIN_DEL_TORNEO` o `ACCESO_DENEGADO`.

| Endpoint | Método | Envía | Responde | Errores esperados |
|---|---|---|---|---|
| `/api/supercopa/admin/torneos/{torneoId}/roles` | GET | — | `List<RolTorneoDto>` | 403 |
| `/api/supercopa/admin/torneos/{torneoId}/roles/{rolId}` | DELETE | — | 204 | 409 `ULTIMO_ADMIN_DEL_TORNEO` (no aplica a árbitros) |
| `/api/supercopa/admin/torneos/{torneoId}/arbitros/{cuentaId}/todos-los-partidos` | PUT | `{ "todosLosPartidos": true }` | `RolTorneoDto` | 404 `NO_ES_ARBITRO_DEL_TORNEO` |
| `/api/supercopa/admin/torneos/{torneoId}/invitaciones` | GET | — | `List<InvitacionDto>` (pendientes) | 403 |
| `/api/supercopa/admin/torneos/{torneoId}/invitaciones` | POST | `{ "rol": "ARBITRO", "correo": "…" }` (`correo` opcional) | 201 `InvitacionDto` | 409 `INVITACION_DUPLICADA` · 400 `VALIDACION` |
| `/api/supercopa/admin/torneos/{torneoId}/invitaciones/{invitacionId}` | DELETE | — | 204 | 404 |
| `/api/supercopa/admin/torneos/{torneoId}/configuracion` | GET | — | `TorneoConfigDto` | 403 |
| `/api/supercopa/admin/torneos/{torneoId}/iniciar` | POST | — | el torneo (no se usa) | 400 `REGLA_NEGOCIO` |
| `/api/supercopa/admin/torneos/{torneoId}/fixture` | POST | — | `List<PartidoAdminDto>` | 400 `REGLA_NEGOCIO` con su mensaje |
| `/api/supercopa/admin/torneos/{torneoId}/partidos` | GET | — | `List<PartidoAdminDto>` | 403 |
| `/api/supercopa/admin/partidos/{partidoId}/arbitros` | POST | `{ "cuentaId": "…" }` | `List<ArbitroAsignadoDto>` | 409 `NO_ES_ARBITRO_DEL_TORNEO` · 409 `ARBITRO_EN_PLANTILLA` · 409 `PARTIDO_CERRADO` |
| `/api/supercopa/admin/partidos/{partidoId}/arbitros/{cuentaId}` | DELETE | — | `List<ArbitroAsignadoDto>` | 404 |
| `/api/auth/invitaciones/{token}` | GET | — | `InvitacionVistaDto` (no necesita sesión) | 404 `INVITACION_NO_ENCONTRADA` |
| `/api/auth/invitaciones/{token}/aceptar` | POST | — | la cuenta (no se usa) | 409 `INVITACION_NO_VIGENTE` · 403 `INVITACION_OTRO_CORREO` |

**Respuestas (solo los campos que se usan):**

```json
// RolTorneoDto: la lista trae también a los ADMIN; la app se queda con los ARBITRO
{ "id": "r1", "cuentaId": "c1", "nombre": "Ana Ruiz", "correo": "ana@correo.com",
  "rol": "ARBITRO", "todosLosPartidos": false }

// InvitacionDto: la lista trae también invitaciones de coadministrador y cupos de equipo;
// la app se queda con las de rol ARBITRO
{ "id": "i1", "correo": null, "rol": "ARBITRO", "estado": "PENDIENTE",
  "enlace": "https://…/invitacion/AbC123…", "createdAt": "2026-11-16T10:00:00" }

// TorneoConfigDto
{ "torneoId": "t1", "estado": "PUBLICADO", "formato": "LIGA",
  "equiposAprobados": 4, "fixtureGenerado": false }

// PartidoAdminDto
{ "id": "p1", "fecha": "2026-11-21T08:00:00", "estado": "PROGRAMADO",
  "localNombre": "Halcones FC", "visitanteNombre": "Tiburones FC",
  "golesLocal": 0, "golesVisitante": 0, "fase": "GRUPOS", "jornada": 1, "grupo": null,
  "lugar": null, "arbitros": [{ "cuentaId": "c1", "nombre": "Ana Ruiz" }] }

// InvitacionVistaDto
{ "rol": "ARBITRO", "ambito": "TORNEO", "ambitoDescripcion": "Copa Prueba 2026",
  "estado": "PENDIENTE", "invitadoPor": "Carlos Rojas", "paraCorreo": "a***@correo.com" }
```

- **El enlace de la invitación** apunta a la web (`…/invitacion/{token}`). La app saca el token del enlace; también acepta el token suelto. El token es de 43 caracteres, letras, dígitos, `-` y `_`.
- **Generar los partidos exige el torneo `EN_CURSO`.** Por eso, con el torneo `PUBLICADO`, la app llama primero a `iniciar` y después a `fixture`. Si `iniciar` pasa y `fixture` falla, el torneo queda en curso sin partidos y basta volver a tocar "Generar partidos".
- **`REGLA_NEGOCIO`** trae el texto para mostrar (por ejemplo, la primera ronda no tiene cupos para tantos equipos). La app muestra el `mensaje` tal cual.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** un torneo de "Mis torneos" o la franja de organizador del detalle, **cuando** lo toco, **entonces** se abre la gestión con Inscripciones, Árbitros y Partidos, y cada fila abre su pantalla. "Por revisar" sigue abriendo las inscripciones.
2. **CA-2** · **Dado** que abro Árbitros, **entonces** veo solo los roles `ARBITRO` del torneo, por nombre, y solo las invitaciones pendientes de rol `ARBITRO`.
3. **CA-3** · **Dado** el diálogo "Invitar árbitro", **cuando** confirmo sin correo o con un correo bien escrito, **entonces** se envía el `POST` con `rol: ARBITRO`, la invitación aparece en pendientes y se ofrece compartir su enlace. Un correo mal escrito no viaja y el error sale bajo el campo. Un 409 `INVITACION_DUPLICADA` muestra su mensaje y el diálogo sigue abierto.
4. **CA-4** · **Dado** un árbitro, **cuando** cambio "Arbitra todos los partidos", **entonces** el interruptor cambia al instante y se envía el `PUT`. Si falla, vuelve a como estaba y se ve el mensaje.
5. **CA-5** · **Dado** un árbitro, **cuando** confirmo "Quitar", **entonces** se envía el `DELETE` del rol y sale de la lista. **Dado** una invitación pendiente, **cuando** la cancelo, **entonces** se envía su `DELETE` y sale de la lista.
6. **CA-6** · **Dado** un torneo sin partidos, **entonces** veo los equipos aprobados y "Generar partidos". Con menos de 2 equipos aprobados, o con el torneo en borrador o finalizado, el botón está deshabilitado y se lee el motivo de §3.3.
7. **CA-7** · **Dado** un torneo `PUBLICADO`, **cuando** toco "Generar partidos", **entonces** un diálogo avisa que el torneo empieza; al confirmar se llama a `iniciar` y luego a `fixture`. **Dado** un torneo `EN_CURSO`, **entonces** se llama solo a `fixture`, sin diálogo. El doble toque no genera dos veces. Si el backend responde un error, se ve su mensaje y la pantalla refleja el estado real del torneo.
8. **CA-8** · **Dado** un torneo con partidos, **entonces** los veo agrupados y ordenados como dice §3.3, con "Por definir" donde falta un equipo y con sus árbitros o "Sin árbitro".
9. **CA-9** · **Dado** un partido abierto, **cuando** lo toco, **entonces** se abre la hoja con los árbitros del torneo. Marcar envía el `POST` y desmarcar el `DELETE`; la fila del partido queda con los árbitros que responde el backend. Un 409 `ARBITRO_EN_PLANTILLA` muestra su mensaje y no marca. Quien arbitra todos los partidos sale marcado y fijo. Un partido cerrado no abre la hoja.
10. **CA-10** · **Dado** que el torneo no tiene árbitros, **cuando** abro la hoja de un partido, **entonces** ofrece "Invitar árbitros", que abre la pantalla Árbitros.
11. **CA-11** · **Dado** "Tengo una invitación", **cuando** pego el enlace o el código y toco "Buscar", **entonces** veo el rol, el torneo y quién invita. Un texto que no parece una invitación se avisa sin llamar al backend. Una invitación que no está `PENDIENTE` se muestra sin "Aceptar".
12. **CA-12** · **Dado** la vista previa, **cuando** toco "Aceptar", **entonces** se envía el `POST` y veo la confirmación "Invitación aceptada" con "Listo". El doble toque no acepta dos veces. `INVITACION_OTRO_CORREO` e `INVITACION_NO_VIGENTE` muestran su mensaje.
13. **CA-13** · **Dado** un torneo que no administro, **entonces** Árbitros y Partidos muestran el aviso con "Volver". **Dado** que no hay conexión, **entonces** muestran "Reintentar". Si falla al actualizar, se conserva lo que había.
14. **CA-14** · Las pantallas nuevas se ven bien en claro, en oscuro y con la letra al 130 % (emulador).

## 6. Fuera de alcance / qué NO debe cambiar

- **Cambiar la fecha, la hora o el lugar de un partido** (`PUT …/programacion`): spec propia. Los partidos nacen con las fechas que pone el backend.
- **Borrar o volver a generar los partidos** (`DELETE …/fixture`).
- **Configurar el formato** (grupos, rondas, repechaje), la cuota, los premios y las multas.
- **Invitar coadministradores** y los cupos de equipo.
- **Enlaces que abren la app** (deep links): la invitación se pega a mano.
- **W.O., cancelar o reabrir un partido** desde la gestión.
- **No cambian** `InscripcionesScreen`, la planilla ni la sección Árbitro. De Perfil solo se suma una fila.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) o verificación | Nivel |
|---|---|---|
| CA-1 | Verificación en el emulador | Emulador |
| CA-2 | `GestionRepositoryImplTest › CA-2 …` y `ArbitrosViewModelTest › CA-2 …` | Red + ViewModel |
| CA-3 | `GestionRepositoryImplTest › CA-3 …`, `ArbitrosViewModelTest › CA-3 …` (sin correo, con correo, correo malo, duplicada) | Red + ViewModel |
| CA-4 | `GestionRepositoryImplTest › CA-4 …`, `ArbitrosViewModelTest › CA-4 …` | Red + ViewModel |
| CA-5 | `GestionRepositoryImplTest › CA-5 …`, `ArbitrosViewModelTest › CA-5 …` | Red + ViewModel |
| CA-6 | `GestionTorneoTest › CA-6 …` (un caso por motivo), `PartidosViewModelTest › CA-6 …` | Dominio + ViewModel |
| CA-7 | `GestionTorneoTest › CA-7 …` (el caso de uso), `PartidosViewModelTest › CA-7 …`, `GestionRepositoryImplTest › CA-7 …` | Dominio + ViewModel + Red |
| CA-8 | `GestionTorneoTest › CA-8 …`, `GestionRepositoryImplTest › CA-8 …` | Dominio + Red |
| CA-9 | `PartidosViewModelTest › CA-9 …`, `GestionRepositoryImplTest › CA-9 …` | ViewModel + Red |
| CA-10 | `PartidosViewModelTest › CA-10 …` y emulador | ViewModel + emulador |
| CA-11 | `GestionTorneoTest › CA-11 …`, `InvitacionViewModelTest › CA-11 …`, `GestionRepositoryImplTest › CA-11 …` | Dominio + ViewModel + Red |
| CA-12 | `InvitacionViewModelTest › CA-12 …`, `GestionRepositoryImplTest › CA-12 …` | ViewModel + Red |
| CA-13 | `ArbitrosViewModelTest › CA-13 …`, `PartidosViewModelTest › CA-13 …` | ViewModel |
| CA-14 | Capturas del emulador | Emulador |

- Las pruebas usan implementaciones falsas (`testutil/FakesGestion.kt`), no librerías de mocks.

## 8. Verificación en el emulador

Con el backend de desarrollo y las cuentas del seed:

1. Entra con `admin01`, abre Creador y toca un torneo de "Mis torneos": se abre la gestión.
2. Árbitros: invita sin correo y comparte el enlace. Cambia "Arbitra todos los partidos" de un árbitro.
3. Con otra cuenta, Perfil → "Tengo una invitación": pega el enlace y acepta. Inicio muestra la sección Árbitro.
4. Crea un torneo nuevo, aprueba dos equipos y toca Partidos → "Generar partidos".
5. Toca un partido y asigna un árbitro. Entra con ese árbitro: el partido aparece en su sección.

**Capturas** en `docs/specs/evidencias/MOV-0011/`: gestión, árbitros, partidos sin generar, partidos generados, la hoja de asignar y la invitación; en claro, en oscuro y con letra al 130 %.

## 9. Archivos afectados

**Nuevos**

- `domain/model/GestionTorneo.kt` (árbitros, invitaciones, partidos, estado de los partidos y la invitación recibida)
- `domain/repository/GestionTorneoRepository.kt`, `InvitacionesRepository.kt`
- `domain/use_case/competicion/GestionTorneoUseCases.kt`
- `domain/use_case/identidad/InvitacionUseCases.kt`
- `data/remote/api/GestionTorneoApi.kt`
- `data/remote/dto/GestionTorneoDtos.kt`
- `data/repository/GestionTorneoRepositoryImpl.kt`, `InvitacionesRepositoryImpl.kt`
- `presentacion/creador/GestionTorneoScreen.kt`
- `presentacion/creador/ArbitrosTorneoScreen.kt` y `ArbitrosTorneoViewModel.kt`
- `presentacion/creador/PartidosTorneoScreen.kt` y `PartidosTorneoViewModel.kt`
- `presentacion/perfil/InvitacionScreen.kt` e `InvitacionViewModel.kt`
- Las pruebas de §7 y `testutil/FakesGestion.kt`
- `docs/specs/evidencias/MOV-0011/`

**Modificados**

- `data/remote/api/AuthApi.kt`: ver y aceptar una invitación
- `di/AppContainer.kt`
- `presentacion/navigation/Rutas.kt` y `AppNavigation.kt`: rutas `GestionTorneo`, `ArbitrosTorneo`, `PartidosTorneo` e `Invitacion`
- `presentacion/perfil/PerfilScreen.kt`: la fila "Tengo una invitación"
- `presentacion/creador/SeccionCreadorScreen.kt`: "Por revisar" y "Mis torneos" abren destinos distintos (`onRevisarTorneo` y `onAbrirTorneo`)
- `presentacion/principal/PrincipalScreen.kt`: pasa `onAbrirInvitacion` a Perfil
- `res/values/strings.xml`
- `docs/arquitectura.md` (§3 y §8), `docs/specs/README.md` y esta spec

**`testTag` mínimos:** `gestion_inscripciones`, `gestion_arbitros`, `gestion_partidos`, `arbitros_invitar`, `arbitro_todos`, `arbitro_quitar`, `invitacion_compartir`, `invitacion_cancelar`, `invitar_correo`, `invitar_confirmar`, `partidos_generar`, `partidos_generar_confirmar`, `partido`, `asignar_arbitro`, `asignar_invitar`, `perfil_invitacion`, `invitacion_enlace`, `invitacion_buscar` e `invitacion_aceptar`.

## 10. Datos de prueba

- **Entorno:** el backend de desarrollo (DesafioSportsDev + Render), con las cuentas del seed.
- `admin01@prueba.local` administra "Copa Prueba 2026" y "Liga Relámpago (prueba)", que ya tienen árbitros y partidos.
- **Para probar "Generar partidos"** hace falta un torneo sin partidos con 2 equipos aprobados: se crea uno nuevo y se inscriben dos equipos del seed.
- **Casos extremos** (cubiertos con JSON armado a mano): un árbitro sin nombre, una eliminatoria con equipos por definir, un partido con tres árbitros, una fase que la app no conoce.

## 11. Notas y decisiones

- **Una pantalla de gestión sin red.** `GestionTorneo` solo enlaza. El estado del torneo se pide en Partidos, que es donde hace falta.
- **Iniciar y generar en un solo toque.** El backend separa "iniciar el torneo" de "generar el fixture"; para el organizador es una sola decisión, y el diálogo explica la consecuencia.
- **La app filtra por rol.** `roles` trae también a los administradores e `invitaciones` trae las de coadministrador y los cupos de equipo. Esta spec solo trata árbitros.
- **Aceptar la invitación va en Perfil** y no en Explorar, porque es algo de la cuenta. Explorar ya tiene "Abrir con enlace" para torneos.
- **Los diálogos y la hoja llevan `testTagsAsResourceId`:** son ventanas aparte y sin eso sus `testTag` no se ven desde fuera.
- **El interruptor "Arbitra todos los partidos" va en tinta,** no en naranja: el naranja es de "Invitar árbitro".
- **Asignar árbitros es optimista por fila:** la casilla cambia al tocarla y, si el backend rechaza, vuelve atrás. Mismo criterio que pasar lista (MOV-0009).

### Pendientes que no bloquean

- **El enlace de la invitación sale con `http://localhost:5173/…` en el backend de Render,** porque allá no está definida `FRONTEND_URLS`. Pegarlo en la app funciona igual (solo se usa el token), pero abrirlo en un navegador no. Se arregla poniendo esa variable en Render.

- Programar fecha, hora y lugar de cada partido.
- Volver a generar los partidos si el organizador se equivocó.
- Que el enlace de la invitación abra la app directamente.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-16 | Redactada e implementada por pedido de David ("asignar árbitros y fixture automático"). Las pruebas se escribieron junto con el código. Pasa a `en-revision` |
| 2026-11-16 | 355 tests, 0 fallos (`evidencias/MOV-0011/02-green.txt`). Prueba de dientes: con tres reglas rotas a propósito fallan 6 pruebas (`01-dientes.txt`) |
| 2026-11-16 | Verificada en el emulador contra el backend desplegado, con la sesión de `arbitro02`: se creó el torneo "Prueba MOV-0011" (solo con enlace), se abrió su gestión, se invitó un árbitro por enlace (hoja de compartir), se aceptó la invitación desde Perfil, el árbitro apareció en la lista y se le dio "Arbitra todos los partidos". Partidos mostró el motivo y el botón deshabilitado con 0 equipos. Capturas 03 a 14 (oscuro, y claro con letra al 130 %). **Sin verificar en el emulador:** generar los partidos y asignar árbitros a un partido (CA-7 a CA-10), porque hacen falta dos equipos aprobados y no había a mano la clave de las cuentas del seed; quedan cubiertos por pruebas de dominio, de ViewModel y de red. Tampoco quitar un árbitro ni cancelar una invitación |
| 2026-11-16 | Revisión de David: la invitación con correo no le aparece al invitado, la planilla deja anotar a quien no está alineado o ya fue expulsado, y el organizador no tiene por dónde abrir la planilla. Se resuelve en [MOV-0012](MOV-0012-revision-arbitraje.md) (cambia §2.3: un partido cerrado abre su planilla) |
| 2026-11-23 | **Integrada a `main`** por decisión de David ("Sí, intégralo todo"), junto con MOV-0011 a MOV-0015 y el backend 0030, con 455 pruebas y 0 fallos. Pasa a `hecha`. Sigue sin verificar en el emulador: generar los partidos y asignar árbitros a un partido (CA-7 a CA-10), que necesitan un torneo con dos equipos aprobados |
