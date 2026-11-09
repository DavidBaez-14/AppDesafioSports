---
id: MOV-0004
titulo: Inicio (jugador), navegación principal y Perfil
tipo: feature
estado: borrador
prioridad: P1
fase: F1
modo: Jugador
responsable: —
depende_de: [MOV-0003, backend "Inicio y secciones" (§4.2)]
rama: mov-0004-inicio-y-perfil
---

# MOV-0004 · Inicio (jugador), navegación principal y Perfil

## 1. Contexto

- **Estructura decidida** (8-nov-2026, `PRODUCT.md`): todos entran como jugador. **Creador** y **Árbitro** son secciones a las que se entra desde Inicio, sin elegir modo.
- **Referencia de diseño:** el prototipo aprobado `docs/prototipo-roles/index.html`, con la estructura "Jugador de base": pantallas Inicio y Perfil, y el panel "Qué necesita del backend".
- **Hoy, después de entrar, se abre el diagnóstico** (inicio provisional de MOV-0002). Esta spec pone el Inicio real y el diagnóstico queda solo como herramienta de debug.

## 2. Alcance

- **Barra inferior** de Material 3: **Inicio · Explorar · Perfil**. Explorar muestra un estado vacío hasta MOV-0005.
- **Inicio:**
  - **"Tus secciones":** solo las que la persona tiene, con lo urgente de cada una. *Creador*, con sus inscripciones y comprobantes por revisar; *Árbitro*, con el partido de hoy. Cada una abre su sección (MOV-0008 y MOV-0009).
  - **"Tu próximo partido".**
  - **"Mis equipos":** equipo, torneo, rol (juega o delega) y el estado del trámite (inscripción o comprobante).
  - **Botón flotante "Crear torneo":** abre MOV-0007.
  - **Estados vacíos:** cuenta nueva sin secciones ni equipos.
- **Perfil:**
  - el nombre visible;
  - la **trayectoria**: torneos jugados, resultado y cifras básicas;
  - **sanciones y multas** con su estado;
  - "Perfil deportivo (opcional)";
  - el acceso a las secciones y "Cerrar sesión".
- **Arranque con sesión guardada:** consulta `/api/auth/me`. Si la cuenta no tiene registro, va a completar registro (MOV-0003); si está bloqueada, va al login con el aviso. Esto cierra el hueco de MOV-0002, que iba siempre al diagnóstico.
- **Diagnóstico:** sigue accesible en debug con la pulsación larga en el logotipo del login, y desde Perfil en debug.

## 3. Diseño

- **Prototipo:** pantallas `inicio` (estructura "Jugador de base") y `perfil`. En Compose se adaptan a Material 3: `NavigationBar`, `ExtendedFloatingActionButton` y listas. Tokens en `DESIGN.md`.
- **Estados por pantalla:** cargando (esqueleto), vacío, error con reintentar, sin conexión y éxito.
- **El naranja,** solo para "Crear torneo" y para lo que pasa hoy (el chip "Planilla").

## 4. Contrato con el backend

### 4.1 Lo que ya existe

| Endpoint | Uso | Nota |
|---|---|---|
| `GET /api/auth/me` | Nombre, estado y roles | Hoy los roles son globales (`JUGADOR`, `DELEGADO`, `ARBITRO`, `ADMINISTRADOR`) |
| `GET /api/supercopa/mi-perfil` | Trayectoria y perfil deportivo | **Antes de implementar,** revisar el contrato en Swagger (`http://localhost:8082/swagger-ui/index.html`) |
| `PUT /api/supercopa/mi-perfil` | Perfil deportivo opcional | Existe (backend 0020) |
| `GET /api/finanzas/multas/mias` | Sanciones y multas con su estado | Existe |

### 4.2 Lo que falta (backend, spec propia en `CODECUP/.context/specs/`)

Sale de la **M3** (roles por torneo: specs backend 0024, 0025 y 0026) y debe implementarse **antes** que esta pantalla. Si no, "Tus secciones" queda oculto.

