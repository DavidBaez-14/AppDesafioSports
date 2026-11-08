# Evidencia · MOV-0001

> Fecha: 8-nov-2026.
> - **Emulador:** AVD `Pixel_8` (Android 17, API 37), con GPU por software.
> - **Backend:** BackendCodeCup local en `:8082`, levantado con `--spring.flyway.enabled=false` (solo lectura).
> - **Usuario:** `jugador01@prueba.local`, del seed del backend.

## Capturas

| Archivo | Qué muestra | Criterios |
|---|---|---|
| `01-inicio-sin-sesion.png` | Servidor "En línea (UP)" vía `http://10.0.2.2:8082/` (HTTP en debug) y sesión "Sin sesión" | CA-4, CA-13 |
| `02-sesion-y-cuenta.png` | "Sesión activa: jugador01@prueba.local" y la tarjeta de `GET /api/auth/me`: Juan Castro · ACTIVA · JUGADOR · sin roles pendientes | CA-6, CA-7 |
| `03-girado-conserva-estado.png` | En horizontal, la tarjeta de la cuenta sigue en pantalla. La cuenta solo vive en el estado del ViewModel, que no la vuelve a pedir al crearse | CA-12 |
| `04-tema-oscuro.png` | Tema oscuro provisional con los tokens del Design System (azul marino y naranja, texto oscuro sobre el naranja) | — |

## Tests (JVM)

`./gradlew testDebugUnitTest`, sin variables de humo:

| Clase | Tests | Fallos |
|---|---|---|
| `ErroresHttpTest` | 5 | 0 |
| `RedSesionTest` | 8 | 0 |
| `CuentaRepositoryImplTest` | 3 | 0 |
| `IniciarSesionUseCaseTest` | 5 | 0 |
| `DiagnosticoViewModelTest` | 7 | 0 |
| `BackendRealHumoTest` | 3 (saltados) | 0 |
| **Total** | **31 (28 pasan · 3 saltados)** | **0** |

**Mutación (RED real):** se desactivó a propósito el control `priorResponse` de `TokenAuthenticator`. Resultado: `8 tests completed, 1 failed`, y el que falló fue `CA-8 si el reintento vuelve a dar 401 no hay bucle y se cierra la sesion`. Después se revirtió y la suite volvió a verde.

## Prueba de humo contra el backend y el Supabase reales

`DESAFIO_HUMO=1 HUMO_CLAVE=*** ./gradlew testDebugUnitTest --tests "*BackendRealHumoTest*" --rerun` → 3 tests, 0 saltados, 0 fallos:

```
[humo] http://localhost:8082/ actuator/health -> UP
[humo] GET api/auth/me sin token -> NoAutenticado: Inicia sesión para continuar.
[humo] GET api/auth/me -> Juan Castro <jugador01@prueba.local> estado=ACTIVA roles=[JUGADOR]
```
