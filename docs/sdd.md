# Desarrollo guiado por specs (SDD) · app Desafío Sports

> **Propósito:** cada cambio en la app nace de una **spec** con criterios de aceptación verificables, y se entrega con **pruebas** y **evidencia** (capturas del emulador si hay pantalla) que lo demuestran.
> **Origen:** adapta la guía del proyecto (`CODECUP/.context/spec.md`) a este repo, enfocada solo en la interfaz móvil, y la hace trazable de punta a punta.
> **Audiencia:** el equipo **y** los agentes de IA. Cuando le pidas trabajo a un agente, enlaza este archivo (ver §9).

---

## 0. Cómo usar este documento

| Si vas a… | Lee |
|---|---|
| Entender el flujo de un vistazo | §2 y §3 |
| Escribir una spec nueva | §5 y la plantilla [`specs/_PLANTILLA.md`](specs/_PLANTILLA.md) |
| Saber en qué estado está cada spec | [`specs/README.md`](specs/README.md) |
| Saber si algo está terminado | §7 (Definition of Done) |
| Tomar una spec si eres del equipo | §8 |
| Pedirle una spec a un agente de IA | §9 |

**Regla de oro:** nada entra a `main` sin estas tres cosas:
- una spec **aprobada** con criterios de aceptación;
- **pruebas** que cubren cada criterio;
- si hay pantalla, **capturas del emulador** guardadas como evidencia.

---

## 1. Alcance

- **Este SDD es solo de la app:** pantallas, navegación, estado, red de la app y diseño.
- **El backend tiene sus propias specs** en `CODECUP/.context/specs/` (numeradas `0001`, `0002`…).
  - Cuando una spec de la app necesita un endpoint que no existe, queda **`bloqueada`** y se pide la spec del backend.
- **Para citar entre repos:** se escribe `backend 0018` (spec del backend) o `MOV-0003` (spec de esta app). Así nunca se confunden.
- **El orden es primero el móvil y después la web.** Lo que funcione aquí se lleva a la web con una spec propia en el repo de la web.

---

## 2. Trazabilidad: cómo se conecta todo

Cada pieza lleva el ID de su spec. Desde un test se llega al criterio, a la spec, a la rama, al PR y a la evidencia, y también en el sentido contrario.

| Pieza | Formato | Ejemplo |
|---|---|---|
| ID de la spec | `MOV-NNNN`, correlativo de 4 dígitos | `MOV-0004` |
| Archivo | `docs/specs/MOV-NNNN-<slug>.md` | `docs/specs/MOV-0004-login.md` |
| Criterio de aceptación | `CA-n` dentro de la spec; `MOV-NNNN/CA-n` fuera de ella | `MOV-0004/CA-2` |
| Rama | `mov-NNNN-<slug>` | `mov-0004-login` |
| Commit | `tipo(alcance): resumen (MOV-NNNN)` | `feat(acceso): pantalla de login (MOV-0004)` |
| Pull request | Título `MOV-NNNN · <título>`, con el cuerpo de la plantilla | `MOV-0004 · Pantalla de login` |
| Test | El nombre empieza por el criterio | ``fun `CA-2 credenciales malas muestran el error`()`` |
| Evidencia | `docs/specs/evidencias/MOV-NNNN/` | capturas, salida de tests |
| Índice | `docs/specs/README.md` | estado de todas las specs |

**Tipos de commit:** `feat` · `fix` · `refactor` · `test` · `docs` · `chore` (versiones, configuración).

**Alcances sugeridos:** `base` · `acceso` · `modo` · `jugador` · `arbitro` · `creador` · `publico` · `perfil` · `red` · `tema` · `docs`.

> Un criterio sin test, o un test sin criterio, rompe la trazabilidad. Si un criterio no se puede automatizar (por ejemplo, "girar el teléfono no pierde el estado"), su verificación manual se describe en la spec y se guarda su captura.

---

## 3. El ciclo de una spec

```
 1. SPEC ──▶ 2. DISEÑO ──▶ 3. RED ──▶ 4. GREEN ──▶ 5. VERIFICAR ──▶ 6. CERRAR
 criterios   Impeccable   tests que  lo mínimo   suite + build   PR, revisión,
 y contrato  (si hay      FALLAN     para pasar  + emulador      merge, índice
             pantalla)                           + evidencia
      ▲                                                   │
      └──── si la realidad contradice la spec, primero se corrige la spec
```

