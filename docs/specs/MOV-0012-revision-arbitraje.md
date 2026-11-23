---
id: MOV-0012
titulo: Invitaciones recibidas, planilla solo con alineados y planilla del organizador
tipo: bug + feature
estado: hecha
prioridad: P1
fase: F3
modo: Jugador · Árbitro · Creador
responsable: David
depende_de: [MOV-0009, MOV-0011, backend 0030]
rama: mov-0012-revision-arbitraje
---

# MOV-0012 · Invitaciones recibidas, planilla solo con alineados y planilla del organizador

> **Para quien la implementa:** el backend que usa está en la spec **backend 0030** (rama `feature/0030-planilla-alineados-invitaciones`). Lee antes [`../sdd.md`](../sdd.md) y [`../arquitectura.md`](../arquitectura.md).

## 1. Contexto

Revisión de David de la MOV-0011 en el emulador (16-nov-2026). Tres hallazgos:

1. **La invitación con correo no le llega a nadie.** El organizador invita a un árbitro por su correo, pero en la cuenta del árbitro no aparece nada: hay que pasarle el enlace y pegarlo en Perfil. No fue un descuido de quien probó: el botón no existía. Y no debe depender de las notificaciones futuras.
2. **La planilla deja anotar a cualquiera.**
   - Se le anotó un gol a un jugador que no estaba alineado.
   - Se le siguió anotando a un jugador después de su tarjeta roja.
   - En la alineación, el motivo por el que alguien no puede jugar salía con el código del backend (`MULTA_PENDIENTE`).
3. **La planilla no es solo del árbitro.** Quien organiza el torneo puede operar cualquier partido y, además, corregirlo después de cerrado. El backend ya lo permitía; la app no le daba por dónde entrar.

## 2. Alcance

### 2.1 Invitaciones recibidas

- **Inicio** muestra, antes de "Tus secciones", el bloque **"Te invitaron"** con una fila por invitación pendiente: "Te invitan como árbitro" · torneo · quién invita. Tocarla abre la pantalla Invitación.
- **Invitación** (Perfil → "Tengo una invitación") muestra arriba las invitaciones pendientes de la cuenta, cada una con su botón **"Aceptar"**. No hace falta el enlace.
- Debajo sigue el campo para pegar un enlace: las invitaciones "por enlace" (sin correo) no son de nadie hasta que alguien las abre.
- Si la lista no se puede pedir, las dos pantallas se ven como antes, sin aviso.

### 2.2 A quién se le anota un evento

- La hoja "¿Quién?" solo muestra a los **alineados que siguen en la cancha**, por número de camiseta.
- **Expulsado** es quien ya tiene una tarjeta **azul o roja** en ese partido. No aparece en la hoja; en la alineación sale como "Expulsado en este partido" y su casilla no se puede cambiar.
- **Sin nadie alineado**, la hoja lo dice y ofrece **"Pasar lista"**, que abre la alineación de ese equipo.
- En la alineación, quien no puede jugar muestra el motivo en palabras: "tiene una multa sin pagar" o "está suspendido".
- El backend rechaza lo mismo (409) por si la planilla que se ve está vieja; su mensaje sale como aviso.

### 2.3 La planilla del organizador

- En **Partidos** del torneo, tocar un partido abierto abre la hoja "Partido", que ahora empieza con **"Abrir planilla"** y sigue con los árbitros.
- Un partido **cerrado** (finalizado o W.O.) abre la planilla directo.
- En la planilla de un partido finalizado, quien organiza ve **"Reabrir partido"**. Tras confirmar, el partido vuelve a estar en juego y se puede corregir y cerrar otra vez. Un árbitro no ve ese botón.
- Al volver de la planilla, la lista de partidos se actualiza.

## 3. Diseño

- **Componentes:** los de `presentacion/comun/` (`Fila`, `EncabezadoBloque`, `EstadoVacio`, `BotonPrimario`, `BotonContorno`), como en MOV-0009 y MOV-0011.
- **"Reabrir partido" va en contorno,** no en naranja: es una corrección, no la acción del día. Ocupa el lugar de "Cerrar partido".
- **"Aceptar" de una invitación sí va en naranja:** es la acción principal de esa pantalla.

### Textos (todos en `strings.xml`)

| Dónde | Texto |
|---|---|
| Inicio | "Te invitaron" |
| Invitación, con pendientes | "¿Te pasaron otro enlace? Pégalo aquí." |
| Hoja del evento, sin alineados | "Aún no hay nadie alineado" · "Pasa lista de este equipo: solo a quien está alineado se le anota algo." · "Pasar lista" |
| Hoja del evento, todos expulsados | "No queda nadie en la cancha" · "Los alineados de este equipo ya fueron expulsados. Si falta alguien, pasa lista." |
| Alineación | "Expulsado en este partido" · "No puede jugar: tiene una multa sin pagar" · "No puede jugar: está suspendido" |
| Hoja del partido | "Partido" · "Abrir planilla" · "Alineación, goles, tarjetas y cierre" |
| Planilla cerrada (organizador) | "Reabrir partido" · "¿Reabrir el partido?" · "Vuelve a estar en juego para que corrijas la planilla. Cuando termines, ciérralo otra vez: las tarjetas nuevas generan su multa y las que ya la tenían no se repiten." · "Partido reabierto. Ciérralo cuando termines de corregir." |

