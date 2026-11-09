---
id: MOV-0002
titulo: Tema de marca, arranque e inicio de sesión con correo
tipo: feature
estado: en-revision
prioridad: P1
fase: F0
modo: Transversal
responsable: David
depende_de: [MOV-0001]
rama: mov-0002-login
---

# MOV-0002 · Tema de marca, arranque e inicio de sesión con correo

## 1. Contexto

- El usuario revisó el prototipo navegable (`docs/prototipo-roles/index.html`) y pidió empezar por el **login, funcionando** (8-nov-2026). Las demás pantallas se revisan después.
- Hoy la app abre en el diagnóstico de MOV-0001, que tiene un formulario de prueba sin diseño. El inicio de sesión con Supabase y `GET /api/auth/me` ya funcionan.
- El login necesita el tema de marca para verse como en el prototipo:
  - el tema oscuro, que es el principal, lleva el azul marino y el naranja de la web;
  - el tema claro queda a criterio del diseño.

  Decisión de `PRODUCT.md`, P1 y P2 del plan de UI.

## 2. Alcance

- **Tema de marca** en Compose: esquemas claro y oscuro con roles de Material 3, y tipografía Barlow Condensed (títulos y logotipo) más Barlow (texto), empaquetadas en `res/font`.
- **Arranque (A-1):** lee la sesión guardada. Con sesión va al inicio provisional y sin sesión, al login. Mientras lee, muestra el logotipo.
- **Login (A-2), solo correo:**
  - logotipo, frase de bienvenida, correo, contraseña con mostrar u ocultar y el botón Entrar;
  - "¿Olvidaste tu contraseña?";
  - errores en el campo y errores generales;
  - comprobación de la cuenta con `GET /api/auth/me`.
- **Recuperar contraseña (N-3):** pide el correo, envía el enlace de Supabase y muestra "Revisa tu correo". La contraseña nueva se crea en la web, igual que hoy.
- **Inicio provisional:** después de entrar se abre el **diagnóstico** de MOV-0001, que muestra la cuenta y permite cerrar sesión. Inicio llega con su propia spec, después de revisar esas pantallas.
- **Cerrar sesión** desde cualquier pantalla vuelve al login.
- **Diagnóstico desde el login:** en el build de debug, una pulsación larga en el logotipo lo abre (P16 del plan, recomendación aplicada).

## 3. Diseño

- **Referencia:** pantalla "Entrar" del prototipo (`docs/prototipo-roles/index.html#entrar`), aprobada por el usuario. Impeccable, modo *Operate*: Material 3 manda en estructura e interacción y la marca entra por el tema.
- **Tokens:** los del prototipo.
  - Oscuro: fondo `#04070F`, superficies `#090E1C` → `#1E2D5A`, tinta `#EEF0F7` / `#A9B1CC` y naranja `#FF5500` con texto azul marino.
  - Claro: fondo `#F4F5F9`, tinta `#0D1429` y naranja `#C23C00` con texto blanco (AA: 5,3:1).
  - Bordes de campos: `#6878A8`, con 3:1 o más en los dos temas.
- **Logotipo provisional:** "DESAFÍO" sobre "SPORTS", en Barlow Condensed ExtraBold Itálica y mayúsculas. "SPORTS" va en el color primario.
- **Estados:**

| Pantalla | Cargando | Error | Sin conexión | Éxito |
|---|---|---|---|---|
| Arranque | Logotipo centrado | — | Con sesión guardada sin renovar: entra igual | Login o inicio provisional |
| Login | Botón con progreso, "Entrando…", controles deshabilitados | En el campo (validación) o en un aviso bajo los campos (credenciales, correo sin confirmar, cuenta bloqueada, sin registro) | Aviso con el texto de `AppError.SinConexion` | Navega al inicio provisional |
| Recuperar | Botón con progreso, "Enviando…" | En el campo o en un aviso | Aviso de sin conexión | "Revisa tu correo" con el correo y el botón "Volver a entrar" |

- **Textos:** en `strings.xml` (sección MOV-0002). Los mensajes de error salen de `AppError`.
- **Accesibilidad:**
  - objetivos de 48 dp o más;
  - etiquetas en los campos y en el ícono de mostrar contraseña;
  - el aviso de error se anuncia con TalkBack (región viva);
  - la pantalla se desplaza con la letra al 130 % y con el teclado abierto;
  - Atrás: en el login sale de la app, en Recuperar vuelve al login.

## 4. Contrato con el backend

