# Índice de specs · Desafío Sports

> Cómo se trabaja: [`../sdd.md`](../sdd.md). Plantilla: [`_PLANTILLA.md`](_PLANTILLA.md).
> Cada spec es `MOV-NNNN-<slug>.md`. Su evidencia (capturas, salidas) va en `evidencias/MOV-NNNN/`.
> **Mantén esta tabla al día:** es lo primero que se mira para saber qué está libre, qué está en curso y qué falta.

## Specs

| ID | Título | Estado | Fase | Responsable | Depende de | Rama |
|---|---|---|---|---|---|---|
| [MOV-0001](MOV-0001-estructura-base.md) | Estructura base y diagnóstico de conexión | hecha | Base | David | backend `/api/auth/me` y `/actuator/health` | `mov-0001-estructura-base` |
| [MOV-0002](MOV-0002-login.md) | Tema de marca, arranque e inicio de sesión con correo | hecha | F0 | David | MOV-0001 | `mov-0002-login` |
| [MOV-0003](MOV-0003-crear-cuenta.md) | Crear cuenta, revisa tu correo y completar registro | en-revision | F0 | David | MOV-0002 | `mov-0003-crear-cuenta` |
| [MOV-0006](MOV-0006-google.md) | Continuar con Google (Credential Manager → Supabase) | hecha | F0 | David | MOV-0003 · cliente OAuth Android | `mov-0006-google` |
| [MOV-0004](MOV-0004-inicio-y-perfil.md) | Inicio (jugador), navegación principal y Perfil | borrador | F1 | — | MOV-0003 · backend 0026 y 0029 (ya en `main`) | `mov-0004-inicio-y-perfil` |
| [MOV-0005](MOV-0005-explorar-y-torneo.md) | Explorar torneos públicos y detalle del torneo | borrador | F4 | — | MOV-0004 · backend M4: visibilidad, deporte y `/api/public` | `mov-0005-explorar-y-torneo` |
| [MOV-0007](MOV-0007-crear-torneo.md) | Crear torneo en tres pasos | borrador | F3 | — | MOV-0004 · backend 0024 y M4 | `mov-0007-crear-torneo` |
| [MOV-0008](MOV-0008-seccion-creador.md) | Sección Creador, mis torneos y por revisar | borrador | F3 | — | MOV-0004, MOV-0005 · backend 0024 | `mov-0008-seccion-creador` |
| [MOV-0009](MOV-0009-seccion-arbitro-y-planilla.md) | Sección Árbitro y planilla del partido | borrador | F2 | — | MOV-0004 · backend 0025 | `mov-0009-seccion-arbitro` |
| [MOV-0010](MOV-0010-diseno-secciones.md) | Diseño de las secciones Árbitro y Creador, e Inicio provisional | en-revision | F2 | David | MOV-0007, MOV-0008, MOV-0009 | `mov-0010-diseno-secciones` |

- **MOV-0004 ya está reescrita con el contrato real del backend** y fija los nombres de las rutas que usan las demás (su §11.1).
- **MOV-0005, 0007, 0008 y 0009 siguen como borradores viejos:** su §4 dice "lo que falta" en el backend, pero ese backend ya existe (specs 0024 a 0029 en `main` de BackendCodeCup) y con rutas distintas a las propuestas. Se reescriben antes de implementarlas.

## Próximas (por redactar)

Salen del plan de UI ([`../plan-ui-impeccable.md`](../plan-ui-impeccable.md) §5) y de la estructura de roles decidida el 8-nov: todos entran como jugador y Creador y Árbitro son secciones ([`../../PRODUCT.md`](../../PRODUCT.md), prototipo en [`../prototipo-roles/`](../prototipo-roles/index.html)). Los números se fijan al redactar cada una.

| Título | Depende de |
|---|---|
| Inscribirme o inscribir un equipo (individual y por equipos) | MOV-0005 |
| Delegado: plantel, solicitudes de ingreso e inscripciones | MOV-0004 |
| Comprobantes (inscripción y multa) y mis multas | backend: subida de multas por el jugador |
| Configuración del torneo (formato, cuota, premios, multas y árbitros) | MOV-0008 |
