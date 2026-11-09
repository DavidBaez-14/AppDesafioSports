---
version: 1
slug: "docs-prototipo-roles-index-html"
primary_target: "docs/prototipo-roles/index.html"
related_targets: []
---

# Inicio y roles · Desafío Sports (Android)

## Alcance

- **Superficie:** la estructura de la app (Inicio, secciones Creador y Árbitro, detalle del torneo) y cómo aparecen los 4 roles. Modo de superficie: **Operate**.
- **Etapa:** prototipo HTML navegable (`docs/prototipo-roles/index.html`) para decidir la estructura antes de escribir M3 y M4. La versión real se construye en Compose con Material 3 en la spec de diseño siguiente (MOV-0002).
- **Usuario y tarea:** el organizador manda (crear y operar un torneo en minutos). Jugadores y delegados encuentran torneos, se inscriben y siguen sus partidos. Árbitros abren la planilla de sus partidos.
- **Restricciones:** Material 3 manda en estructura y navegación; marca por tema (azul marino oscuro y naranja `#FF5500`); neutral al deporte; estado siempre a la vista.

## Direction contract

- **THESIS:** todos entran como jugador; Creador y Árbitro son secciones a las que se entra, y avisan en Inicio cuando te necesitan. Rechaza el selector de modo al primer login y las barras inferiores de dos destinos.
- **OWN-WORLD:** fondo azul marino casi negro (`#04070F`), superficies escalonadas en azul marino, tinta blanca azulada, naranja `#FF5500` reservado para la acción principal y para lo que pasa ahora. Barlow Condensed en mayúsculas para títulos de sección y cifras de marcador; Barlow para el texto. Material Symbols Rounded. Chips de estado con color semántico.
- **STORY:** al entrar ves tu próximo partido y tus equipos; arriba, "Tus secciones" con lo urgente (3 inscripciones por aprobar, la planilla de hoy). Un toque te lleva a Creador o a la planilla; "Crear torneo" te deja en Creador al terminar.
- **FIRST VIEWPORT:** barra superior con el logotipo "DESAFÍO SPORTS" (SPORTS en naranja), buscar y la foto (abre "Tus secciones"). Debajo, "Tus secciones" (Creador con su conteo, Árbitro con el chip Planilla), "Tu próximo partido" con la hora en cifras de marcador, "Mis equipos" con el estado del trámite. FAB naranja "Crear torneo" abajo a la derecha; barra inferior Inicio · Explorar · Perfil.
- **FORM:** "El modo del plan" (tercera tarjeta de la segunda mano), modificado por el usuario: sin selector al entrar, jugador como pantalla principal. seed:eeded967cb3bf242
- **FINISH:** unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

## Momento memorable

La fila de Árbitro en Inicio que dice "Hoy 4:00 pm · Sistemas vs Civil" y abre la planilla en un toque, sin cambiar de modo.

## Decisiones abiertas

- Visibilidad por defecto al crear un torneo: el prototipo deja "Público"; el plan del backend proponía "Solo con enlace".
- Tipografía (P4) e íconos (P5) del plan de UI: el prototipo usa Barlow Condensed + Barlow y Material Symbols Rounded como propuesta, sin confirmar.
- La revisión final y el `DESIGN.md` se hacen sobre la versión en Compose, no sobre este prototipo.
