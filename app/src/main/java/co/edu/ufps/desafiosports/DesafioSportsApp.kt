package co.edu.ufps.desafiosports

import android.app.Application
import co.edu.ufps.desafiosports.di.AppContainer

/** Crea el contenedor de dependencias una sola vez, al abrir la app. */
class DesafioSportsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(AppContainer.configuracionDesdeBuild())
    }
}
