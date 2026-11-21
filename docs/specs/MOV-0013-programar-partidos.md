---
id: MOV-0013
titulo: Programar los partidos (fecha, hora y lugar) y volver a generarlos
tipo: feature
estado: en-revision
prioridad: P1
fase: F3
modo: Creador
responsable: David
depende_de: [MOV-0011, MOV-0012, backend 0025]
rama: mov-0013-programar-partidos
---

# MOV-0013 · Programar los partidos (fecha, hora y lugar) y volver a generarlos

> **Para quien la implementa:** el backend que usa **ya existe** en `main` de BackendCodeCup (spec 0025 y el fixture de siempre). No hace falta la 0030. Lee antes [`../sdd.md`](../sdd.md) y [`../arquitectura.md`](../arquitectura.md).

## 1. Contexto

- **Los partidos nacen con las fechas que pone el backend** al generarlos. MOV-0011 dejó fuera cambiarlas, así que hoy el organizador no puede decir "este va el sábado a las 3 en la cancha 2" sin usar Swagger.
- **Si el organizador se equivoca al generar** (aprobó un equipo tarde, por ejemplo), no hay forma de rehacer los partidos desde la app.
- Las dos cosas estaban en "Próximas" del índice y en los pendientes de MOV-0011.

## 2. Alcance

### 2.1 Fecha, hora y lugar

- **En la hoja del partido** (Partidos → tocar un partido abierto), debajo de "Abrir planilla", va la fila **"Fecha y lugar"** con lo que tiene ahora: "Sáb 21 · 3:30 p. m. · Cancha 2", o "Sin lugar".
- **Tocarla cierra la hoja y abre el diálogo "Fecha y lugar":**
  - **Día:** abre el selector de fecha de Material. Deja elegir días pasados, por si se registra un partido que se movió.
  - **Hora:** abre el selector de hora de Material, en formato de 12 horas.
  - **Lugar:** texto opcional, hasta 120 caracteres. Vacío borra el lugar.
  - **"Guardar"** envía y, si sale bien, cierra el diálogo. La fila del partido queda con lo que responde el backend y la lista se reordena por fecha.
- **Un partido cerrado** (finalizado o W.O.) no se programa: tampoco abre la hoja.

### 2.2 Volver a generar los partidos

- **Al final de la lista de partidos** va **"Volver a generar los partidos"**, con el detalle de lo que pasa.
- **Pide confirmación:** "Se borran todos los partidos del torneo, con sus fechas, lugares y árbitros asignados, y se arman de nuevo con los equipos aprobados de hoy. No se puede deshacer." El botón de confirmar va en el color de error.
- **Solo mientras ningún partido se haya jugado ni esté en juego.** Si alguno ya tiene resultado o está en juego, en su lugar se lee "Ya hay partidos jugados o en juego: no se pueden volver a generar."
- **Si los borra pero no los puede generar** (por ejemplo, ya no hay 2 equipos aprobados), la pantalla queda sin partidos, con "Generar partidos" y el motivo, igual que un torneo nuevo.

## 3. Diseño

- **Componentes:** `Fila`, `FlechaFila`, los diálogos de Material y los selectores `DatePicker` y `TimePicker`, que ya vienen en Material 3. No se agregan librerías.
- **El naranja no aparece:** guardar la fecha es una acción secundaria dentro de la hoja, y volver a generar es destructiva.
- **El día y la hora son campos que se tocan** (`CampoQueAbre`), con su ícono a la derecha. TalkBack lee "Día: sáb 21".
- **Los errores van dentro del diálogo,** porque un `Snackbar` quedaría tapado.

### Textos (todos en `strings.xml`)

