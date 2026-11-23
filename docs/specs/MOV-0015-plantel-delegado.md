---
id: MOV-0015
titulo: Plantel del equipo para su delegado
tipo: feature
estado: hecha
prioridad: P1
fase: F1
modo: Jugador (delegado)
responsable: David
depende_de: [MOV-0005, MOV-0014, backend 0018, backend 0022, backend 0023, backend 0026]
rama: mov-0015-plantel-delegado
---

# MOV-0015 · Plantel del equipo para su delegado

> **Para quien la implementa:** el backend que usa **ya existe** en `main` de BackendCodeCup (HU44 y specs 0018, 0022, 0023 y 0026) y está desplegado en Render. **No depende de la 0030.** Lee antes [`../sdd.md`](../sdd.md) y [`../arquitectura.md`](../arquitectura.md).

## 1. Contexto

- **Hoy un equipo inscrito desde la app (MOV-0014) se queda solo con su delegado.** No hay cómo agregar jugadores, quitarlos ni darles número sin pasar por la web.
- **Sin plantel no hay partido de verdad:** el árbitro solo anota a los jugadores alineados (MOV-0012), y solo puede alinear a los del plantel.
- **Esta spec completa el lado del equipo:** crear torneo → inscribir equipos (MOV-0014) → **armar el plantel** → aprobar (MOV-0008) → generar y programar los partidos (MOV-0011 y MOV-0013).
- **Pedido de David (21-nov-2026):** "Sí, continúa con la de plantel", la siguiente recomendada después de MOV-0014. Se retomó el 23-nov, porque el hilo del 21-nov se cortó a mitad de la implementación.

## 2. Alcance

### 2.1 Cómo se llega al plantel

- **Desde el detalle del torneo:** la franja "Inscribiste a *Equipo*" suma el botón de contorno **"Ver plantel"**. Sale con la inscripción en cualquier estado, también pendiente de pago, porque el plantel se arma mientras el organizador la revisa.
- **Desde la confirmación de la inscripción (MOV-0014):** el botón principal **"Agregar jugadores"** queda encima de "Listo". El plantel **reemplaza** a la confirmación, así que Atrás vuelve al torneo y no a una inscripción ya hecha.

### 2.2 La pantalla "Plantel"

- **Arriba:** "Plantel", con "*Equipo* · *Torneo*" debajo.
- **Resumen:** "*N* jugadores" (solo los activos) y la ayuda "Toca a un jugador para cambiar su número o quitarlo."
- **Los activos:** ordenados por número de camiseta, con los que no tienen número al final, y después por nombre. Cada fila lleva:
  - el número, con el mismo círculo de la planilla del árbitro;
  - el nombre;
  - el detalle que el delegado necesita: "Delegado", "Sin cuenta" (lo agregó sin cuenta y aún no puede entrar a la app) y "Doc. *número*". El documento solo lo ve el delegado.
- **"Ya no están":** los que el delegado quitó, por nombre y sin acción. Su historial se conserva.
- **"Agregar jugador":** botón flotante que abre la pantalla de §2.4.
- **Al deslizar hacia abajo** se vuelve a pedir el plantel. **Al volver de agregar jugadores** también, pero sin el indicador y sin quitar lo que se ve.
- **Torneo finalizado:** el plantel se ve, pero no se puede cambiar. Sin botón flotante, las filas sin acción, y la ayuda dice "El torneo terminó: el plantel ya no cambia."

### 2.3 El diálogo de un jugador

- **Tocar a un activo** abre un diálogo con su nombre y el campo **"Número de camiseta"**:
  - solo admite dígitos, como mucho dos;
  - vacío le quita el número;
  - tiene los botones **"Guardar"** y **"Cancelar"**.
- **"Quitar del plantel"** (en rojo) sale para todos menos el delegado. Al delegado el diálogo le dice "Eres el delegado: sigues en el plantel."
- **Quitar pide confirmación:** "¿Quitar a *X* del plantel?", con el detalle "Deja de jugar con el equipo en los partidos que faltan. Lo que ya jugó se conserva." y el botón **"Quitar"**.
  - Al terminar, se cierra todo, sale el aviso "*X* ya no está en el plantel." y la persona pasa a "Ya no están".
  - Si falla, se vuelve al diálogo del jugador con el motivo.

