---
name: Desafío Sports
description: Torneos amateur de cualquier deporte desde el celular. Azul marino casi negro, naranja para lo que importa ahora.
colors:
  fondo: "#04070F"
  contenedor-minimo: "#020409"
  contenedor-bajo: "#090E1C"
  contenedor: "#0D1429"
  contenedor-alto: "#151F3F"
  contenedor-maximo: "#1E2D5A"
  tinta: "#EEF0F7"
  tinta-secundaria: "#A9B1CC"
  naranja: "#FF5500"
  naranja-contenedor: "#4A1700"
  sobre-naranja-contenedor: "#FFDBC9"
  azul: "#93C5FD"
  azul-contenedor: "#0D2350"
  sobre-azul-contenedor: "#DBEAFE"
  error: "#F87171"
  error-contenedor: "#3B1114"
  sobre-error-contenedor: "#FECACA"
  borde: "#6878A8"
  claro-fondo: "#F4F5F9"
  claro-contenedor-minimo: "#FFFFFF"
  claro-contenedor-bajo: "#F0F2F7"
  claro-contenedor: "#EBEEF5"
  claro-contenedor-alto: "#E5E8F0"
  claro-contenedor-maximo: "#DFE3EC"
  claro-tinta: "#0D1429"
  claro-tinta-secundaria: "#4A5578"
  claro-naranja: "#C23C00"
  claro-naranja-contenedor: "#FFE4CC"
  claro-sobre-naranja-contenedor: "#5C1A00"
  claro-azul: "#1D4ED8"
  claro-azul-contenedor: "#DBEAFE"
  claro-sobre-azul-contenedor: "#0D2350"
  claro-error: "#B91C1C"
  claro-error-contenedor: "#FEE2E2"
  claro-sobre-error-contenedor: "#7F1D1D"
  blanco: "#FFFFFF"
typography:
  logotipo:
    fontFamily: "Barlow Condensed, sans-serif"
    fontSize: "46px"
    fontWeight: 800
    lineHeight: 0.95
    letterSpacing: "0.01em"
  display:
    fontFamily: "Barlow Condensed, sans-serif"
    fontSize: "57px"
    fontWeight: 700
    lineHeight: "64px"
    letterSpacing: "-0.25px"
  headline:
    fontFamily: "Barlow Condensed, sans-serif"
    fontSize: "28px"
    fontWeight: 700
    lineHeight: "36px"
    letterSpacing: "0px"
  title:
    fontFamily: "Barlow, sans-serif"
    fontSize: "22px"
    fontWeight: 600
    lineHeight: "28px"
    letterSpacing: "0px"
  title-medium:
    fontFamily: "Barlow, sans-serif"
    fontSize: "16px"
    fontWeight: 600
    lineHeight: "24px"
    letterSpacing: "0.15px"
  body:
    fontFamily: "Barlow, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: "24px"
    letterSpacing: "0.15px"
  body-medium:
    fontFamily: "Barlow, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "20px"
    letterSpacing: "0.1px"
  body-small:
    fontFamily: "Barlow, sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: "16px"
    letterSpacing: "0.2px"
  label:
    fontFamily: "Barlow, sans-serif"
    fontSize: "14px"
    fontWeight: 600
    lineHeight: "20px"
    letterSpacing: "0.1px"
  label-medium:
    fontFamily: "Barlow, sans-serif"
    fontSize: "12px"
    fontWeight: 600
    lineHeight: "16px"
    letterSpacing: "0.3px"
rounded:
  sm: "4px"
  md: "8px"
  lg: "12px"
  xl: "16px"
  2xl: "24px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "24px"
  2xl: "32px"
  3xl: "40px"
components:
  button-primary:
    backgroundColor: "{colors.naranja}"
    textColor: "{colors.fondo}"
    typography: "{typography.label}"
    rounded: "{rounded.2xl}"
    height: "52px"
    width: "100%"
  button-primary-claro:
    backgroundColor: "{colors.claro-naranja}"
    textColor: "{colors.blanco}"
    typography: "{typography.label}"
    rounded: "{rounded.2xl}"
    height: "52px"
    width: "100%"
  button-text:
    textColor: "{colors.tinta}"
    typography: "{typography.label}"
    height: "48px"
  button-text-claro:
    textColor: "{colors.claro-tinta}"
    typography: "{typography.label}"
    height: "48px"
  input-outlined:
    backgroundColor: "{colors.fondo}"
    textColor: "{colors.tinta}"
    typography: "{typography.body}"
    rounded: "{rounded.md}"
    width: "100%"
  input-outlined-claro:
    backgroundColor: "{colors.claro-fondo}"
    textColor: "{colors.claro-tinta}"
    typography: "{typography.body}"
    rounded: "{rounded.md}"
    width: "100%"
  aviso-error:
    backgroundColor: "{colors.error-contenedor}"
    textColor: "{colors.sobre-error-contenedor}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.lg}"
    padding: "12px 14px"
    width: "100%"
  aviso-error-claro:
    backgroundColor: "{colors.claro-error-contenedor}"
    textColor: "{colors.claro-sobre-error-contenedor}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.lg}"
    padding: "12px 14px"
    width: "100%"
  top-app-bar:
    backgroundColor: "{colors.fondo}"
    textColor: "{colors.tinta}"
    typography: "{typography.title}"
    height: "64px"
