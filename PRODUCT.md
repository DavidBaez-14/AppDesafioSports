# Product

<!-- impeccable:product-schema 1 -->

> Alcance: este registro describe **solo la app Android Desafío Sports** (este repo). La web CodeCup tiene su propio `PRODUCT.md` en `CODECUP/`. Comparten backend (BackendCodeCup) y Supabase Auth; el móvil manda y la web lo sigue después.

## Platform

android

## Users

- **Principal: el organizador.** Cualquier persona que quiere armar un torneo amateur desde el celular, en minutos, sin ser experta en paneles. Ejemplo de referencia: un torneo relámpago de tenis de mesa entre compañeros. **Cuando las necesidades chocan, gana el organizador** (decisión del 8-nov-2026).
  - No es el admin global de la web CodeCup, que maneja dashboards a diario. Aquí la persona organiza de vez en cuando y la app no puede pedirle que aprenda un panel.
- **Participantes:**
  - **Jugador:** encuentra un torneo, se inscribe (solo, en deportes individuales; en un equipo, en los de equipo), sigue sus partidos y acumula su historial deportivo.
  - **Delegado:** inscribe a su equipo, arma el plantel y sube los comprobantes.
- **Árbitro:** opera los partidos que le asignan (planilla: alineación, eventos en orden, cerrar).
- **Visitante sin cuenta:** mira los torneos públicos. Para inscribirse necesita cuenta.

## Product Purpose

Crear, encontrar y jugar torneos amateur de cualquier deporte desde el celular, y que el historial deportivo de quien juega (partidos, estadísticas, títulos) quede registrado de un torneo a otro.

Hay éxito cuando:
- un organizador crea y publica un torneo en pocos minutos y lo opera sin ayuda;
- un participante encuentra el torneo por deporte o por nombre, sin que nadie le mande un enlace, y completa su inscripción sin preguntarle al organizador en qué va;
- un jugador no profesional puede mostrar su trayectoria deportiva.

## Positioning

- **Encontrar sin enlaces.** Si el organizador hace público su torneo, no necesita el número de nadie: basta decir "búsquenlo en Desafío Sports, en tenis de mesa". Otras apps dependen de que el organizador reparta un enlace.
- **Trazabilidad deportiva amateur.** El historial del jugador no muere con el torneo ni con el deporte: se acumula en su perfil.
- **Simple por diseño.** Una sola app para todos los roles, sin paneles de administración ni modos que elegir al entrar.

## Operating Context