| Dónde | Texto |
|---|---|
| Hoja del partido | "Fecha y lugar" · "Sin fecha" · "Sin lugar" |
| Diálogo | "Fecha y lugar" · "Día" · "Hora" · "Elegir día" · "Elegir hora" · "Lugar (opcional)" · "Por ejemplo, Coliseo UFPS, cancha 1" · "Guardar" · "Guardando…" |
| Validación | "Elige el día y también la hora." · "El lugar puede tener hasta 120 caracteres." |
| Fin de la lista | "Volver a generar los partidos" · "Generando…" · "Se borran estos partidos con sus fechas, lugares y árbitros, y se arman de nuevo con los equipos aprobados." · "Ya hay partidos jugados o en juego: no se pueden volver a generar." |
| Confirmación | "¿Volver a generar los partidos?" · el aviso de §2.2 · "Volver a generar" |

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde | Errores esperados |
|---|---|---|---|---|
| `/api/supercopa/admin/partidos/{partidoId}/programacion` | PUT | `{ "fecha": "2026-11-21T15:30:00", "lugar": "Cancha 2" }` | `ArbitroPartidoDTO` (se usan `fechaHora` y `lugar`) | 409 `PARTIDO_CERRADO` · 403 |
| `/api/supercopa/admin/torneos/{torneoId}/fixture` | DELETE | — | 204 | 400 `REGLA_NEGOCIO` ("ya hay partidos jugados o en curso") |
| Generar (de MOV-0011) | `GET …/configuracion` y `POST …/fixture` | — | — | 400 `REGLA_NEGOCIO` |

- **La fecha va sin zona,** en hora de Colombia, como la guarda el backend. Si `fecha` no viaja, no cambia. `lugar: ""` borra el lugar.
- **Borrar el fixture** borra también los eventos, las alineaciones y los árbitros asignados de esos partidos. No toca a los árbitros del torneo.
- **Volver a generar** es borrar y después el mismo caso de uso de "Generar partidos": como el torneo ya está en curso, no vuelve a llamar a `iniciar`.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** un partido abierto, **cuando** toco "Fecha y lugar" en su hoja, **entonces** la hoja se cierra y se abre el diálogo con el día, la hora y el lugar que tiene el partido.
2. **CA-2** · **Dado** el diálogo, **cuando** elijo día y hora, escribo el lugar y toco "Guardar", **entonces** se envía el `PUT` con la fecha sin zona y el lugar sin espacios de sobra, se cierra el diálogo y la fila queda con lo que respondió el backend, en su nuevo orden. Un lugar vacío viaja vacío.
3. **CA-3** · **Dado** el diálogo, **entonces** un día sin hora (o al revés) o un lugar de más de 120 caracteres no viaja, y el motivo sale dentro del diálogo. Si el backend rechaza, el diálogo sigue abierto con su mensaje y la fila no cambia. El doble toque no guarda dos veces, y mientras guarda no se cierra ni se edita.
4. **CA-4** · **Dado** un partido cerrado, **entonces** no se puede programar. "Cancelar" cierra sin enviar nada.
5. **CA-5** · **Dado** que ningún partido se jugó ni está en juego, **entonces** al final de la lista se ofrece "Volver a generar los partidos". Si alguno ya se jugó o está en juego, se lee el motivo en su lugar.
6. **CA-6** · **Dado** "Volver a generar", **cuando** confirmo, **entonces** se borran los partidos, se generan otra vez y se ven los nuevos. Si el backend no deja borrar, los partidos siguen y se ve su mensaje. Si se borran pero no se generan, la pantalla queda sin partidos, con "Generar partidos" y el mensaje.
7. **CA-7** · El diálogo y el final de la lista se ven bien en claro, en oscuro y con la letra al 130 % (emulador).

## 6. Fuera de alcance / qué NO debe cambiar

- **Programar varios partidos a la vez** (por ejemplo, "todos los de la jornada 2 el sábado").
- **Avisar a los equipos** del cambio de fecha: va con las notificaciones.
- **Elegir el lugar de una lista** de canchas del torneo: el lugar es texto libre, como en el backend.
- **No cambian** la generación de MOV-0011, la asignación de árbitros ni la planilla.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) | Nivel |
|---|---|---|
| CA-1 | `ProgramacionTest › CA-1 …`, `PartidosViewModelTest › MOV-0013 CA-1 …` | Dominio + ViewModel |
| CA-2 | `ProgramacionTest › CA-2 …` (2), `ProgramacionRepositoryTest › CA-2 …` (2), `PartidosViewModelTest › MOV-0013 CA-2 …` | Dominio + Red + ViewModel |
| CA-3 | `ProgramacionTest › CA-3 …` (2), `PartidosViewModelTest › MOV-0013 CA-3 …` (3) | Dominio + ViewModel |
| CA-4 | `ProgramacionRepositoryTest › CA-4 …`, `PartidosViewModelTest › MOV-0013 CA-4 …` (2) | Red + ViewModel |
| CA-5 | `ProgramacionTest › CA-5 …`, `PartidosViewModelTest › MOV-0013 CA-5 …` | Dominio + ViewModel |
| CA-6 | `ProgramacionTest › CA-6 …` (3), `ProgramacionRepositoryTest › CA-6 …`, `PartidosViewModelTest › MOV-0013 CA-6 …` (3) | Dominio + Red + ViewModel |
| CA-7 | Capturas del emulador | Emulador |