---

# Design System: Desafío Sports

<!-- Registrado el 9-nov-2026 a partir del primer build en Compose (MOV-0002: tema de marca y pantallas de acceso). Fuente de verdad: app/src/main/java/co/edu/ufps/desafiosports/ui/theme/. El prototipo HTML docs/prototipo-roles/ fue la exploración de origen; donde difiere, manda Compose. -->

## Overview

**Creative North Star: "La cancha de noche"**

Desafío Sports se ve como una cancha iluminada de noche: un fondo azul marino casi negro que se aparta, tinta blanca azulada que se lee de pie y con prisa, y un solo foco naranja sobre lo que hay que hacer ahora. El oscuro es el tema principal; el claro es un gris azulado con tinta azul marino que conserva la misma jerarquía, con el naranja oscurecido para que el texto blanco del botón pase AA.

La energía de marca vive en la tipografía, no en la decoración: Barlow Condensed para los titulares y el logotipo, Barlow para todo lo que se lee y se toca. Las superficies son planas; la profundidad sale de escalones tonales en azul marino, no de sombras. El único movimiento registrado es el aviso de error que se despliega. La densidad es la de un formulario cómodo para el pulgar: columna única, márgenes de 24, separación de 12 y la acción principal abajo.

Este primer build cubre solo el tema y el acceso (arranque, login, recuperar contraseña). Lo que el sistema todavía no tiene se marca como pendiente en Components; no se inventa aquí.

**Key Characteristics:**
- Fondo azul marino casi negro con superficies escalonadas en la misma familia.
- Un naranja reservado para la acción principal y para "lo de ahora".
- Barlow Condensed (titulares, logotipo) + Barlow (texto, botones, campos), fuentes empaquetadas.
- Plano: sin sombras, la profundidad es tonal.
- Las pantallas leen roles de color, nunca valores sueltos.

**Comunes y propios de Android.** Este archivo se llevará a la web CodeCup. Son **comunes** (portables a la web): la paleta de los dos temas, las familias tipográficas y la escala de tamaños, los radios, el ritmo de espaciado, la Regla del Naranja Único y la Regla Plana. Son **propios de Android/M3**: el mapeo a roles de `ColorScheme` (primary, surfaceContainer...), los nombres de la escala `Typography` y `Shapes` de Material 3, las unidades `sp`/`dp` (en la web se leen como px a 100 %), los componentes M3 (`OutlinedTextField`, `TopAppBar`, `TextButton`), los íconos como vectores XML y la desactivación del color dinámico. En el frontmatter, `fondo`..`borde` son el tema oscuro y las claves `claro-*` el tema claro.

## Colors

Azul marino casi negro con tinta fría y un naranja de cancha; el claro invierte la luminosidad y conserva el reparto.

### Primary
- **Naranja Desafío** (`naranja`, oscuro / `claro-naranja`, claro): el color de la acción principal (Entrar, Enviar enlace, Volver a entrar) y del "SPORTS" del logotipo. En oscuro lleva tinta `fondo` encima; en claro, `blanco` (5,3:1). Es la familia `#FF5500` del Code Cup Design System, restricción de marca.
- **Brasa** (`naranja-contenedor` / `claro-naranja-contenedor`, con `sobre-naranja-contenedor`): rol `primaryContainer` de M3. Definido en el esquema; sin uso en las pantallas de acceso.

### Secondary
- No hay un segundo acento. El rol `secondary` de M3 apunta a la tinta secundaria y `secondaryContainer` a `contenedor-maximo`: es neutro a propósito.

### Tertiary
- **Azul Marcador** (`azul` / `claro-azul`, con sus contenedores): rol `tertiary` de M3. Definido en el esquema, todavía sin uso en pantalla; su rol (información, enlaces o estados) queda pendiente de la primera pantalla que lo necesite.