### 2.4 La pantalla "Agregar jugador"

- **Arriba:** "Agregar jugador", con el nombre del equipo debajo, y la explicación "Si ya tiene cuenta en Desafío Sports, basta con su documento: se usa el nombre de su cuenta."
- **Campos:**
  - **Tipo** (desplegable, CC por defecto) y **Documento**. El campo del tipo muestra la sigla (CC, TI, CE, PPT, PA) y su menú, el nombre completo. El documento es opcional y su etiqueta dice "Documento (opcional)"; si el torneo lo exige, dice "Documento".
  - **Nombre completo**, con la ayuda "Si no tiene cuenta, escríbelo como quiere que aparezca."
  - **Camiseta**, opcional, del 0 al 99.
- **"Agregar al plantel"** (naranja) se habilita con un documento o un nombre. Al terminar:
  - sale el aviso "*X* quedó en el plantel.";
  - el formulario se vacía, pero conserva el tipo de documento, que casi siempre es el mismo para todo el equipo;
  - abajo se acumula "Agregaste: *A*, *B*…".

  Así se carga un plantel entero sin salir de la pantalla.
- **Atrás** vuelve al plantel, que se actualiza solo.

## 3. Diseño

- **Componentes que se reutilizan:** `TituloConTorneo`, `Fila`, `NumeroCamiseta` (de la planilla), `EncabezadoBloque`, `EstadoVacio`, `Cargando`, `EstadoError`, `BotonPrimario` y `BotonContorno`. También el `ExtendedFloatingActionButton` naranja, como en la sección Creador, el `PullToRefreshBox`, el `AlertDialog` y el `Snackbar`.
- **Agregar va en una pantalla y no en un diálogo:** son cuatro campos y se cargan varias personas seguidas, con el teclado abierto.
- **El tipo de documento va con su sigla:** comparte fila con el número, y "Cédula de ciudadanía" no cabía (se cortaba en "Cédula de", visto en el emulador). El menú se abre con su propio ancho y muestra los nombres completos.
- **Los errores:**
  - en Agregar, cada uno va bajo el campo que lo causó; los demás van sobre el botón, con `liveRegion` para TalkBack;
  - en el diálogo de un jugador van dentro del diálogo, porque un aviso abajo quedaría tapado.

### Estados

| Pantalla | Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|---|
| Plantel | Indicador | "Aún no hay jugadores" + "Agrégalos con su documento o su nombre." (no debería pasar: el delegado siempre está) | Mensaje + "Reintentar". Si no es su delegado (403), "Volver" | Texto de sin conexión + "Reintentar" | La lista. Si falla al refrescar, se conserva y sale un aviso |
| Agregar jugador | El botón dice "Agregando…" y los campos se bloquean | Formulario vacío, botón deshabilitado | Bajo el campo o sobre el botón | Sobre el botón | Aviso + formulario vacío + "Agregaste: …" |

### Textos (todos en `strings.xml`)

| Dónde | Texto |
|---|---|
| Franja del torneo | "Ver plantel" |
| Confirmación de la inscripción | "Agregar jugadores" |
| Plantel | "Plantel" · "*N* jugador / jugadores" · "Toca a un jugador para cambiar su número o quitarlo." · "El torneo terminó: el plantel ya no cambia." · "Aún no hay jugadores" · "Agrégalos con su documento o su nombre." · "Agregar jugador" · "Sin cuenta" · "Ya no están" · "Retirado del plantel" |
| Diálogo del jugador | "Número de camiseta" · "Del 0 al 99. Déjalo vacío para quitarle el número." · "Guardar" · "Guardando…" · "Quitar del plantel" · "Eres el delegado: sigues en el plantel." |
| Confirmar quitar | "¿Quitar a *X* del plantel?" · "Deja de jugar con el equipo en los partidos que faltan. Lo que ya jugó se conserva." · "Quitar" · "Quitando…" · aviso "*X* ya no está en el plantel." |
| Agregar jugador | "Agregar jugador" · "Si ya tiene cuenta en Desafío Sports, basta con su documento: se usa el nombre de su cuenta." · "Tipo" · siglas "CC", "TI", "CE", "PPT", "PA" · "Documento (opcional)" / "Documento" · "Nombre completo" · "Si no tiene cuenta, escríbelo como quiere que aparezca." · "Camiseta" · "Opcional, del 0 al 99." · "Agregar al plantel" · "Agregando…" · aviso "*X* quedó en el plantel." · "Agregaste: *A*, *B*" |
| Validación | "Este torneo exige documento: escribe el del jugador." · "El documento puede tener hasta 20 caracteres." · "Escribe su nombre completo, o su documento si ya tiene cuenta." · "El nombre puede tener hasta 150 caracteres." · "Usa un número del 0 al 99, o déjalo vacío." · "El *N* ya lo tiene *X*." · "No puedes quitarte del plantel: eres su delegado." |

