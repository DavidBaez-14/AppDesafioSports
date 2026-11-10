---
id: MOV-0009
titulo: Sección Árbitro y planilla del partido
tipo: feature
estado: en-revision
prioridad: P1
fase: F2
modo: Árbitro
responsable: David
depende_de: [MOV-0003, backend 0025, backend 0029]
rama: mov-0009-seccion-arbitro
---

# MOV-0009 · Sección Árbitro y planilla del partido

## 1. Contexto

- **Árbitro es una sección,** no un modo (`PRODUCT.md`). La ve quien es árbitro de algún torneo.
- **Los árbitros se asignan por partido.** Solo operan todos los partidos de un torneo si el organizador les da ese permiso (backend 0025).
- **Referencia de diseño:** prototipo `docs/prototipo-roles/index.html`, pantallas `arbitro` y `planilla`.
- **El backend ya existe** (specs 0025 y 0029 en `main` de BackendCodeCup). Esta spec reemplaza el borrador anterior, que lo daba por pendiente.

## 2. Alcance

### 2.1 Sección Árbitro

- **Lista de partidos** en tres bloques: **Hoy**, **Próximos** y **Jugados**.
  - Cada fila: día y hora, "Local vs Visitante", "*torneo* · *lugar*".
  - El partido de hoy sin cerrar lleva el chip naranja "Planilla".
  - Los cerrados llevan "Cerrado 3–2"; los ganados por W.O., "W.O."; los aplazados, "Aplazado".
- **Tocar un partido abre su planilla.** Un partido cuyo cruce aún no está definido no se puede abrir.
- **Estado vacío:** "Aún no tienes partidos asignados".
- **Se actualiza** al deslizar hacia abajo y al volver de la planilla.

### 2.2 Planilla

- **Encabezado:** torneo, fase y lugar; los dos equipos; el marcador en grande y el estado (Programado, En juego, Partido cerrado, W.O., Aplazado).
- **Alineación:** una fila con el conteo ("5 de Halcones FC · 4 de Tiburones FC") que abre una hoja con los dos planteles.
  - Cada jugador se marca o desmarca. "Alinear a todos" marca el plantel completo del equipo elegido.
  - **Un jugador que no puede jugar** (suspendido o con multa pendiente) aparece marcado con el motivo y no se puede alinear.
  - Se ve el número de camiseta y, si existe, el documento, para verificar la identidad en la cancha.
- **Registrar un evento:** se elige el equipo, se toca el tipo de evento y, en la hoja que se abre, el jugador.
  - **Los tipos de evento los manda el backend** (`tiposEvento`). Hoy son los de fútbol sala: gol, amarilla, azul y roja.
  - Al registrar aparece "Gol de *jugador* registrado" con **Deshacer**.
- **Eventos en orden,** sin minuto, cada uno con **Quitar** (pide confirmación).
- **Cerrar partido:** pide confirmación mostrando el marcador. Al cerrar, la planilla queda de solo lectura.
- **Planilla de solo lectura** cuando `editable` es `false`: no hay botones de evento, alineación editable ni "Cerrar partido".

## 3. Diseño

- **Referencia:** prototipo `arbitro` y `planilla`; tokens de [`../../DESIGN.md`](../../DESIGN.md).
- **Marcador:** cifras en Barlow Condensed (`displayLarge`), con ancho fijo.
- **El naranja** va en "Cerrar partido" (la acción principal) y en lo que pasa ahora: el chip "Planilla" y "En juego".
- **Tarjetas:** cada tipo de evento se pinta con su color e icono desde `ui/deporte/` (regla 8 de la arquitectura). Un tipo desconocido usa el caso genérico y muestra su código.

| Pantalla | Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|---|
| Sección Árbitro | Indicador centrado | "Aún no tienes partidos asignados" | Mensaje + "Reintentar" | Aviso de sin conexión + "Reintentar" | Los tres bloques |
| Planilla | Indicador centrado | "Aún no hay eventos. Elige el equipo y toca lo que pasó." | Mensaje + "Reintentar"; si es `NO_ARBITRO_DEL_PARTIDO`, mensaje + "Volver" | Igual que el error | Marcador, alineación, eventos |

