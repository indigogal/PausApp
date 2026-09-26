package com.github.indigogal.pausapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.material3.Player
import com.github.indigogal.pausapp.ui.theme.AppTheme
import com.github.indigogal.pausapp.viewmodel.ExerciseViewModel
import kotlinx.coroutines.delay

@Composable
fun ExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExerciseViewModel = viewModel()
) {
    val exerciseSet by viewModel.currentExerciseSet.collectAsState()
    val currentExercise = exerciseSet?.let { set ->
        val index = set.amountCompleted
        set.exercises.getOrNull(index)
    }

    val totalTimeSeconds = currentExercise?.durationSeconds ?: 60
    val totalTimeMs = totalTimeSeconds.toLong() * 1000L

    var timeRemainingMs by remember(totalTimeMs) { mutableLongStateOf(totalTimeMs) }
    var isRunning by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    val exerciseVideo = MediaItem.fromUri(currentExercise!!.assetPath)

    // Release the player when this composable leaves composition
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

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

    val progress = if (totalTimeMs > 0) {
        (totalTimeMs - timeRemainingMs).toFloat() / totalTimeMs
    } else {
        0f
    }

    val secondsLeft = (timeRemainingMs / 1000).toInt()
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format(LocalConfiguration.current.locales[0], "%02d:%02d", minutes, seconds)

    val exerciseTitle = currentExercise?.name ?: "Cargando ejercicio..."

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        ProgressTimerImage(
            progress = progress,
            size = 320.dp,
            player = player
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
                text = exerciseTitle,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    isRunning = !isRunning
                    player.play()
                          },
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
    progressColor: Color = Color(0xFF673AB7),
    player: Player
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
        Player(
            modifier = Modifier
                .size(size - strokeWidth)
                .padding(20.dp)
                .clip(CircleShape)
            ,
            player = player
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExerciseScreenPreview() {
    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ExerciseScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = TODO()
            )
        }
    }
}
