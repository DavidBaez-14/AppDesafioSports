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
| [MOV-0004](MOV-0004-inicio-y-perfil.md) | Inicio (jugador), navegación principal y Perfil | hecha | F1 | David | MOV-0003, MOV-0005, MOV-0010 · backend 0026 y 0029 | `mov-0004-inicio-y-perfil` |
| [MOV-0005](MOV-0005-explorar-y-torneo.md) | Explorar torneos públicos y detalle del torneo | hecha | F4 | equipo | MOV-0002 · backend 0029 | `mov-0005-explorar-y-torneo` |
| [MOV-0007](MOV-0007-crear-torneo.md) | Crear torneo en tres pasos | hecha | F3 | David | MOV-0003 · backend 0024 y 0029 | `mov-0007-crear-torneo` |
| [MOV-0008](MOV-0008-seccion-creador.md) | Sección Creador, mis torneos y por revisar | hecha | F3 | David | MOV-0007 · backend 0024 y 0029 | `mov-0007-crear-torneo` |
| [MOV-0009](MOV-0009-seccion-arbitro-y-planilla.md) | Sección Árbitro y planilla del partido | hecha | F2 | David | MOV-0003 · backend 0025 y 0029 | `mov-0009-seccion-arbitro` |
| [MOV-0010](MOV-0010-diseno-secciones.md) | Diseño de las secciones Árbitro y Creador, e Inicio provisional | hecha | F2 | David | MOV-0007, MOV-0008, MOV-0009 | `mov-0010-diseno-secciones` |
| [MOV-0011](MOV-0011-arbitros-y-partidos.md) | Gestión del torneo, árbitros y partidos automáticos | hecha | F3 | David | MOV-0004, MOV-0008, MOV-0009 · backend 0024 y 0025 | `mov-0011-arbitros-y-partidos` |
| [MOV-0012](MOV-0012-revision-arbitraje.md) | Invitaciones recibidas, planilla solo con alineados y planilla del organizador | hecha | F3 | David | MOV-0009, MOV-0011 · backend 0030 | `mov-0012-revision-arbitraje` |
| [MOV-0013](MOV-0013-programar-partidos.md) | Programar los partidos (fecha, hora y lugar) y volver a generarlos | hecha | F3 | David | MOV-0011, MOV-0012 · backend 0025 | `mov-0013-programar-partidos` |
| [MOV-0014](MOV-0014-inscribir-equipo.md) | Inscribir un equipo en un torneo | hecha | F1 | David | MOV-0004, MOV-0005 · backend 0026 y 0029 | `mov-0014-inscribir-equipo` |
| [MOV-0015](MOV-0015-plantel-delegado.md) | Plantel del equipo para su delegado | hecha | F1 | David | MOV-0005, MOV-0014 · backend 0018, 0022, 0023 y 0026 | `mov-0015-plantel-delegado` |

- **MOV-0001 a MOV-0015 están en `main`** (23-nov-2026), junto con el **backend 0030**. MOV-0003 sigue `en-revision` solo porque falta probar el registro real con un correo propio.
- **Pendiente en el emulador** (cubierto por pruebas, ver el registro de cada spec): generar partidos y asignar árbitros (MOV-0011); invitaciones recibidas, expulsados y reabrir (MOV-0012); programar partidos (MOV-0013); enviar una inscripción (MOV-0014); y agregar, cambiar números y quitar en el plantel (MOV-0015). Todo eso se prueba en un solo recorrido: inscribir dos equipos, aprobarlos, armar los planteles, generar y programar los partidos, y arbitrar uno.
- **Sin comprobantes** (MOV-0014): la cuota se informa y la inscripción queda pendiente.
- **MOV-0005, 0007 y 0008 se pusieron al día el 16-nov:** sus specs eran borradores anteriores al backend real y ahora describen lo que hace el código.
- **Pendiente de MOV-0009:** medir en la app si pasar lista sigue lento; si se confirma, va como spec de bug.

## Próximas (por redactar)

Salen del plan de UI ([`../plan-ui-impeccable.md`](../plan-ui-impeccable.md) §5) y de la estructura de roles decidida el 8-nov: todos entran como jugador y Creador y Árbitro son secciones ([`../../PRODUCT.md`](../../PRODUCT.md), prototipo en [`../prototipo-roles/`](../prototipo-roles/index.html)). Los números se fijan al redactar cada una.

| Título | Depende de |
|---|---|
| Inscribirme en un torneo individual (el backend aún responde `TORNEO_INDIVIDUAL`) | backend |
| Delegado: solicitudes de ingreso al equipo y "¿Este eres tú?" | MOV-0015 · backend (solicitudes y 0023) |
| Comprobantes (inscripción y multa) y mis multas | backend: subida de multas por el jugador |
| Configuración del torneo (formato, cuota, premios y multas) | MOV-0008 |