- **Accesibilidad:** las filas y los botones miden 48 dp o más. Los diálogos llevan `testTagsAsResourceId`, porque son otra ventana. El campo del número abre el teclado numérico. El botón Atrás del sistema cierra el diálogo abierto o vuelve a la pantalla anterior.

## 4. Contrato con el backend

Todos van bajo `/api/supercopa/delegado/equipo-torneo/{equipoTorneoId}` y responden 403 `NO_DELEGADO_DE_LA_INSCRIPCION` si quien llama no es el delegado de esa inscripción.

| Endpoint | Método | Envía | Responde | Errores esperados |
|---|---|---|---|---|
| `…/miembros` | GET | — | `[MiembroEquipoDTO]` en orden de ingreso, activos y retirados. Se usan `personaId`, `nombre`, `numeroDocumento`, `numeroCamiseta`, `estado` (`ACTIVO` o `RETIRADO`), `esDelegado`, `tieneCuenta` y `torneoEstado` | 404 si la inscripción no existe |
| `…/miembros` | POST | `{ numeroDocumento?, tipoDocumento?, nombre?, numeroCamiseta? }`. Lo vacío no viaja | `MiembroEquipoDTO` del agregado | 400 `DOCUMENTO_REQUERIDO` · `NOMBRE_REQUERIDO` · `VALIDACION` (documento inválido o largo) · 400 `REGLA_NEGOCIO` (ya juega en un equipo del torneo; número ocupado) · 409 `JUGADOR_BLOQUEADO` |
| `…/miembros/{personaId}/remover` | POST | — | `MiembroEquipoDTO`, ya `RETIRADO` | 400 `REGLA_NEGOCIO` (es el delegado) · 404 |
| `…/miembros/{personaId}/camiseta` | PATCH | `{ numeroCamiseta }`, o `{}` para quitarlo | `MiembroEquipoDTO` | 400 `REGLA_NEGOCIO` (el número lo tiene otro activo) · 404 |

- **De dónde sale el nombre (backend 0023):**
  - si el documento es de una persona con cuenta, se usa el nombre de su cuenta;
  - si es de alguien sin cuenta, el que escribe el delegado, o el que ya tenía de otro torneo;
  - sin documento, la persona siempre es nueva y el nombre es obligatorio.
- **El tipo de documento** solo se usa al crear a una persona nueva; si no llega, el backend pone CC. La app lo manda solo junto con un documento.
- **Quitar no borra:** deja al miembro `RETIRADO`, con fecha de fin. Volver a agregar a la misma persona por su documento la reactiva.
- **El backend no limita el número a 0–99 ni bloquea cambios con el torneo `FINALIZADO`.** Las dos reglas son de la app (ver §11).
- **La ruta lleva** el `equipoTorneoId`, el nombre del equipo y del torneo, y si el torneo exige documento. Salen del detalle del torneo (`miRelacion.delegadoDe`) o de la inscripción recién hecha.

## 5. Criterios de aceptación

