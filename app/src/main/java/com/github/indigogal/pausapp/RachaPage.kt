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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import kotlin.math.roundToInt

// Compact day-cell size so the calendar fits on smaller screens
private val CalendarDayCellSize = 36.dp

// Number of rows the calendar grid needs for the current month
private fun calendarGridRows(startOffset: Int, totalDays: Int): Int =
    (startOffset + totalDays + 6) / 7

fun calculateStreakDays(
    streakStart: LocalDate?,
    streakEnd: LocalDate?,
    today: LocalDate = LocalDate.now()
): Int {
    // A user who has never completed a routine has no active streak
    if (streakStart == null || streakEnd == null) {
        return 0
    }
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
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(
            Modifier.height(12.dp)
        )
        Button(
            onClick = onStartRoutine
        ) {
            Text(
                text = if (isCompletedToday) "Repetir rutina" else "Iniciar rutina",
                style = AppTypography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(
            Modifier.height(12.dp)
        )
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
    trackColor: Color? = null,
    progressColor: Color? = null
) {
    // Colors come exclusively from the AppTheme color scheme
    val resolvedTrackColor = trackColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val resolvedProgressColor = progressColor ?: MaterialTheme.colorScheme.primary

    val progressPercent = (progress * 100).roundToInt()

    Box(
        modifier = Modifier
            .size(size)
            .semantics(mergeDescendants = true) {
                contentDescription = "Fuego de racha, progreso del $progressPercent por ciento"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = size.toPx()
            val arcSize = canvasSize - strokePx
            val topLeftOffset = strokePx / 2

            // Fondo circular deshabilitado/incompleto
            drawArc(
                color = resolvedTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Arco de avance activo
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

        // Decorativo: el contenido lo anuncia el Box padre (progreso de la racha)
        Image(
            painter = painterResource(id = R.drawable.fuego),
            contentDescription = null,
            modifier = Modifier.size(size / 2.2f)
        )
    }
}

@Composable
fun StreakCalendar(
    streakStart: LocalDate?,
    streakEnd: LocalDate?,
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now()
) {
    val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S")
    // Nombres completos para que TalkBack anuncie los días sin ambigüedad
    val daysOfWeekFull = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    val yearMonth = YearMonth.from(currentDate)
    val totalDaysInMonth = yearMonth.lengthOfMonth()

    // Calculate starting offset for Sunday-first calendar grid (D, L, M, M, J, V, S)
    val firstDayOfMonth = currentDate.withDayOfMonth(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    val monthName = currentDate.month
        .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es")) else it.toString() }

    // Grid height adapts to the month (5 or 6 rows) and the compact cell size
    val gridRows = calendarGridRows(startOffset, totalDaysInMonth)
    val gridHeight = (gridRows * CalendarDayCellSize.value).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(12.dp)
    ) {
        // Cabecera del mes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$monthName ${currentDate.year}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Días de la semana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEachIndexed { index, day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = daysOfWeekFull[index]
                        }
                )
            }
        }

        // Matriz de días
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(gridHeight),
            userScrollEnabled = false
        ) {
            items(startOffset) {
                Spacer(modifier = Modifier.size(CalendarDayCellSize))
            }

            items(totalDaysInMonth) { index ->
                val day = index + 1
                val dayDate = currentDate.withDayOfMonth(day)
                // Smart-casts after the null guards: no streak -> nothing marked
                val isMarked = streakStart != null &&
                    streakEnd != null &&
                    !currentDate.isAfter(streakEnd.plusDays(1)) &&
                    !dayDate.isBefore(streakStart) &&
                    !dayDate.isAfter(streakEnd)
                val isToday = (day == currentDate.dayOfMonth)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(CalendarDayCellSize)
                            .then(
                                when {
                                    isMarked -> Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                    isToday -> Modifier
                                        .clip(CircleShape)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else -> Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontSize = 12.sp,
                            color = when {
                                isMarked -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (isMarked || isToday) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.semantics {
                                contentDescription = when {
                                    isMarked && isToday -> "Día $day, hoy y parte de tu racha"
                                    isMarked -> "Día $day, parte de tu racha"
                                    isToday -> "Día $day, hoy"
                                    else -> "Día $day"
                                }
                            }
                        )
                    }
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