- **Uso:** en el celular, muchas veces en la cancha o justo antes de jugar: el árbitro llena la planilla de pie, el organizador aprueba inscripciones entre partidos.
- **Sesión:** Supabase Auth (correo y contraseña; Google cuando exista el cliente OAuth Android). El perfil y los permisos vienen de `GET /api/auth/me`.
- **Pagos:** inscripciones y multas se pagan subiendo un comprobante que aprueba el organizador, o en persona. Sin pasarela de pagos.
- **Entorno de trabajo:** todo corre en local (emulador contra BackendCodeCup en `:8082` y Supabase #1). Despliegue sin decidir.
- **Proceso:** cada cambio nace de una spec `MOV-NNNN` en `docs/specs/` y trae tests. La verificación visual es en el emulador (claro, oscuro y letra al 130 %).

## Capabilities and Constraints

- **Stack:** Kotlin, Jetpack Compose y Material 3. Clean Architecture (domain, data, presentacion), MVVM y DI manual. Sin Firebase, sin Hilt y sin vistas XML. Ver `docs/arquitectura.md`.
- **Roles (decisión del 8-nov-2026):**
  - Los 4 roles se mantienen: **administrador del torneo, delegado, árbitro y jugador**. Son **por torneo**, no globales: la misma cuenta puede organizar un torneo, arbitrar en otro y jugar en un tercero.
  - **Jugador de base, con secciones.** Todos entran como jugador: el login no pregunta rol ni modo. **Creador** y **Árbitro** son secciones a las que se entra desde Inicio, desde la foto de perfil o al crear un torneo. Creador está para cualquiera; Árbitro aparece solo si te invitaron. El usuario eligió separar los roles porque así hay menos carga cognitiva.
  - Inicio muestra arriba "Tus secciones", con lo urgente de cada una, y "Crear torneo" es su acción principal: el organizador llega en un toque.
  - El torneo se ve igual desde cualquier sección. Las acciones completas de un rol están en su sección; desde Inicio o Explorar hay un acceso directo ("Abrir en Creador").
  - **En el móvil nadie solicita roles:** crear un torneo te hace su administrador, una invitación te hace árbitro o coadministrador, inscribir un equipo te hace su delegado, inscribirte te hace jugador. El "solicitar rol con motivo" queda solo en la web, mientras exista allí.
  - Esto reemplaza la regla "tres modos sobre una misma cuenta" del plan de UI: ya no se elige modo al primer login ni se guarda el modo.
  - Prototipo navegable de esta decisión, con la alternativa "sin modos" para comparar: `docs/prototipo-roles/index.html`.
- **Visibilidad:** la decide el organizador al crear el torneo.
  - **Público:** aparece en Explorar (por deporte) y en la búsqueda, y se ve sin cuenta.
  - **No listado:** solo con el enlace.
  - **Privado:** solo lo ven sus participantes.
  - **Abierto:** el valor por defecto al crear. El plan del backend proponía "No listado".
- **Deportes individuales:** cada persona se inscribe sola, y en la tabla y en los cruces aparece la persona, no un equipo. Necesita el catálogo de deportes del backend.
- **Neutral al deporte:** los eventos no llevan minuto y la línea de tiempo va en orden. Nada de la interfaz común asume goles, tarjetas o posiciones de fútbol.
- **Backend pendiente a propósito:** M3 (roles por torneo) y M4 (visibilidad y lecturas públicas) **no se implementan hasta validar la interfaz**. El backend se escribe para lo que las pantallas aprobadas necesitan, nada más. Hoy el backend tiene roles globales (`ADMINISTRADOR`, `ARBITRO`, `DELEGADO`, `JUGADOR`).
- **Terminología:** "Bloquear", nunca "Eliminar". El bloqueo conserva el historial.
- **Ninguna feature se quita sin confirmación:** las vistas se reubican.
- **Solo en la web:** el panel de Usuarios (buscar, bloquear, invitar a la plataforma).

## Brand Commitments

- **Nombre:** Desafío Sports. Paquete `co.edu.ufps.desafiosports`.
- **Logo:** todavía no existe. Cuando exista, mantiene los colores de la identidad: azul marino oscuro y naranja.
- **Color (restricción del usuario):** el tema oscuro conserva el azul marino oscuro de la web CodeCup con el naranja de marca (familia `#FF5500` del Code Cup Design System). El tema claro queda a criterio del diseño.
- **Idioma:** español en toda la interfaz, tuteando.
- **Voz:** enérgica en lo público (torneos, resultados, salón de la fama), con titulares cortos, los números como protagonistas y sin emojis. Sobria y clara en la gestión, donde la energía de marca no debe estorbar al trámite.

## Evidence on Hand

- **Code Cup Design System:** `CODECUP/Code Cup Design System/` (tokens en `colors_and_type.css`). Sus ilustraciones y el balón son de fútbol: no sirven como marca multideporte.
- **Mockups de Figma** "Prototipo Móviles" (`fcRKu4JesiNsCnRxNRJvfy`): orientativos, hechos para CodeCup antes del giro a multideporte.
- **Flujos de la web** que ya funcionan: `CODECUP/Frontend-CodeCup/codecup`.
- **Datos de demo:** seed de usuarios `@prueba.local` del backend.
- **No hay:** logo, usuarios reales, testimonios, cifras de uso ni fotos de torneos. No inventarlos.

## Product Principles

1. **El organizador primero.** Crear y operar un torneo desde el celular tiene que caber en minutos. Si una decisión ayuda a otro rol pero frena al organizador, se replantea.
2. **Cada rol en su lugar, sin pedir permisos.** Todos entran como jugador; lo que organizas o arbitras vive en su sección y avisa en Inicio cuando te necesita. Los roles salen de lo que haces (crear, ser invitado, inscribir), nunca de una solicitud.
3. **Simple al empezar, completo cuando se necesita.** Al crear se pide lo esencial y lo demás se configura después. La complejidad de la web no se traslada.
4. **El estado siempre a la vista.** Pendiente, aprobado, rechazado, bloqueado o inhabilitado: nadie pregunta en qué va su trámite.
5. **Sancionar sin borrar.** Bloqueos y multas conservan el historial y se nombran con honestidad.

## Accessibility & Inclusion

- Contraste AA en ambos temas: el blanco sobre `#FF5500` no alcanza para texto normal.
- Objetivos táctiles de 48 × 48 dp como mínimo, texto en `sp` y verificación con la letra al 130 %.
- El botón Atrás del sistema siempre funciona.
