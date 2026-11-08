# Plan de UI · Pantallas con Impeccable y el Design System

> **Estado:** borrador para revisar (8 nov 2026).
> **Cómo responder:** las preguntas están en el **§9**. Debajo de cada una hay un espacio **"Tu respuesta"**: escribe ahí, aunque sea "de acuerdo". Con esas respuestas se redacta la primera spec de diseño (MOV-0002).
> **Base:** la estructura y la red ya funcionan ([MOV-0001](specs/MOV-0001-estructura-base.md)): la app inicia sesión en Supabase y consulta `GET /api/auth/me` en el emulador.

---

## 1. Objetivo y prioridades

- **Objetivo:** diseñar e implementar las pantallas de Desafío Sports con la skill **Impeccable**, sobre el **Code Cup Design System**.
- **La prioridad es que funcionen.** Cada pantalla llega conectada al backend real y con todos sus estados: cargando, vacío, error, sin conexión y éxito. Recién después se pule. Una pantalla linda que no funciona no cuenta.
- **El móvil manda.** Lo que quede bien aquí se lleva después a la web, con su propia spec. Por eso el resultado del diseño se documenta en un `DESIGN.md` reutilizable (§3).

---

## 2. Insumos

| Insumo | Dónde | Cómo se usa |
|---|---|---|
| **Code Cup Design System** | `CODECUP/Code Cup Design System/`: tokens en `colors_and_type.css`, `assets/`, `preview/` | Base de color, tipografía, radios, espaciado y voz. **Se adapta** a Material 3; no se copia la web |
| **Mockups de Figma** "Prototipo Móviles" | Archivo `fcRKu4JesiNsCnRxNRJvfy` (MCP de Figma, cuenta UFPS) | **Orientativos.** Se toma la estructura de navegación y de las vistas (clasificación, partidos, estadísticas). La mayoría no sigue el Design System y se hicieron para CodeCup, antes del giro a multideporte |
| **Web CodeCup** | `Frontend-CodeCup/codecup` | Flujos que ya funcionan: planilla, plantel, comprobantes, perfil |
| **Backend** | BackendCodeCup, con Swagger en `http://localhost:8082/swagger-ui/index.html` | Contratos y códigos de error |
| **Inventario de pantallas** | §7 de este documento | Qué pantallas hay, por modo y por fase |

---

## 3. Cómo trabaja Impeccable en Android

Lo que hay que saber antes de empezar:

- **Material 3 manda en la estructura, la navegación y la interacción. La marca entra por el tema:** roles de color, escala tipográfica, formas y movimiento. No se copian componentes de la web ni de iOS.
- **Componentes esperados:**
  - barra superior;
  - barra de navegación inferior, con 3 a 5 destinos;
  - **un FAB por pantalla, solo para la acción principal**;
  - *snackbar* para avisos breves;
  - hojas inferiores;
  - diálogos solo para decisiones que deben interrumpir.
- **Táctil y accesible:**
  - objetivos de **48 × 48 dp** como mínimo, con 8 dp entre ellos;
  - texto en `sp`;
  - pantalla de borde a borde respetando las barras del sistema;
  - el botón **Atrás del sistema siempre funciona**.
- **El tema oscuro es un esquema completo,** no una inversión de colores.
- **La verificación es con capturas del emulador por `adb`, nunca del navegador.** Siempre en teléfono, en tema claro y oscuro, y con la **letra al 130 %**. Los modos `live` y `generate` de Impeccable trabajan sobre el navegador, así que **no aplican** aquí.
- **Necesita un `PRODUCT.md` con `Platform: android` en la raíz de este repo.** El de `CODECUP/` describe solo la web CodeCup. Se crea con `/impeccable init` en la Fase 0.
- **Al terminar una fase, `/impeccable document`** genera el `DESIGN.md` a partir de lo que de verdad quedó construido, no de lo que se pensaba hacer. Ese archivo es el que después se lleva a la web.
- **Modo de superficie:** las pantallas de los modos (Jugador, Árbitro, Creador) son de tipo *Operate*: claridad, estado y patrones conocidos. Las vistas públicas (inicio, torneo, salón de la fama) pueden ser más expresivas, siempre dentro de Material.

