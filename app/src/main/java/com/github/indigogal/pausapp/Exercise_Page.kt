package com.github.indigogal.pausapp

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

@Composable
fun ExerciseScreen(nombre: String, numDias: Int, modifier: Modifier = Modifier) {
    val totalTimeSeconds = 60
    val totalTimeMs = totalTimeSeconds * 1000L

    var timeRemainingMs by remember { mutableLongStateOf(totalTimeMs) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            var lastTime = System.currentTimeMillis()
            while (isRunning && timeRemainingMs > 0) {
                delay(16)
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastTime
                lastTime = currentTime

                timeRemainingMs = (timeRemainingMs - deltaTime).coerceAtLeast(0L)
            }
            if (timeRemainingMs == 0L) {
                isRunning = false
            }
        }
    }

    val progress = (totalTimeMs - timeRemainingMs).toFloat() / totalTimeMs

    val secondsLeft = (timeRemainingMs / 1000).toInt()
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Empezemos, $nombre!!!",
            fontSize = 22.sp,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        ProgressTimerImage(
            progress = progress,
            size = 220.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = timeFormatted,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )

            Text(
                text = "Nombre de ejercicio Text Sample",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
        }

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
                    timeRemainingMs = totalTimeMs
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
    size: Dp = 200.dp,
    strokeWidth: Dp = 16.dp,
    trackColor: Color = Color(0xFFEADBFF),
    progressColor: Color = Color(0xFF673AB7)
) {
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExerciseScreenPreview() {
    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ExerciseScreen(
                nombre = "Android",
                numDias = 12,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
