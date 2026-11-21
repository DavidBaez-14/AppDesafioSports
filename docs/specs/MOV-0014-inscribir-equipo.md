---
id: MOV-0014
titulo: Inscribir un equipo en un torneo
tipo: feature
estado: en-revision
prioridad: P1
fase: F1
modo: Jugador (delegado)
responsable: David
depende_de: [MOV-0004, MOV-0005, backend 0026, backend 0029]
rama: mov-0014-inscribir-equipo
---

# MOV-0014 · Inscribir un equipo en un torneo

> **Para quien la implementa:** el backend que usa **ya existe** en `main` de BackendCodeCup (specs 0026 y 0029). Lee antes [`../sdd.md`](../sdd.md) y [`../arquitectura.md`](../arquitectura.md).

## 1. Contexto

- **Hoy un equipo no se puede inscribir desde la app.** La franja del detalle del torneo decía "Inscribir tu equipo desde la app llega en la próxima versión". Los torneos creados en la app (MOV-0007) solo reciben equipos por la web o por Swagger.
- **Esta spec cierra el ciclo dentro de la app:** crear torneo → **inscribir equipos** → aprobarlos (MOV-0008) → generar partidos (MOV-0011) → programarlos (MOV-0013).
- **Pedido de David (21-nov-2026):** "la primera", de las opciones propuestas, con el alcance acordado: solo deportes por equipos y sin comprobantes.

## 2. Alcance

### 2.1 El botón en el detalle del torneo

- Con sesión, inscripciones abiertas y un deporte por equipos, la franja "¿Juegas este torneo?" suma el botón **"Inscribir mi equipo"** (naranja: es la acción principal de esa pantalla para quien no participa).
- El texto de la franja deja de prometer una versión futura: "Si diriges un equipo, inscríbelo aquí. Si juegas en uno que ya se inscribió, pídele a su delegado que te agregue."
- Las demás franjas (organizas, arbitras, inscribiste, juegas, cerradas, individual) no cambian.

### 2.2 La pantalla "Inscribir equipo"

- **Arriba:** quien inscribe queda como delegado del equipo en este torneo y entra a su plantel.
- **Condiciones, si aplican:** la cuota por equipo ("Se la pagas al organizador; tu inscripción queda pendiente hasta que él la apruebe") y si el torneo exige documento.
- **¿Qué equipo?** Sus equipos base, en el orden del backend (el más reciente primero), y la opción **"Crear un equipo nuevo"**, que muestra el campo del nombre.
  - Con un solo equipo, queda elegido.
  - Con varios, no se elige ninguno hasta que la persona toca uno.
  - Sin equipos, la pantalla arranca en "Crear un equipo nuevo", sin opciones.
- **"Inscribir equipo"** (naranja). Al terminar, la pantalla muestra **"*Equipo* quedó inscrito"** con lo que sigue (pagar la cuota, o esperar la aprobación) y **"Listo"**, que vuelve al torneo. El torneo se vuelve a pedir solo y su franja pasa a "Inscribiste a *Equipo*" con el chip de su estado. Inicio lo muestra en "Mis equipos" con su chip de trámite (MOV-0004).
- **Torneo por enlace:** si el torneo se abrió con su enlace (`NO_LISTADO`), el token viaja con la inscripción.

## 3. Diseño

- **Componentes:** `TituloConTorneo`, `EncabezadoBloque`, `colorDePanel` para las condiciones, `BotonPrimario`, `EstadoVacio` para la confirmación, `Cargando` y `EstadoError`. Las opciones son filas con `RadioButton` dentro de un `selectableGroup`, y la fila entera es el control.
- **Los errores:** los del nombre (vacío, largo o repetido) van bajo el campo; los demás (ya inscrito, sin cupos, inscripciones cerradas, documento) van sobre el botón, con `liveRegion` para TalkBack.

### Estados

| Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|
| Indicador | Sin equipos: el campo del nombre | Mensaje + "Reintentar" | Texto de sin conexión + "Reintentar" | Confirmación con "Listo" |

### Textos (todos en `strings.xml`)