**Dónde corre la skill:**
- Hoy está instalada en el workspace `CODECUP/.claude/skills/impeccable`.
- Para que lea **este** `PRODUCT.md`, Claude Code debe abrirse con `DesafioSports/` como carpeta raíz.
- Si ahí no aparece la skill, se copia a `~/.claude/skills/impeccable` (nivel usuario). No se sube a este repo.

---

## 4. Flujo de trabajo por spec de pantalla

```
spec MOV en borrador
   └─▶ /impeccable shape <pantalla>  → plan de UX: jerarquía, estados, textos (se anota en la spec)
        └─▶ spec aprobada
             └─▶ construir en Compose: XxxScreen + XxxContenido + XxxViewModel + XxxState,
                 conectado al backend (o a fakes si el endpoint no existe aún)
                  └─▶ /impeccable critique y /impeccable audit (variante nativa) → arreglar
                       └─▶ /impeccable polish
                            └─▶ emulador: claro, oscuro, letra 130 %, girar y dato extremo
                                → capturas a docs/specs/evidencias/MOV-NNNN/
                                 └─▶ tests (ViewModel; UI si es un flujo crítico) → PR → hecha
```

Comandos de Impeccable que se usarán:

| Comando | Cuándo |
|---|---|
| `init` | Una vez, en la Fase 0, para crear el `PRODUCT.md` de la app |
| `shape` | Antes de construir cada pantalla: el plan de UX |
| `critique` · `audit` | Al terminar de construir: revisión de UX y revisión técnica (accesibilidad, Android) |
| `polish` | Pasada final antes del PR |
| `typeset` · `colorize` · `layout` | Ajustes puntuales del sistema visual (Fase 0) |
| `harden` · `onboard` | Errores y casos límite; primer uso y estados vacíos |
| `document` | Al cerrar cada fase: actualiza `DESIGN.md` |

---

## 5. Fases

### Fase 0 · Fundaciones del sistema visual (MOV-0002 y MOV-0003)

1. `/impeccable init`: crea el `PRODUCT.md` de Desafío Sports (Android, usuarios, tono, principios y marca provisional).
2. **Identidad provisional:**
   - nombre en `strings.xml` (ya está: "Desafío Sports");
   - ícono adaptativo y monocromo;
   - logotipo tipográfico.
3. **Tema definitivo en Compose:**
   - esquemas claro y oscuro hechos con los tokens;
   - tipografía de marca, con las fuentes empaquetadas en `res/font`;
   - formas, espaciado (4, 8, 12, 16, 24 y 32 dp), elevación tonal y movimiento.
4. **Componentes base:**
   - botones: lleno, tonal, contorno y texto;
   - campos con error, tarjetas y estados vacío, cargando, error y sin conexión;
   - barra superior, **barra inferior por modo** y selector de modo;
   - **chips de estado**: pendiente, aprobado, rechazado, bloqueado e inhabilitado;
   - escudo o avatar con iniciales;
   - fila de tabla con cifras de ancho fijo.
5. **Pantallas de calibración**, con datos de ejemplo y sin backend: la **tabla de clasificación** y la **planilla del árbitro**. Son las dos más densas: si el sistema aguanta ahí, aguanta en todas.
6. **Mapeo de deporte** (`ui/deporte/`): fútbol sala, más un deporte de prueba (voleibol), para comprobar que nada depende del fútbol.
7. `/impeccable document`: primera versión del `DESIGN.md`.

**Entregable:** catálogo de componentes en `@Preview` y capturas del emulador.

### Fase 1 · Acceso, F0 (MOV-0004 a MOV-0006)

- **Pantallas:**
  - arranque;
  - login: correo, Google, "¿Olvidaste tu contraseña?", "Crear cuenta" y "Entrar sin cuenta";
  - registro en 2 pasos y "Revisa tu correo";
  - completar registro: nombres, apellidos, tipo y número de documento, más la solicitud de rol con motivo;
  - recuperar contraseña;
  - cuenta bloqueada o solicitud pendiente;
  - elegir y cambiar modo;
  - la estructura de navegación de cada modo, con sus estados vacíos.
- **Backend:** todo existe (`/api/auth/me`, `/registro`, `/solicitar-rol` y Supabase).
- **Google nativo** entra cuando exista el cliente OAuth Android (pregunta P14).
- El diagnóstico deja de ser la pantalla de inicio (pregunta P16).

