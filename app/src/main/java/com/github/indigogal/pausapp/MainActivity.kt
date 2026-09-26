package com.github.indigogal.pausapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.indigogal.pausapp.data.ExerciseRepository
import com.github.indigogal.pausapp.ui.theme.AppTheme
import com.github.indigogal.pausapp.viewmodel.ExerciseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val exerciseViewModel: ExerciseViewModel = viewModel()
                LaunchedEffect(Unit) {
                    exerciseViewModel.loadRandomExerciseSet()
                }

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
                                route = "exercise",
                            ) { backStackEntry ->
                                ExerciseScreen(
                                    viewModel = exerciseViewModel
                                )
                            }
                            composable(
                                route = "racha",
                                arguments = listOf(
                                    navArgument("nombre") { type = NavType.StringType },
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