## 4. Contrato con el backend (backend 0030)

| Endpoint | Método | Responde | Errores esperados |
|---|---|---|---|
| `/api/auth/mis-invitaciones` | GET | `[{ token, rol, ambito, ambitoDescripcion, invitadoPor }]` | — |
| `/api/supercopa/arbitro/partidos/{id}/planilla` | GET | suma `puedeReabrir` y `jugadores[].expulsado` | — |
| `/api/supercopa/admin/partidos/{id}/eventos` | POST | — | 409 `JUGADOR_NO_ALINEADO` · 409 `JUGADOR_EXPULSADO` |
| `/api/supercopa/admin/partidos/{id}/alineacion/{personaId}` | DELETE | — | 409 `JUGADOR_CON_EVENTOS` |
| `/api/supercopa/admin/partidos/{id}/reabrir` | POST | el partido (no se usa) | 403 si no organiza el torneo |

- **Con un backend sin la 0030** la app no falla: no hay invitaciones en Inicio, nadie sale como expulsado y no aparece "Reabrir partido". La hoja del evento ya filtra por alineados, porque ese dato existía.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** que me invitaron por mi correo, **cuando** abro Inicio o Invitación, **entonces** veo la invitación con su rol, su torneo y quién invita, y la acepto con un toque, sin enlace. El doble toque no acepta dos veces; si falla, el mensaje sale junto a esa invitación. Si la lista no carga, las pantallas se ven como antes.
2. **CA-2** · **Dado** un equipo, **cuando** abro la hoja de un evento, **entonces** solo veo a sus alineados. Sin alineados, la hoja ofrece "Pasar lista", que abre la alineación de ese equipo.
3. **CA-3** · **Dado** un jugador con tarjeta azul o roja en el partido, **entonces** no aparece en la hoja del evento y en la alineación sale como expulsado, sin poder cambiarlo.
4. **CA-4** · **Dado** que el backend rechaza un evento o un cambio de alineación (`JUGADOR_NO_ALINEADO`, `JUGADOR_EXPULSADO`, `JUGADOR_CON_EVENTOS`), **entonces** se ve su mensaje y la planilla se vuelve a pedir.
5. **CA-5** · **Dado** que organizo el torneo, **cuando** toco un partido en Partidos, **entonces** puedo abrir su planilla: desde la hoja si está abierto, directo si está cerrado. Un partido sin los dos equipos o en descanso no la ofrece. Al volver, la lista se actualiza.
6. **CA-6** · **Dado** un partido finalizado, **entonces** solo quien organiza ve "Reabrir partido"; al confirmar se envía el `POST`, la planilla queda editable y se avisa. Si falla, sigue cerrado y se ve el mensaje.
7. **CA-7** · Lo nuevo se ve bien en claro, en oscuro y con la letra al 130 % (emulador).

## 6. Fuera de alcance / qué NO debe cambiar

- **Rechazar una invitación.** La que no se acepta queda pendiente hasta que el organizador la cancele.
- **Notificaciones** (push o bandeja).
- **Doble amarilla automática:** el árbitro anota la roja.
- **W.O., cancelar o programar un partido** desde la gestión.
- **No cambian** el flujo de pegar el enlace, pasar lista, cerrar el partido ni la sección Árbitro.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) | Nivel |
|---|---|---|
| CA-1 | `RevisionArbitrajeRepositoryTest › CA-1 …` (2), `InvitacionViewModelTest › MOV-0012 CA-1 …` (3), `InicioViewModelTest › MOV-0012 CA-1 …` (2) | Red + ViewModel |
| CA-2 | `PartidosDeArbitroTest › CA-4 solo se ofrecen los alineados…`, `PlanillaViewModelTest › MOV-0012 CA-2 …` | Dominio + ViewModel |
| CA-3 | `PartidosDeArbitroTest › CA-4 …`, `PlanillaViewModelTest › MOV-0012 CA-3 …`, `RevisionArbitrajeRepositoryTest › CA-3 y CA-6 …` | Dominio + ViewModel + Red |
| CA-4 | `RevisionArbitrajeRepositoryTest › CA-4 …`, `PlanillaViewModelTest › MOV-0012 CA-4 …` | Red + ViewModel |
| CA-5 | `GestionTorneoTest › MOV-0012 CA-5 …`, `PartidosViewModelTest › MOV-0012 CA-5 …` y emulador | Dominio + ViewModel + emulador |
| CA-6 | `PlanillaViewModelTest › MOV-0012 CA-6 …` (3), `RevisionArbitrajeRepositoryTest › CA-6 …` (2) | ViewModel + Red |
| CA-7 | Capturas del emulador | Emulador |

## 8. Verificación en el emulador