1. **Resumen de secciones en `GET /api/auth/me`.** Reemplaza el "resumen por modo" de la 0026 (decisión 4); los modos ya no existen.
   ```json
   "secciones": {
     "creador": { "torneos": 2, "porRevisar": 3 },
     "arbitro": { "partidosAsignados": 2,
                  "hoy": { "partidoId": 41, "local": "Sistemas", "visitante": "Civil",
                           "fechaHora": "2026-11-12T16:00:00", "torneo": "Interfacultades Fútbol Sala" } }
   }
   ```
   - `creador` va solo si administra algún torneo (`torneo_roles` ADMIN, 0024). `porRevisar` suma inscripciones y comprobantes pendientes de sus torneos.
   - `arbitro` va solo si tiene partidos asignados (`partido_arbitros`, 0025). `hoy` es el próximo partido del día, o `null`.
2. **`GET /api/supercopa/yo/equipos`:** los equipos de la persona en cualquier torneo, como jugador o como delegado.
   ```json
   [{ "equipoTorneoId": 7, "equipo": "Los Pibes FC", "torneoId": 3, "torneo": "Copa Sistemas",
      "deporte": "FUTSAL", "rol": "DELEGADO", "estadoInscripcion": "PENDIENTE_PAGO",
      "comprobante": "PENDIENTE",
      "proximoPartido": { "partidoId": 90, "rival": "Real Código", "fechaHora": "…", "lugar": "Cancha 2" } }]
   ```
   - El delegado sale de `equipo_torneo.delegado_persona_id` (0026), sin el rol global `DELEGADO`.
   - `comprobante` es `null` para quien solo juega.
3. **"Tu próximo partido"** es el `proximoPartido` más cercano de la lista anterior. No hace falta otro endpoint.

**Errores esperados:** 401 `NO_AUTENTICADO`, 403 `CUENTA_BLOQUEADA` y 404 `CUENTA_NO_REGISTRADA`, con los mismos `AppError` de MOV-0002.

## 5. Criterios de aceptación (borrador)

1. **CA-1** · Con sesión y cuenta registrada, el arranque abre Inicio con la barra Inicio · Explorar · Perfil. Atrás desde Inicio sale de la app.
2. **CA-2** · "Tus secciones" muestra solo las secciones que vienen en `secciones`, con su conteo o su partido de hoy. Si no viene ninguna, la sección no aparece.
3. **CA-3** · "Mis equipos" lista lo que devuelve `yo/equipos`, con el rol y el estado del trámite en un chip.
4. **CA-4** · Con una cuenta nueva sin equipos ni secciones, se ven los estados vacíos con "Ir a Explorar" y "Crear torneo".
5. **CA-5** · Perfil muestra la trayectoria de `mi-perfil` y las multas de `multas/mias`. "Cerrar sesión" vuelve al login.
6. **CA-6** · Al arrancar con sesión: una cuenta sin registro va a completar registro y una cuenta bloqueada va al login con el aviso.
7. **CA-7** · Sin conexión, cada pantalla muestra el aviso con "Reintentar".
8. **CA-8** · Claro, oscuro y letra al 130 % sin cortes (emulador).

## 6. Fuera de alcance

- Las secciones Creador (MOV-0008) y Árbitro (MOV-0009), Explorar (MOV-0005) y Crear torneo (MOV-0007): aquí solo se enlazan.
- Editar los datos personales y la foto.

## 7. Plan de pruebas (borrador)

`InicioViewModelTest` y `PerfilViewModelTest` (con fakes), `CuentaRepositoryImplTest` y `PerfilRepositoryImplTest` (MockWebServer, con el JSON de §4), `RaizViewModelTest` (CA-6) y capturas del emulador.

## 8. Dudas para David

- **Q1.** ¿Se implementa primero el backend de §4.2 (recomendado), o el Inicio se publica sin "Tus secciones" y se completa cuando exista?

## 9. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
