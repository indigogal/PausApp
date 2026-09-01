package com.github.indigogal.pausapp

import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun OpenRachaButton() {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(context, RachaPage::class.java)
            context.startActivity(intent)
        }
    ) {
        Text("Ver pantalla de Racha")
    }
}