### Fase 2 · Modo Jugador, F1 (MOV-0007 a MOV-0010)

- Mi perfil e historial: equipos, partidos, estadísticas y títulos.
- Perfil deportivo opcional.
- Unirme a un equipo y mis solicitudes.
- Como delegado: plantel (con y sin cuenta, camisetas), solicitudes de ingreso, mis inscripciones y subir comprobantes.
- Mis tarjetas y mis multas pendientes.
- El backend existe, con roles globales por ahora.

### Después: dependen del backend

| Fase | Pantallas | Depende de |
|---|---|---|
| F2 · Árbitro | Partidos asignados y planilla | Backend M3: roles por torneo y partidos asignados |
| F3 · Creador | Mis torneos, crear torneo, configuración, inscripciones y comprobantes, fixture, premios y árbitros | Backend M3 |
| F4 · Visitante | Inicio, torneos, detalle del torneo, clasificación, partidos, detalle del partido, estadísticas y salón de la fama | Backend M4: lecturas públicas |
| F5 · Compartir | Enlace de un torneo no listado y perfil público | Backend M4: visibilidad |
| Multideporte | Selector de deporte y marcador por sets | Catálogo de deportes en el backend |

---

## 6. Reglas de diseño ya decididas

Salen de decisiones anteriores; no se vuelven a discutir salvo que tú lo pidas.

1. **Tres modos sobre una misma cuenta:** Jugador, Árbitro y Creador, más el Visitante sin sesión.
   - El modo es la interfaz; **los permisos los decide el backend**.
   - No hay administradores globales: el Creador administra **solo sus torneos**.
2. **Árbitros:** se asignan por partido. Solo pueden operar todo un torneo si el Creador les da ese permiso.
3. **Neutral al deporte.** Los eventos **no llevan minuto**: la línea de tiempo va en orden.
4. **Sin datos personales en vistas públicas:** se usa el nombre visible "Nombres A.", y no se muestra ningún nombre bajo los equipos en la clasificación.
5. **El estado siempre a la vista:** pendiente, aprobado, rechazado, bloqueado o inhabilitado. Nadie debería tener que preguntarle al organizador en qué va su trámite.
6. **"Bloquear", nunca "Eliminar".** Sancionar sin borrar el historial.
7. **No se quitan features:** las vistas se reubican.
8. **Grupo y fecha se eligen con dos selectores,** no con una fila de *pills* (con 12 fechas y 4 grupos no caben).
9. **Voz:** español y tuteo.
   - **Lo público es enérgico:** titulares cortos, números protagonistas y sin emojis.
   - **Los modos son sobrios y claros.**
10. **Multas:** se pagan con comprobante (lo sube el jugador o su delegado y lo aprueba el Creador) o en persona. Sin pasarela de pagos.
11. **"Inscribir equipo"** se diseña después de revisar los roles por torneo (backend M3).
12. **El panel de Usuarios** (buscar, bloquear e invitar) queda **solo en la web**.

---

## 7. Inventario de pantallas

Los IDs `N-x` vienen de la revisión de los mockups del 2-nov.