- **Si una acción falla** (registrar, quitar, alinear, cerrar), la planilla no cambia y el mensaje del backend sale en un `Snackbar`. Mientras una acción está en curso, los controles no aceptan otro toque.
- **Accesibilidad:** objetivos de 48 dp o más; los botones de evento miden 76 dp de alto; el marcador se lee como "Halcones FC 3, Tiburones FC 2"; letra al 130 % sin cortes.

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde | Errores esperados → AppError |
|---|---|---|---|---|
| `/api/supercopa/arbitro/partidos` | GET | — | `List<ArbitroPartidoDto>`, en orden de fecha. Vacía si no arbitra nada | 401 → `NoAutenticado` |
| `/api/supercopa/arbitro/partidos/{partidoId}/planilla` | GET | — | `PlanillaDto` | 403 `NO_ARBITRO_DEL_PARTIDO` → `Negocio` · 404 `NO_ENCONTRADO` → `NoEncontrado` |
| `/api/supercopa/admin/partidos/{partidoId}/eventos` | POST | `{personaId, equipoTorneoId, tipoEvento}` | `EventoDto` | 400 `REGLA_NEGOCIO` → `Negocio` (partido cerrado, tipo inválido…) |
| `/api/supercopa/admin/partidos/{partidoId}/eventos/{eventoId}` | DELETE | — | 204 | 404 → `NoEncontrado` |
| `/api/supercopa/admin/partidos/{partidoId}/alineacion` | POST | `{personaId, equipoTorneoId}` | 200 sin cuerpo | 400 `REGLA_NEGOCIO` → `Negocio` ("Jugador no apto: …") |
| `/api/supercopa/admin/partidos/{partidoId}/alineacion/{personaId}` | DELETE | — | 204 | 404 → `NoEncontrado` |
| `/api/supercopa/admin/partidos/{partidoId}/alineacion/add-all` | POST | `{equipoTorneoId}` | 200 sin cuerpo | 400 `REGLA_NEGOCIO` |
| `/api/supercopa/admin/partidos/{partidoId}/cerrar` | POST | — | el partido (no se usa) | 400 `REGLA_NEGOCIO` |

- **Las rutas de acción llevan `admin` en el nombre, pero las autoriza el mismo permiso de árbitro del partido.**
- **Después de cada acción la app vuelve a pedir la planilla.** El marcador, la alineación y la lista siempre son los del backend.
- **Registrar un evento en un partido programado lo pasa a `EN_CURSO`.** No hace falta alinear antes: al cerrar, el backend cuenta como jugado a todo el plantel activo que no se haya alineado.
- **Ids:** UUID, `String` en la app. **Fechas:** `2026-11-12T16:00:00`, sin zona.

```json
{
  "partido": {
    "partidoId": "5f0c…", "torneoId": "9a1b…", "torneo": "Liga Relámpago (prueba)", "deporte": "FUTSAL",
    "fase": "Fecha 1", "faseCodigo": "GRUPOS", "jornada": 1, "grupo": null,
    "local": { "equipoTorneoId": "c2d3…", "nombre": "Halcones FC" },
    "visitante": { "equipoTorneoId": "e4f5…", "nombre": "Tiburones FC" },
    "fechaHora": "2026-11-12T16:00:00", "lugar": "Cancha 1", "estado": "EN_CURSO",
    "marcador": { "local": 1, "visitante": 0 }, "asignado": true
  },
  "tiposEvento": ["GOL", "AMARILLA", "AZUL", "ROJA"],
  "local": { "equipoTorneoId": "c2d3…", "nombre": "Halcones FC", "jugadores": [
    { "personaId": "7e8f…", "nombre": "Juan Castro", "numeroCamiseta": 10, "numeroDocumento": "9000000301",
      "esDelegado": false, "alineado": true, "goles": 1, "apto": true, "motivos": [] }
  ] },
  "visitante": { "equipoTorneoId": "e4f5…", "nombre": "Tiburones FC", "jugadores": [] },
  "eventos": [
    { "id": "ab12…", "orden": 1, "personaId": "7e8f…", "jugadorNombre": "Juan Castro",
      "equipoTorneoId": "c2d3…", "equipoNombre": "Halcones FC", "tipoEvento": "GOL" }
  ],
  "editable": true
}
```

