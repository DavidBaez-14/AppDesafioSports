---
id: MOV-0010
titulo: Diseño de las secciones Árbitro y Creador, e Inicio provisional
tipo: diseño
estado: en-revision
prioridad: P1
fase: F2
modo: Transversal
responsable: David
depende_de: [MOV-0007, MOV-0008, MOV-0009]
rama: mov-0010-diseno-secciones
---

# MOV-0010 · Diseño de las secciones Árbitro y Creador, e Inicio provisional

## 1. Contexto

MOV-0007, MOV-0008 y MOV-0009 dejaron funcionando crear torneo, la sección Creador con las inscripciones, y la sección Árbitro con la planilla (eventos en orden y cerrar el partido). Funcionan, pero se ven como listas genéricas: el marcador es texto suelto, los eventos no dicen de qué lado pasaron, los partidos se leen como "A vs B" partidos en dos renglones y la app arranca en la pantalla de diagnóstico.

Esta spec sube el nivel visual **dentro del sistema actual** ("La cancha de noche", `DESIGN.md`): mismo color, misma tipografía, mismos radios. Cambia la composición, no la identidad. Decisión de David del 10-nov-2026.

## 2. Alcance

- **Inicio provisional** (nuevo): reemplaza al diagnóstico como pantalla de arranque con sesión.
  - Barra superior con el logotipo y la foto (iniciales) que abre el menú de la cuenta: cerrar sesión y, solo en debug, el diagnóstico.
  - Saludo con el nombre de `GET /api/auth/me`.
  - **Tus secciones:** Árbitro (solo si tiene partidos), con el próximo partido y un toque directo a su planilla; Creador, con lo que tiene por revisar o sus torneos.
  - FAB "Crear torneo", la acción principal.
  - Usa solo los endpoints que ya existen. El Inicio completo (Explorar, Perfil, Mis equipos) sigue siendo MOV-0004.
- **Sección Árbitro:** el partido de hoy que falta por cerrar va destacado arriba (hora en cifras de marcador, botón "Abrir planilla"); los demás partidos se leen como un fixture, con los equipos uno sobre otro y el marcador alineado a cada equipo.
- **Planilla:** el marcador es un tablero (nombres en Barlow Condensed, cifras grandes de ancho fijo, estado del partido); los eventos se muestran en una línea de tiempo de dos lados: lo del local a la izquierda, lo del visitante a la derecha, con el número de orden al centro.
- **Sección Creador:** cada torneo muestra su deporte, su estado con color y los cupos ocupados en cifras de marcador con una barra de llenado.
- **Crear torneo:** el formato se elige con tarjetas que dibujan su esquema (liga, llaves, grupos más llaves); el control de cupos usa botones tonales de 48 dp; "Tu torneo está listo" muestra la ficha del torneo creado.
- **Inscripciones:** cada equipo lleva su inicial en un círculo; las pendientes conservan Aprobar y Rechazar.

## 3. Diseño

- **Referencia:** Impeccable (refinamiento dentro del mundo existente), `DESIGN.md`, surface brief `.impeccable/surfaces/docs-prototipo-roles-index-html.md` (momento memorable: la fila de Árbitro en Inicio que abre la planilla en un toque).
- **Reglas que no cambian:** Naranja Único (acción principal y "lo de ahora"), Roles no hex, Plana (sin sombras; profundidad por escalones de contenedor), Doble Barlow.
- **Estados de cada pantalla:**

| Pantalla | Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|---|
| Inicio | indicador por sección | Árbitro se oculta sin partidos; Creador invita a crear el primero | aviso en la sección con Reintentar | igual que error, con el texto de sin conexión | secciones con su resumen |
| Árbitro | sin cambio | sin cambio | sin cambio | sin cambio | destacado + fixture |
| Planilla | sin cambio | "Aún no hay eventos" centrado en la línea | sin cambio | sin cambio | tablero + línea de dos lados |
| Creador | sin cambio | sin cambio | sin cambio | sin cambio | filas con cupos |
| Crear torneo | sin cambio | sin cambio | sin cambio | sin cambio | ficha del torneo |
| Inscripciones | sin cambio | sin cambio | sin cambio | sin cambio | filas con inicial |

- **Textos nuevos:** saludo ("Hola, %1$s"), "Tus secciones", "Abrir planilla", "Tu próximo partido", "Nada pendiente", "Ver la sección", resumen de torneos, menú de la cuenta. Van a `strings.xml`.
- **Accesibilidad:** objetivos de 48 dp, contraste AA, letra al 130 % sin cortes, el tablero y cada evento se leen de corrido en TalkBack ("Gol de Juan Castro, Sistemas"), Atrás del sistema funcionando.

## 4. Contrato con el backend

Sin cambios. Inicio reúne tres lecturas que ya existen:

| Endpoint | Método | Para qué |
|---|---|---|
| `/api/auth/me` | GET | nombre del saludo y las iniciales |
| `/api/supercopa/arbitro/partidos` (vía `ObtenerPartidosDeArbitroUseCase`) | GET | próximo partido de Árbitro |
| `/api/supercopa/creador/torneos` (vía `ObtenerMisTorneosUseCase`) | GET | resumen de Creador |

## 5. Criterios de aceptación

