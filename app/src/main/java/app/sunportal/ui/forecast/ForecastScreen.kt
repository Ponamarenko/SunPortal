package app.sunportal.ui.forecast

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.R
import app.sunportal.model.HourlyForecast
import app.sunportal.model.SocHour
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.BatteryColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.LoadColor
import app.sunportal.ui.theme.PVColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * Forecast (индекс 4) в стиле Svitgrid:
 * тёмный фон, карточки SvitgridCard (градиент #131A24 → #0A0D12, скругление 18.dp),
 * шрифт JetBrains Mono, Canvas-графики с подписями.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ForecastScreen(vm: ForecastViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvitScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📊 Прогноз",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono,
            color = Color.White
        )

        if (state.loading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text(
                text = "Помилка: ${state.error}",
                color = Color.Red,
                fontFamily = JetBrainsMono
            )
        } else state.data?.let { d ->

            // --- РОЗРАХУНОК НА ЗАВТРА ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_sun),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "☀️ Розрахунок на завтра по прогнозу сонця",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
                val tomorrowKwh = d.forecast_solar_kwh ?: d.average_kwh ?: d.today_kwh ?: 0f
                if (tomorrowKwh > 0f) {
                    val canSpend = (tomorrowKwh * 0.7f)
                    Text(
                        text = "Ожидаемая выработка: ${String.format("%.2f", tomorrowKwh)} кВт·год",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color.White,
                        fontFamily = JetBrainsMono
                    )
                    Text(
                        text = "Можно потратить (70% від виробітку): ${String.format("%.2f", canSpend)} кВт·год",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = LabelGray,
                        fontFamily = JetBrainsMono
                    )
                    if (canSpend >= 5f) {
                        Text(
                            text = "Можна запускати важкі задачі",
                            color = PVColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono
                        )
                    } else {
                        Text(
                            text = "Рекомендуються: зарядка EV, прання, нагрів води",
                            color = LabelGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontFamily = JetBrainsMono
                        )
                    }
                } else {
                    Text(
                        text = "Немає даних",
                        color = LabelGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = JetBrainsMono
                    )
                }
            }

            // --- ТОЧНИЙ ПРОГНОЗ НА 3 ДНІ ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_sun),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Точний прогноз на 3 дні",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Оновлено: ${d.updated}",
                    color = LabelGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = JetBrainsMono,
                    maxLines = 1
                )
                Spacer(Modifier.height(10.dp))

                val todayV = d.today_kwh ?: 0f
                val tomorrowV = d.tomorrow_kwh ?: d.average_kwh ?: 0f
                val dayAfterV = d.day_after_kwh ?: 0f
                val fmt = { v: Float -> if (v <= 0f) "—" else String.format("%.2f", v) }
                val dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM"))
                val dateTomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM"))
                val dateAfter = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd.MM"))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayColumn("Сьогодні", dateToday, fmt(todayV), PVColor)
                    DayColumn("Завтра", dateTomorrow, fmt(tomorrowV), LoadColor)
                    DayColumn("Післязавтра", dateAfter, fmt(dayAfterV), BatteryColor)
                }
            }

            // --- ДЖЕРЕЛА ПРОГНОЗУ ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Джерела прогнозу (завтра)",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                val sources = d.sources ?: emptyMap()
                if (sources.isEmpty()) {
                    Text(
                        text = "Немає даних",
                        color = LabelGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = JetBrainsMono
                    )
                } else {
                    sources.forEach { (name, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = JetBrainsMono
                            )
                            Text(
                                text = "${String.format("%.2f", value)} кВт·год",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PVColor,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }
                }
                val errors = d.errors ?: emptyList()
                if (errors.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Помилки: ${errors.joinToString()}",
                        color = LabelGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = JetBrainsMono
                    )
                }
            }

            // --- ПОГОДИННИЙ ПРОГНОЗ ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_sun),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Погодинний прогноз (сьогодні)",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
                val hoursList = d.hours ?: emptyList()
                if (hoursList.isNotEmpty()) {
                    HourlyForecastChart(hoursList)
                    Spacer(Modifier.height(8.dp))
                    ChartLegend(
                        items = listOf(
                            Triple("p90", Color(0xFF2ECC71), true),
                            Triple("прогноз", PVColor, false),
                            Triple("p10", Color(0xFFE74C3C), true)
                        )
                    )
                } else {
                    ForecastChart(d.average_kwh ?: 0f)
                }
                val totalPred = if (hoursList.isNotEmpty()) {
                    hoursList.sumOf { it.predicted_kwh.toDouble() }.toFloat()
                } else 0f
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "опережаем: ${String.format("%.2f", totalPred)} кВт·год",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }

            // --- ПРОГНОЗ БАТАРЕЇ (SOC) ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_battery),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Прогноз батареї (SOC по годинах)",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
                val socList = d.soc_hours ?: emptyList()
                if (socList.isNotEmpty()) {
                    SocChart(socList)
                } else {
                    Text(
                        text = "Немає даних",
                        color = LabelGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = JetBrainsMono
                    )
                }
            }
        }
    }
}

