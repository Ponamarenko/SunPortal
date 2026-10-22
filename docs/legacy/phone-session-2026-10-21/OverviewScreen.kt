package app.sunportal.ui.overview

import android.graphics.PathMeasure
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.R
import app.sunportal.ui.components.GlowCard
import app.sunportal.ui.components.Pill
import app.sunportal.ui.components.SectionTitle
import app.sunportal.ui.components.StatTile
import app.sunportal.ui.components.LottieArt
import app.sunportal.ui.components.LottieArtView
import app.sunportal.ui.theme.EnergyPalette

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OverviewScreen(vm: OverviewViewModel = viewModel(), bvm: BlackoutViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val bstate by bvm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BlackoutCard(bstate)
        SectionTitle("Потоки енергії")

        if (state.loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.error != null) {
            Text("Помилка: ${state.error}", color = MaterialTheme.colorScheme.error)
        } else {
            state.data?.let { d ->
                EnergyFlowCard(d)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Сонце (PV)", "${d.p_sun.toInt()}", "Вт", EnergyPalette.solar, Modifier.weight(1f))
                    StatTile("Дім", "${d.p_load.toInt()}", "Вт", EnergyPalette.load, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val gridColor = if (d.p_grid < -50) EnergyPalette.gridExport else EnergyPalette.gridImport
                    StatTile("Мережа", "${d.p_grid.toInt()}", "Вт", gridColor, Modifier.weight(1f))
                    StatTile("АКБ", "${d.batt_soc.toInt()}", "%", EnergyPalette.battery, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EnergyFlowCard(d: app.sunportal.model.LiveData) {
    val pvActive = d.p_sun > 50
    val loadActive = d.p_load > 50
    val battActive = kotlin.math.abs(d.p_batt) > 50
    val gridActive = kotlin.math.abs(d.p_grid) > 50
    val battCharging = d.p_batt < -50
    val gridExport = d.p_grid < -50

    GlowCard(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(460.dp)) {
            val w = with(LocalDensity.current) { maxWidth.toPx() }
            val h = with(LocalDensity.current) { maxHeight.toPx() }
            val cx = w / 2f
            val cy = h / 2f
            val topY = 30f
            val panelY = 60f
            val inverterY = cy - 10f
            val batteryY = h - 80f
            val houseX = w - 70f
            val houseY = cy + 20f
            val gridX = 70f
            val gridY = cy + 20f

            // 🌞 ENERGY FLOW LINES with curved paths and animated particles
            // Panel -> Inverter (vertical)
            AnimatedEnergyPath(
                start = Offset(cx, panelY + 40f),
                end = Offset(cx, inverterY - 40f),
                color = EnergyPalette.solar,
                active = pvActive,
                speed = if (pvActive) 1.5f else 0f,
                particleCount = 4,
                particleSize = 6f,
                curveType = CurveType.VERTICAL
            )

            // Inverter -> Battery (down curve)
            AnimatedEnergyPath(
                start = Offset(cx, inverterY + 15f),
                end = Offset(cx + 20f, batteryY - 30f),
                color = EnergyPalette.battery,
                active = battActive,
                speed = if (battActive) 1.2f else 0f,
                particleCount = 3,
                particleSize = 5f,
                curveType = CurveType.CURVE_RIGHT,
                reverse = battCharging
            )

            // Inverter -> House (horizontal right curve)
            AnimatedEnergyPath(
                start = Offset(cx + 15f, cy + 10f),
                end = Offset(houseX - 25f, houseY),
                color = EnergyPalette.load,
                active = loadActive,
                speed = if (loadActive) 1.3f else 0f,
                particleCount = 3,
                particleSize = 5f,
                curveType = CurveType.CURVE_RIGHT
            )

            // Inverter -> Grid (horizontal left curve)
            AnimatedEnergyPath(
                start = Offset(cx - 15f, cy + 10f),
                end = Offset(gridX + 25f, gridY),
                color = if (gridExport) EnergyPalette.gridExport else EnergyPalette.gridImport,
                active = gridActive,
                speed = if (gridActive) (if (gridExport) -1f else 1f) else 0f,
                particleCount = 3,
                particleSize = 5f,
                curveType = CurveType.CURVE_LEFT,
                reverse = gridExport
            )

            // 🌞 SOLAR PANEL - Lottie (top)
            LottieArtView(
                art = LottieArt.SOLAR_PANEL,
                size = 150.dp,
                speed = if (pvActive) 1f else 0.2f,
                loop = true,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 12.dp)
            )

            // ⚡ ENERGY FLOW LINES with curved paths and animated particles
            AnimatedEnergyPath(
                start = Offset(cx, panelY + 50f),
                end = Offset(cx, inverterY - 48f),
                color = EnergyPalette.solar,
                active = pvActive,
                speed = if (pvActive) 1.7f else 0f,
                particleCount = 4,
                particleSize = 6f,
                curveType = CurveType.VERTICAL
            )

            AnimatedEnergyPath(
                start = Offset(cx, inverterY + 20f),
                end = Offset(cx + 28f, batteryY - 26f),
                color = EnergyPalette.battery,
                active = battActive,
                speed = if (battActive) 1.3f else 0f,
                particleCount = 3,
                particleSize = 5f,
                curveType = CurveType.CURVE_RIGHT,
                reverse = battCharging
            )

            AnimatedEnergyPath(
                start = Offset(cx + 22f, cy + 14f),
                end = Offset(houseX - 30f, houseY),
                color = EnergyPalette.load,
                active = loadActive,
                speed = if (loadActive) 1.4f else 0f,
                particleCount = 3,
                particleSize = 5f,
                curveType = CurveType.CURVE_RIGHT
            )

            AnimatedEnergyPath(
                start = Offset(cx - 22f, cy + 14f),
                end = Offset(gridX + 30f, gridY),
                color = if (gridExport) EnergyPalette.gridExport else EnergyPalette.gridImport,
                active = gridActive,
                speed = if (gridActive) (if (gridExport) -1.2f else 1.2f) else 0f,
                particleCount = 3,
                particleSize = 5f,
                curveType = CurveType.CURVE_LEFT,
                reverse = gridExport
            )

            // 🏠 HOUSE - Lottie
            LottieArtView(
                art = LottieArt.HOME_SUN,
                size = 88.dp,
                speed = if (loadActive) 1f else 0.12f,
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-6f).dp, y = (houseY - 92f).dp)
            )

            // 🔋 BATTERY - Lottie
            LottieArtView(
                art = LottieArt.BATTERY,
                size = 80.dp,
                speed = if (battActive) (if (battCharging) 1.3f else 0.9f) else 0.12f,
                modifier = Modifier.align(Alignment.BottomCenter).offset(x = 30.dp, y = (-48f).dp)
            )

            // 🔌 GRID / POLE TRANSFORMER - Lottie
            LottieArtView(
                art = if (gridExport) LottieArt.TRANSFORMER else LottieArt.TRANSFORMER_NIGHT,
                size = 90.dp,
                speed = if (gridActive) 1f else 0.12f,
                modifier = Modifier.align(Alignment.CenterStart).offset(x = (12f).dp, y = (gridY - 92f).dp)
            )

            // ⚡ INVERTER - Lottie (center)
            LottieArtView(
                art = LottieArt.INVERTER,
                size = 98.dp,
                speed = if (pvActive || loadActive || battActive || gridActive) 1f else 0.12f,
                modifier = Modifier.align(Alignment.Center).offset(y = (-6f).dp)
            )

            // 📊 VALUE LABELS
            Column(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (panelY + 96f).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${d.p_sun.toInt()} Вт", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EnergyPalette.solar)
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).offset(x = 30.dp, y = (-24f).dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${d.batt_soc.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EnergyPalette.battery)
                    Text(if (battCharging) "ЗАРЯД" else "РОЗРЯД", fontSize = 10.sp, color = EnergyPalette.battery.copy(alpha = 0.7f))
                }
            }

            Box(modifier = Modifier.align(Alignment.CenterStart).offset(x = 20f.dp, y = (gridY + 42f).dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${d.p_grid.toInt()} Вт", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (gridExport) EnergyPalette.gridExport else EnergyPalette.gridImport)
                    Text(if (gridExport) "ЕКСПОРТ" else "ІМПОРТ", fontSize = 9.sp, color = if (gridExport) EnergyPalette.gridExport.copy(alpha = 0.7f) else EnergyPalette.gridImport.copy(alpha = 0.7f))
                }
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = (2f).dp, y = (houseY + 42f).dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${d.p_load.toInt()} Вт", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EnergyPalette.load)
                    Text("СПОЖИВ", fontSize = 9.sp, color = EnergyPalette.load.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// PATH CREATION HELPERS
// ──────────────────────────────────────────────────────────────────────────

private fun createVerticalPath(x: Float, startY: Float, endY: Float): Path = Path().apply {
    moveTo(x, startY)
    lineTo(x, endY)
}

private fun createCurvePath(
    startX: Float, startY: Float,
    endX: Float, endY: Float,
    curveRight: Boolean
): Path = Path().apply {
    moveTo(startX, startY)
    val ctrlX = (startX + endX) / 2f + if (curveRight) 40f else -40f
    val ctrlY = (startY + endY) / 2f
    quadraticBezierTo(ctrlX, ctrlY, endX, endY)
}

// ──────────────────────────────────────────────────────────────────────────
// ANIMATED ENERGY PATH - simplified without PathMeasure
// Uses parametric curves with pre-calculated positions
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun AnimatedEnergyPath(
    start: Offset,
    end: Offset,
    color: Color,
    active: Boolean,
    speed: Float = 1f,
    particleCount: Int = 3,
    particleSize: Float = 5f,
    curveType: CurveType = CurveType.CURVE_RIGHT,
    reverse: Boolean = false
) {
    if (!active) {
        // Draw static dimmed path
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = createCurvePath(start, end, curveType)
            drawPath(
                path,
                color = color.copy(alpha = 0.15f),
                style = androidx.compose.ui.graphics.Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "energyFlow")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1200f / speed.abs()).toLong(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = createCurvePath(start, end, curveType)
        
        // Base path glow
        drawPath(
            path,
            color = color.copy(alpha = 0.2f),
            style = androidx.compose.ui.graphics.Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Main path
        drawPath(
            path,
            color = color,
            style = androidx.compose.ui.graphics.Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // Particles flowing along path using parametric curve
        repeat(particleCount) { i ->
            val t = (phase + i.toFloat() / particleCount) % 1f
            val actualT = if (reverse) 1f - t else t
            val pos = getPointOnCurve(start, end, curveType, actualT)
            val glowAlpha = 0.3f + 0.4f * (1f - (actualT % 0.5f) * 2f).abs()
            val glowColor = color.copy(alpha = glowAlpha)
            
            drawCircle(
                color = glowColor,
                radius = particleSize * 1.5f,
                center = pos
            )
            drawCircle(
                color = color,
                radius = particleSize,
                center = pos
            )
        }
    }
}

enum class CurveType { VERTICAL, CURVE_RIGHT, CURVE_LEFT }

private fun createCurvePath(start: Offset, end: Offset, curveType: CurveType): Path {
    val path = Path()
    path.moveTo(start.x, start.y)
    when (curveType) {
        CurveType.VERTICAL -> path.lineTo(end.x, end.y)
        CurveType.CURVE_RIGHT -> {
            val ctrlX = (start.x + end.x) / 2f + 50f
            val ctrlY = (start.y + end.y) / 2f
            path.quadraticBezierTo(ctrlX, ctrlY, end.x, end.y)
        }
        CurveType.CURVE_LEFT -> {
            val ctrlX = (start.x + end.x) / 2f - 50f
            val ctrlY = (start.y + end.y) / 2f
            path.quadraticBezierTo(ctrlX, ctrlY, end.x, end.y)
        }
    }
    return path
}

private fun getPointOnCurve(start: Offset, end: Offset, curveType: CurveType, t: Float): Offset {
    return when (curveType) {
        CurveType.VERTICAL -> Offset(
            start.x + (end.x - start.x) * t,
            start.y + (end.y - start.y) * t
        )
        CurveType.CURVE_RIGHT -> {
            val ctrlX = (start.x + end.x) / 2f + 50f
            val ctrlY = (start.y + end.y) / 2f
            quadraticBezierPoint(start, end, Offset(ctrlX, ctrlY), t)
        }
        CurveType.CURVE_LEFT -> {
            val ctrlX = (start.x + end.x) / 2f - 50f
            val ctrlY = (start.y + end.y) / 2f
            quadraticBezierPoint(start, end, Offset(ctrlX, ctrlY), t)
        }
    }
}

private fun quadraticBezierPoint(p0: Offset, p2: Offset, p1: Offset, t: Float): Offset {
    val u = 1f - t
    val x = u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x
    val y = u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y
    return Offset(x, y)
}

// ──────────────────────────────────────────────────────────────────────────
// BLACKOUT CARD (unchanged)
// ──────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BlackoutCard(bstate: BlackoutUiState) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Відключення", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Pill(bstate.statusText, Color(bstate.statusColor))
        }
        Spacer(Modifier.height(8.dp))
        val addr = bstate.data?.address
        val grp = bstate.data?.groupInfo
        val line = when {
            grp != null && addr != null -> "Черга: $grp ($addr)"
            grp != null -> "Черга: $grp"
            else -> "Черга: —"
        }
        Text(line, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val day = bstate.data?.schedule?.find { it.date == today }
        val slots = day?.slots ?: emptyList()
        if (slots.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            slots.forEach { s ->
                Text("Слот сьогодні: ${s.start}–${s.end}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (bstate.error != null || bstate.data == null) {
            Spacer(Modifier.height(6.dp))
            Text("Немає даних про відключення", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}