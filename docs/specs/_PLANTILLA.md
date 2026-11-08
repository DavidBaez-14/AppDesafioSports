---
id: MOV-NNNN
titulo: <título imperativo y corto>
tipo: feature            # feature | bugfix | refactor | chore | diseño
estado: borrador         # borrador → aprobada → en-curso → en-revision → hecha | bloqueada | descartada
prioridad: P2            # P0 crítico · P1 alto · P2 medio · P3 bajo
fase: F0                 # F0 acceso · F1 Jugador · F2 Árbitro · F3 Creador · F4 Visitante · F5 Compartir · Base
modo: Transversal        # Visitante | Jugador | Árbitro | Creador | Transversal
responsable: —
depende_de: []           # p. ej. [backend 0018, MOV-0002]
rama: mov-NNNN-<slug>
---

# MOV-NNNN · <título>

## 1. Contexto

<Qué necesita el usuario y por qué. Si es un bug: el síntoma observable y la causa (`archivo:línea`).>

## 2. Alcance

<Pantallas y flujos que entran. Una spec es un cambio cohesivo.>

- …

## 3. Diseño

> Solo si hay pantalla. Flujo en `docs/plan-ui-impeccable.md` §4.

- **Referencia:** <Impeccable (shape/critique), tokens del Design System, nodo de Figma si hay>
- **Estados de cada pantalla:**

| Pantalla | Cargando | Vacío | Error | Sin conexión | Éxito |
|---|---|---|---|---|---|
| … | … | … | … | … | … |

- **Textos:** <los textos visibles: títulos, botones, mensajes. Van a strings.xml.>
- **Accesibilidad:** objetivos de 48 dp como mínimo, contraste AA, letra al 130 % sin cortes, etiquetas para TalkBack y el botón Atrás del sistema funcionando.

## 4. Contrato con el backend

| Endpoint | Método | Envía | Responde | Errores esperados → AppError |
|---|---|---|---|---|
| `/api/...` | GET | — | `…Dto` | 404 `CODIGO` → `AppError.…` |

<Si un endpoint no existe: esta spec queda `bloqueada` y se pide la spec del backend en `.context/specs`.>

## 5. Criterios de aceptación

1. **CA-1** · **Dado** … **cuando** … **entonces** …
2. **CA-2** · **Dado** … **cuando** … **entonces** …

## 6. Fuera de alcance / qué NO debe cambiar

- …

## 7. Plan de pruebas

| Criterio | Prueba (clase › método) o verificación | Nivel |
|---|---|---|
| CA-1 | `XxxViewModelTest › CA-1 …` | ViewModel |
| CA-2 | `XxxRepositoryImplTest › CA-2 …` | Red (MockWebServer) |

## 8. Verificación en el emulador

1. …
2. Capturas a guardar en `docs/specs/evidencias/MOV-NNNN/`: claro, oscuro, letra al 130 % y el caso extremo.

## 9. Archivos afectados

<Lista cerrada. Nada fuera de aquí sin actualizar la spec.>

- `app/src/main/java/co/edu/ufps/desafiosports/...`

## 10. Datos de prueba

- Usuarios del seed: `jugador01@prueba.local`… (contraseña en `SEED_PASSWORD` del `.env` del backend).
- Casos extremos: …

## 11. Notas y decisiones

- …

## 12. Registro

| Fecha | Cambio |
|---|---|
| AAAA-MM-DD | Borrador inicial |