### Neutral
- **Noche de Cancha** (`fondo` / `claro-fondo`): fondo y `surface` de toda pantalla, también la barra superior. La ventana de Android arranca en este mismo color en cada tema (sin destello).
- **Escalones de Grada** (`contenedor-minimo` a `contenedor-maximo`, y sus `claro-*`): la escala `surfaceContainerLowest`..`Highest` de M3. Es el único medio de profundidad.
- **Tinta Reflector** (`tinta` / `claro-tinta`): texto principal, el "DESAFÍO" del logotipo, botones de texto.
- **Tinta de Grada** (`tinta-secundaria` / `claro-tinta-secundaria`): textos de apoyo (bienvenida, explicaciones), etiquetas de campo, íconos decorativos.
- **Línea de Cal** (`borde`): contorno de los campos, igual en los dos temas; pasa 3:1 sobre ambos fondos.
- **Tarjeta Roja** (`error`, `error-contenedor`, `sobre-error-contenedor` y sus `claro-*`): errores de campo y el aviso de error.

### Named Rules
**The Naranja Único Rule.** El naranja marca la acción principal de la pantalla y lo que está pasando ahora; nada más. Los botones secundarios van en tinta (el "¿Olvidaste tu contraseña?" es `tinta`, no naranja) y los íconos decorativos en tinta secundaria (el sobre de "Revisa tu correo"). Prueba: si hay dos cosas naranjas en pantalla y una no es la acción principal ni "lo de ahora", sobra una.

**The Roles, No Hex Rule.** Las pantallas leen `MaterialTheme.colorScheme`; los valores viven solo en `ui/theme`. En el build no hay un solo color literal fuera del tema. En la web, el equivalente es leer variables, nunca hex sueltos.

**The Sin Material You Rule.** El color dinámico está apagado: taparía la marca.

## Typography

**Display Font:** Barlow Condensed (SemiBold, Bold y ExtraBold Itálica empaquetadas, OFL)
**Body Font:** Barlow (Regular, Medium y SemiBold empaquetadas, OFL)

**Character:** una condensada atlética para lo que se grita (titulares, marcadores, el nombre) sobre una sans humanista abierta para lo que se lee y se toca. Las fuentes van dentro del APK; no dependen de Google Play.

### Hierarchy
La escala de tamaños e interlineados es la de Material 3, siempre en `sp` (respeta la letra del sistema; verificada al 130 %).
- **Logotipo** (ExtraBold Itálica, 46, interlineado 0,95, mayúsculas): solo el logotipo tipográfico provisional, "DESAFÍO" en tinta sobre "SPORTS" en naranja. TalkBack lo lee una vez como encabezado.
- **Display** (Barlow Condensed Bold, 57 / 45 / 36): mapeado en el tema para cifras de marcador; sin uso todavía.
- **Headline** (Barlow Condensed Bold, 32 / 28 / 24): titulares de pantalla y de sección, en mayúsculas ("REVISA TU CORREO", headlineMedium).
- **Title** (Barlow SemiBold, 22 / 16 / 14): el título de la barra superior (titleLarge) y títulos de bloque.
- **Body** (Barlow Regular, 16 / 14 / 12): texto corrido (bodyLarge para bienvenida y explicaciones, bodyMedium en el aviso de error), con espaciado reducido respecto a M3.
- **Label** (Barlow SemiBold 14 y 12, Medium 11): botones, botones de texto, etiquetas.

### Named Rules
**The Doble Barlow Rule.** Condensada para display, headline y el logotipo; Barlow para title, body y label. La ExtraBold Itálica es del logotipo y de nadie más.

**The Espaciado Ajustado Rule.** El espaciado de letras de M3 está pensado para Roboto; en Barlow se reduce sin llegar a cero (body 0,15 / 0,1 / 0,2; labelMedium y labelSmall 0,3). No se vuelve al valor de M3.

## Layout

Columna única centrada con ancho máximo de 480 dp (cabe en tabletas sin estirarse), márgenes laterales de 24 y separación vertical de 12 entre elementos del formulario. Respeta `safeDrawing` e `imePadding`: con el teclado abierto o la letra grande, todo se desplaza en vez de recortarse.

En el login, el logotipo queda arriba (32 de aire superior) y el formulario baja hacia el pulgar: un espacio con peso 1 encima del formulario y 0,35 debajo reparte lo que sobra. En pantallas secundarias (recuperar contraseña) el contenido arranca bajo la barra superior con 8 de margen vertical. Los estados de confirmación ("Revisa tu correo") se centran con 40 de margen vertical.

Ritmo observado: 4, 8, 12, 16, 24, 32, 40. Los objetivos táctiles miden al menos 48 × 48 dp; el botón principal, 52 de alto.

## Elevation & Depth

Plano. Ninguna superficie del build lleva sombra ni elevación tonal; la profundidad se expresa con los escalones de contenedor en azul marino (y en gris azulado en claro), y los contenedores con significado (el aviso de error) usan su propio rol de color.

