# Índice de specs · Desafío Sports

> Cómo se trabaja: [`../sdd.md`](../sdd.md). Plantilla: [`_PLANTILLA.md`](_PLANTILLA.md).
> Cada spec es `MOV-NNNN-<slug>.md`. Su evidencia (capturas, salidas) va en `evidencias/MOV-NNNN/`.
> **Mantén esta tabla al día:** es lo primero que se mira para saber qué está libre, qué está en curso y qué falta.

## Specs

| ID | Título | Estado | Fase | Responsable | Depende de | Rama |
|---|---|---|---|---|---|---|
| [MOV-0001](MOV-0001-estructura-base.md) | Estructura base y diagnóstico de conexión | en-revision | Base | David | backend `/api/auth/me` y `/actuator/health` | `mov-0001-estructura-base` |
| [MOV-0002](MOV-0002-login.md) | Tema de marca, arranque e inicio de sesión con correo | en-revision | F0 | David | MOV-0001 | `mov-0002-login` |
| [MOV-0003](MOV-0003-crear-cuenta.md) | Crear cuenta, revisa tu correo y completar registro | en-revision | F0 | David | MOV-0002 | `mov-0003-crear-cuenta` |
| [MOV-0006](MOV-0006-google.md) | Continuar con Google (Credential Manager → Supabase) | en-revision | F0 | David | MOV-0003 · cliente OAuth Android | `mov-0006-google` |

## Próximas (por redactar)

Salen del plan de UI ([`../plan-ui-impeccable.md`](../plan-ui-impeccable.md) §5) y de la estructura de roles decidida el 8-nov: todos entran como jugador y Creador y Árbitro son secciones ([`../../PRODUCT.md`](../../PRODUCT.md), prototipo en [`../prototipo-roles/`](../prototipo-roles/index.html)). Los números son tentativos: se fijan al redactar cada una.

| ID tentativo | Título | Fase | Depende de |
|---|---|---|---|
| MOV-0004 | Inicio (jugador), secciones Creador y Árbitro y navegación, con sus estados vacíos; cuenta bloqueada | F0 | MOV-0002 · backend M3 para los datos de las secciones |
| MOV-0005 | Componentes base y pantallas de calibración (clasificación y planilla con datos de ejemplo) | Base | MOV-0002 |
| MOV-0007 | Jugador: mi perfil, historial y perfil deportivo | F1 | MOV-0004 |
| MOV-0008 | Jugador: unirme a un equipo y mis solicitudes | F1 | MOV-0004 |
| MOV-0009 | Delegado: plantel, solicitudes de ingreso e inscripciones | F1 | MOV-0004 |
| MOV-0010 | Comprobantes (inscripción y multa) y mis multas | F1 | MOV-0009 · backend: subida de multas por el jugador |
