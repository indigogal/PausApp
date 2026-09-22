package com.github.indigogal.pausapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.indigogal.pausapp.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.util.Locale

class Exercise_Page : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Recuperación ultra-segura del Extra:
        // Evita el crash al convertir cualquier objeto enviado (incluyendo TextFieldState/TextFieldBuffer) a String
        val rawExtra = intent.extras?.get(RachaPage.EXTRA_NOMBRE)
        val nombre = if (rawExtra != null && rawExtra.toString().isNotBlank()) {
            rawExtra.toString()
        } else {
            "Usuario"
        }

        val numDias = intent.getIntExtra(RachaPage.EXTRA_NUM_DIAS, 0)

        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EPage(
                        nombre = nombre,
                        num_dias = numDias,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun EPage(nombre: String, num_dias: Int, modifier: Modifier = Modifier) {
    val totalTimeSeconds = 60
    var timeRemainingMs by remember { mutableLongStateOf(totalTimeSeconds * 1000L) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeRemainingMs) {
        if (isRunning && timeRemainingMs > 0) {
            delay(10)
            timeRemainingMs = (timeRemainingMs - 10).coerceAtLeast(0L)
        } else if (timeRemainingMs == 0L) {
            isRunning = false
        }
    }

    val progress = (totalTimeSeconds * 1000L - timeRemainingMs).toFloat() / (totalTimeSeconds * 1000L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Bienvenido de vuelta, $nombre!",
            fontSize = 22.sp,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        ProgressTimerImage(
            progress = progress,
            timeRemainingMs = timeRemainingMs,
            size = 220.dp
        )

        Text(
            text = "Tu Racha Actual es de $num_dias días",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isRunning) "Pausar" else "Iniciar")
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    timeRemainingMs = totalTimeSeconds * 1000L
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reiniciar")
            }
        }
    }
}

@Composable
fun ProgressTimerImage(
    progress: Float,
    timeRemainingMs: Long,
    size: Dp = 200.dp,
    strokeWidth: Dp = 16.dp,
    trackColor: Color = Color(0xFFEADBFF),
    progressColor: Color = Color(0xFF673AB7)
) {
    val secondsLeft = (timeRemainingMs / 1000).toInt()
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = size.toPx()
            val arcSize = canvasSize - strokePx
            val topLeftOffset = strokePx / 2

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Text(
            text = timeFormatted,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Exercise_PagePreview() {
    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            EPage(
                nombre = "Android",
                num_dias = 12,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}