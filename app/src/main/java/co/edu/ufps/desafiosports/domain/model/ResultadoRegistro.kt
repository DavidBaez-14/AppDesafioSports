package co.edu.ufps.desafiosports.domain.model

/** Qué pasó al crear la cuenta en Supabase (MOV-0003). */
enum class ResultadoRegistro {
    /** Supabase exige confirmar el correo: no hay sesión todavía. */
    CONFIRMAR_CORREO,

    /** Supabase no exige confirmación y dejó la sesión abierta: sigue completar registro. */
    SESION_ACTIVA,
}