| Dónde | Texto |
|---|---|
| Franja | "Inscribir mi equipo" · el texto nuevo de §2.1 (con y sin cuota) |
| Pantalla | "Inscribir equipo" · "Quedas como delegado del equipo en este torneo y entras a su plantel. Después agregas a tus jugadores." · "¿Qué equipo?" · "Crear un equipo nuevo" · "Nombre del equipo" · "Así lo verán los demás equipos y el organizador." |
| Condiciones | "Cuota de inscripción: *$50.000* por equipo" · "Se la pagas al organizador. Tu inscripción queda pendiente hasta que él la apruebe." · "Este torneo exige documento: el tuyo debe estar en tu perfil, y el de cada jugador que agregues." |
| Botón | "Inscribir equipo" · "Inscribiendo…" |
| Validación | "Escribe el nombre del equipo." · "El nombre puede tener hasta 150 caracteres." · "Ya tienes un equipo llamado "*X*". Elígelo en la lista." |
| Confirmación | "*Equipo* quedó inscrito" · con cuota: "Paga la cuota de *$X* al organizador. Cuando la confirme, tu inscripción queda aprobada. Mientras tanto, la ves en Inicio, en Mis equipos." · sin cuota: "El organizador revisa tu inscripción y la aprueba. Mientras tanto, la ves en Inicio, en Mis equipos." |

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde | Errores esperados |
|---|---|---|---|---|
| `/api/supercopa/delegado/equipos` | GET | — | `[{ id, nombre }]`, el más reciente primero | — |
| `/api/supercopa/delegado/torneos/{torneoId}/inscribir` | POST | `{ equipoId }` o `{ nombreEquipo }`, y `enlaceToken` si aplica | `InscripcionDTO` (se usan `id`, `equipoNombre` y `estadoInscripcion`) | 409 `YA_INSCRITO` · `EQUIPO_DUPLICADO` · `CUPOS_AGOTADOS` · `INSCRIPCIONES_CERRADAS` · `TORNEO_INDIVIDUAL` · `YA_EN_UN_EQUIPO_DEL_TORNEO` · `DOCUMENTO_REQUERIDO` · 400 `NOMBRE_INVALIDO` · 403 `EQUIPO_AJENO` |

- **Basta con estar registrado:** no hace falta un rol global de delegado (backend 0026).
- **`nombreEquipo` crea el equipo y lo inscribe en el mismo paso.** Tiene prioridad sobre `equipoId`, así que la app manda solo uno de los dos.
- **La inscripción nace `PENDIENTE_PAGO`,** con quien inscribe como delegado y dentro del plantel. El organizador la aprueba en MOV-0008.
- **La cuota y "exige documento"** salen del detalle del torneo que ya se tiene (MOV-0005) y viajan en la ruta. No se pide `inscripcion-info`, que hoy solo sirve para los comprobantes.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** un torneo con inscripciones abiertas de un deporte por equipos, **cuando** lo abro con sesión y no participo en él, **entonces** la franja muestra "Inscribir mi equipo", que abre la pantalla de inscripción con el nombre del torneo, la cuota y si exige documento.
2. **CA-2** · **Dado** que abro la pantalla, **entonces** veo mis equipos en el orden del backend: con uno solo queda elegido, con varios no se elige ninguno, y sin equipos arranca en "Crear un equipo nuevo". Si no cargan, se ofrece "Reintentar".
3. **CA-3** · **Dado** un equipo elegido, **cuando** toco "Inscribir equipo", **entonces** se envía el `POST` solo con su `equipoId` (y el `enlaceToken` si el torneo se abrió por enlace) y veo la confirmación.
4. **CA-4** · **Dado** "Crear un equipo nuevo" con un nombre, **cuando** inscribo, **entonces** viaja `nombreEquipo` sin espacios de sobra y la confirmación muestra ese nombre.
5. **CA-5** · **Dado** un nombre vacío, de más de 150 caracteres o igual al de uno de mis equipos (sin importar mayúsculas), **entonces** no viaja y el motivo sale bajo el campo. El `EQUIPO_DUPLICADO` y el `NOMBRE_INVALIDO` del backend también salen ahí. Escribir otra vez borra el error.
6. **CA-6** · **Dado** cualquier otro rechazo del backend, **entonces** su mensaje sale sobre el botón y se puede volver a intentar. El doble toque no inscribe dos veces, y mientras viaja no se cambia la elección.
7. **CA-7** · **Dado** "Listo", **entonces** vuelvo al torneo, que muestra "Inscribiste a *Equipo*" con su estado. Inicio lo muestra en "Mis equipos".
8. **CA-8** · La pantalla y la franja se ven bien en claro, en oscuro y con la letra al 130 % (emulador).

## 6. Fuera de alcance / qué NO debe cambiar