- `partido.local` y `partido.visitante` son `null` mientras el cruce no está definido. `marcador` es `null` mientras está `PROGRAMADO`.
- `estado`: `PROGRAMADO`, `EN_CURSO`, `FINALIZADO`, `APLAZADO` o `WO`.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** que arbitro varios partidos, **cuando** abro la sección, **entonces** los veo en Hoy, Próximos y Jugados según su fecha y su estado, y el de hoy sin cerrar lleva el chip "Planilla".
2. **CA-2** · **Dado** que no arbitro ningún partido, **entonces** veo el estado vacío.
3. **CA-3** · **Dado** un partido con el cruce definido, **cuando** lo toco, **entonces** se abre su planilla. **Dado** uno sin cruce, **entonces** no se puede tocar.
4. **CA-4** · **Dado** que la planilla es editable, **cuando** elijo equipo, tipo de evento y jugador, **entonces** se envía el evento, la planilla se vuelve a pedir, el evento queda al final de la lista y el marcador es el que responde el backend.
5. **CA-5** · **Dado** que acabo de registrar un evento, **cuando** toco "Deshacer", **entonces** ese evento se borra en el backend y desaparece de la lista.
6. **CA-6** · **Dado** un evento de la lista, **cuando** toco "Quitar" y confirmo, **entonces** se borra. Si cancelo, no pasa nada.
7. **CA-7** · **Dado** un jugador apto, **cuando** lo marco en la alineación, **entonces** queda alineado; al desmarcarlo, deja de estarlo. **Dado** un jugador no apto, **entonces** aparece con su motivo y no se puede marcar.
8. **CA-8** · **Dado** un equipo, **cuando** toco "Alinear a todos", **entonces** se pide al backend y la alineación se actualiza.
9. **CA-9** · **Dado** que toco "Cerrar partido", **entonces** veo la confirmación con el marcador. Al confirmar, el partido se cierra y la planilla queda de solo lectura con "Partido cerrado".
10. **CA-10** · **Dado** que `editable` es `false`, **entonces** no hay botones de evento, ni "Quitar", ni "Cerrar partido", y la alineación solo se consulta.
11. **CA-11** · **Dado** que una acción falla, **entonces** veo el mensaje del backend, la planilla no cambia y puedo seguir.
12. **CA-12** · **Dado** que abro la planilla de un partido que no arbitro (403 `NO_ARBITRO_DEL_PARTIDO`), **entonces** veo el aviso con "Volver".
13. **CA-13** · **Dado** un tipo de evento o un estado que la app no conoce, **entonces** se pinta con el caso genérico y la app no se cierra.
14. **CA-14** · **Dado** que una acción está en curso, **cuando** toco otra vez, **entonces** no se envía una segunda petición.
15. **CA-15** · Sin conexión se ve el aviso con "Reintentar"; claro, oscuro y letra al 130 % sin cortes (emulador).

## 6. Fuera de alcance / qué NO debe cambiar

- **W.O. y cancelar partido.** Van en una spec propia; el menú "Más opciones" no se muestra para no dejar botones muertos.
- **Planilla sin conexión,** deportes con marcador por sets y minuto de los eventos.
- **Asignar árbitros o programar partidos** (es de la sección Creador).
- **No cambia** nada del acceso ni del diagnóstico, salvo el acceso provisional de §11.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) | Nivel |
|---|---|---|
| CA-1, CA-2, CA-3 | `PartidosDeArbitroTest › CA-1 …`, `SeccionArbitroViewModelTest › CA-1 …`, `› CA-2 …`, `› CA-3 …` | Dominio y ViewModel |
| CA-1, CA-13 | `ArbitrajeRepositoryImplTest › CA-1 convierte la lista de partidos`, `› CA-13 un estado desconocido no falla` | Red (MockWebServer) |
| CA-4 | `PlanillaViewModelTest › CA-4 registrar un evento lo envia y recarga`; `ArbitrajeRepositoryImplTest › CA-4 registrar evento envia POST con el cuerpo` | ViewModel y red |
| CA-5, CA-6 | `PlanillaViewModelTest › CA-5 deshacer borra el evento recien creado`, `› CA-6 quitar pide confirmacion` | ViewModel |
| CA-7, CA-8 | `PlanillaViewModelTest › CA-7 …`, `› CA-8 …`; `ArbitrajeRepositoryImplTest › CA-7 …`, `› CA-8 …` | ViewModel y red |
| CA-9, CA-10 | `PlanillaViewModelTest › CA-9 cerrar pide confirmacion y deja la planilla de solo lectura`, `› CA-10 …` | ViewModel |
| CA-11, CA-12 | `PlanillaViewModelTest › CA-11 …`, `› CA-12 …`; `ArbitrajeRepositoryImplTest › CA-12 …` | ViewModel y red |
| CA-14 | `PlanillaViewModelTest › CA-14 el doble toque no envia dos veces` | ViewModel |
| CA-15 | Capturas del emulador | Emulador |

