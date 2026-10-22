package app.sunportal.ui.charts

import android.util.Log
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.api.RetrofitClient
import app.sunportal.model.HistoryPoint
import app.sunportal.model.HistoryResponse
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.BatteryColor
import app.sunportal.ui.theme.GridColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.LoadColor
import app.sunportal.ui.theme.PVColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChartsUiState(
    val loading: Boolean = false,
    val data: HistoryResponse? = null,
    val error: String? = null
)

class ChartsViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChartsUiState())
    val state = _state.asStateFlow()

    fun load(period: String, fields: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val r = RetrofitClient.api.getHistory(period = period, fields = fields)
                _state.value = ChartsUiState(loading = false, data = r)
            } catch (e: Exception) {
                Log.e("Charts", "load failed: $e", e)
                _state.value = ChartsUiState(loading = false, error = e.message)
            }
        }
    }
}

/**
 * Charts (индекс 5) в стиле Svitgrid:
 * тёмный фон, карточки SvitgridCard (градиент #131A24 → #0A0D12, скругление 18.dp),
 * шрифт JetBrains Mono, Canvas-график с подписями осей и легендой.
 */
@Composable
fun ChartsScreen(vm: ChartsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var selectedPeriod by remember { mutableStateOf("day") }
    var selectedFields by remember { mutableStateOf(setOf("pv", "load")) }

    val periods = listOf("День" to "day", "Тиждень" to "week", "Місяць" to "month")
    val metrics = listOf(
        "PV" to Pair("pv", PVColor),
        "Дім" to Pair("load", LoadColor),
        "Батарея" to Pair("battery", BatteryColor),
        "Мережа" to Pair("grid", GridColor)
    )

    LaunchedEffect(selectedPeriod, selectedFields) {
        val fields = if (selectedFields.isEmpty()) null else selectedFields.joinToString(",")
        vm.load(selectedPeriod, fields)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvitScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📈 Графіки",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono,
            color = Color.White
        )

        // --- ПЕРІОД ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Період",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periods.forEach { (label, value) ->
                    val sel = selectedPeriod == value
                    FilterChip(
                        selected = sel,
                        onClick = { selectedPeriod = value },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMono,
                                color = Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color.White,
                            selectedLabelColor = Color.White,
                            selectedContainerColor = Color.White.copy(alpha = 0.16f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = Color.White.copy(alpha = 0.25f),
                            selectedBorderColor = PVColor
                        )
                    )
                }
            }
        }

        // --- МЕТРИКИ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Метрики",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { (label, pair) ->
                    val (key, color) = pair
                    val sel = selectedFields.contains(key)
                    FilterChip(
                        selected = sel,
                        onClick = {
                            if (sel) selectedFields = selectedFields - key
                            else selectedFields = selectedFields + key
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMono,
                                color = Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color.White,
                            selectedLabelColor = Color.White,
                            selectedContainerColor = color.copy(alpha = 0.22f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = Color.White.copy(alpha = 0.25f),
                            selectedBorderColor = color
                        )
                    )
                }
            }
        }

        if (state.loading) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null || state.data == null || state.data!!.ok.not()) {
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Немає даних за цей період",
                        color = LabelGray,
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono
                    )
                }
            }
        } else {
            state.data?.let { d ->
                val metricColors = mapOf(
                    "pv" to PVColor,
                    "load" to LoadColor,
                    "battery" to BatteryColor,
                    "grid" to GridColor
                )

                // --- ГРАФІК ---
                SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Графік",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${d.from} — ${d.to}  ·  крок ${d.stepMinutes} хв",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = LabelGray,
                        fontFamily = JetBrainsMono,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(8.dp))
                    ChartView(d.points, selectedFields.toList(), metricColors)
                    Spacer(Modifier.height(10.dp))
                    // Легенда — 11.sp, LabelGray
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        selectedFields.toList().forEach { k ->
                            val c = metricColors[k] ?: Color.Gray
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 14.dp, height = 3.dp)
                                        .background(c)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = metrics.find { it.second.first == k }?.first ?: k,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = LabelGray,
                                    fontFamily = JetBrainsMono
                                )
                            }
                        }
                    }
                }

                // --- ПІДСУМОК ЗА ПЕРІОД ---
                SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Підсумок за період",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Вироблено", "${String.format("%.2f", d.summary.pvKwh)} кВт·год", PVColor)
                    SummaryRow("Спожито", "${String.format("%.2f", d.summary.loadKwh)} кВт·год", LoadColor)
                    SummaryRow("Заряджено в АКБ", "${String.format("%.2f", d.summary.batteryChargedKwh)} кВт·год", BatteryColor)
                    SummaryRow("Розряджено з АКБ", "${String.format("%.2f", d.summary.batteryDischargedKwh)} кВт·год", BatteryColor)
                    SummaryRow("Імпорт з мережі", "${String.format("%.2f", d.summary.gridImportKwh)} кВт·год", GridColor)
                    SummaryRow("Експорт в мережу", "${String.format("%.2f", d.summary.gridExportKwh)} кВт·год", GridColor)
                }
            }
        }
    }
}