1. **CA-1** · **Dado** que soy delegado de un equipo inscrito en un torneo, **cuando** abro el torneo, **entonces** la franja "Inscribiste a…" muestra "Ver plantel", que abre el plantel de ese equipo. **Y dado** que acabo de inscribir un equipo, **cuando** toco "Agregar jugadores" en la confirmación, **entonces** se abre su plantel, y Atrás vuelve al torneo.
2. **CA-2** · **Dado** el plantel, **entonces** veo a los activos ordenados por número (los que no tienen número al final, y después por nombre), con "Delegado", "Sin cuenta" y el documento cuando corresponde. Aparte, en "Ya no están", veo a los retirados. El resumen cuenta solo a los activos, y un miembro que llega sin `personaId` no se muestra.
3. **CA-3** · **Dado** que el plantel no carga, **entonces** se ofrece "Reintentar". Si el backend responde que no soy su delegado, se ofrece "Volver" en vez de "Reintentar". Si falla al refrescar con el plantel ya en pantalla, se conserva lo que se ve y sale un aviso.
4. **CA-4** · **Dado** "Agregar jugador", **cuando** escribo un documento, un nombre o ambos y toco "Agregar al plantel", **entonces**:
   - viaja solo lo escrito, sin espacios de sobra, y el tipo solo si hay documento;
   - sale el aviso "*X* quedó en el plantel.";
   - el formulario se vacía, pero conserva el tipo;
   - al volver, el plantel lo muestra.
5. **CA-5** · **Dado** cualquiera de estos casos, **entonces** no viaja nada y el motivo sale bajo su campo:
   - el torneo exige documento y no lo escribí;
   - el documento tiene más de 20 caracteres;
   - no hay ni documento ni nombre;
   - el nombre tiene más de 150 caracteres;
   - el número está fuera de 0–99 o ya lo tiene otro activo.

   Los `DOCUMENTO_REQUERIDO`, `VALIDACION` y `NOMBRE_REQUERIDO` del backend también van bajo su campo. Los demás rechazos (ya juega en otro equipo, cuenta bloqueada, sin conexión) van sobre el botón. Escribir otra vez borra el error de ese campo, y el doble toque no agrega dos veces.
6. **CA-6** · **Dado** un jugador activo, **cuando** lo toco, cambio su número y guardo, **entonces** viaja el `PATCH` con el número (o `{}` si lo dejé vacío), y la fila se actualiza sin volver a pedir el plantel. Si el número no cambió, no viaja nada. Un número que ya tiene otro activo no viaja y el motivo sale en el diálogo, igual que el rechazo del backend. El doble toque en "Guardar" envía una sola vez.
7. **CA-7** · **Dado** un jugador activo que no es el delegado, **cuando** toco "Quitar del plantel" y confirmo, **entonces** viaja `remover`, la persona pasa a "Ya no están" y sale el aviso. Al delegado no se le ofrece quitarse, y si se pidiera, no viaja. Si falla, vuelvo al diálogo del jugador con el motivo.
8. **CA-8** · **Dado** un torneo finalizado, **entonces** el plantel se ve, pero no se puede agregar a nadie, cambiar números ni quitar.
9. **CA-9** · El plantel, el diálogo y "Agregar jugador" se ven bien en claro, en oscuro y con la letra al 130 % (emulador).

## 6. Fuera de alcance / qué NO debe cambiar

- **Solicitudes de ingreso** (un jugador pide entrar a un equipo; backend `SolicitudController`) y **"¿Este eres tú?"** (reclamar un historial anotado sin cuenta, backend 0023): cada una tiene su spec.
- **Volver a agregar a un retirado sin documento:** el backend solo agrega por documento, así que, sin él, se crearía otra persona. Ver §11.
- **Posición, pierna hábil y altura:** `MiembroEquipoDTO` las trae, pero el plantel no las muestra.
- **El plantel visto por el organizador o por los jugadores,** cambiar de delegado, comprobantes y multas.
- **No cambian:** las demás franjas del torneo, la inscripción (salvo el botón nuevo de la confirmación), la planilla del árbitro ni "Mis equipos" de Inicio.

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) | Nivel |
|---|---|---|
| CA-1 | Emulador (la franja, la confirmación y Atrás) | Emulador |
| CA-2 | `PlantelTest › CA-2 …`, `DelegadoRepositoryImplTest › CA-2 …`, `PlantelViewModelTest › CA-2 …` | Dominio + Red + ViewModel |
| CA-3 | `DelegadoRepositoryImplTest › CA-3 …`, `PlantelViewModelTest › CA-3 …` (3) | Red + ViewModel |
| CA-4 | `AgregarJugadorTest › CA-4 …` (2), `DelegadoRepositoryImplTest › CA-4 …`, `PlantelTest › CA-4 …`, `AgregarJugadorViewModelTest › CA-4 …` (2), `PlantelViewModelTest › CA-4 …` | Dominio + Red + ViewModel |
| CA-5 | `AgregarJugadorTest › CA-5 …` (3), `DelegadoRepositoryImplTest › CA-5 …`, `AgregarJugadorViewModelTest › CA-5 …` (4) | Dominio + Red + ViewModel |
| CA-6 | `CamisetaYQuitarTest › CA-6 …` (3), `PlantelTest › CA-6 …`, `DelegadoRepositoryImplTest › CA-6 …`, `PlantelViewModelTest › CA-6 …` (4) | Dominio + Red + ViewModel |
| CA-7 | `CamisetaYQuitarTest › CA-7 …`, `DelegadoRepositoryImplTest › CA-7 …`, `PlantelViewModelTest › CA-7 …` (3) | Dominio + Red + ViewModel |
| CA-8 | `PlantelTest › CA-8 …`, `DelegadoRepositoryImplTest › CA-8 …`, `PlantelViewModelTest › CA-8 …` | Dominio + Red + ViewModel |
| CA-9 | Capturas del emulador | Emulador |

