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
import com.github.indigogal.pausapp.ui.theme.AppTypography
import com.github.indigogal.pausapp.viewmodel.ExerciseViewModel
import com.github.indigogal.pausapp.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel = viewModel(),
    userViewModel: UserViewModel? = null,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val exerciseSet by viewModel.currentExerciseSet.collectAsState()
    val totalExercises = exerciseSet?.exercises?.size ?: 0
    val currentIndex = exerciseSet?.amountCompleted ?: 0
    val currentExercise = exerciseSet?.let { set ->
        set.exercises.getOrNull(currentIndex)
    }

    // Each exercise runs for a fixed 30 seconds regardless of the video length;
    // the video loops (REPEAT_MODE_ONE) underneath until the timer finishes.
    val totalTimeSeconds = 20
    val totalTimeMs = totalTimeSeconds.toLong() * 1000L

    var timeRemainingMs by remember(currentExercise) { mutableLongStateOf(totalTimeMs) }
    var isRunning by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val player = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(currentExercise) {
        currentExercise?.let { exercise ->
            val uriString = if (exercise.assetPath.startsWith("asset:///")) {
                exercise.assetPath
            } else {
                "asset:///${exercise.assetPath}"
            }
            player.setMediaItem(MediaItem.fromUri(uriString))
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.prepare()
        }
    }

    // Release the player when this composable leaves composition
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            player.play()
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
                player.pause()
            }
        } else {
            player.pause()
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
    val stepText = if (totalExercises > 0) "Ejercicio ${currentIndex + 1} de $totalExercises" else ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (stepText.isNotEmpty()) {
            Text(
                text = stepText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

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
                style = AppTypography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = exerciseTitle,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
        }

        if (timeRemainingMs == 0L && currentExercise != null) {
            Button(
                onClick = {
                    val finishedAll = viewModel.completeCurrentExercise()
                    if (finishedAll || currentIndex >= totalExercises - 1) {
                        userViewModel?.completeRoutine()
                        onNavigateBack()
                    } else {
                        isRunning = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = if (currentIndex >= totalExercises - 1) "Finalizar Rutina" else "Siguiente Ejercicio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isRunning = !isRunning
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRunning) "Pausar" else "Iniciar")
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        timeRemainingMs = totalTimeMs
                        player.seekTo(0)
                        player.pause()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reiniciar")
                }
            }
        }
    }
}

@Composable
fun ProgressTimerImage(
    progress: Float,
    size: Dp = 200.dp,
    strokeWidth: Dp = 16.dp,
    trackColor: Color? = null,
    progressColor: Color? = null,
    player: Player
) {
    // Colors come exclusively from the AppTheme color scheme
    val resolvedTrackColor = trackColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val resolvedProgressColor = progressColor ?: MaterialTheme.colorScheme.primary

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
                color = resolvedTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            drawArc(
                color = resolvedProgressColor,
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
                .clip(CircleShape),
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
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