| Servicio | Llamada | Envía | Responde | Errores → AppError |
|---|---|---|---|---|
| Supabase Auth | `signInWith(Email)` | correo, contraseña | sesión | `invalid_credentials` → `CredencialesInvalidas`; `email_not_confirmed` → `CorreoNoConfirmado`; sin red → `SinConexion` |
| BackendCodeCup | `GET /api/auth/me` | token | `MeResponseDto` | 404 `CUENTA_NO_REGISTRADA` → `CuentaNoRegistrada`; 403 `CUENTA_BLOQUEADA` → `CuentaBloqueada` |
| Supabase Auth | `resetPasswordForEmail` | correo | — | `over_email_send_rate_limit` / `over_request_rate_limit` → `Negocio` con mensaje en español; sin red → `SinConexion` |

Todo existe; no hay endpoints nuevos.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** que la app abre, **cuando** la sesión guardada se resuelve, **entonces**:
   - sin sesión va al login;
   - con sesión, o con una sesión que no se pudo renovar, va al inicio provisional;
   - con `local.properties` incompleto va al diagnóstico, que dice qué falta;
   - mientras se lee, muestra el logotipo.
2. **CA-2** · **Dado** un correo mal escrito o una contraseña de menos de 8 caracteres, **cuando** toca Entrar, **entonces** el error aparece bajo el campo que corresponde y no se llama a Supabase.
3. **CA-3** · **Dado** un correo y una contraseña que Supabase rechaza, **cuando** toca Entrar, **entonces** ve "Correo o contraseña incorrectos." y conserva el correo escrito.
4. **CA-4** · **Dado** credenciales válidas y una cuenta registrada, **cuando** toca Entrar, **entonces** entra al inicio provisional y la contraseña se borra del estado.
5. **CA-5** · **Dado** credenciales válidas pero una cuenta sin registro completo (404) o bloqueada (403), **cuando** toca Entrar, **entonces** se cierra la sesión, se queda en el login y ve el mensaje del backend.
6. **CA-6** · **Dado** que no hay conexión, **cuando** toca Entrar, **entonces** ve el aviso de sin conexión y puede volver a intentarlo.
7. **CA-7** · **Dado** que ya tocó Entrar, **mientras** espera la respuesta, **entonces** el botón muestra el progreso y un segundo toque no repite la llamada.
8. **CA-8** · **Dado** el correo sin confirmar, **cuando** toca Entrar, **entonces** ve el mensaje de confirmar el correo.
9. **CA-9** · **Dado** un correo válido en Recuperar contraseña, **cuando** toca "Enviar enlace", **entonces**:
   - se pide el correo de recuperación a Supabase y se muestra "Revisa tu correo";
   - con un correo mal escrito, el error va en el campo y no se llama;
   - si se superó el límite de envíos, el aviso está en español.
10. **CA-10** · **Dado** una sesión activa, **cuando** se cierra, **entonces** la app vuelve al login sin dejar pantallas atrás.
11. **CA-11** · **Dado** el tema de marca, **entonces**:
    - el login se ve como en el prototipo en claro y en oscuro, con Barlow empaquetada;
    - con la letra al 130 % no se corta nada.

    Verificación en el emulador.

## 6. Fuera de alcance / qué NO debe cambiar

- **No entran aquí** (cada uno con su spec, sin botones muertos en el login mientras tanto):
  - Crear cuenta: registro, "Revisa tu correo" y completar registro;
  - Continuar con Google: necesita el cliente OAuth Android, P14;
  - Explorar sin cuenta: necesita las lecturas públicas de M4.
- **Cambiar la contraseña** sigue en la web: el enlace del correo abre la web.
- **El diagnóstico no se borra:** deja de ser la primera pantalla y queda como inicio provisional y como herramienta de debug.
- **No se agregan librerías.** Las fuentes son archivos en `res/font` y los íconos, vectores de Material Symbols en `res/drawable`.

## 7. Plan de pruebas

| Criterio | Prueba | Nivel |
|---|---|---|
| CA-1, CA-10 | `RaizViewModelTest › CA-1 …`, `› CA-10 …` | ViewModel |
| CA-2 | `IniciarSesionUseCaseTest › CA-2 …`, `LoginViewModelTest › CA-2 …` | Dominio y ViewModel |
| CA-3 a CA-8 | `LoginViewModelTest › CA-n …` | ViewModel |
| CA-9 | `RecuperarClaveUseCaseTest › CA-9 …`, `RecuperarClaveViewModelTest › CA-9 …` | Dominio y ViewModel |
| CA-11 | Capturas del emulador | Manual |

Los tests de Compose UI necesitan dependencias de `androidTest`, que entran con su propia spec. Mientras tanto, CA-11 y la navegación se verifican en el emulador.

