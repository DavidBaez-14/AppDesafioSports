---
id: MOV-0008
titulo: Sección Creador, mis torneos y por revisar
tipo: feature
estado: hecha
prioridad: P1
fase: F3
modo: Creador
responsable: David
depende_de: [MOV-0003, MOV-0007, backend 0024, backend 0029]
rama: mov-0007-crear-torneo
---

# MOV-0008 · Sección Creador, mis torneos y por revisar

> **Spec puesta al día el 16-nov-2026 con lo que hace el código de `main`.** El borrador del 9-nov proponía un backend que todavía no existía; el contrato de §4 es el real (backend 0024 y 0029).

## 1. Contexto

- **Creador es una sección,** no un modo. Se entra desde "Tus secciones" en Inicio o al crear un torneo, y Atrás vuelve a Inicio.
- **Referencia:** prototipo, pantalla `creador`. El diseño final de las filas es el de MOV-0010.
- **No hay administradores globales:** cada persona ve y gestiona **solo los torneos que administra** (backend 0024).

## 2. Alcance

- **Sección Creador** (`SeccionCreadorScreen`):
  - **"Por revisar":** los torneos con inscripciones o comprobantes pendientes, con su contador;
  - **"Mis torneos":** nombre, deporte, estado, visibilidad y cupos ocupados;
  - botón flotante "Crear torneo";
  - estado vacío "Crea tu primer torneo".
  - Los torneos con algo por revisar van primero. La lista se vuelve a pedir al regresar a la pantalla.
- **Inscripciones de un torneo** (`InscripcionesScreen`): por aprobar (las más antiguas primero), aprobadas y rechazadas.
  - **Aprobar** envía la acción y recarga la lista.
  - **Rechazar** pide el motivo en un diálogo (mínimo 3 caracteres): es lo que ve quien se inscribió.
  - Una acción a la vez: el doble toque no aprueba dos veces.

## 3. Diseño

- **Prototipo** `creador` y MOV-0010 (cifra de cupos con su barra de llenado, chip de estado con color).
- **Estados:** cargando, vacío, error y sin conexión con "Reintentar". Un fallo al refrescar conserva la lista y avisa con un `Snackbar`.

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde |
|---|---|---|---|
| `/api/supercopa/creador/torneos` | GET | — | `[{ id, nombre, estado, deporte, visibilidad, cupos, enlace, inscritos, pendientes: { inscripciones, comprobantes } }]` |
| `/api/supercopa/admin/torneos/{torneoId}/inscripciones` | GET | — | `[{ id, equipoNombre, delegadoNombre, estadoInscripcion, motivoRechazo, fechaInscripcion, montoInscripcion }]` |
| `/api/supercopa/admin/torneos/{torneoId}/inscripciones/{equipoTorneoId}/aprobar` | POST | — | la inscripción (no se usa: la lista se vuelve a pedir) |
| `/api/supercopa/admin/torneos/{torneoId}/inscripciones/{equipoTorneoId}/rechazar` | POST | `{ "motivo": "…" }` | la inscripción |

- Las rutas `admin/torneos/{id}/…` las autoriza el rol ADMIN **de ese torneo**. En un torneo ajeno responden **403 `NO_ADMIN_DEL_TORNEO`**.
- `estado` del torneo: `BORRADOR`, `PUBLICADO` (inscripciones abiertas), `EN_CURSO` o `FINALIZADO`.
- `estadoInscripcion`: `PENDIENTE_PAGO`, `APROBADO`, `RECHAZADO` o `EXPULSADO`.

## 5. Criterios de aceptación

1. **CA-1** · La sección lista solo los torneos que la persona administra, con sus pendientes, y los que tienen algo por revisar van primero. Al volver de crear un torneo, la lista se vuelve a pedir.
2. **CA-2** · "Por revisar" abre las inscripciones del torneo, repartidas en por aprobar, aprobadas y rechazadas.
3. **CA-3** · Aprobar envía la acción, recarga y mueve la inscripción a aprobadas. Rechazar exige un motivo. Si la acción falla, la lista no cambia y se ve el mensaje del backend.
4. **CA-4** · Un 403 `NO_ADMIN_DEL_TORNEO` muestra el aviso con "Volver".
5. **CA-5** · Sin torneos se ve el estado vacío con "Crear torneo".
6. **CA-6** · Sin conexión se ofrece "Reintentar"; si falla al actualizar, se conserva la lista.
7. **CA-7** · Claro, oscuro y letra al 130 % (emulador).
8. **CA-8** · Cada torneo muestra los cupos ocupados en cifras con su barra de llenado (MOV-0010).

## 6. Plan de pruebas

| Criterio | Prueba |
|---|---|
| CA-1 a CA-6 | `SeccionCreadorViewModelTest`, `CreadorRepositoryImplTest › MOV-0008 …` |
| CA-2 | `BorradorTorneoTest › MOV-0008 CA-2 …` |
| CA-7, CA-8 | Verificación en el emulador (`evidencias/MOV-0008/` y `evidencias/MOV-0010/`) |

## 7. Fuera de alcance

- **Árbitros y partidos del torneo:** MOV-0011.
- Revisar comprobantes (ver la imagen o el PDF y aprobar o rechazar): spec propia.
- Las pantallas de configuración (formato, cuota, premios y multas) e invitar coadministradores.
- "Deshacer" una aprobación desde el aviso.

## 8. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
| 2026-11-10 | Implementada sobre el backend 0024 y 0029 e integrada a `main` (`a6d1242`), en la rama de MOV-0007. MOV-0010 rediseña sus filas |
| 2026-11-16 | Spec puesta al día con el código: contrato real, criterios y plan de pruebas. Revisar comprobantes y "Deshacer" salen del alcance porque no se implementaron. Pasa a `hecha` |
