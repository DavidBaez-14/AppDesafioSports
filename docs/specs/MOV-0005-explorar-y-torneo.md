---
id: MOV-0005
titulo: Explorar torneos públicos y detalle del torneo
tipo: feature
estado: hecha
prioridad: P1
fase: F4
modo: Visitante y Jugador
responsable: equipo
depende_de: [MOV-0002, backend 0029]
rama: mov-0005-explorar-y-torneo
---

# MOV-0005 · Explorar torneos públicos y detalle del torneo

> **Spec puesta al día el 16-nov-2026 con lo que hace el código de `main`.** El borrador del 9-nov proponía un backend que todavía no existía; el contrato de §4 es el real (backend 0029).

## 1. Contexto

- **El diferencial del producto** (`PRODUCT.md`): si el torneo es **público**, nadie necesita un enlace; lo buscan por deporte o por nombre.
- **Referencia:** prototipo `docs/prototipo-roles/index.html`, pantallas `explorar` y `torneo`. En el detalle, la **franja de acciones cambia según la relación de la persona con el torneo**, y la parte pública es igual para todos.
- **"Explorar sin cuenta"** del login (pendiente desde MOV-0002) entra aquí.

## 2. Alcance

- **Explorar** (`ExplorarScreen`):
  - búsqueda por nombre, que espera a que se deje de escribir;
  - chips por deporte, del catálogo `GET /api/public/deportes`;
  - la lista de torneos públicos con el nombre, el estado, los cupos y el cierre; "Ver más" trae la página siguiente;
  - **"Abrir con enlace":** se pega el enlace o el token de un torneo y se abre su detalle. Es la única forma de llegar a un torneo `NO_LISTADO`.
- **Detalle del torneo** (`TorneoScreen`):
  - cabecera con el deporte, el formato, el estado, la visibilidad y los datos clave;
  - pestañas **Equipos · Partidos · Tabla · Info** (reglas y premios);
  - **franja por relación**, que sale de `miRelacion` (`AccionDelTorneo` en `domain/model/Publico.kt`). Lo que la persona ya es pesa más que lo que podría hacer:
    - sin sesión y con inscripciones abiertas → "Entrar" (vuelve al login);
    - organiza → abre las inscripciones del torneo (la gestión del torneo, desde MOV-0011);
    - arbitra → abre la sección Árbitro;
    - delega o juega → el estado de su equipo;
    - sin relación → inscribirse (la acción en sí va en otra spec), "inscripción individual pendiente" o "inscripciones cerradas".
  - **Compartir:** la hoja de compartir de Android con el enlace del torneo.
- **Login:** el botón "Explorar sin cuenta" abre Explorar en modo visitante, con flecha de volver. Con sesión, Explorar es una pestaña de la barra inferior y no lleva flecha (MOV-0004).

## 3. Diseño

- **Prototipo:** pantallas `explorar` y `torneo`.
- **Estados:** cargando, vacío (con filtro explica que los torneos con enlace no salen en la búsqueda), error y sin conexión con "Reintentar". Un fallo al refrescar conserva lo que había y avisa con un `Snackbar`.

## 4. Contrato con el backend

Todo es de backend 0029 y **no necesita sesión** (`/api/public/**`). Si la petición trae token, el detalle suma `miRelacion`.

| Endpoint | Método | Responde |
|---|---|---|
| `/api/public/deportes` | GET | `[{codigo, nombre, tipoCompetencia}]` (`EQUIPO` o `INDIVIDUAL`) |
| `/api/public/torneos?deporte=&q=&page=&size=` | GET | Página (`contenido`, `pagina`, `totalPaginas`) con **solo los `PUBLICO`**. `q` busca cada palabra en el nombre, sin mayúsculas ni tildes |
| `/api/public/torneos/{torneoId}` | GET | El detalle: datos, equipos aprobados, tabla por grupo, partidos, premios y `miRelacion` |
| `/api/public/t/{enlaceToken}` | GET | El mismo detalle por enlace; abre también los `NO_LISTADO` |

- **`miRelacion`:** `{ administra, arbitra, delegadoDe, juegaEn }`. Las dos últimas traen `equipoTorneoId`, `equipo`, `estadoInscripcion` y `comprobante`.
- **Errores:** 404 `NO_ENCONTRADO`. Un torneo que no existe y uno que no se puede ver responden igual, para no revelar torneos privados.
- **Privacidad:** en lo público los nombres van como "Nombres A.", sin documento ni correo.

## 5. Criterios de aceptación

1. **CA-1** · Explorar lista los torneos públicos y los deportes; "Ver más" suma la página siguiente sin repetir. Sin conexión, la primera carga muestra el error con "Reintentar".
2. **CA-2** · La búsqueda por nombre y el chip de deporte filtran la lista y vuelven a pedir desde la primera página. Sin filtro no se envían `deporte` ni `q`.
3. **CA-3** · "Abrir con enlace" acepta el enlace completo o el token suelto. Lo que no parece un enlace de torneo se avisa sin llamar al backend. Un torneo abierto por enlace se vuelve a pedir por enlace.
4. **CA-4** · La franja del detalle sale de `miRelacion`. Sin sesión invita a entrar solo si las inscripciones están abiertas.
5. **CA-5** · Un torneo privado o inexistente muestra el error y no la franja. Un fallo al refrescar conserva el detalle y avisa.
6. **CA-6** · Compartir abre la hoja de Android con el enlace.

## 6. Plan de pruebas

| Criterio | Prueba |
|---|---|
| CA-1, CA-2, CA-3 | `ExplorarYTorneoViewModelTest`, `TorneosRepositoryImplTest` |
| CA-3, CA-4 | `TorneoDetalleTest` |
| CA-4, CA-5 | `ExplorarYTorneoViewModelTest`, `TorneosRepositoryImplTest` |
| CA-6 | Verificación en el emulador |

## 7. Fuera de alcance

- Inscribirse o inscribir un equipo (spec propia, con el flujo de comprobante).
- Las columnas de la tabla según el deporte: hoy son las de fútbol sala para todos.
- El salón de la fama y el perfil público de un jugador.

## 8. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
| 2026-11-11 | Implementada por el equipo e integrada a `main` (`460baa7`), sobre el backend 0029 |
| 2026-11-14 | MOV-0004 la pone como pestaña de la barra inferior: `onVolver` pasa a ser opcional |
| 2026-11-16 | Spec puesta al día con el código: contrato real, "Abrir con enlace", criterios y plan de pruebas. Pasa a `hecha` |
