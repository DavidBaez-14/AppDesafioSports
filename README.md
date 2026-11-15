# Desafío Sports

App Android multideporte para los torneos universitarios de la UFPS. Empieza con fútbol sala.

- **Tecnología:** Kotlin + Jetpack Compose (Material 3).
- **De dónde saca todo:** inicia sesión con **Supabase Auth** y consume **BackendCodeCup**, el mismo backend de la web CodeCup.
- **Todos entran como jugador.** Árbitro y Creador son secciones que aparecen en Inicio si la cuenta las tiene. El visitante explora sin sesión.

> **Estado (14-nov-2026):** ya se puede entrar (correo o Google), ver Inicio con tus equipos y tu próximo partido, explorar torneos, ver tu perfil, crear un torneo, aprobar inscripciones y llevar la planilla de un partido como árbitro.
> El detalle de cada parte está en el [índice de specs](docs/specs/README.md).

## Requisitos

- **Android Studio** 2026.1 o posterior (el proyecto usa AGP 9.1).
- **JDK 21.** Si no está instalado, Gradle lo descarga.
- **Android SDK 36**, más un emulador o un teléfono con Android 8.0 (API 26) o superior.
  - El emulador necesita **varios GB libres** en disco.
- **BackendCodeCup** corriendo en el puerto **8082**. Se levanta desde el Spring Boot Dashboard de VS Code.

## Configurar

1. Clona el repo.
2. Crea `local.properties` en la raíz del proyecto con las claves que David te pasa por privado:
   - `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY`: son las mismas que usa la web (`VITE_SUPABASE_URL` y `VITE_SUPABASE_PUBLISHABLE_KEY`). Pídeselas a David por privado.
   - `API_BASE_URL`: opcional. Por defecto es `http://10.0.2.2:8082/`, el PC visto desde el emulador.
   - `GOOGLE_WEB_CLIENT_ID`: opcional. Sin él, el botón "Continuar con Google" no aparece. Ver [Continuar con Google](#continuar-con-google).
3. Abre la carpeta en Android Studio y deja que sincronice.

### Continuar con Google

El login con Google es nativo ([MOV-0006](docs/specs/MOV-0006-google.md)). Para que funcione en tu máquina hacen falta tres cosas:

1. **`GOOGLE_WEB_CLIENT_ID` en `local.properties`.** Es el ID del cliente de tipo **Aplicación web** de Google Cloud, el mismo que está en Supabase → Authentication → Providers → Google → Client IDs. **No** es el del cliente Android: con ese, el selector falla con "No pudimos continuar con Google".
2. **Un cliente Android con tu huella.** Google solo entrega el token a las apps firmadas con una llave registrada, y cada máquina tiene su propia llave de debug.
   - Saca tu SHA-1 con `gradlew signingReport` (variante `debug`).
   - En Google Cloud → Google Auth Platform → Clients → Create client → **Android**: paquete `co.edu.ufps.desafiosports` y tu SHA-1. Su ID no se copia en ningún lado.
3. **Un emulador con Google Play** (o un teléfono) y una cuenta de Google iniciada en el dispositivo. Mientras la app de Google esté en modo *Testing*, la cuenta debe estar en Audience → Test users.

> `local.properties` está en `.gitignore`. **Nunca subas claves.** La clave secreta de Supabase (`sb_secret_…`) no va en la app.

## Correr

- **Android Studio:** *Run ▶ app*.
- **Consola:** `gradlew installDebug` (en Windows, `gradlew.bat installDebug`).
- **Teléfono por USB:** corre `adb reverse tcp:8082 tcp:8082` y usa `API_BASE_URL=http://localhost:8082/`.
- **Usuarios de prueba:** los del seed del backend (`jugador01@prueba.local`, `admin01@prueba.local`…). La contraseña está en `SEED_PASSWORD` del `.env` del backend; pídesela a David.

## Probar

```bash
./gradlew testDebugUnitTest      # toda la suite; las 3 pruebas de humo se saltan solas
./gradlew assembleDebug          # compila el APK de debug

# Prueba de humo contra el backend real (con el backend levantado):
DESAFIO_HUMO=1 HUMO_CLAVE=<contraseña del seed> ./gradlew testDebugUnitTest --tests "*BackendRealHumoTest*" --rerun
```

## Cómo contribuir

Se trabaja con **specs (SDD)**: cada cambio nace de una spec en `docs/specs/` y llega con sus pruebas y su evidencia.

1. Elige una spec `aprobada` en el [índice](docs/specs/README.md) y pon tu nombre.
2. Crea la rama `mov-NNNN-<slug>`. Los commits van como `feat(alcance): resumen (MOV-NNNN)`.
3. Escribe los tests primero, implementa y verifica en el emulador.
4. Abre el PR con la plantilla.

Detalle en [`docs/sdd.md`](docs/sdd.md).

## Estructura

```
app/src/main/java/co/edu/ufps/desafiosports/
├── di/            inyección manual (AppContainer)
├── domain/        modelos, interfaces de repositorio, casos de uso (Kotlin puro)
├── data/          Supabase Auth, Retrofit, DTO, repositorios
├── presentacion/  pantallas (Screen + ViewModel + State) y navegación
└── ui/theme/      tema con los tokens del Design System
docs/              arquitectura, SDD, plan de UI, specs y evidencias
```

Documentación completa: [`docs/`](docs/README.md).
