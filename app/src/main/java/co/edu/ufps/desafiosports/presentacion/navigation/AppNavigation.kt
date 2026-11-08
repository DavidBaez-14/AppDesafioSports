package co.edu.ufps.desafiosports.presentacion.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.ufps.desafiosports.presentacion.diagnostico.DiagnosticoScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Diagnostico) {
        composable<Diagnostico> { DiagnosticoScreen() }
    }
}
