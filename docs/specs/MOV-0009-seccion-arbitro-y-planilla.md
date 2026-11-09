---
id: MOV-0009
titulo: Sección Árbitro y planilla del partido
tipo: feature
estado: borrador
prioridad: P1
fase: F2
modo: Árbitro
responsable: —
depende_de: [MOV-0004, backend 0025 (árbitros por partido, §4.2)]
rama: mov-0009-seccion-arbitro
---

# MOV-0009 · Sección Árbitro y planilla del partido

## 1. Contexto

- **Árbitro es una sección** que solo aparece si la persona tiene partidos asignados. Desde Inicio, la fila de Árbitro lleva directo a la planilla del partido de hoy.
- **Referencia:** prototipo, pantallas `arbitro` y `planilla`.
- **Los árbitros se asignan por partido.** Operan todo un torneo solo con permiso del administrador (decisión del 2-nov, backend 0025).

## 2. Alcance

- **Sección Árbitro:** "Hoy" y "Próximos", con hora, equipos, torneo y lugar. El partido de hoy lleva el chip naranja "Planilla"; los cerrados, "Cerrado" y el marcador.
- **Planilla:**
  - marcador grande;
  - alineación de cada equipo (quién juega);
  - selector de equipo y los botones de evento **del deporte** (fútbol sala: Gol, Amarilla, Azul y Roja);
  - al tocar un evento, una hoja inferior para elegir al jugador;
  - la lista de **eventos en orden, sin minuto**, con "Quitar" y "Deshacer";
  - "Cerrar partido" con un diálogo de confirmación que muestra el marcador;
  - en el menú, "Registrar W.O." y "Cancelar partido".
- **Restricción:** un jugador suspendido (roja o multa pendiente) se ve marcado en la alineación y no se puede alinear.

## 3. Diseño

- **Prototipo** `arbitro` y `planilla`: cifras de marcador en Barlow Condensed, botones segmentados para el equipo, una cuadrícula de eventos, la barra inferior fija con "Cerrar partido" y un diálogo M3.

## 4. Contrato con el backend

### 4.1 Lo que ya existe

Hoy cualquier `ARBITRO` global opera cualquier partido:

- `…/partidos/{id}/alineacion` y `…/partidos/{id}/eventos`;
- `…/partidos/{id}/cerrar`, `…/wo` y `…/cancelar`;
- los eventos son el enum `GOL`, `AMARILLA`, `AZUL` y `ROJA`.

**Pendiente conocido** (roadmap C6/C7): la web leía `apto`/`motivos` y el backend manda `suspensionActiva`/`multaPendiente`. La app debe usar **lo que responde hoy el backend**; revisarlo en Swagger.

### 4.2 Lo que falta (backend **0025**: `CODECUP/.context/specs/0025-arbitros-por-partido.md`)

1. **`competicion.partido_arbitros (partido_id, cuenta_id)`,** con varios árbitros por partido. También `torneo_roles(ARBITRO)` y el permiso "todos los partidos del torneo".
2. **`@authz.arbitroDePartido(#partidoId)`** en alineación, eventos, cerrar, W.O. y cancelar. Sin asignación (ni permiso de torneo) responde **403 `NO_ARBITRO_DEL_PARTIDO`**.
3. **`GET /api/supercopa/arbitro/partidos?desde=&hasta=`:** los partidos asignados, ordenados por fecha.
   ```json
   [{ "partidoId": 41, "torneoId": 9, "torneo": "Interfacultades Fútbol Sala", "deporte": "FUTSAL",
      "fase": "Grupo A · Fecha 3", "local": "Sistemas", "visitante": "Civil",
      "fechaHora": "2026-11-12T16:00:00", "lugar": "Cancha 2", "estado": "PROGRAMADO",
      "marcador": null }]
   ```
4. **No se puede asignar como árbitro** a quien está en la plantilla de uno de los dos equipos: 409 `ARBITRO_EN_PLANTILLA`.
5. **Multideporte (después):** los tipos de evento pasan de enum a tabla por deporte, y voleibol y tenis de mesa llevan marcador por sets. La app debe pedir los tipos de evento al backend en lugar de dejarlos fijos, cuando exista el endpoint. Mientras tanto, solo fútbol sala.

## 5. Criterios de aceptación (borrador)

1. **CA-1** · La sección lista solo los partidos asignados, separados en Hoy y Próximos.
2. **CA-2** · Registrar un evento lo agrega al final de la lista, actualiza el marcador si suma, y ofrece "Deshacer".
3. **CA-3** · "Cerrar partido" pide confirmación con el marcador. Al cerrar, el partido queda "Cerrado" en la sección y en Inicio.
4. **CA-4** · Un jugador suspendido aparece marcado y no se puede alinear.
5. **CA-5** · Un 403 `NO_ARBITRO_DEL_PARTIDO` muestra el aviso y vuelve a la sección.
6. **CA-6** · W.O. y cancelar piden confirmación y el motivo.
7. **CA-7** · Claro, oscuro y letra al 130 %, y sin conexión con el aviso, sin perder lo registrado (emulador).

## 6. Fuera de alcance

- La planilla sin conexión (Room), los deportes por sets y el minuto de los eventos (decisión: van en orden, sin minuto).

## 7. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