| ID | Pantalla | Modo | Fase | Endpoints | ¿Backend listo? |
|---|---|---|---|---|---|
| A-1 | Arranque (resuelve la sesión) | Todos | F0 | `GET /api/auth/me` | Sí |
| A-2 | Login (+ Google, olvidé mi contraseña, crear cuenta, entrar sin cuenta) | Visitante | F0 | Supabase Auth | Sí (Google: falta el cliente OAuth Android) |
| A-3 | Registro: correo y contraseña | Visitante | F0 | Supabase `signUp` | Sí |
| N-1 | Revisa tu correo | Visitante | F0 | — | Sí |
| N-2 | Completar registro + solicitar rol | Con sesión | F0 | `POST /api/auth/registro`, `POST /api/auth/solicitar-rol` | Sí |
| N-3 | Recuperar contraseña | Visitante | F0 | Supabase `resetPasswordForEmail` | Sí (el cambio de contraseña sigue en la web) |
| N-5 | Cuenta bloqueada o solicitud pendiente | Con sesión | F0 | `GET /api/auth/me` | Sí |
| N-7 | Elegir y cambiar modo | Con sesión | F0 | — (local) | Sí |
| N-8 | Mi perfil e historial | Jugador | F1 | `GET /api/supercopa/mi-perfil` | Sí |
| N-9 | Editar perfil deportivo (opcional) | Jugador | F1 | `PUT /api/supercopa/mi-perfil` | Sí |
| N-10 | Unirme a un equipo y mis solicitudes | Jugador | F1 | torneos, equipos, `POST /solicitudes`, `mis-solicitudes` | Sí (roles globales) |
| N-12 | Plantel (con y sin cuenta, camisetas) | Jugador (delegado) | F1 | `…/equipo-torneo/{id}/miembros`, remover, camiseta | Sí |
| N-13 | Solicitudes de ingreso | Jugador (delegado) | F1 | `solicitudes/pendientes`, aprobar, rechazar | Sí |
| N-14 | Mis inscripciones | Jugador (delegado) | F1 | `delegado/inscripciones/mias`, `torneos/disponibles` | Sí |
| N-15 | Subir comprobante (inscripción y multa) | Jugador | F1 | `comprobantes/upload-url`, `comprobantes`, `mis-comprobantes` | Inscripción sí. Multa subida por el jugador: falta backend |
| — | Mis tarjetas y mis multas pendientes | Jugador | F1 | `GET /api/finanzas/multas/mias` | Sí |
| N-4 | Aceptar invitación (enlace) | Cualquiera | F5 | `POST /api/auth/invitaciones/{token}/aceptar` | Sí, sin ámbito (M3) |
| N-16 | Partidos asignados | Árbitro | F2 | Partidos asignados | **No** (M3) |
| N-17 | Planilla: alineación, eventos en orden, cerrar, W.O., cancelar | Árbitro | F2 | `…/partidos/{id}/alineacion`, `/eventos`, `/cerrar`, `/wo`, `/cancelar` | Sí |
| N-18 | Mis torneos | Creador | F3 | `GET /api/supercopa/admin/torneos` | Sí, con rol global (M3 filtra) |
| N-19 | Crear torneo: deporte, visibilidad, documento obligatorio | Creador | F3 | `POST …/admin/torneos` | Parcial (M3, M4 y multideporte) |
| N-20 | Configuración: formato, inscripción y pagos, multas | Creador | F3 | `…/configuracion`, `inscripcion-config`, `multa-config` | Sí |
| N-21 | Premios | Creador | F3 | `…/premios`, asignar | Sí |
| N-22 | Inscripciones y comprobantes | Creador | F3 | `…/inscripciones`, `comprobantes/pendientes`, aprobar, rechazar | Sí |
| N-23 | Fixture e iniciar torneo | Creador | F3 | `…/fixture`, `…/iniciar` | Sí |
| N-24 | Árbitros y coadministradores | Creador | F3 | Invitaciones con ámbito, `torneo_roles` | **No** (M3) |
| P-1 | Inicio (torneo destacado) | Visitante | F4 | `/api/public/…` | **No** (M4) |
| P-2 | Torneos | Visitante | F4 | `GET /api/public/torneos` | **No** (M4) |
| P-3 | Detalle del torneo (requisitos, premios, inscribirse) | Visitante | F4 | `GET /api/public/torneos/{id}` | **No** (M4) |
| P-4 | Clasificación (grupos, global, eliminatorias) | Visitante | F4 | `/clasificacion` pública | **No** (M4) |
| P-5 | Partidos (selector de grupo y de fecha) | Visitante | F4 | `/partidos` públicos | **No** (M4) |
| N-6 | Detalle del partido (eventos) | Visitante | F4 | Eventos públicos | **No** (M4) |
| P-6 | Estadísticas: goleadores y tarjetas | Visitante | F4 | `/tarjetas` y goleadores | **No** (M4) |
| P-7 | Salón de la fama | Visitante | F4 | `/api/public/salon-de-la-fama` | **No** (M4) |
| N-25 | Compartir enlace de un torneo no listado | Creador y público | F5 | `GET /api/public/t/{token}` | **No** (M4) |
| N-11 | Perfil público de un jugador | Visitante | F5 | `GET /api/public/personas/{id}` | **No** (M4) |
| N-27 | Selector de deporte y filtros | Todos | Multideporte | Catálogo de deportes | **No** |
| N-26 | Usuarios: buscar, bloquear e invitar | — | — | — | **Solo en la web** |

