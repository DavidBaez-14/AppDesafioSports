package co.edu.ufps.desafiosports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.ufps.desafiosports.presentacion.navigation.AppNavigation
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/**
 * Única Activity de la app. Solo aplica el tema y monta la navegación.
 *
 * Los ViewModel no se crean aquí: cada pantalla los pide con viewModel(factory = XxxViewModel.Factory),
 * así sobreviven al girar el teléfono.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DesafioSportsTheme {
                AppNavigation()
            }
        }
    }
}