1. **CA-1** · **Dado** una sesión activa, **cuando** abre la app, **entonces** llega a Inicio (no al diagnóstico) y ve su nombre en el saludo.
2. **CA-2** · **Dado** que arbitra partidos, **cuando** carga Inicio, **entonces** la sección Árbitro muestra el próximo partido sin cerrar (el de hoy primero) y un toque abre su planilla.
3. **CA-3** · **Dado** que no tiene partidos que arbitrar, **cuando** carga Inicio, **entonces** la sección Árbitro no aparece.
4. **CA-4** · **Dado** que administra torneos con inscripciones por revisar, **cuando** carga Inicio, **entonces** Creador muestra cuántas hay por revisar en total.
5. **CA-5** · **Dado** que falla la carga de una sección, **cuando** carga Inicio, **entonces** esa sección muestra el error con Reintentar y las demás siguen funcionando.
6. **CA-6** · **Dado** Inicio, **cuando** toca "Cerrar sesión" en el menú de la cuenta, **entonces** se cierra la sesión y vuelve al login.
7. **CA-7** · **Dado** la planilla, **cuando** se registran eventos de los dos equipos, **entonces** cada evento aparece del lado de su equipo con su número de orden.
8. **CA-8** · **Dado** un torneo con cupos, **cuando** se ve en Creador, **entonces** muestra los inscritos sobre los cupos y una barra de llenado proporcional.
9. **CA-9** · **Dado** cualquier pantalla de esta spec, **cuando** se ve en oscuro, en claro y con la letra al 130 %, **entonces** no hay textos cortados ni contraste por debajo de AA.

## 6. Fuera de alcance / qué NO debe cambiar

- La lógica de los ViewModels de MOV-0007, MOV-0008 y MOV-0009 (estados, validaciones, llamadas): solo cambia cómo se pintan.
- Los `testTag` existentes se conservan.
- Explorar, Perfil, Mis equipos y la barra de navegación inferior: MOV-0004. No se muestran botones para lo que no existe.
- El diagnóstico no se borra: queda en debug, desde el menú de la cuenta y desde el login.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) o verificación | Nivel |
|---|---|---|
| CA-1 | `InicioViewModelTest › CA-1 carga el nombre de la cuenta` | ViewModel |
| CA-2 | `InicioViewModelTest › CA-2 el proximo partido es el de hoy sin cerrar` y `CA-2 sin partidos de hoy toma el proximo` | ViewModel |
| CA-3 | `InicioViewModelTest › CA-3 sin partidos la seccion arbitro no aparece` | ViewModel |
| CA-4 | `InicioViewModelTest › CA-4 suma lo que hay por revisar` | ViewModel |
| CA-5 | `InicioViewModelTest › CA-5 si falla una seccion la otra carga` y `CA-5 reintentar vuelve a pedir` | ViewModel |
| CA-6 | `InicioViewModelTest › CA-6 cerrar sesion llama al caso de uso` | ViewModel |
| CA-7 | `LineaDeEventosTest › CA-7 cada evento queda del lado de su equipo` | Unidad |
| CA-8 | `LlenadoDeCuposTest › CA-8 la barra es proporcional y no pasa de lleno` | Unidad |
| CA-9 | Emulador: capturas en oscuro, claro y letra al 130 % | Manual |

## 8. Verificación en el emulador

1. Iniciar sesión con un árbitro del seed: Inicio muestra Árbitro con su próximo partido; un toque abre la planilla.
2. Registrar eventos de los dos equipos en la planilla y ver la línea de dos lados.
3. Crear un torneo de prueba (nombre con "(prueba)") y verlo en Creador con su barra de cupos.
4. Capturas en `docs/specs/evidencias/MOV-0010/`: oscuro, claro y letra al 130 %.

## 9. Archivos afectados

- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/inicio/` (nuevo: `InicioScreen.kt`, `InicioViewModel.kt`)
- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/navigation/` (`AppNavigation.kt`, `Rutas.kt`)
- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/comun/Componentes.kt`
- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/arbitro/` (`SeccionArbitroScreen.kt`, `PlanillaScreen.kt`; nuevo `TableroYLinea.kt`)
- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/creador/` (`SeccionCreadorScreen.kt`, `CrearTorneoScreen.kt`, `InscripcionesScreen.kt`)
- `app/src/main/java/co/edu/ufps/desafiosports/ui/theme/Logotipo.kt` (variante en una línea para la barra de Inicio)
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/co/edu/ufps/desafiosports/presentacion/inicio/InicioViewModelTest.kt`, `presentacion/arbitro/LineaDeEventosTest.kt`, `presentacion/comun/LlenadoDeCuposTest.kt`
- `DESIGN.md`, `docs/specs/README.md`

## 10. Datos de prueba

- `arbitro01@prueba.local` (contraseña en `SEED_PASSWORD` del `.env` del backend): tiene partidos próximos y jugados.
- Torneo de prueba creado en Supabase #1 con "(prueba)" en el nombre, autorizado por David el 10-nov-2026.
- Casos extremos: nombres de equipo largos ("Real Código de Ingeniería de Sistemas"), torneo sin cupos, partido sin fecha.

## 11. Notas y decisiones

- **10-nov-2026 (David):** subir el nivel dentro del sistema actual, no un mundo visual nuevo; Inicio provisional con buen diseño; spec y rama propias.
- La línea de tiempo no lleva marcador parcial: saber qué evento suma punto depende del deporte, y la interfaz común no lo asume.
- En claro, el escalón `contenedor-bajo` casi no se distingue del fondo: los paneles usan `colorDePanel()` (blanco en claro, `contenedor-bajo` en oscuro).
- La línea de dos lados se verificó con datos reales en un partido ya cerrado de "Liga Relámpago (prueba)" (solo lectura). No se registraron eventos en #1: el partido programado de "Torneo QA roles 1355 (prueba)" quedó intacto.
- Se creó en #1 el torneo "Relampago de Tenis de Mesa (prueba)" (eliminación directa, 8 cupos, público) para ver Creador con datos.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-10 | Borrador inicial y en curso |
| 2026-11-10 | Implementada y verificada en el emulador (Pixel_8): 13 capturas en `evidencias/MOV-0010/`; pasa a en-revision |