---

## 8. Riesgos y avisos de diseño

| Riesgo | Detalle | Qué se propone |
|---|---|---|
| **Contraste del naranja** | El blanco sobre `#FF5500` da 3:1 y no pasa el contraste AA para texto normal. Impeccable lo va a marcar | Pregunta P3. El tema provisional ya usa texto azul marino sobre el naranja |
| **Material gráfico de fútbol** | Los assets del Design System (balón sci-fi, ilustraciones) son de fútbol, y la app es multideporte | No usar el balón como marca (pregunta P1) |
| **Fuentes** | Barlow y las demás vienen de Google Fonts, sin archivos propios | Empaquetar los TTF (licencia OFL) en `res/font`. Las fuentes descargables dependen de Google Play |
| **Íconos** | El Design System usa Lucide (web). Lo nativo en Android es Material Symbols | Pregunta P5 |
| **Disco lleno** | El 8-nov el disco C: estaba al 100 % (760 MB libres) y el emulador no arrancaba. Impeccable verifica **siempre** con el emulador | **Libera al menos 15 GB** antes de la Fase 0 (acción tuya, §10) |
| **Un solo Auth para dos marcas** | El correo de confirmación y la pantalla de consentimiento de Google muestran el nombre configurado en Supabase y en GCP, el mismo para CodeCup y para Desafío Sports | Pregunta P14 |
| **Límite del MCP de Figma** | El plan Starter limita las llamadas | Consultar solo los nodos de la pantalla en curso |

---

## 9. Preguntas para empezar

> Responde debajo de cada una. Si estás de acuerdo con la recomendación, basta con "de acuerdo".

### P1 · Identidad visual de Desafío Sports

**Contexto:** el Design System es de CodeCup: naranja `#FF5500` sobre azul marino, Barlow Condensed y un balón sci-fi como logo. Desafío Sports es otra marca, y multideporte.

**Opciones:**
- **(a)** Usar la paleta y las tipografías del Design System como base, con un logotipo tipográfico provisional "DESAFÍO SPORTS" y sin el balón.
- **(b)** Crear una identidad propia: paleta y logo nuevos.
- **(c)** Esperar a tener un logo definitivo antes de diseñar.

**Recomendación:** **(a)**. Así se puede avanzar ya, y el logo final se cambia en un solo lugar (el ícono y `strings.xml`).

**Tu respuesta:**
>

### P2 · Tema claro, oscuro o los dos

**Contexto:** el Design System es oscuro. Android espera los dos temas como esquemas completos. Hoy la base sigue al sistema: claro u oscuro según el teléfono.

**Opciones:**
- **(a)** Oscuro como principal, más un claro completo.
- **(b)** Solo oscuro.
- **(c)** Los dos al mismo nivel, siguiendo al sistema.

**Recomendación:** **(a)**, **sin** color dinámico (Material You), porque tapa los colores de la marca.

**Tu respuesta:**
>

### P3 · Contraste del naranja en los botones

**Contexto:** el blanco sobre `#FF5500` no alcanza el contraste mínimo para texto normal (3:1 cuando se piden 4,5:1).

**Opciones:**
- **(a)** Botón naranja de marca con texto azul marino, como en la captura 04 del diagnóstico.
- **(b)** Botón de un naranja más oscuro (`#B33500`) con texto blanco.
- **(c)** Mantener el blanco sobre `#FF5500` y aceptar el bajo contraste.

**Recomendación:** **(a)** en el tema oscuro y **(b)** en el claro.

**Tu respuesta:**
>

### P4 · Tipografía

**Contexto:** el Design System usa 4 familias: Barlow Condensed, Barlow, Rajdhani y JetBrains Mono. En un móvil eso pesa y se ve recargado.

**Recomendación:**
- **Barlow Condensed** para titulares y marcadores, y **Barlow** para el texto, ambas empaquetadas en la app;
- cifras de ancho fijo para tablas y marcadores;
- sacar Rajdhani y JetBrains Mono.

**Tu respuesta:**
>

### P5 · Íconos

**Contexto:** la web usa Lucide. En Android lo nativo, y lo que espera Impeccable, es **Material Symbols**.

