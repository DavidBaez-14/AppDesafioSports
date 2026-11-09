---
id: MOV-0005
titulo: Explorar torneos públicos y detalle del torneo
tipo: feature
estado: borrador
prioridad: P1
fase: F4
modo: Visitante y Jugador
responsable: —
depende_de: [MOV-0004, backend "Lecturas públicas" (M4, §4.2)]
rama: mov-0005-explorar-y-torneo
---

# MOV-0005 · Explorar torneos públicos y detalle del torneo

## 1. Contexto

- **El diferencial del producto** (`PRODUCT.md`): si el torneo es **público**, nadie necesita un enlace; lo buscan por deporte o por nombre.
- **Referencia:** prototipo `docs/prototipo-roles/index.html`, pantallas `explorar` y `torneo`. En el detalle, la **franja de acciones cambia según la relación de la persona con el torneo**, y la parte pública es igual para todos.
- **"Explorar sin cuenta"** del login (pendiente desde MOV-0002) entra aquí.

## 2. Alcance

- **Explorar:**
  - barra de búsqueda por nombre;
  - chips por deporte;
  - secciones por deporte con los torneos públicos: nombre, estado, cupos libres y cierre.
- **Detalle del torneo:**
  - cabecera con el deporte, el formato, el estado, la visibilidad y los datos clave (inicio, cupos, cuota y organizador);
  - pestañas según el estado: inscritos o equipos y cruces mientras hay inscripciones; tabla y partidos cuando está en curso; e Info (reglas, premios y visibilidad);
  - **franja por relación**, que lee `miRelacion`:
    - sin cuenta o sin relación → "Inscribirme" o "Inscribir equipo" (la acción en sí va en otra spec; aquí se pide cuenta);
    - juega → su estado o su próximo partido;
    - delega → su comprobante;
    - arbitra → acceso a la sección Árbitro;
    - organiza → acceso a la sección Creador.
- **Login:** el botón "Explorar sin cuenta" abre Explorar en modo visitante.
- **Compartir:** la hoja de compartir de Android con el enlace del torneo.

## 3. Diseño

- **Prototipo:** pantallas `explorar` y `torneo`. En Compose: `SearchBar`, `FilterChip`, `TabRow`, la tabla con cifras de ancho fijo y la franja como una superficie contenedora.
- **Las columnas de la tabla dependen del deporte:** fútbol sala usa PJ, G, E, P, DG y Pts; voleibol usa PJ, G, P, Sets y Pts.

## 4. Contrato con el backend

### 4.1 Lo que ya existe

- **Datos del torneo:** tabla, partidos y premios existen en los endpoints con sesión (`/api/supercopa/...`). **No hay lecturas públicas.**

### 4.2 Lo que falta (backend, **M4 mínima**, spec propia en `CODECUP/.context/specs/`)

Diseño de referencia: `CODECUP/.context/docs/plan_auth_modos_identidad.md` §6.

1. **Columnas nuevas:**
   - `torneos.visibilidad`: `PUBLICO` | `NO_LISTADO` | `PRIVADO`, con un valor por defecto que aún no se decide (ver MOV-0007, Q1);
   - `torneos.enlace_token`: aleatorio y regenerable;
   - `torneos.deporte`: FK a un catálogo `deportes (codigo, nombre, tipo_competencia EQUIPO|INDIVIDUAL)`. Arranca con FUTSAL, VOLEIBOL, BALONCESTO y TENIS_MESA.

   La migración deja los torneos existentes como `FUTSAL`, con la visibilidad que se decida.
2. **Endpoints sin sesión** (`permitAll` en `/api/public/**`):
   - `GET /api/public/deportes` → `[{codigo, nombre, tipoCompetencia}]`.
   - `GET /api/public/torneos?deporte=&q=&page=&size=`: **solo `PUBLICO`**. `q` busca por nombre sin distinguir mayúsculas ni tildes.
     ```json
     { "contenido": [{ "id": 3, "nombre": "Relámpago de Tenis de Mesa", "deporte": "TENIS_MESA",
         "formato": "ELIMINACION_DIRECTA", "estado": "INSCRIPCIONES", "cupos": 16, "inscritos": 12,
         "cierreInscripciones": "2026-11-13", "inicio": "2026-11-21T08:00:00",
         "organizador": "Andrés P." }], "pagina": 0, "totalPaginas": 1 }
     ```
   - `GET /api/public/torneos/{id}`: lo ve quien cumpla `VisibilidadService.puedeVer(quien, torneo)`. `PUBLICO` lo ve cualquiera; `PRIVADO`, solo sus participantes. Responde el detalle, los inscritos o equipos **aprobados**, la tabla por grupo, los partidos por fecha y los premios.
   - `GET /api/public/t/{enlaceToken}`: el mismo detalle para un torneo `NO_LISTADO`.
3. **`miRelacion` en el detalle,** solo si la petición trae token:
   ```json
   "miRelacion": { "administra": false, "arbitra": true,
     "delegadoDe": { "equipoTorneoId": 7, "equipo": "Los Pibes FC", "comprobante": "PENDIENTE" },
     "inscrito": { "estado": "APROBADA" } }
   ```
   Un solo campo pinta toda la franja: **no hacen falta endpoints por rol.**
4. **Privacidad:** en todo lo público, los nombres van como "Nombres A." (nombre más la inicial del apellido), sin documento ni correo.

**Errores:** 404 `NO_ENCONTRADO` (un torneo que no existe o que no se puede ver responde igual, para no revelar torneos privados).

## 5. Criterios de aceptación (borrador)

1. **CA-1** · Explorar lista solo los torneos públicos, agrupados por deporte. Los `NO_LISTADO` y los `PRIVADO` no aparecen aunque la persona participe en ellos.
2. **CA-2** · La búsqueda por nombre y el chip de deporte filtran la lista. Sin resultados, se ve un estado vacío que explica que los torneos con enlace no salen.
3. **CA-3** · Sin cuenta se puede explorar y ver el detalle. "Inscribirme" pide entrar o crear cuenta.
4. **CA-4** · La franja del detalle cambia según `miRelacion`, y sin relación y sin inscripciones abiertas no hay franja.
5. **CA-5** · Las pestañas y las columnas de la tabla dependen del estado y del deporte.
6. **CA-6** · Compartir abre la hoja de Android con el enlace.
7. **CA-7** · Claro, oscuro, letra al 130 %, sin conexión y estado vacío (emulador).

## 6. Fuera de alcance

- Inscribirse o inscribir un equipo (spec propia, con el flujo de comprobante).
- El salón de la fama.
- El perfil público de un jugador.

## 7. Dudas para David

- **Q1.** Torneos existentes al migrar: ¿quedan `PUBLICO` (la Supercopa es pública en codecup.games) o `NO_LISTADO`?

## 8. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
