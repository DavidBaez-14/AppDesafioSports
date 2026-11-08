# Índice de specs · Desafío Sports

> Cómo se trabaja: [`../sdd.md`](../sdd.md). Plantilla: [`_PLANTILLA.md`](_PLANTILLA.md).
> Cada spec es `MOV-NNNN-<slug>.md`. Su evidencia (capturas, salidas) va en `evidencias/MOV-NNNN/`.
> **Mantén esta tabla al día:** es lo primero que se mira para saber qué está libre, qué está en curso y qué falta.

## Specs

| ID | Título | Estado | Fase | Responsable | Depende de | Rama |
|---|---|---|---|---|---|---|
| [MOV-0001](MOV-0001-estructura-base.md) | Estructura base y diagnóstico de conexión | en-revision | Base | David | backend `/api/auth/me` y `/actuator/health` | `mov-0001-estructura-base` |

## Próximas (por redactar)

Salen del plan de UI ([`../plan-ui-impeccable.md`](../plan-ui-impeccable.md) §5). Los números son tentativos: se fijan al redactar cada una.

| ID tentativo | Título | Fase | Depende de |
|---|---|---|---|
| MOV-0002 | Fundaciones de diseño: PRODUCT.md, identidad provisional, tema y tipografía | Base | Respuestas del plan de UI (§9) |
| MOV-0003 | Componentes base y pantallas de calibración (clasificación y planilla con datos de ejemplo) | Base | MOV-0002 |
| MOV-0004 | Acceso: arranque, login, registro en 2 pasos, revisa tu correo, recuperar contraseña | F0 | MOV-0003 |
| MOV-0005 | Completar registro, cuenta bloqueada o pendiente, elegir y cambiar modo, estructura de navegación por modo | F0 | MOV-0004 |
| MOV-0006 | Google nativo (Credential Manager → Supabase) | F0 | Cliente OAuth Android en GCP |
| MOV-0007 | Modo Jugador: mi perfil, historial y perfil deportivo | F1 | MOV-0005 |
| MOV-0008 | Modo Jugador: unirme a un equipo y mis solicitudes | F1 | MOV-0005 |
| MOV-0009 | Delegado: plantel, solicitudes de ingreso e inscripciones | F1 | MOV-0005 |
| MOV-0010 | Comprobantes (inscripción y multa) y mis multas | F1 | MOV-0009 · backend: subida de multas por el jugador |
