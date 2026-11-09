---
id: MOV-0007
titulo: Crear torneo en tres pasos
tipo: feature
estado: borrador
prioridad: P1
fase: F3
modo: Creador
responsable: —
depende_de: [MOV-0004, backend "Crear torneo sin rol global" (§4.2), backend M4 (visibilidad y deporte, MOV-0005 §4.2)]
rama: mov-0007-crear-torneo
---

# MOV-0007 · Crear torneo en tres pasos

## 1. Contexto

- **Manda el organizador** (`PRODUCT.md`): crear un torneo tiene que caber en minutos. Lo demás se configura después, en la sección Creador.
- **Referencia:** prototipo, pantallas `crear` (3 pasos) y `creado`.

## 2. Alcance

- **Se entra** desde el botón flotante "Crear torneo" de Inicio o desde la sección Creador.
- **Paso 1, deporte:** las opciones vienen del catálogo (`GET /api/public/deportes`) y cada una dice si es individual o por equipos.
- **Paso 2, nombre y formato:**
  - nombre (obligatorio, mínimo 3 caracteres);
  - formato: Liga, Eliminación directa o Grupos y eliminatorias, cada uno con una línea que lo explica;
  - cupos máximos (2 a 64);
  - fecha de inicio.
- **Paso 3, visibilidad:** Público, Solo con enlace o Privado, cada uno con su explicación. Además, el interruptor "Cobrar inscripción" con el valor de la cuota.
- **Torneo creado:** "Tu torneo está listo". Si es público, avisa que ya aparece en Explorar. Botones "Compartir enlace" e "Ir al torneo"; "Ir al torneo" abre el detalle con la franja de Creador.
- **Cerrar (X)** en cualquier paso descarta el borrador. Si ya escribió algo, pide confirmarlo con un diálogo.

## 3. Diseño

- **Prototipo** `crear` y `creado`: barra con X y "Paso n de 3", `LinearProgressIndicator`, opciones como tarjetas seleccionables y una barra inferior fija con "Atrás" y "Siguiente".
- **Champions, premios, multas y árbitros** van en la configuración del torneo (sección Creador), no aquí.

## 4. Contrato con el backend

### 4.1 Lo que ya existe

- `POST /api/supercopa/admin/torneos`: **exige el rol global `ADMINISTRADOR`** y no tiene `deporte` ni `visibilidad`.

### 4.2 Lo que falta (backend, spec propia en `CODECUP/.context/specs/`; base: backend 0024)

1. **Cualquier cuenta registrada crea un torneo** y queda como su administrador: inserta `torneo_roles(torneo_id, cuenta_id, ADMIN)`.
2. **`POST /api/supercopa/torneos`** (o la misma ruta, autorizada sin el rol global):
   ```json
   { "nombre": "Relámpago de Tenis de Mesa", "deporte": "TENIS_MESA", "formato": "ELIMINACION_DIRECTA",
     "cupos": 16, "fechaInicio": "2026-11-21", "visibilidad": "PUBLICO",
     "cobraInscripcion": true, "valorInscripcion": 5000 }
   ```
   - Responde 201 con `{ "id", "nombre", "visibilidad", "enlace": "https://…/t/{enlaceToken}" }`.
   - El enlace existe siempre: para un torneo público también sirve para compartir.
   - El torneo nace en estado de inscripciones abiertas, con la configuración por defecto del formato.
3. **Validaciones y `code` de error (400/409):** `NOMBRE_INVALIDO`, `DEPORTE_INEXISTENTE`, `FORMATO_NO_SOPORTADO_POR_DEPORTE` (si aplica), `CUPOS_FUERA_DE_RANGO` y `FECHA_INICIO_PASADA`. Todos con su `mensaje` en español, que la app muestra tal cual.
4. **Deportes individuales** (tenis de mesa): la inscripción es por persona, con `tipo_competencia = INDIVIDUAL` en el catálogo. En esta spec solo se guarda el deporte; la inscripción individual va en su propia spec.

## 5. Criterios de aceptación (borrador)

1. **CA-1** · "Siguiente" solo se habilita con el paso completo: deporte elegido; nombre válido y formato; visibilidad elegida.
2. **CA-2** · Al crear se envía el `POST` con todos los campos, y la app muestra "Tu torneo está listo" con el enlace.
3. **CA-3** · Un error del backend se ve como aviso con su `mensaje`, y la persona sigue en el paso correspondiente sin perder lo escrito.
4. **CA-4** · El torneo creado aparece en "Tus secciones → Creador" y, si es público, en Explorar.
5. **CA-5** · Cerrar con datos escritos pide confirmación. Atrás vuelve al paso anterior.
6. **CA-6** · Claro, oscuro y letra al 130 % (emulador).

## 6. Fuera de alcance

- La configuración avanzada: Champions, premios, multas, árbitros, fixture y reglas.

## 7. Dudas para David

- **Q1.** **Visibilidad por defecto** en el paso 3: el prototipo usa "Público"; el plan del backend decía "Solo con enlace".
- **Q2.** Deportes de la primera versión: ¿fútbol sala, voleibol, baloncesto y tenis de mesa?

## 8. Registro

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Borrador para revisión del usuario |