En total son 39 pruebas nuevas: 13 de dominio, 7 de red y 19 de ViewModel. La lista está en `evidencias/MOV-0015/02-green.txt`.

## 8. Verificación en el emulador

1. Con `delegado01` (Halcones FC), abre su torneo: la franja muestra "Ver plantel".
2. Tócalo: el plantel muestra a los activos con su número, el delegado marcado y el documento de cada uno.
3. Toca a un jugador: el diálogo muestra su número y "Quitar del plantel". Toca al delegado: no se ofrece quitarlo.
4. "Agregar jugador": el formulario arranca vacío y el botón se habilita al escribir un nombre.
5. Si se acuerda con el equipo crear datos en DesafioSportsDev: agrega a alguien, cámbiale el número y quítalo, y luego mira "Ya no están".

**Capturas** en `docs/specs/evidencias/MOV-0015/`: oscuro, claro y letra al 130 %.

## 9. Archivos afectados

**Nuevos**

- `presentacion/delegado/PlantelScreen.kt`, `PlantelViewModel.kt`, `AgregarJugadorScreen.kt` y `AgregarJugadorViewModel.kt`
- `test/…/domain/model/PlantelTest.kt`, `AgregarJugadorTest.kt` y `CamisetaYQuitarTest.kt`
- `test/…/presentacion/delegado/PlantelViewModelTest.kt` y `AgregarJugadorViewModelTest.kt`

**Modificados**

- `domain/model/Delegado.kt` (`MiembroDePlantel`, `Plantel` y `BorradorJugador`), `domain/error/CampoValidado.kt` (`CAMISETA`)
- `domain/repository/DelegadoRepository.kt` y `domain/use_case/competicion/DelegadoUseCases.kt` (los cuatro casos de uso del plantel)
- `data/remote/api/DelegadoApi.kt`, `data/remote/dto/DelegadoDtos.kt` y `data/repository/DelegadoRepositoryImpl.kt`
- `presentacion/explorar/TorneoScreen.kt` ("Ver plantel") y `presentacion/delegado/InscribirEquipoScreen.kt` ("Agregar jugadores")
- `presentacion/navigation/Rutas.kt` (`PlantelEquipo` y `AgregarJugador`) y `AppNavigation.kt`
- `di/AppContainer.kt` y `res/values/strings.xml` (textos del plantel y siglas de los tipos de documento)
- `test/…/testutil/FakesDelegado.kt` y `test/…/data/repository/DelegadoRepositoryImplTest.kt`

**`testTag` nuevos:** `torneo_plantel`, `inscribir_agregar_jugadores`, `plantel_atras`, `plantel_agregar`, `plantel_lista`, `plantel_resumen`, `plantel_jugador`, `plantel_retirado`, `plantel_reintentar`, `plantel_volver`, `dialogo_jugador`, `plantel_camiseta`, `plantel_guardar`, `plantel_quitar`, `plantel_quitar_confirmar`, `plantel_quitar_cancelar`, `agregar_atras`, `agregar_tipo`, `agregar_documento`, `agregar_nombre`, `agregar_camiseta`, `agregar_error`, `agregar_confirmar` y `agregar_agregados`.