## 8. Verificación en el emulador

1. Abrir sin sesión → login. Entrar con `jugador01@prueba.local` → inicio provisional (diagnóstico) con la cuenta.
2. Cerrar sesión → login. Probar una contraseña mala, un correo mal escrito y el modo avión.
3. "¿Olvidaste tu contraseña?" → enviar → "Revisa tu correo".
4. Capturas en `docs/specs/evidencias/MOV-0002/`: login oscuro, login claro, letra al 130 %, error de credenciales y "Revisa tu correo".

**Resultado (9-nov-2026, Pixel_8 API 37):** pasos 1 a 3 hechos, con las capturas 02 a 11 y el resumen en `12-green.txt`.
- **Sin el backend en `:8082`:** CA-5 no se vio en el emulador; lo cubren los tests. El login entró igual, como define §11, y el diagnóstico mostró el servidor caído.
- **"Revisa tu correo":** no se envió, para no mandar correos a los dominios falsos del seed. Queda la prueba manual con una cuenta real.

## 9. Archivos afectados

- `app/src/main/res/font/` (nuevas: Barlow y Barlow Condensed, OFL) y `app/src/main/res/drawable/` (íconos de Material Symbols)
- `app/src/main/res/values/strings.xml`, `values/colors.xml`, `values-night/colors.xml`
- `app/src/main/java/co/edu/ufps/desafiosports/ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `Logotipo.kt` (nuevo)
- `app/src/main/java/co/edu/ufps/desafiosports/domain/error/AppError.kt` (campo en `Validacion`)
- `app/src/main/java/co/edu/ufps/desafiosports/domain/repository/SesionRepository.kt`, `data/repository/SesionRepositoryImpl.kt`, `data/auth/ErroresSupabase.kt`
- `app/src/main/java/co/edu/ufps/desafiosports/domain/use_case/identidad/IniciarSesionUseCase.kt`, `RecuperarClaveUseCase.kt` (nuevo)
- `app/src/main/java/co/edu/ufps/desafiosports/di/AppContainer.kt`
- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/navigation/Rutas.kt`, `AppNavigation.kt`, `RaizViewModel.kt` (nuevo)
- `app/src/main/java/co/edu/ufps/desafiosports/presentacion/acceso/` (nuevo: arranque, login y recuperar)
- `app/src/test/java/co/edu/ufps/desafiosports/...` (tests del plan y `Fakes.kt`)
- `docs/licencias/OFL-Barlow.txt` (licencia de las fuentes)
- `docs/specs/MOV-0002-login.md`, `docs/specs/README.md`, `docs/specs/evidencias/MOV-0002/`

## 10. Datos de prueba

- Usuarios del seed `@prueba.local` (contraseña en `SEED_PASSWORD` del backend).
- Casos extremos: correo de 60 caracteres, letra al 130 % con el teclado abierto, modo avión y una cuenta bloqueada.

## 11. Notas y decisiones

- **Inicio provisional = diagnóstico.** Es temporal y está a la vista: así el login queda probado de punta a punta sin inventar Inicio antes de revisarlo.
- **Errores de `/me` que no bloquean:** si Supabase acepta pero el backend no responde, se entra igual. El diagnóstico muestra el estado del servidor. Solo cuenta sin registro y cuenta bloqueada cierran la sesión.
- **Tipografía (P4) e íconos (P5):** se aplica la recomendación del plan (Barlow Condensed + Barlow y Material Symbols), que es lo que usa el prototipo aprobado.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-08 | Borrador inicial y paso a en-curso por pedido del usuario ("empieza con el login funcional") |
| 2026-11-09 | RED (26 tests nuevos fallando, `evidencias/MOV-0002/01-red.txt`) → GREEN (62 tests, 0 fallos). CA-1 suma el caso de configuración incompleta. Verificación en el emulador. Ajuste: espaciado de letras de Barlow en los textos de cuerpo y en las etiquetas pequeñas |
| 2026-11-09 | MOV-0003/CA-7 reemplaza el caso de 404 `CUENTA_NO_REGISTRADA` de CA-5: ya no cierra la sesión, va a completar registro. La cuenta bloqueada sigue igual |
| 2026-11-09 | Revisión final de Impeccable (disposición *fix*). Aplicado: el formulario baja hacia el pulgar cuando sobra pantalla, el ícono de "Revisa tu correo" sin naranja y el aviso de error con entrada y salida animadas. Declinado: el título de la barra en Barlow Condensed, porque el prototipo usa Barlow en las barras. Comprobado sin cambios: la semilla del sorteo y el correo conservado al volver de Recuperar |
