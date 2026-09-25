package com.github.indigogal.pausapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.indigogal.pausapp.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "register"
                        ) {
                            composable("register") {
                                RegisterForm(navController = navController)
                            }
                            composable(
                                route = "exercise/{nombre}/{numDias}",
                                arguments = listOf(
                                    navArgument("nombre") { type = NavType.StringType },
                                    navArgument("numDias") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val nombre = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
                                val numDias = backStackEntry.arguments?.getInt("numDias") ?: 12
                                ExerciseScreen(
                                    nombre = nombre,
                                    numDias = numDias
                                )
                            }
                            composable(
                                route = "racha/{nombre}/{numDias}",
                                arguments = listOf(
                                    navArgument("nombre") { type = NavType.StringType },
                                    navArgument("numDias") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val nombre = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
                                val numDias = backStackEntry.arguments?.getInt("numDias") ?: 12
                                RachaScreen(
                                    nombre = nombre,
                                    numDias = numDias
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