**Hace falta el backend con la 0030** (integrada a `main` y desplegada en Render, o corriendo en local).

1. Con `admin01`, Creador → un torneo → Árbitros → invita con el correo de otra cuenta.
2. Con esa cuenta: Inicio muestra "Te invitaron". Tócala y acepta. Inicio muestra la sección Árbitro.
3. Abre una planilla, elige un equipo sin alinear y toca "Gol": la hoja ofrece "Pasar lista".
4. Alinea a tres, anota una roja a uno: ya no sale en la hoja y en la alineación dice "Expulsado en este partido".
5. Con `admin01`: Partidos → toca un partido → "Abrir planilla". Cierra el partido, vuelve a entrar y toca "Reabrir partido".

## 9. Archivos afectados

**Nuevos:** esta spec, `RevisionArbitrajeRepositoryTest.kt` y `docs/specs/evidencias/MOV-0012/`.

**Modificados**

- `domain/model/Planilla.kt` (`expulsado`, `puedeReabrir`, `candidatos()`), `GestionTorneo.kt` (`tienePlanilla`)
- `domain/repository/ArbitrajeRepository.kt`, `InvitacionesRepository.kt`
- `domain/use_case/competicion/ArbitrajeUseCases.kt` (`ReabrirPartidoUseCase`), `identidad/InvitacionUseCases.kt` (`ObtenerMisInvitacionesUseCase`)
- `data/remote/api/ArbitrajeApi.kt`, `AuthApi.kt`; `data/remote/dto/ArbitrajeDtos.kt`, `GestionTorneoDtos.kt`
- `data/repository/ArbitrajeRepositoryImpl.kt`, `InvitacionesRepositoryImpl.kt`
- `di/AppContainer.kt`
- `presentacion/arbitro/PlanillaViewModel.kt`, `PlanillaScreen.kt`, `AlineacionHoja.kt`
- `presentacion/creador/PartidosTorneoViewModel.kt`, `PartidosTorneoScreen.kt`
- `presentacion/perfil/InvitacionViewModel.kt`, `InvitacionScreen.kt`
- `presentacion/inicio/InicioViewModel.kt`, `InicioScreen.kt`; `presentacion/principal/PrincipalScreen.kt`
- `presentacion/navigation/AppNavigation.kt`
- `res/values/strings.xml`
- Pruebas y `testutil/FakesArbitraje.kt`, `FakesGestion.kt`

**`testTag` nuevos:** `inicio_invitacion`, `invitacion_pendiente`, `invitacion_aceptar_pendiente`, `jugador_vacio`, `jugador_pasar_lista`, `partido_planilla`, `planilla_reabrir`, `reabrir_confirmar` y `reabrir_cancelar`.

## 10. Notas y decisiones

- **La hoja del evento filtra en la app y el backend lo vuelve a comprobar.** Antes la app ofrecía a todos los aptos porque el backend no exigía alinear; esa nota de MOV-0009 queda sin efecto.
- **Las invitaciones de Inicio cargan en silencio.** Son un extra de la pantalla: si fallan no deben tapar el resto ni sacar un aviso.
- **Un partido cerrado no abre la hoja de árbitros** (ya no se le cambian), así que el toque queda libre para la planilla.
- **Cambia la MOV-0011 §2.3:** "Un partido ya cerrado no se puede tocar" pasa a "abre su planilla".

## 11. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-16 | Redactada e implementada por la revisión de David de la MOV-0011. Pasa a `en-revision` |
| 2026-11-16 | 374 pruebas, 0 fallos (19 nuevas); `assembleDebug` en verde |
| 2026-11-16 | Verificada en el emulador **en parte**, contra el backend de Render, que aún **no tiene la 0030**, con la sesión que estaba abierta (árbitro y creadora de "Prueba MOV-0011"). **Visto:** Inicio carga sin el bloque de invitaciones y sin error; en un partido sin alineados, "Gol" abre la hoja con "Aún no hay nadie alineado" y "Pasar lista" abre la alineación de ese equipo (capturas 01 y 02, oscuro). **Sin ver en el emulador:** el bloque "Te invitaron" y aceptar sin enlace (CA-1), el expulsado (CA-3), los 409 nuevos (CA-4), "Abrir planilla" desde Partidos (CA-5: el torneo de esa cuenta no tiene partidos) y "Reabrir partido" (CA-6); tampoco claro ni letra al 130 % (CA-7). Quedan cubiertos por pruebas de dominio, ViewModel y red, y por las 10 del backend. Se completa cuando la 0030 esté desplegada |
| 2026-11-23 | **Integrada a `main`** por decisión de David ("Sí, intégralo todo"), junto con MOV-0011 a MOV-0015 y el backend 0030, con 455 pruebas y 0 fallos. Pasa a `hecha`. Con la 0030 ya en `main`, queda por verificar en el emulador: "Te invitaron" y aceptar sin enlace (CA-1), el expulsado (CA-3), los 409 nuevos (CA-4), "Abrir planilla" desde Partidos (CA-5), "Reabrir partido" (CA-6), y claro con letra al 130 % (CA-7) |
