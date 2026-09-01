package com.github.indigogal.pausapp

import android.R.attr.padding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.indigogal.pausapp.ui.theme.AppTheme

class RachaPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(){
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Page(
                        nombre = "Android",
                        num_dias=12,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Page(nombre: String, num_dias: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Bienvenido de vuelta $nombre!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        ProgressFireImage(
            progress = 0.45f, //porcentaje de 0 a 1.0
            size = 180.dp
        )
        Text(
            text = "Tu Racha Actual es de $num_dias dias",
            modifier = modifier
        )
        StreakCalendar(
            markedDays = (6..17).toSet(),
            currentDay = 18
        )
    }
}

@Composable
fun ProgressFireImage(
    progress: Float,
    size: Dp = 180.dp,
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

        // Imagen del fuego en el centro
        Image(
            painter = painterResource(id = R.drawable.fuego),
            contentDescription = "Fuego de racha",
            modifier = Modifier.size(size / 2.2f)
        )
    }
}

@Composable
fun StreakCalendar(
    markedDays: Set<Int>,
    currentDay: Int
) {
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val totalDaysInMonth = 31
    val startOffset = 5 // Espacios en blanco para alinear el primer día del mes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF3EDF7))
            .padding(16.dp)
    ) {
        // Cabecera del mes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "August 2025 ▼",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F)
            )
            Text(
                text = "<   >",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F)
            )
        }

        // Días de la semana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Matriz de días
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(250.dp),
            userScrollEnabled = false
        ) {
            items(startOffset) {
                Spacer(modifier = Modifier.aspectRatio(1f))
            }

            items(totalDaysInMonth) { index ->
                val day = index + 1
                val isMarked = day in markedDays
                val isToday = day == currentDay

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .then(
                            when {
                                isMarked -> Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF673AB7))
                                isToday -> Modifier
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF673AB7), CircleShape)
                                else -> Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 13.sp,
                        color = if (isMarked) Color.White else Color(0xFF1D1B20),
                        fontWeight = if (isMarked || isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RachaPagePreview() {
    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Page(
                nombre = "Android",
                num_dias = 12,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}