### Named Rules
**The Plana Rule.** Nada proyecta sombra. Si algo tiene que separarse del fondo, sube un escalón de contenedor.

## Shapes

Esquinas suaves y redondeadas con una escala fija: 4 (sm), 8 (md), 12 (lg), 16 (xl) y 24 (2xl), mapeadas a `extraSmall`..`extraLarge` de M3. Uso registrado: campos de texto en 8, el aviso de error en 12 y el botón principal en 24 (con 52 de alto queda casi en píldora). Los íconos son Material Symbols Rounded, coherentes con las esquinas. Sin bordes salvo el contorno de los campos.

## Components

### Buttons
Firmes y anchos, hechos para el pulgar.
- **Shape:** casi píldora (`2xl`, 24) a todo el ancho, 52 de alto mínimo.
- **Primary (BotonPrincipal):** naranja con tinta `fondo` en oscuro, naranja claro con `blanco` en claro; texto en label (Barlow SemiBold 14), sin mayúsculas forzadas.
- **En proceso:** muestra un indicador circular de 18 (trazo 2) en el color del texto, 10 de separación y el texto en gerundio ("Entrando…"). No acepta toques pero **conserva el naranja**: un botón gris parece roto, no ocupado.
- **Texto (TextButton):** tinta principal, label, centrado; para acciones secundarias como "¿Olvidaste tu contraseña?". Nunca naranja.

### Inputs / Fields
- **Style:** `OutlinedTextField` de M3, contorno `borde`, radio `md` (8), fondo transparente sobre el fondo de pantalla, etiqueta flotante en tinta secundaria, texto en body.
- **Acciones finales:** botón de ícono (mostrar/ocultar contraseña) con su descripción para TalkBack.
- **Error:** el contorno y el texto de apoyo pasan al rol `error`, con el mensaje bajo el campo.
- **Disabled:** mientras el formulario trabaja, los campos se deshabilitan con el estado de M3.

### Aviso de error (signature)
- Contenedor `error-contenedor` con tinta `sobre-error-contenedor`, radio `lg` (12), relleno 12 × 14, ícono de error de 20 a 12 del texto (bodyMedium).
- Va justo encima del botón principal, con 12 de separación, y es región viva (TalkBack lo anuncia).
- **Motion:** entra con fundido + despliegue desde arriba y sale al revés, conservando el último texto mientras se va. Es el único movimiento de las pantallas de acceso; con "Quitar animaciones" del sistema queda instantáneo.

### Navigation
- **Barra superior (TopAppBar):** fondo igual al de la pantalla (no se separa), título en title (Barlow SemiBold 22), flecha atrás Rounded a la izquierda. El Atrás del sistema siempre funciona.
- **Pendiente:** barra de navegación inferior (Inicio · Explorar · Perfil) y FAB "Crear torneo": están en el contrato de dirección, no en el build.

### Logotipo (signature)
Tipográfico y provisional: "DESAFÍO" sobre "SPORTS", Barlow Condensed ExtraBold Itálica en mayúsculas, "SPORTS" en el color primario. Centrado en el arranque, alineado a la izquierda en el login. El logo definitivo lo reemplaza en un solo lugar.

### Pendiente (no existe todavía)
Tarjetas, chips de estado con color semántico, tablas de posiciones, cifras de marcador en display, barra inferior y FAB. Se registran cuando haya un build que los muestre.

## Do's and Don'ts

### Do:
- **Do** usar el naranja solo para la acción principal y para "lo de ahora" (Naranja Único).
- **Do** leer roles de `MaterialTheme.colorScheme` (en la web, variables), nunca hex.
- **Do** dar profundidad con los escalones de contenedor, no con sombras.
- **Do** usar Barlow Condensed para titulares y Barlow para todo lo que se lee y se toca, con la escala en `sp`.
- **Do** mantener el botón principal en naranja mientras trabaja, con indicador y texto en gerundio.
- **Do** verificar cada pantalla en oscuro, en claro y con la letra al 130 %.

### Don't:
- **Don't** usar el blanco sobre `#FF5500` para texto: en oscuro el botón lleva tinta `fondo`; en claro el naranja baja a `#C23C00`.
- **Don't** pintar en naranja botones secundarios, íconos decorativos o enlaces.
- **Don't** activar el color dinámico (Material You).
- **Don't** usar la ExtraBold Itálica fuera del logotipo.
- **Don't** devolver el espaciado de letras de M3 al texto en Barlow.
- **Don't** mostrar botones muertos para funciones que todavía no existen (Google, crear cuenta, explorar sin cuenta llegan con su spec).