1. **SPEC.** Se copia la plantilla, se llena y se agrega al índice como `borrador`.
   - Lleva los criterios en Dado/Cuando/Entonces, el contrato con el backend, el plan de pruebas y lo que queda fuera de alcance.
   - El dueño del producto (David) la revisa y la pasa a `aprobada`.
2. **DISEÑO.** Solo si hay pantalla. Se trabaja con la skill Impeccable sobre el Design System, siguiendo el flujo de [`plan-ui-impeccable.md`](plan-ui-impeccable.md) §4.
   - Las decisiones de diseño (estados, textos, jerarquía) se anotan en la spec antes de programar.
3. **RED.** Se escriben primero los tests del plan de pruebas. Deben **fallar por la razón correcta**, no por un error de compilación.
4. **GREEN.** Se implementa lo mínimo para que pasen, sin tocar nada fuera de "Archivos afectados".
5. **VERIFICAR.** Se corre todo esto, y lo que salga se guarda en `evidencias/MOV-NNNN/`:
   - **toda** la suite y el build;
   - el emulador (claro, oscuro, letra al 130 %, girar el teléfono y el caso extremo);
   - la prueba de humo, si la spec toca la red.
6. **CERRAR.** Se abre el PR con la plantilla. Otra persona lo revisa, se hace el merge, la spec pasa a `hecha` y se actualiza el índice.

> Un bug sigue el mismo ciclo. El paso RED es un **test de regresión** que reproduce el bug.

---

## 4. Estados de una spec

```
borrador ──▶ aprobada ──▶ en-curso ──▶ en-revision ──▶ hecha
                 │             │
                 └─────────────┴──▶ bloqueada (con motivo) ──▶ vuelve a aprobada
cualquiera ──▶ descartada (con motivo; el archivo no se borra)
```

| Estado | Significa | Quién lo cambia |
|---|---|---|
| `borrador` | Se está escribiendo | Quien la redacta |
| `aprobada` | Lista para trabajar | David (dueño del producto) |
| `en-curso` | Alguien la tomó: su nombre va en `responsable` | Quien la toma |
| `en-revision` | Implementada, con el PR abierto, esperando revisión o verificación | Quien la implementa |
| `hecha` | Mergeada a `main` y con el Definition of Done cumplido | Quien hace el merge |
| `bloqueada` | Depende de algo externo (endpoint, decisión, cuenta…). El motivo se anota | Cualquiera |
| `descartada` | No se hará. El motivo se anota | David |

---

## 5. Anatomía de una spec

La plantilla completa está en [`specs/_PLANTILLA.md`](specs/_PLANTILLA.md). Sus secciones:

1. **Contexto:** qué necesita el usuario y por qué. Si es un bug, el síntoma y la causa (`archivo:línea`).
2. **Alcance:** pantallas y flujos que entran.
3. **Diseño**, si hay pantalla:
   - la referencia (Impeccable, Design System y el nodo de Figma si hay);
   - los **estados** de cada pantalla: cargando, vacío, error, sin conexión y éxito;
   - los **textos**;
   - la **accesibilidad**: 48 dp, contraste AA, letra al 130 %, TalkBack y el botón Atrás.
4. **Contrato con el backend:** endpoints, qué se envía, qué responde y qué `code` de error se espera, con su `AppError`.
5. **Criterios de aceptación:** `CA-1`, `CA-2`… en Dado/Cuando/Entonces. Cada uno debe poder verificarse.
6. **Fuera de alcance / qué NO debe cambiar:** protege contra "features que desaparecen".
7. **Plan de pruebas:** tabla criterio → test → nivel.
8. **Verificación en el emulador:** pasos y capturas a guardar.
9. **Archivos afectados:** lista cerrada.
10. **Datos de prueba:** usuarios del seed y casos extremos (nombres largos, 0 y muchos elementos, 12 o más fechas…).
11. **Notas y registro de cambios** de la spec.

**Una spec es un cambio cohesivo.** Si necesita más de un flujo independiente, se divide. Muchas veces un archivo por pantalla, o un par de pantallas muy unidas, es el tamaño justo.

---

## 6. Niveles de prueba

