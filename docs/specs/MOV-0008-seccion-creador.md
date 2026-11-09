---
id: MOV-0008
titulo: Sección Creador, mis torneos y por revisar
tipo: feature
estado: borrador
prioridad: P1
fase: F3
modo: Creador
responsable: —
depende_de: [MOV-0004, MOV-0005, backend 0024 (roles por torneo, §4.2)]
rama: mov-0008-seccion-creador
---

# MOV-0008 · Sección Creador, mis torneos y por revisar

## 1. Contexto

- **Creador es una sección,** no un modo. Se entra desde "Tus secciones" en Inicio, desde Perfil o al crear un torneo, y Atrás vuelve a Inicio.
- **Referencia:** prototipo, pantalla `creador` y la franja "Organizas este torneo" del detalle.
- **No hay administradores globales:** cada persona ve y gestiona **solo los torneos que administra** (decisión del 2-nov, backend 0024).

## 2. Alcance

- **Sección Creador:**
  - **"Por revisar":** inscripciones y comprobantes pendientes, agrupados por torneo, con "Revisar";
  - **"Mis torneos":** nombre, estado, cupos, visibilidad y un contador de pendientes;
  - botón flotante "Crear torneo";
  - estado vacío "Crea tu primer torneo".
- **En el detalle del torneo,** la franja de organizador:
  - Inscripciones;
  - Cruces ("se generan al cerrar inscripciones");
  - Árbitros (invitar por enlace);
  - Configuración (solo el acceso; las pantallas van en otra spec).
- **Revisar inscripciones:** aprobar o rechazar cada una. Rechazar pide el motivo. "Deshacer" en un snackbar.
- **Revisar comprobantes:** ver la imagen o el PDF y aprobar o rechazar.

## 3. Diseño

- **Prototipo** `creador` y la franja del organizador: listas M3 con contadores, `Badge`, snackbar con "Deshacer" y un diálogo para el motivo del rechazo.

## 4. Contrato con el backend

### 4.1 Lo que ya existe

Autorizados hoy con el rol **global** `ADMINISTRADOR`, que ve **todos** los torneos:

- `GET /api/supercopa/admin/torneos` y los `admin/*` de cada torneo (inscripciones, configuración y fixture);
- comprobantes: `comprobantes/pendientes`, aprobar y rechazar.

### 4.2 Lo que falta (backend **0024**: `CODECUP/.context/specs/0024-roles-por-torneo-creador.md`)

1. **`competicion.torneo_roles (torneo_id, cuenta_id, rol ADMIN|ARBITRO)`** con `UNIQUE (torneo_id, cuenta_id, rol)`. Quien crea un torneo queda como ADMIN (ver MOV-0007).
2. **Bean `@authz`:** reemplaza `hasRole('ADMINISTRADOR')` por `@authz.adminDeTorneo(#torneoId)` en los endpoints de un torneo. Con otro id (inscripción, comprobante, partido), primero resuelve su torneo. Ante un torneo ajeno responde **403 `NO_ADMIN_DEL_TORNEO`**.
3. **`GET /api/supercopa/creador/torneos`:** solo los torneos que administra.
   ```json
   [{ "id": 3, "nombre": "Relámpago de Tenis de Mesa", "deporte": "TENIS_MESA", "estado": "INSCRIPCIONES",
      "visibilidad": "PUBLICO", "cupos": 16, "inscritos": 12,
      "pendientes": { "inscripciones": 3, "comprobantes": 1 } }]
   ```
4. **Bandejas filtradas por torneo:** las inscripciones y los comprobantes pendientes solo de sus torneos, con `?torneoId=` opcional.
5. **Rechazar con motivo:** el cuerpo `{ "motivo": "…" }` se guarda y lo ve quien se inscribió.
6. **Migración (0024, Q1):** las cuentas con `ADMINISTRADOR` global quedan como ADMIN de los torneos que ya existen, para no romper nada.

## 5. Criterios de aceptación (borrador)

1. **CA-1** · La sección lista solo los torneos que la persona administra, con sus pendientes.
2. **CA-2** · "Por revisar" agrupa los pendientes por torneo y "Revisar" abre la lista del torneo.
3. **CA-3** · Aprobar mueve la inscripción a aprobadas, con "Deshacer". Rechazar exige un motivo.
4. **CA-4** · Un 403 `NO_ADMIN_DEL_TORNEO` muestra el aviso y vuelve a la sección.
5. **CA-5** · Sin torneos se ve el estado vacío con "Crear torneo".
6. **CA-6** · Claro, oscuro y letra al 130 % (emulador).

## 6. Fuera de alcance

- Las pantallas de configuración (formato, cuota, premios y multas), generar el fixture e invitar coadministradores.

## 7. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