**Opciones:**
- **(a)** Material Symbols (variante *Rounded* u *Outlined*).
- **(b)** Lucide también en Android.

**Recomendación:** **(a)**. La web puede seguir con Lucide: lo que tiene que coincidir es qué representa cada ícono, no su trazo exacto.

**Tu respuesta:**
>

### P6 · Material 3 frente a los mockups

**Contexto:** con Impeccable, la app se sentirá "Android" (barras, botón flotante, hojas inferiores), con tu marca encima. Algunas cosas de los mockups cambian de forma: las *pills*, el encabezado con "Ingresar" y el selector de rol.

**Recomendación:** aceptarlo. Al pasar a la web se llevan los tokens, la jerarquía y los flujos, no los componentes de Material tal cual.

**Tu respuesta:**
>

### P7 · Alcance de la primera pasada con Impeccable

**Opciones:**
- **(a)** Fase 0 (sistema visual, componentes y pantallas de calibración) más la Fase 1 (acceso).
- **(b)** Diseñar todas las pantallas de una vez.
- **(c)** Solo la Fase 0.

**Recomendación:** **(a)**. Las pantallas de acceso usan endpoints que ya existen, así que quedan funcionando de punta a punta.

**Tu respuesta:**
>

### P8 · Datos de ejemplo y multideporte

**Contexto:** las pantallas que todavía no tienen backend (públicas, calibración) se diseñan con datos de ejemplo.

**Recomendación:** fútbol sala **más un torneo de voleibol simulado**, sobre todo en la clasificación y en los eventos, para comprobar que el diseño no depende del fútbol.

**Tu respuesta:**
>

### P9 · Orientación y dispositivos

**Opciones:**
- **(a)** Teléfono. Se diseña y verifica en vertical, pero la app puede girar sin romperse (hoy lo hace). Las tablets quedan para después.
- **(b)** Bloquear la app en vertical.
- **(c)** Incluir tablets desde ya.

**Recomendación:** **(a)**.

**Tu respuesta:**
>

### P10 · Tono

**Contexto:** el `PRODUCT.md` de la web dice "enérgico en lo público, sobrio en los paneles".

**Recomendación:** lo mismo en la app. Las vistas públicas celebran y los modos priorizan la claridad y el estado.

**Tu respuesta:**
>

### P11 · Marca al llevar el diseño a la web

**Contexto:** la web es CodeCup (`codecup.games`) y la app es Desafío Sports.

**Opciones:**
- **(a)** Un `DESIGN.md` con tokens comunes, y cada producto con su nombre y su logo.
- **(b)** La web adopta también la identidad de Desafío Sports.

**Recomendación:** **(a)**.

**Tu respuesta:**
>

### P12 · "Crear torneo" como botón flotante

**Contexto:** el borrador del plan móvil tenía una pestaña "Crear (+)" en el modo Creador. Material 3 pide "un FAB, una acción principal".

**Recomendación:**
- "Crear torneo" pasa a ser un **FAB dentro de "Mis torneos"**;
- la barra del Creador queda en "Mis torneos · Perfil", o suma una tercera pestaña real si aparece una.

**Tu respuesta:**
>

### P13 · Las 12 preguntas abiertas de la revisión de mockups

Marca **sí** si estás de acuerdo o escribe el cambio.

| # | Pregunta | Propuesta | ¿De acuerdo? |
|---|---|---|---|
| 5 | Crear equipo: mínimo de jugadores, documento, camiseta | El mínimo lo pone el torneo (o el deporte). Documento opcional, salvo que el torneo lo exija; si se pide, va con su tipo. Camiseta opcional. Se diseña junto con los roles por torneo (M3) | |
| 7 | Qué abre un partido en el modo Árbitro | La **planilla**: alineación → eventos en orden → cerrar, W.O. o cancelar | |
| 8 | Inscripción presencial | Pasa al flujo en la app: inscribir → comprobante → aprobación. Los "requisitos" son un texto libre del Creador | |
| 9 | Próxima reunión, avisos y galería | Fuera del MVP, en el backlog (no se borran) | |
| 10 | Nombre bajo cada equipo en la clasificación | No se muestra | |
| 11 | ¿Los correos abren la app? | Por ahora no: la confirmación y la recuperación siguen en la web. Los enlaces que abren la app llegan en la F5 | |
| 13 | *Pills* de grupo y fecha | Un selector de grupo y otro de fecha, en orden numérico | |
| 14 | Dónde va el historial del jugador | En la pestaña Perfil | |
| 15 | ¿La barra inferior cambia con sesión? | Sí, según el modo | |
| 16 | Visibilidad del torneo | Se elige al crear el torneo (no listado por defecto). Se comparte con la hoja de compartir de Android | |
| 17 | "Faltas" en el modo Jugador | Dos bloques: "Mis tarjetas" y "Multas pendientes" | |
| 18 | Usuarios (buscar, bloquear, invitar) | Solo en la web | |