| Nivel | Qué atrapa | Ejemplo en el repo |
|---|---|---|
| Caso de uso (dominio) | Validaciones y reglas | `IniciarSesionUseCaseTest` |
| ViewModel | Qué estado ve la pantalla en cada caso | `DiagnosticoViewModelTest` |
| Red y repositorios (MockWebServer) | Contrato con el backend, errores, 401 y 403 | `RedSesionTest`, `CuentaRepositoryImplTest` |
| Humo (backend real, opcional) | Que el entorno funcione de punta a punta | `BackendRealHumoTest` (con `DESAFIO_HUMO=1`) |
| UI (Compose UI Test) | Flujos críticos en pantalla | Llega con la primera pantalla real |
| Emulador (capturas) | Lo visual: diseño, temas, letra grande, giro | `docs/specs/evidencias/MOV-0001/` |

- **Usa siempre el nivel más barato que pueda fallar por el bug.**
- **Comprueba que el test tiene dientes:** cambia el código a propósito y mira que falle. En MOV-0001 se quitó a propósito el control que corta el bucle de reintentos y falló justo el test esperado.

Detalle técnico y comandos: [`arquitectura.md`](arquitectura.md) §10.

---

## 7. Definition of Done

Una spec está **hecha** cuando todo esto es cierto:

- [ ] Está en `docs/specs/` con estado `hecha` y sus criterios numerados.
- [ ] Cada criterio tiene al menos una prueba, o una verificación manual descrita con su evidencia.
- [ ] Hubo un momento en que las pruebas nuevas fallaban (RED real).
- [ ] `./gradlew testDebugUnitTest assembleDebug` pasa en verde, con **toda** la suite y no solo los tests nuevos.
- [ ] Si hay pantalla: hay capturas del emulador en `evidencias/` (claro, oscuro, letra al 130 % y el caso extremo).
- [ ] Si toca la red: la prueba de humo contra el backend real pasa.
- [ ] No se quitó ninguna pantalla ni comportamiento fuera del alcance: las vistas se **reubican**, no se borran.
- [ ] Los textos están en `strings.xml`, los colores salen del tema y los controles llevan `testTag`.
- [ ] El PR usa la plantilla y el índice de specs está actualizado.

---

## 8. Trabajo en equipo

1. **Tomar una spec:**
   - elige una `aprobada` del índice;
   - pon tu nombre en `responsable`;
   - pásala a `en-curso` en la spec y en el índice;
   - crea la rama `mov-NNNN-<slug>`.
2. **Una spec por persona a la vez,** con ramas cortas. Si te bloqueas, pásala a `bloqueada` y explica por qué.
3. **Archivos compartidos** (`AppContainer.kt`, `Rutas.kt`, `AppNavigation.kt`, `strings.xml`): cambios pequeños y `git pull --rebase` seguido.
4. **Revisión:** al menos una persona distinta del autor revisa el PR. David aprueba las specs y hace el merge a `main`.
5. **No se mergea con tests en rojo.** Nunca.
6. **Specs nuevas:** cualquiera puede proponer una en `borrador`. El número es el siguiente libre del índice.

---

## 9. Pedirle trabajo a un agente de IA

**Para implementar una spec**, pega esto al inicio del pedido:

```
Trabaja la spec docs/specs/MOV-NNNN-<slug>.md siguiendo docs/sdd.md y docs/arquitectura.md.
1. Escribe primero las pruebas del plan de pruebas. Córrelas y muéstrame que FALLAN.
2. Implementa lo mínimo para que pasen. No toques archivos fuera de "Archivos afectados".
3. Corre ./gradlew testDebugUnitTest assembleDebug y pega la salida.
4. Si hay pantalla, instálala en el emulador y guarda capturas (claro, oscuro, letra 130 %)
   en docs/specs/evidencias/MOV-NNNN/.
5. Lista los archivos que tocaste. Si algo de "Fuera de alcance" tuvo que cambiar, PARA y pregunta.
```

**Para diseñar las pantallas de una spec:**

```
Diseña las pantallas de docs/specs/MOV-NNNN-<slug>.md con la skill Impeccable sobre el
Design System, siguiendo docs/plan-ui-impeccable.md (§3 y §4). Material 3 manda en la
estructura y la marca entra por el tema. Verifica con capturas del emulador por adb,
no en el navegador. Anota en la spec las decisiones de diseño antes de programar.
```

**Reglas para cualquier pedido:**
- **Test obligatorio:** "escribe un test que falle sin tu cambio y pase con él, y muéstrame la salida".
- **Alcance cerrado:** "no elimines ni reemplaces nada fuera de los archivos listados sin preguntar".
- **Dato extremo:** prueba con el caso límite de la spec, no solo con el demo feliz.
- **Versiones:** no subas librerías sin una spec propia (ver [`arquitectura.md`](arquitectura.md) §2.2).
