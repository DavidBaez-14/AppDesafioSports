---
id: MOV-0006
titulo: Continuar con Google (Credential Manager → Supabase)
tipo: feature
estado: en-revision
prioridad: P1
fase: F0
modo: Transversal
responsable: David
depende_de: [MOV-0003, cliente OAuth Android en Google Cloud]
rama: mov-0006-google
---

# MOV-0006 · Continuar con Google

## 1. Contexto

- El usuario pidió seguir con "login con Google, crear cuenta y todo eso" (9-nov-2026). Crear cuenta quedó en MOV-0003.
- **El login con Google es nativo:**
  - Credential Manager muestra la cuenta de Google del teléfono y devuelve un *ID token*;
  - la app lo cambia por una sesión de Supabase con `signInWith(IDToken)`;
  - no se abre el navegador.
- **Necesita un cliente OAuth Android en Google Cloud** (paquete `co.edu.ufps.desafiosports` y la huella SHA-1 de la llave de debug), más el ID del cliente **web** que ya usa Supabase (plan de UI, Anexo A). Mientras falte ese ID en `local.properties`, el botón no aparece.

## 2. Alcance

- Botón "Continuar con Google" en el login, bajo "Entrar" y separado por una "o". Lleva el logotipo de Google, como piden sus guías de marca.
- **Flujo:**
  1. Credential Manager (`GetSignInWithGoogleOption`) con un *nonce*.
  2. `signInWith(IDToken)` en Supabase, con el *nonce* sin hash.
  3. `GET /api/auth/me`, igual que el login con correo:
     - cuenta registrada → inicio;
     - sin registro → completar registro (MOV-0003);
     - bloqueada → aviso y cierre de sesión.
- **Errores:** cancelar no muestra nada; sin cuentas de Google en el teléfono o con un fallo de configuración, se ve un aviso en español.
- `GOOGLE_WEB_CLIENT_ID` en `local.properties` y en `local.properties.ejemplo`.

## 3. Diseño

- **"Continuar con Google"** es un botón contorneado de ancho completo, con la "G" de Google a color a la izquierda. Va entre "Entrar" y "Crear cuenta", separado por una línea con "o", como en el prototipo.
- **Mientras se elige la cuenta,** el login queda en proceso: los campos y los botones se deshabilitan para evitar el doble toque.
- **Sin `GOOGLE_WEB_CLIENT_ID`:** no aparecen ni el botón ni la "o", y el login queda igual que en MOV-0003.

## 4. Contrato

| Servicio | Llamada | Envía | Responde | Errores |
|---|---|---|---|---|
| Credential Manager | `getCredential(GetSignInWithGoogleOption)` | ID del cliente web y hash SHA-256 del *nonce* | `GoogleIdTokenCredential` (idToken) | cancelado → nada; sin credenciales → aviso; otros → aviso genérico |
| Supabase Auth | `signInWith(IDToken)` | idToken, proveedor Google y *nonce* sin hash | sesión | `AppError` como el login con correo |
| BackendCodeCup | `GET /api/auth/me` | token | `MeResponseDto` | igual que en MOV-0002 y MOV-0003 |

## 5. Criterios de aceptación

1. **CA-1** · **Dado** que `GOOGLE_WEB_CLIENT_ID` está vacío, **entonces** el login no muestra el botón de Google ni la "o".
2. **CA-2** · **Dado** el botón, **cuando** se toca, **entonces** el login queda en proceso y un segundo toque no abre otro selector.
3. **CA-3** · **Dado** que la persona cancela el selector, **entonces** el login vuelve a estar disponible sin aviso de error.
4. **CA-4** · **Dado** un error de Credential Manager (sin cuentas o mala configuración), **entonces** se ve un aviso en español.
5. **CA-5** · **Dado** un idToken válido y una cuenta registrada, **entonces** se inicia sesión en Supabase con ese token y el *nonce* sin hash, y la app entra al inicio.
6. **CA-6** · **Dado** un idToken válido de una cuenta sin registro, **entonces** va a completar registro; si la cuenta está bloqueada, se ve el aviso y se cierra la sesión.
7. **CA-7** · **Dado** que Supabase rechaza el token, **entonces** se ve el aviso y el login vuelve a estar disponible.
8. **CA-8** · El *nonce* es aleatorio en cada intento, y a Google va su SHA-256 en hexadecimal.

