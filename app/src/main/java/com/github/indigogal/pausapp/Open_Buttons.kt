// Archivo: Open_Buttons.kt
package com.github.indigogal.pausapp

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun OpenRachaButton(
    navController: NavController,
    nombre: String = "Usuario",
    numDias: Int = 12,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            navController.navigate("racha/$nombre/$numDias")
        },
        modifier = modifier
    ) {
        Text("Ver pantalla de Racha")
    }
}

@Composable
fun OpenExerciseButton(
    navController: NavController,
    nombre: String = "Usuario",
    numDias: Int = 12,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            navController.navigate("exercise/$nombre/$numDias")
        },
        modifier = modifier
    ) {
        Text("Ir a Ejercicios")
    }
}
