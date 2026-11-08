package co.edu.ufps.desafiosports.domain.model

/** Configuración del entorno, leída de local.properties (vía BuildConfig) en di/. */
data class Configuracion(
    val supabaseUrl: String,
    val supabaseClavePublicable: String,
    val apiBaseUrl: String,
) {
    fun faltantes(): List<String> = buildList {
        if (supabaseUrl.isBlank()) add("SUPABASE_URL")
        if (supabaseClavePublicable.isBlank()) add("SUPABASE_PUBLISHABLE_KEY")
        if (apiBaseUrl.isBlank()) add("API_BASE_URL")
    }

    val completa: Boolean get() = faltantes().isEmpty()
}
