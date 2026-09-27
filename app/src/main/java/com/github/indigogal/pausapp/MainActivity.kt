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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.indigogal.pausapp.data.AppDatabase
import com.github.indigogal.pausapp.ui.theme.AppTheme
import com.github.indigogal.pausapp.viewmodel.ExerciseViewModel
import com.github.indigogal.pausapp.viewmodel.UserViewModel
import com.github.indigogal.pausapp.viewmodel.UserViewModelFactory

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

                val database = AppDatabase.getInstance(applicationContext)
                val userDao = database.getUserDAO()

                val factory = UserViewModelFactory(userDao)
                val userViewModel: UserViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

                val user by userViewModel.user.collectAsState()

                // If a registered user (other than temp user uid = 0) exists, skip registration screen
                LaunchedEffect(user.isRegistered) {
                    if (user.isRegistered) {
                        navController.navigate("racha") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "register"
                        ) {
                            composable("register") {
                                RegisterForm(navController, userViewModel)
                            }
                            composable("exercise") {
                                ExerciseScreen(
                                    viewModel = exerciseViewModel
                                )
                            }
                            composable("racha") {
                                RachaScreen(userViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
