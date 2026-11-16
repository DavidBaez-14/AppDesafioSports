---
id: MOV-0007
titulo: Crear torneo en tres pasos
tipo: feature
estado: hecha
prioridad: P1
fase: F3
modo: Creador
responsable: David
depende_de: [MOV-0003, backend 0024, backend 0029]
rama: mov-0007-crear-torneo
---

# MOV-0007 · Crear torneo en tres pasos

> **Spec puesta al día el 16-nov-2026 con lo que hace el código de `main`.** El borrador del 9-nov proponía un backend que todavía no existía; el contrato de §4 es el real (backend 0029).

## 1. Contexto

- **Manda el organizador** (`PRODUCT.md`): crear un torneo tiene que caber en minutos. Lo demás se configura después, en la sección Creador.
- **Referencia:** prototipo, pantallas `crear` (3 pasos) y `creado`.

## 2. Alcance

- **Se entra** desde el botón flotante "Crear torneo" de Inicio o desde la sección Creador.
- **Paso 1, deporte:** las opciones vienen del catálogo (`GET /api/public/deportes`) y cada una dice si es individual o por equipos.
- **Paso 2, nombre y formato:**
  - nombre (obligatorio, de 3 a 150 caracteres);
  - formato: Liga, Eliminación directa o Grupos y eliminatorias, cada uno con una línea que lo explica;
  - cupos, con el rango de cada formato: Liga 2 a 64, Eliminación directa 2 a 16, Grupos y eliminatorias 4 a 64. Cambiar de formato ajusta los cupos a su rango;
  - fecha de inicio, de hoy en adelante.
- **Paso 3, visibilidad:** Público, Solo con enlace o Privado, cada uno con su explicación. Además, el interruptor "Cobrar inscripción" con el valor de la cuota (solo dígitos, mayor que cero).
- **Torneo creado:** "Tu torneo está listo", con "Compartir enlace" y "Ver mis torneos" (abre la sección Creador).
- **Cerrar (X)** en cualquier paso, o Atrás desde el primero, descarta el borrador. Si ya se escribió algo, pide confirmarlo con un diálogo.

## 3. Diseño

- **Prototipo** `crear` y `creado`: barra con X y "Paso n de 3", indicador de avance, opciones como tarjetas seleccionables y una barra inferior fija con "Atrás" y "Siguiente".
- **Premios, multas, árbitros y partidos** van en la gestión del torneo (sección Creador), no aquí.

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde |
|---|---|---|---|
| `/api/public/deportes` | GET | — | `[{codigo, nombre, tipoCompetencia}]` |
| `/api/supercopa/creador/torneos` | POST | `CrearTorneoRequestDto` | 201 `{ id, nombre, deporte, visibilidad, enlace }` |

```json
{ "nombre": "Relámpago de Tenis de Mesa", "deporte": "TENIS_MESA", "formato": "ELIMINACION_DIRECTA",
  "cupos": 16, "fechaInicio": "2026-11-21", "visibilidad": "PUBLICO",
  "cobraInscripcion": true, "valorInscripcion": 5000 }
```

- **Cualquier cuenta registrada crea un torneo** y queda como su administrador (backend 0024). No hay administradores globales.
- El torneo nace con las **inscripciones abiertas** (`PUBLICADO`) y con la configuración por defecto de su formato.
- `valorInscripcion` no viaja si no se cobra.
- El enlace existe siempre: un torneo público también se comparte con él.
- **Errores:** 400 con su `code` y su `mensaje`. La app muestra el `mensaje` y vuelve al paso del campo que falló.

## 5. Criterios de aceptación

1. **CA-1** · "Siguiente" solo avanza con el paso completo: deporte elegido; nombre, formato, cupos en rango y fecha válidos; visibilidad elegida y, si cobra, un valor mayor que cero.
2. **CA-2** · Al crear se envía el `POST` con todos los campos y la app muestra "Tu torneo está listo" con el enlace. El doble toque no crea dos torneos.
3. **CA-3** · Un error del backend se ve con su `mensaje` y la persona vuelve al paso que corresponde sin perder lo escrito. Si el error no es de un campo, se queda en el último paso.
4. **CA-4** · El torneo creado aparece en la sección Creador y, si es público, en Explorar.
5. **CA-5** · Cerrar con datos escritos pide confirmación. Atrás vuelve al paso anterior.
6. **CA-6** · Claro, oscuro y letra al 130 % (emulador).
7. **CA-7** · Si el catálogo de deportes no carga, se ofrece "Reintentar".

## 6. Plan de pruebas

| Criterio | Prueba |
|---|---|
| CA-1, CA-2, CA-5 | `BorradorTorneoTest`, `CrearTorneoViewModelTest` |
| CA-1, CA-2, CA-3 | `CreadorRepositoryImplTest › MOV-0007 …` |
| CA-3, CA-7 | `CrearTorneoViewModelTest` |
| CA-4, CA-6 | Verificación en el emulador (`evidencias/MOV-0007/`) |

## 7. Fuera de alcance

- La gestión del torneo después de crearlo: árbitros y partidos (MOV-0011), premios, multas y reglas.
- La inscripción individual de los deportes individuales: aquí solo se guarda el deporte.

## 8. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
| 2026-11-10 | Implementada sobre el backend 0029 e integrada a `main` (`a6d1242`), junto con MOV-0008 |
| 2026-11-16 | Spec puesta al día con el código: contrato real, rangos de cupos por formato, criterios y plan de pruebas. Las dudas Q1 y Q2 quedaron resueltas: la visibilidad se elige en el paso 3 y los deportes salen del catálogo del backend. Pasa a `hecha` |