/** Одна колонка прогноза на 3 дня: название, дата, значение, единица. */
@Composable
private fun RowScope.DayColumn(title: String, date: String, value: String, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color.White,
            fontFamily = JetBrainsMono,
            maxLines = 1
        )
        Text(
            text = date,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = JetBrainsMono,
            maxLines = 1
        )
        Text(
            text = "кВт·год",
            fontSize = 10.sp,
            lineHeight = 14.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono,
            maxLines = 1
        )
    }
}

/** Легенда графика: подпись + цвет + пунктир? */
@Composable
private fun ChartLegend(items: List<Triple<String, Color, Boolean>>) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { (label, color, dashed) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(width = 14.dp, height = 2.dp)) {
                    if (dashed) {
                        drawLine(
                            color = color,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 3f))
                        )
                    } else {
                        drawLine(
                            color = color,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 3f
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastChart(hours: List<HourlyForecast>) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = LabelGray,
        fontSize = 11.sp,
        fontFamily = JetBrainsMono
    )
    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val w = size.width
        val h = size.height
        val left = 24f
        val right = w
        val top = 16f
        val bottom = h - 30f
        val ww = right - left
        val hh = bottom - top
        if (hours.isEmpty()) return@Canvas
        val maxVal = hours.maxOf { max(it.predicted_kwh, max(it.p90_kwh, it.p10_kwh)) }
        val minVal = hours.minOf { min(it.predicted_kwh, min(it.p90_kwh, it.p10_kwh)) }
        val range = (maxVal - minVal).takeIf { it > 0f } ?: 1f

        fun y(v: Float) = bottom - (v - minVal) / range * hh

        fun pathFor(transform: (HourlyForecast) -> Float): Path {
            val p = Path()
            hours.forEachIndexed { i, hf ->
                val x = left + ww * (i.toFloat() / (hours.size - 1).coerceAtLeast(1))
                val py = y(transform(hf))
                if (i == 0) p.moveTo(x, py) else p.lineTo(x, py)
            }
            return p
        }

        drawPath(
            pathFor { it.p90_kwh },
            Color(0xFF2ECC71),
            style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
        )
        drawPath(
            pathFor { it.p10_kwh },
            Color(0xFFE74C3C),
            style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
        )
        drawPath(pathFor { it.predicted_kwh }, PVColor, style = Stroke(width = 4f))

        val nowHour = (System.currentTimeMillis() / 3600000L % 24).toInt()
        val idxNow = hours.indexOfFirst { it.hour == nowHour }.takeIf { it >= 0 }
            ?: ((nowHour * hours.size) / 24).coerceIn(0, hours.size - 1)
        val xNow = left + ww * (idxNow.toFloat() / (hours.size - 1).coerceAtLeast(1))
        drawLine(
            Color(0xFF9E9E9E),
            Offset(xNow, top),
            Offset(xNow, bottom),
            strokeWidth = 2f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        )

        // Подписи часов — 11.sp, LabelGray
        val step = if (hours.size > 12) 4 else 2
        hours.forEachIndexed { i, hf ->
            if (i % step == 0) {
                val layout = textMeasurer.measure("${hf.hour}", labelStyle)
                val x = left + ww * (i.toFloat() / (hours.size - 1).coerceAtLeast(1))
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(x - layout.size.width / 2f, bottom + 6f)
                )
            }
        }
    }
}

@Composable
private fun SocChart(hours: List<SocHour>) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = LabelGray,
        fontSize = 11.sp,
        fontFamily = JetBrainsMono
    )
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height
        val left = 24f
        val right = w
        val top = 8f
        val bottom = h - 30f
        val ww = right - left
        val barW = if (hours.isEmpty()) 0f else ww / hours.size * 0.6f
        hours.forEachIndexed { i, sh ->
            val x = left + ww * (i.toFloat() / hours.size)
            val soc = sh.soc.coerceIn(0f, 100f)
            val bh = (soc / 100f) * (bottom - top)
            val y = bottom - bh
            val color = if (soc >= 50f) Color(0xFF2ECC71) else if (soc >= 30f) Color(0xFFFFB74D) else Color(0xFFE74C3C)
            drawRect(color = color, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(barW, bh))
        }

        // Подписи часов — 11.sp, LabelGray
        val step = if (hours.size > 12) 4 else 2
        hours.forEachIndexed { i, sh ->
            if (i % step == 0) {
                val layout = textMeasurer.measure("${sh.hour}", labelStyle)
                val x = left + ww * (i.toFloat() / hours.size)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(x - layout.size.width / 2f, bottom + 6f)
                )
            }
        }
    }
}

@Composable
private fun ForecastChart(totalKwh: Float) {
    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val w = size.width
        val h = size.height
        val path = Path()
        val points = 24
        for (i in 0..points) {
            val x = w * i / points
            val t = i.toFloat() / points
            val y = h - h * kotlin.math.sin(t * Math.PI).toFloat() * (totalKwh / 50f).coerceIn(0f, 1f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = PVColor, style = Stroke(width = 4f))
    }
}