## 8. Verificación en el emulador

1. Entrar con `arbitro01@prueba.local` y abrir la sección Árbitro.
2. Abrir el partido de "Liga Relámpago (prueba)": alinear, registrar un gol y una amarilla, deshacer uno, quitar otro.
3. Cerrar el partido y comprobar que queda de solo lectura y aparece en "Jugados" con su marcador.
4. Capturas en `docs/specs/evidencias/MOV-0009/`: sección y planilla en oscuro, claro y letra al 130 %.

## 9. Archivos afectados

**Nuevos**

- `domain/model/`: `PartidoArbitro.kt`, `Planilla.kt`
- `domain/repository/ArbitrajeRepository.kt`
- `domain/use_case/competicion/`: `ObtenerPartidosDeArbitroUseCase.kt`, `ObtenerPlanillaUseCase.kt`, `RegistrarEventoUseCase.kt`, `QuitarEventoUseCase.kt`, `AlinearJugadorUseCase.kt`, `QuitarDeAlineacionUseCase.kt`, `AlinearEquipoUseCase.kt`, `CerrarPartidoUseCase.kt`
- `data/remote/api/ArbitrajeApi.kt`, `data/remote/dto/ArbitrajeDtos.kt`, `data/repository/ArbitrajeRepositoryImpl.kt`
- `presentacion/arbitro/`: `SeccionArbitroScreen.kt`, `SeccionArbitroState.kt`, `SeccionArbitroViewModel.kt`, `PlanillaScreen.kt`, `PlanillaState.kt`, `PlanillaViewModel.kt`, `AlineacionHoja.kt`
- `presentacion/comun/`: `Componentes.kt` (encabezado de bloque, fila, chip de estado, vacío, error con reintentar, cargando) y `MensajesDeError.kt`
- `ui/deporte/`: `Deportes.kt`, `TiposEvento.kt`
- `ui/formato/Formatos.kt` (fechas, horas y pesos)
- `ui/theme/ColoresExtra.kt` (éxito, advertencia y colores de tarjeta, en los dos temas)
- `res/drawable/ic_*.xml` (iconos nuevos)
- Las pruebas de §7

**Modificados**

- `di/AppContainer.kt`, `presentacion/navigation/Rutas.kt` y `AppNavigation.kt` (rutas `SeccionArbitro` y `Planilla(partidoId)`)
- `ui/theme/Color.kt` y `Theme.kt` (provee los colores extra)
- `presentacion/acceso/MensajesDeError.kt` (se mueve a `presentacion/comun/`)
- `presentacion/diagnostico/DiagnosticoScreen.kt` (acceso provisional, §11)
- `res/values/strings.xml`, `testutil/Fakes.kt`, `docs/specs/README.md`

## 10. Datos de prueba

- `arbitro01@prueba.local` y `arbitro02@prueba.local`: árbitros de "Liga Relámpago (prueba)". El seed les programa un partido para el día siguiente a correrlo, a las 4:00 p. m.
- `jugador01@prueba.local`: no arbitra nada (estado vacío).
- **Casos extremos:** nombres de equipo largos, plantel de 12 o más jugadores, 20 o más eventos, jugador sin número de camiseta, jugador no apto, partido sin fecha.

## 11. Notas y decisiones

- **Acceso provisional.** Inicio llega con MOV-0004. Hasta entonces, el diagnóstico (que hoy es la pantalla después de entrar) lleva un bloque "Secciones" con el acceso a la sección Árbitro. Se quita cuando MOV-0004 esté en `main`.
- **Rutas:** `SeccionArbitro` y `Planilla(partidoId: String)`, los nombres que fija MOV-0004 §11.1.
- **"Quitar" pide confirmación en lugar de ofrecer "Deshacer".** El backend no puede devolver un evento a su posición: al recrearlo quedaría al final.
- **Jugadores que se ofrecen al registrar un evento:** todo el plantel apto, con los alineados primero. El backend no exige alinear antes.
- **`ArbitrajeApi` en lugar de una sola `CompeticionApi`:** una interfaz por sección evita choques entre las ramas que se trabajan en paralelo.
- **Componentes compartidos:** `presentacion/comun/`, `ui/deporte/` y `ui/formato/` nacen aquí. MOV-0004 y las demás los reutilizan.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
| 2026-11-09 | Reescrita con el contrato real del backend (0025 y 0029 en `main`). W.O. y cancelar salen del alcance. Implementada: pasa a `en-revision` |