## 8. Verificación en el emulador

Con una cuenta que organice un torneo con partidos sin jugar (por ejemplo `admin01` y "Liga Relámpago (prueba)"):

1. Creador → el torneo → Partidos → toca un partido → "Fecha y lugar".
2. Cambia el día y la hora, escribe "Coliseo UFPS, cancha 1" y guarda: la fila muestra lo nuevo y cambia de puesto si hace falta.
3. Entra con el árbitro de ese partido: en su sección sale con la fecha y el lugar nuevos.
4. Al final de Partidos, toca "Volver a generar los partidos" y confirma: salen partidos nuevos, sin árbitros asignados.
5. En un torneo con un partido jugado, el final de la lista dice por qué ya no se puede.

**Capturas** en `docs/specs/evidencias/MOV-0013/`: la hoja con la fila, el diálogo, el selector de hora, el final de la lista (las dos variantes) y la confirmación; en claro, en oscuro y con letra al 130 %.

## 9. Archivos afectados

**Nuevos:** esta spec, `ProgramacionTest.kt` y `ProgramacionRepositoryTest.kt`.

**Modificados**

- `domain/model/GestionTorneo.kt` (`BorradorProgramacion`, `Programacion`, `sePuedenRegenerar`)
- `domain/repository/GestionTorneoRepository.kt` (`programarPartido`, `borrarPartidos`)
- `domain/use_case/competicion/GestionTorneoUseCases.kt` (`ProgramarPartidoUseCase`, `VolverAGenerarPartidosUseCase`)
- `data/remote/api/GestionTorneoApi.kt`, `data/remote/dto/GestionTorneoDtos.kt`, `data/repository/GestionTorneoRepositoryImpl.kt`
- `di/AppContainer.kt`
- `presentacion/creador/PartidosTorneoViewModel.kt`, `PartidosTorneoScreen.kt`
- `res/values/strings.xml`
- `PartidosViewModelTest.kt` y `testutil/FakesGestion.kt`

**`testTag` nuevos:** `partido_programar`, `dialogo_programar`, `programar_fecha`, `programar_hora`, `programar_lugar`, `programar_error`, `programar_guardar`, `programar_fecha_aceptar`, `programar_hora_aceptar`, `partidos_regenerar`, `partidos_regenerar_bloqueado`, `partidos_regenerar_confirmar` y `partidos_regenerar_cancelar`.

## 10. Notas y decisiones

- **Días pasados permitidos.** El backend los acepta y sirve para dejar registrado un partido que se jugó en otra fecha. Si se prefiere bloquearlos, es una línea (`SelectableDates`, como en Crear torneo).
- **La fecha no se puede borrar,** solo cambiar: el backend no la admite vacía.
- **Volver a generar va al final de la lista y no en la barra superior:** es raro y destructivo, y así no hace falta un menú ni un ícono nuevo.
- **Después de guardar se usa la respuesta del `PUT`,** sin volver a pedir la lista. El orden se recalcula solo.
- **Sin aviso al guardar:** la fila cambia a la vista, que es la confirmación.

## 11. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-21 | Redactada e implementada (pendiente de "Próximas" en el índice y de MOV-0011). Pasa a `en-revision` |
| 2026-11-21 | 398 pruebas, 0 fallos (24 nuevas); `assembleDebug` en verde. Prueba de dientes: con `sePuedenRegenerar` roto a propósito fallan 2 pruebas |
| 2026-11-21 | **Sin verificar en el emulador:** no había emulador encendido, y la sesión de la app (Diana) organiza un torneo sin partidos. Falta el recorrido de §8 con `admin01` |