/** Строка «подпись — значение» в стиле Svitgrid. */
@Composable
private fun SummaryRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono
        )
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = JetBrainsMono
        )
    }
}

/**
 * Canvas-график: линии метрик, подписи осей (значения слева, время снизу) — 11.sp, LabelGray.
 */
@Composable
private fun ChartView(
    points: List<HistoryPoint>,
    fields: List<String>,
    colors: Map<String, Color>
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = LabelGray,
        fontSize = 11.sp,
        fontFamily = JetBrainsMono
    )

    if (points.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Немає даних за цей період",
                color = LabelGray,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono
            )
        }
        return
    }
    Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        val w = size.width
        val h = size.height
        val left = 52f
        val right = w - 8f
        val top = 8f
        val bottom = h - 30f
        val ww = right - left
        val hh = bottom - top

        val values = fields.mapNotNull { f -> points.mapNotNull { p -> p.valueFor(f) } }
        val all = values.flatten()
        val minVal = if (all.isEmpty()) 0f else all.min()
        val maxVal = if (all.isEmpty()) 1f else all.max()
        val range = if (maxVal - minVal < 1e-6f) 1f else maxVal - minVal

        fun y(v: Float) = bottom - (v - minVal) / range * hh

        // Сетка + подписи значений по оси Y — 11.sp, LabelGray
        for (i in 0..3) {
            val v = minVal + range * (3 - i) / 3
            val yy = y(v)
            drawLine(
                color = Color.White.copy(alpha = 0.10f),
                start = Offset(left, yy),
                end = Offset(right, yy),
                strokeWidth = 1f
            )
            val layout = textMeasurer.measure(formatAxisValue(v), labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(left - 6f - layout.size.width, yy - layout.size.height / 2f)
            )
        }

        // Линии метрик
        fields.forEach { f ->
            val col = colors[f] ?: Color.Gray
            val pts = points.mapNotNull { it.valueFor(f) }
            if (pts.isNotEmpty()) {
                val p = Path()
                points.forEachIndexed { i, pt ->
                    val v = pt.valueFor(f) ?: minVal
                    val x = left + ww * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
                    val yy = y(v)
                    if (i == 0) p.moveTo(x, yy) else p.lineTo(x, yy)
                }
                drawPath(p, color = col, style = Stroke(width = 2f))
            }
        }

        // Подписи времени по оси X — 11.sp, LabelGray
        val step = when {
            points.size > 100 -> 24
            points.size > 40 -> 12
            points.size > 16 -> 6
            else -> 2
        }
        points.forEachIndexed { i, pt ->
            if (i % step == 0) {
                val layout = textMeasurer.measure(formatAxisTime(pt.ts), labelStyle)
                val x = left + ww * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(x - layout.size.width / 2f, bottom + 6f)
                )
            }
        }
    }
}

/** Значение оси Y: целые — без дробной части, иначе одна цифра после запятой. */
private fun formatAxisValue(v: Float): String = when {
    kotlin.math.abs(v) >= 1000f -> String.format("%.0f", v / 1000f) + "k"
    kotlin.math.abs(v) >= 100f -> String.format("%.0f", v)
    else -> String.format("%.1f", v)
}

/** Время оси X: из ISO-строки «2026-10-20T04:50:00» берём «04:50». */
private fun formatAxisTime(ts: String): String {
    val t = ts.substringAfter('T', "")
    return if (t.length >= 5) t.substring(0, 5) else ts
}

private fun HistoryPoint.valueFor(field: String): Float? = when (field) {
    "pv" -> pv
    "load" -> load
    "battery" -> battery
    "grid" -> grid
    "soc" -> soc
    else -> null
}
