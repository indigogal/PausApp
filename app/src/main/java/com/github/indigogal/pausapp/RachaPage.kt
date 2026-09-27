package com.github.indigogal.pausapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.github.indigogal.pausapp.data.User
import com.github.indigogal.pausapp.ui.theme.AppTheme
import com.github.indigogal.pausapp.ui.theme.AppTypography
import com.github.indigogal.pausapp.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

fun calculateStreakDays(
    streakStart: LocalDate,
    streakEnd: LocalDate,
    today: LocalDate = LocalDate.now()
): Int {
    if (streakEnd.isBefore(streakStart)) {
        return 0
    }
    // If today is strictly after streakEnd + 1 day, the streak was broken
    if (today.isAfter(streakEnd.plusDays(1))) {
        return 0
    }
    return (ChronoUnit.DAYS.between(streakStart, streakEnd) + 1).toInt()
}

@Composable
fun RachaScreen(
    userVM: UserViewModel,
    modifier: Modifier = Modifier,
    onStartRoutine: () -> Unit = {}
) {
    val user by userVM.user.collectAsState()
    RachaScreen(
        user = user,
        modifier = modifier,
        onStartRoutine = onStartRoutine
    )
}

@Composable
fun RachaScreen(
    user: User,
    modifier: Modifier = Modifier,
    onStartRoutine: () -> Unit = {}
) {
    val today = LocalDate.now()
    val streakDays = calculateStreakDays(user.streakStart, user.streakEnd, today)
    val isCompletedToday = user.streakEnd == today && streakDays > 0
    // Relate progress percentage to current streak, 0.1 per day (capped between 0 and 1)
    val progress = (streakDays * 0.1f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Bienvenido de vuelta ${user.name}!",
            fontSize = 22.sp,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        ProgressFireImage(
            progress = progress,
            size = 180.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tu Racha Actual es de $streakDays ${if (streakDays == 1) "día" else "dias"}",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            if (isCompletedToday) {
                Text(
                    text = "¡Excelente! Ya completaste tu rutina de hoy",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Button(
            onClick = onStartRoutine
        ) {
            Text(
                text = if (isCompletedToday) "Repetir rutina" else "Iniciar rutina",
                style = AppTypography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
        StreakCalendar(
            streakStart = user.streakStart,
            streakEnd = user.streakEnd,
            currentDate = today
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
                startAngle = -90f,
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
    streakStart: LocalDate,
    streakEnd: LocalDate,
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now()
) {
    val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S")
    val yearMonth = YearMonth.from(currentDate)
    val totalDaysInMonth = yearMonth.lengthOfMonth()

    // Calculate starting offset for Sunday-first calendar grid (D, L, M, M, J, V, S)
    val firstDayOfMonth = currentDate.withDayOfMonth(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    val monthName = currentDate.month
        .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es")) else it.toString() }

    Column(
        modifier = modifier
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
                text = "$monthName ${currentDate.year}",
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
            modifier = Modifier.height(260.dp),
            userScrollEnabled = false
        ) {
            items(startOffset) {
                Spacer(modifier = Modifier.aspectRatio(1f))
            }

            items(totalDaysInMonth) { index ->
                val day = index + 1
                val dayDate = currentDate.withDayOfMonth(day)
                val isStreakValid = !currentDate.isAfter(streakEnd.plusDays(1))
                val isMarked = isStreakValid && !dayDate.isBefore(streakStart) && !dayDate.isAfter(streakEnd)
                val isToday = (day == currentDate.dayOfMonth)

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
fun RachaScreenPreview() {
    val sampleUser = User(
        uid = 1,
        name = "Usuario Demo",
        streakStart = LocalDate.now().minusDays(5),
        streakEnd = LocalDate.now(),
        reminderTime = LocalTime.of(17, 38)
    )

    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            RachaScreen(user = sampleUser, modifier = Modifier.padding(innerPadding))
        }
    }
}