### P14 · Google nativo: cuándo, y con qué nombre

**Contexto:** para el botón "Continuar con Google" hace falta un **cliente OAuth Android** en Google Cloud, con el paquete `co.edu.ufps.desafiosports` y la huella SHA-1 de tu llave de debug. El paso a paso está en el Anexo A.

Además, la pantalla de consentimiento de Google y los correos de Supabase muestran **un solo nombre** para las dos apps.

**Recomendación:**
- crear el cliente antes de la Fase 1, para que el login salga completo;
- poner en la pantalla de consentimiento un nombre neutro que sirva para las dos apps, por ejemplo "Torneos UFPS".

**Tu respuesta:**
>

### P15 · Equipo

**Preguntas:**
- ¿Cuántos compañeros se suman?
- ¿Quién aprueba las specs y quién revisa los PR?

**Recomendación:**
- tú apruebas las specs y haces el merge a `main`;
- cada PR lo revisa al menos una persona distinta del autor;
- una spec por persona a la vez (ver [`sdd.md`](sdd.md) §8).

**Tu respuesta:**
>

### P16 · ¿Qué hacemos con la pantalla de diagnóstico?

**Contexto:** hoy es la pantalla de inicio. Cuando llegue el login deja de serlo.

**Recomendación:** dejarla **solo en el build de debug**, accesible desde el login (por ejemplo, con una pulsación larga en el logo). Sirve para que cada compañero compruebe su entorno: backend, Supabase y su cuenta.

**Tu respuesta:**
>

---

## 10. Acciones que dependen de ti

1. **Responder el §9** en este archivo.
2. **Liberar espacio en el disco C:**, al menos 15 GB. Sin eso el emulador no arranca y Impeccable no puede verificar.
3. **Revisar y mergear la rama `mov-0001-estructura-base` a `main`** en GitHub. El repo estaba vacío, así que la rama se subió aparte; ver el README.
4. **Pasar a tus compañeros los valores de `local.properties`** (URL y clave publicable de Supabase) por un canal privado, nunca en el repo. También la contraseña de los usuarios del seed.
5. Si respondes "ahora" en P14: **crear el cliente OAuth Android** (Anexo A).

---

## Anexo A · Google nativo en Android, paso a paso

1. **Sacar el SHA-1 de debug.** Android Studio ya creó la llave:
   ```
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
   Con el proyecto abierto también sirve `gradlew signingReport`.
2. **En Google Cloud Console**, en el mismo proyecto del cliente web que usa Supabase: *APIs y servicios → Credenciales → Crear credenciales → ID de cliente de OAuth → Android*.
   - **Nombre del paquete:** `co.edu.ufps.desafiosports`.
   - **Huella:** el SHA-1 del paso 1.
   - Este cliente no se copia en la app: solo autoriza la pareja paquete + firma.
3. **En la app:** Credential Manager usa como `serverClientId` el **ID del cliente web** (el que ya está en Supabase), no el del cliente Android.
4. **En Supabase #1**, en *Authentication → Providers → Google*: confirmar que el ID del cliente web está en *Client IDs*. La app genera un *nonce*: se lo pasa con hash a Google y sin hash a `signInWith(IDToken)`.
5. **Pantalla de consentimiento:** sigue en modo Prueba. Los usuarios de prueba que ya existen sirven en el emulador, que necesita Google Play y una de esas cuentas (el AVD `Pixel_8` tiene Play Store).
6. **Más adelante:** la llave de release y Play App Signing tienen otro SHA-1, así que habrá que agregar otro cliente Android o esa huella.