- **Comprobante de pago de la cuota** (`upload-url` y Supabase Storage): spec propia. La inscripción queda pendiente y el organizador la aprueba al recibir el pago.
- **Inscripción individual:** el backend responde `TORNEO_INDIVIDUAL`. La franja sigue diciendo que llega en una próxima versión.
- **El plantel** (agregar jugadores, camisetas): es la siguiente spec, "Delegado: plantel…".
- **Unirse a un equipo ya inscrito:** lo hace su delegado agregando a la persona.
- **No cambian** las franjas de quien ya participa, "Mis equipos" de Inicio ni las inscripciones del organizador.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) | Nivel |
|---|---|---|
| CA-1 | Emulador (la franja y la apertura de la pantalla) | Emulador |
| CA-2 | `DelegadoRepositoryImplTest › CA-2 …`, `InscribirEquipoViewModelTest › CA-2 …` (4) | Red + ViewModel |
| CA-3 | `InscribirEquipoTest › CA-3 …`, `DelegadoRepositoryImplTest › CA-3 …`, `InscribirEquipoViewModelTest › CA-3 …` | Dominio + Red + ViewModel |
| CA-4 | `InscribirEquipoTest › CA-4 …`, `DelegadoRepositoryImplTest › CA-4 …`, `InscribirEquipoViewModelTest › CA-4 …` | Dominio + Red + ViewModel |
| CA-5 | `InscribirEquipoTest › CA-5 …` (2), `InscribirEquipoViewModelTest › CA-5 …` (2) | Dominio + ViewModel |
| CA-6 | `DelegadoRepositoryImplTest › CA-6 …`, `InscribirEquipoViewModelTest › CA-6 …` (2) | Red + ViewModel |
| CA-7 | Emulador. La recarga del torneo al volver ya la cubre `ExplorarYTorneoViewModelTest` (MOV-0005) | Emulador |
| CA-8 | Capturas del emulador | Emulador |

## 8. Verificación en el emulador

1. Con una cuenta que no participe, abre un torneo con inscripciones abiertas: la franja muestra "Inscribir mi equipo".
2. Tócalo: la pantalla muestra el torneo, la cuota y tus equipos.
3. Crea un equipo nuevo e inscríbelo: sale la confirmación.
4. "Listo": la franja dice "Inscribiste a…" y en Inicio sale el equipo con "Falta comprobante".
5. Con `admin01`, aprueba la inscripción (MOV-0008) y genera los partidos (MOV-0011).

**Capturas** en `docs/specs/evidencias/MOV-0014/`.

## 9. Archivos afectados

**Nuevos**

- `domain/model/Delegado.kt`, `domain/repository/DelegadoRepository.kt`, `domain/use_case/competicion/DelegadoUseCases.kt`
- `data/remote/api/DelegadoApi.kt`, `data/remote/dto/DelegadoDtos.kt`, `data/repository/DelegadoRepositoryImpl.kt`
- `presentacion/delegado/InscribirEquipoScreen.kt` e `InscribirEquipoViewModel.kt`
- `InscribirEquipoTest.kt`, `DelegadoRepositoryImplTest.kt`, `InscribirEquipoViewModelTest.kt` y `testutil/FakesDelegado.kt`

**Modificados**

- `presentacion/explorar/TorneoScreen.kt` (el botón y `onInscribir`), `domain/model/Publico.kt` (un comentario)
- `presentacion/navigation/Rutas.kt` (`InscribirEquipo`) y `AppNavigation.kt`
- `di/AppContainer.kt`, `res/values/strings.xml`

**`testTag` nuevos:** `torneo_inscribir`, `inscribir_atras`, `inscribir_condiciones`, `inscribir_equipo`, `inscribir_nuevo`, `inscribir_nombre`, `inscribir_error`, `inscribir_confirmar`, `inscribir_reintentar`, `inscribir_hecha` e `inscribir_listo`.

## 10. Notas y decisiones

- **Sin comprobantes (acordado con David):** se informa la cuota y la inscripción queda pendiente. La subida del comprobante va en su propia spec.
- **"Mis inscripciones" no es una pantalla nueva:** "Mis equipos" de Inicio ya lista cada inscripción con su chip ("Falta comprobante", "En revisión"…), y la franja del torneo muestra la del delegado.
- **El nombre repetido se valida también en la app,** con los equipos que ya se cargaron, para no esperar al 409. El backend sigue siendo quien decide.
- **Con varios equipos no se preselecciona ninguno:** inscribir el equipo equivocado obliga a pedirle al organizador que lo rechace.

## 11. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-21 | Redactada e implementada por pedido de David ("Sí, realiza la primera"). Pasa a `en-revision` |
| 2026-11-21 | 416 pruebas, 0 fallos (18 nuevas); `assembleDebug` en verde. Prueba de dientes: sin el chequeo del nombre repetido fallan 2 pruebas |
| 2026-11-21 | Verificada en el emulador **hasta antes de enviar**, contra el backend de Render, con la sesión de Diana (sin equipos propios) en "Prueba 2029" (público, cuota de $1.000): la franja muestra "Inscribir mi equipo" (CA-1); la pantalla muestra el torneo, la cuota y arranca en el campo del nombre con el botón deshabilitado, que se habilita al escribir (CA-2). Capturas 01 a 03 (oscuro). **No se envió la inscripción** para no crear datos en el entorno compartido: falta el paso 3 de §8 en adelante (CA-3 a CA-7 en el emulador; cubiertos por pruebas), y claro con letra al 130 % (CA-8) |
