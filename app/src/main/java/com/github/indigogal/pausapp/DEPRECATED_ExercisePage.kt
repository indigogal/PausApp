package com.github.indigogal.pausapp

import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.material3.Player
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton

@OptIn(UnstableApi::class)
@Composable
fun ExercisePage() {
    val progress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Release the player when this composable leaves composition
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    // Diameter of the circular track drawn on the Canvas and the arc stroke thickness.
    // The Player is sized to the inner diameter of the ring (circleSize - strokeWidth)
    // so it fits inside the arcs, keeping a 1:1 aspect ratio.
    val circleSize = 276.dp
    val strokeWidth = 16.dp
    val remainingTime by remember {mutableStateOf("Empieza el Ejercicio!")}


            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 24.dp
                    ),
                    modifier = Modifier.padding(PaddingValues(36.dp)),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = remainingTime,
                            style = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.Center,
                            // Added the following line in hopes of not resizing card when switching to countdown
                            // No guarantees itll work tho
                            modifier = Modifier.width(IntrinsicSize.Max)

                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(circleSize)
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                val trackColor = Color(0xFFEADBFF)
                                val progressColor = Color(0xFF673AB7)
                                val strokePx = strokeWidth.toPx()
                                // Arc ring sized from the canvas bounds so it always matches the Player
                                val arcSize = size.minDimension - strokePx
                                val topLeftOffset = strokePx / 2
                                // Fondo circular deshabilitado/incompleto
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = Offset(topLeftOffset, topLeftOffset),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )

                                // Arco de avance activo
                                drawArc(
                                    color = progressColor,
                                    startAngle = 45f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    topLeft = Offset(topLeftOffset, topLeftOffset),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                            }
                            // Circular Player that fits inside the arc ring:
                            // same center as the Canvas, 1:1 aspect ratio, clipped to a circle
                            Player(
                                modifier = Modifier
                                    .size(circleSize - strokeWidth)
                                    .padding(20.dp)
                                    .clip(CircleShape)
                                ,
                                player = player
                            )
                        }
                        // Play/Pause button centered below the player and the arc canvas
                        PlayPauseButton(player)
                    }
                }
            }
        }
    }


@Preview(showBackground = true)
@Composable
private fun ExercisePagePreview() {
    ExercisePage()
}

