---
id: MOV-0003
titulo: Crear cuenta, revisa tu correo y completar registro
tipo: feature
estado: en-revision
prioridad: P1
fase: F0
modo: Transversal
responsable: David
depende_de: [MOV-0002]
rama: mov-0003-crear-cuenta
---

# MOV-0003 · Crear cuenta, revisa tu correo y completar registro

## 1. Contexto

- El usuario probó el login de MOV-0002 y pidió seguir con el resto de la pantalla de entrada: crear cuenta, Google y explorar sin cuenta (9-nov-2026).
- Esta spec cubre **crear cuenta** de punta a punta. Google va en MOV-0006 y Explorar sin cuenta necesita las lecturas públicas del backend (M4).
- **Cómo se confirma el correo hoy:** el enlace del correo de Supabase abre la web (`/auth/callback`, con la Site URL configurada en Supabase). La web confirma la cuenta. Después, la persona entra en la app con su correo y la app le pide completar el registro.
- **Sin solicitud de rol** (decisión del 8-nov, `PRODUCT.md`): al registrarse se queda como jugador. Los demás roles salen de lo que haga en los torneos.

## 2. Alcance

- **Login:** botón "Crear cuenta" bajo "Entrar".
- **Crear cuenta (A-3):** correo y contraseña → `signUp` de Supabase.
- **Revisa tu correo (N-1):** con el correo escrito, "Reenviar correo" y "Ir a entrar".
- **Completar registro (N-2):** nombres, apellidos y documento opcional (tipo y número juntos) → `POST /api/auth/registro`. Después, el inicio provisional (diagnóstico).
- **El login cambia en un caso:** una cuenta confirmada pero sin registro completo (404 `CUENTA_NO_REGISTRADA`) ya no cierra la sesión. Ahora va a completar registro y reemplaza ese caso de MOV-0002/CA-5. La cuenta bloqueada sigue igual.

## 3. Diseño

- **Referencia:** el mismo mundo de MOV-0002 (`DESIGN.md`): formularios con campos M3 contorneados, el botón naranja único por pantalla y el aviso de error animado.
- **Crear cuenta:** barra con Atrás y el título "Crear cuenta". Una frase que explica el correo de confirmación, los campos Correo y Contraseña (mínimo 8, con mostrar u ocultar) y el botón "Crear cuenta".
- **Revisa tu correo:** el mismo patrón que "Revisa tu correo" de Recuperar: ícono sin naranja, titular en mayúsculas, el texto con el correo, "Ir a entrar" en naranja y "Reenviar correo" como botón de texto.
- **Completar registro:** barra con "Salir" (cierra la sesión) y el título "Completa tu registro". Nombres, Apellidos y, bajo "Documento (opcional)", el selector de tipo más el número, con una línea que explica para qué sirve. El botón es "Terminar registro".
- **En el login:** "Crear cuenta" es un botón contorneado de ancho completo bajo "Entrar". Así se ve como la puerta de quien llega por primera vez, sin competir con el naranja.
- **Estados:** cargando (el botón con progreso), error en el campo, aviso general, sin conexión y éxito (navega).
- **Accesibilidad:** 48 dp, etiquetas, el aviso con región viva, el selector de tipo accesible (menú desplegable de M3), letra al 130 % con desplazamiento y Atrás del sistema.

## 4. Contrato con el backend

| Servicio | Llamada | Envía | Responde | Errores → AppError |
|---|---|---|---|---|
| Supabase Auth | `signUpWith(Email)` | correo, contraseña | usuario; sesión solo si no se exige confirmar el correo | `user_already_exists`/`email_exists` → `Negocio` (en español); `weak_password` → `Validacion(CLAVE)`; `signup_disabled` → `Negocio`; límite de envíos → `Negocio` |
| Supabase Auth | `resendEmail(SIGNUP)` | correo | — | límite de envíos → `Negocio` |
| BackendCodeCup | `POST /api/auth/registro` | `{nombres, apellidos, tipoDocumento?, numeroDocumento?}` | 201 `MeResponseDto` | 409 `DOCUMENTO_REGISTRADO` → `Negocio` con el mensaje del backend; 409 `CUENTA_YA_REGISTRADA` → se toma como hecho |

Todo existe.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** el login, **cuando** toca "Crear cuenta", **entonces** abre Crear cuenta. Atrás vuelve al login.
2. **CA-2** · **Dado** un correo mal escrito o una contraseña de menos de 8 caracteres, **cuando** toca "Crear cuenta", **entonces** el error va bajo su campo y no se llama a Supabase.
3. **CA-3** · **Dado** que Supabase exige confirmar el correo, **cuando** se crea la cuenta, **entonces** se muestra "Revisa tu correo" con el correo escrito.
4. **CA-4** · **Dado** que Supabase deja la sesión abierta (sin confirmación), **cuando** se crea la cuenta, **entonces** va directo a completar registro.
5. **CA-5** · **Dado** un correo ya registrado, una contraseña débil, los registros deshabilitados o el límite de envíos, **entonces** se ve un aviso en español y se queda en Crear cuenta. La contraseña débil va bajo su campo.
6. **CA-6** · **Dado** "Revisa tu correo", **cuando** toca "Reenviar correo", **entonces** se pide otro envío y se confirma en pantalla. Si se superó el límite, se ve el aviso.
7. **CA-7** · **Dado** una cuenta confirmada sin registro completo (404 `CUENTA_NO_REGISTRADA`), **cuando** entra por el login, **entonces** va a completar registro y la sesión sigue abierta.
8. **CA-8** · **Dado** nombres o apellidos vacíos, o un documento a medias (tipo sin número o número sin tipo) o con caracteres no válidos, **cuando** toca "Terminar registro", **entonces** el error va bajo su campo y no se llama al backend.
9. **CA-9** · **Dado** datos válidos, **cuando** toca "Terminar registro", **entonces** se envía `POST /api/auth/registro` sin espacios sobrantes y la app va al inicio provisional. Con `CUENTA_YA_REGISTRADA` también va al inicio.
10. **CA-10** · **Dado** un documento que ya tiene cuenta (409 `DOCUMENTO_REGISTRADO`), **entonces** se ve el mensaje del backend y se queda en completar registro.
11. **CA-11** · **Dado** completar registro, **cuando** toca "Salir", **entonces** se cierra la sesión y vuelve al login.
12. **CA-12** · Las tres pantallas se ven bien en claro, en oscuro y con la letra al 130 %. Verificación en el emulador.

