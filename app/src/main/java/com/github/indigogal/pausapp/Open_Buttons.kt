package com.github.indigogal.pausapp

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun OpenRachaButton(navController: NavController, nombre: String = "Usuario", numDias: Int = 12) {
    Button(
        onClick = {
            navController.navigate("racha/$nombre/$numDias")
        }
    ) {
        Text("Ver pantalla de Racha")
    }
}