## 6. Fuera de alcance / qué NO debe cambiar

- **No entran:**
  - Google en la web (sigue con OAuth por redirección);
  - el cliente de *release* y Play App Signing (otra huella SHA-1);
  - traer nombres y apellidos desde Google para completar registro.
- **No cambian:** el login con correo, crear cuenta ni recuperar contraseña.
- **Librerías nuevas, solo las de esta spec:** `androidx.credentials:credentials` y `credentials-play-services-auth` 1.5.0, y `com.google.android.libraries.identity.googleid:googleid` 1.1.1.

## 7. Plan de pruebas

| Criterio | Prueba | Nivel |
|---|---|---|
| CA-2 a CA-7 | `LoginViewModelTest › MOV-0006 CA-n …` | ViewModel |
| CA-5 | `IniciarSesionConGoogleUseCaseTest` | Dominio |
| CA-8 | `NonceTest` | Dominio |
| CA-1 | Captura del emulador sin `GOOGLE_WEB_CLIENT_ID` | Manual |
| Flujo real con una cuenta de Google | Manual, cuando exista el cliente OAuth Android | Manual |

## 8. Verificación

1. Sin `GOOGLE_WEB_CLIENT_ID`: el login se ve como en MOV-0003 (CA-1).
2. **Cuando el usuario cree el cliente OAuth Android** y ponga el ID en `local.properties`: "Continuar con Google" → elegir cuenta → inicio o completar registro. El emulador necesita una imagen con Google Play y una cuenta de prueba de la pantalla de consentimiento.

## 9. Archivos afectados

- `gradle/libs.versions.toml` y `app/build.gradle.kts` (dependencias y `BuildConfig.GOOGLE_WEB_CLIENT_ID`)
- `local.properties.ejemplo`
- `domain/model/Configuracion.kt`, `domain/model/Nonce.kt` (nuevo), `domain/repository/SesionRepository.kt` y `domain/use_case/identidad/IniciarSesionConGoogleUseCase.kt` (nuevo)
- `data/repository/SesionRepositoryImpl.kt`
- `di/AppContainer.kt`
- `presentacion/acceso/` (LoginScreen, LoginViewModel, LoginState, ResultadoGoogle.kt y CuentaGoogle.kt, nuevos)
- `res/drawable/ic_google.xml` (nuevo) y `res/values/strings.xml`
- Tests del plan y `testutil/Fakes.kt`
- `docs/specs/MOV-0006-google.md` y `docs/specs/README.md`

## 10. Datos de prueba

- **Huella SHA-1 de debug de la máquina del usuario:** `99:BE:19:B9:93:05:0C:92:23:E1:D8:2C:7B:DE:E8:1B:7C:87:E8:16`.
- **Cuenta:** una cuenta de Google de prueba de la pantalla de consentimiento.

## 11. Notas y decisiones

- **Por qué `GetSignInWithGoogleOption` y no `GetGoogleIdOption`:** es el flujo para un botón explícito, que siempre deja elegir la cuenta. El otro es para la hoja automática al abrir la app.
- **Sin el ID del cliente web, el botón no se muestra.** Es configuración del entorno, igual que las claves de Supabase.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador y paso a en-curso por pedido del usuario |
| 2026-11-09 | RED (10 tests fallando, `evidencias/MOV-0006/01-red.txt`) → GREEN (106 tests, 0 fallos). Capturas 02 a 05: el selector de Google abre y cancelar libera el login. Falta el cliente OAuth Android para entrar de verdad (`06-green.txt`) |