## 6. Fuera de alcance / qué NO debe cambiar

- **No entran:**
  - Google (MOV-0006);
  - Explorar sin cuenta (necesita backend M4);
  - que el enlace del correo abra la app (hoy abre la web);
  - pedir rol al registrarse.
- **Se conserva:** el login, la recuperación de contraseña y el resto de MOV-0002. El único cambio de MOV-0002 es CA-7.
- **El backend no cambia.**

## 7. Plan de pruebas

| Criterio | Prueba | Nivel |
|---|---|---|
| CA-2 | `RegistrarseUseCaseTest`, `RegistroViewModelTest` | Dominio y ViewModel |
| CA-3, CA-4, CA-5 | `RegistroViewModelTest` | ViewModel |
| CA-6 | `RevisaTuCorreoViewModelTest` | ViewModel |
| CA-7 | `LoginViewModelTest › MOV-0003 CA-7 …` | ViewModel |
| CA-8, CA-9 | `CompletarRegistroUseCaseTest`, `CompletarRegistroViewModelTest`, `CuentaRepositoryImplTest › MOV-0003 CA-9 …` | Dominio, ViewModel y red (MockWebServer) |
| CA-10, CA-11 | `CompletarRegistroViewModelTest`, `CuentaRepositoryImplTest › MOV-0003 CA-10 …` | ViewModel y red |
| CA-1, CA-12 | Capturas del emulador | Manual |

## 8. Verificación en el emulador

1. Login → "Crear cuenta" → validaciones → Atrás.
2. Capturas en `docs/specs/evidencias/MOV-0003/`: Crear cuenta (oscuro, claro y 130 %), error de validación y Completar registro.
3. **El registro real lo hace el usuario con un correo propio.** No se registran cuentas con dominios falsos, porque los rebotes gastan el cupo de Brevo. El correo de confirmación se abre con la web levantada (`npm run dev`).

## 9. Archivos afectados

- `domain`:
  - `error/CampoValidado.kt` (NOMBRES, APELLIDOS, DOCUMENTO);
  - `model/DatosRegistro.kt` y `model/ResultadoRegistro.kt` (nuevos);
  - `repository/SesionRepository.kt` y `repository/CuentaRepository.kt`;
  - `use_case/identidad/RegistrarseUseCase.kt`, `ReenviarConfirmacionUseCase.kt` y `CompletarRegistroUseCase.kt` (nuevos).
- `data`:
  - `remote/api/AuthApi.kt` y `remote/dto/RegistroRequestDto.kt` (nuevo);
  - `repository/SesionRepositoryImpl.kt` y `repository/CuentaRepositoryImpl.kt`;
  - `auth/ErroresSupabase.kt`.
- `di/AppContainer.kt`.
- `presentacion`:
  - `navigation/Rutas.kt` y `navigation/AppNavigation.kt`;
  - `acceso/` (login, registro, revisa tu correo y completar registro).
- `res/values/strings.xml`.
- Tests del plan y `testutil/Fakes.kt`.
- `docs/specs/MOV-0003-crear-cuenta.md`, `docs/specs/README.md`, `docs/specs/evidencias/MOV-0003/` y el registro de cambios de `MOV-0002-login.md`.

## 10. Datos de prueba

- **Registro real:** un correo del usuario que no tenga cuenta.
- **Casos extremos:** nombres de 100 caracteres, documento con puntos (`1.090.123.456`), número sin tipo, letra al 130 % con el teclado abierto y el selector de tipo abierto.

## 11. Notas y decisiones

- **"Crear cuenta" contorneado y no como enlace.** Para quien llega por primera vez es la acción principal, pero el naranja sigue siendo solo de "Entrar".
- **Sin campo "Repetir contraseña":** basta con mostrar u ocultar la contraseña, como en el login.
- **Supabase no dice si un correo ya existe** cuando se exige confirmación: responde igual que con uno nuevo. Por eso "Revisa tu correo" no promete que la cuenta sea nueva.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador y paso a en-curso por pedido del usuario ("pasemos con login con Google, crear cuenta y todo eso") |
| 2026-11-09 | RED (29 tests nuevos fallando, `evidencias/MOV-0003/01-red.txt`) → GREEN (95 tests, 0 fallos). Capturas 02 a 08. Falta la prueba manual del registro real con un correo del usuario (`09-green.txt`) |
| 2026-11-09 | Integrada a `main`. Sigue `en-revision` hasta probar el registro real con un correo propio |