## 10. Datos de prueba

- **Delegados del seed:** `delegado01` (Halcones FC, 5 jugadores), `delegado02` (Tiburones FC, 5), `delegado03` (Cóndores FC, 2) y `delegado04` (Pumas FC, 1). Todos en la copa del seed; algunos también en la liga. La contraseña es `SEED_PASSWORD`.
- **Casos extremos:** un plantel de 25 o más, nombres largos, el número 0, jugadores sin número, un retirado, un miembro sin cuenta y un torneo finalizado.

## 11. Notas y decisiones

- **El número va del 0 al 99 (regla de la app):** el círculo de la planilla está pensado para dos dígitos. El backend acepta cualquier entero, así que un número mayor puesto desde la web se ve, pero para guardar desde la app hay que cambiarlo.
- **Con el torneo finalizado no se cambia nada (regla de la app):** el backend no lo bloquea, pero cambiar el plantel de un torneo terminado reescribiría el historial.
- **El número ocupado se valida también en la app,** con el plantel ya cargado, para no esperar al rechazo. El backend sigue siendo quien decide.
- **Quitar al delegado no se ofrece:** él responde por el equipo. Cambiar de delegado es otra spec.
- **Un retirado no se reactiva desde la lista:** el backend solo agrega por documento. Si la persona tiene documento, basta con volver a agregarla con él. Si no lo tiene, se crearía otra persona. Si hace falta, se pide al backend reactivar por `personaId`.
- **Tras agregar, el formulario se vacía y conserva el tipo:** así se carga un equipo entero sin salir de la pantalla.

## 12. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-21 | Arranca por pedido de David ("Sí, continúa con la de plantel"). El hilo se corta a mitad de la implementación: el código queda sin compilar ni confirmar en la rama `mov-0015-plantel-delegado` |
| 2026-11-23 | Retomada: la spec se redacta a partir del código y del contrato del backend, que se revisó contra `DelegadoController` y `DelegadoService` en `main` |
| 2026-11-23 | 455 pruebas, 0 fallos (39 nuevas), y `assembleDebug` en verde (`evidencias/MOV-0015/02-green.txt`). Como el código ya existía, en vez de un RED previo se hizo la prueba de dientes de `sdd.md` §6: se rompieron 5 reglas a propósito y fallaron 8 pruebas, justo las que las cubren (`01-dientes.txt`). Pasa a `en-revision` |
| 2026-11-23 | Verificada en el emulador (Pixel 8) contra el backend de Render, con `delegado01` en "Copa Prueba 2026": la franja muestra "Ver plantel" (CA-1); el plantel muestra 6 jugadores en orden de camiseta, el delegado marcado y los documentos (CA-2); el diálogo de un jugador ofrece "Quitar del plantel", y el del delegado no (CA-6, CA-7); "Agregar jugador" arranca con el botón deshabilitado, que se habilita al escribir un nombre (CA-4). Capturas 01 a 11, en oscuro, claro y con la letra al 130 % (CA-9). En el emulador apareció el tipo de documento cortado ("Cédula de"); se corrigió con las siglas y se volvió a verificar (captura 11). **No se envió nada** (agregar, cambiar número ni quitar) para no cambiar datos en el entorno compartido: eso queda cubierto por pruebas. Tampoco se probó en el emulador "Agregar jugadores" desde la confirmación de la inscripción (CA-1, segunda parte), porque exige inscribir un equipo, ni el estado de torneo finalizado (CA-8, cubierto por pruebas) |
| 2026-11-23 | **Integrada a `main`** por decisión de David ("Sí, intégralo todo"), junto con MOV-0011 a MOV-0015 y el backend 0030, con 455 pruebas y 0 fallos. Pasa a `hecha`. Sigue sin verificar en el emulador: agregar, cambiar el número y quitar (CA-4 a CA-7), "Agregar jugadores" desde la confirmación de la inscripción (CA-1) y el torneo finalizado (CA-8). Todo está cubierto por pruebas |
