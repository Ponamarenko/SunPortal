package app.sunportal.ui.tou

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.forecast.ForecastViewModel
import app.sunportal.ui.overview.BlackoutViewModel
import app.sunportal.ui.theme.GridColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.PVColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class SlotEdit(
    val index: Int,
    val time: String,
    val power: String,
    val soc: String,
    val grid: Boolean,
    val gen: Boolean
)

/**
 * TOU (индекс 7) в стиле Svitgrid:
 * тёмный фон, карточки SvitgridCard (градиент #131A24 → #0A0D12, скругление 18.dp),
 * шрифт JetBrains Mono, слоты — отдельные карточки.
 */
@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TouScreen(
    vm: TouViewModel = viewModel(),
    bvm: BlackoutViewModel = viewModel(),
    fvm: ForecastViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val bstate by bvm.state.collectAsState()
    val fstate by fvm.state.collectAsState()
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvitScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "⚡ TOU (Time of Use)",
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
            Text(
                text = "Оновлено: ${d.updated}",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono
            )

            val slotEdits = remember(d) {
                mutableStateListOf<SlotEdit>().apply {
                    d.slots.forEach {
                        add(
                            SlotEdit(
                                index = it.index,
                                time = it.time,
                                power = it.power.toString(),
                                soc = it.soc.toString(),
                                grid = it.grid,
                                gen = it.gen
                            )
                        )
                    }
                }
            }

            // --- СЛОТЫ ---
            slotEdits.forEachIndexed { i, edit ->
                // Кінець слота = час наступного (останній замикається на перший)
                val endTime = slotEdits.getOrNull(i + 1)?.time ?: slotEdits.first().time

                SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${edit.index}",
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                            color = Color.White
                        )
                        Text(
                            text = "${edit.time} – $endTime",
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                            color = PVColor
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // Початок / Кінець — только чтение
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SvitField(
                            value = edit.time,
                            onValueChange = { },
                            label = "Початок",
                            modifier = Modifier.weight(1f),
                            enabled = false
                        )
                        SvitField(
                            value = endTime,
                            onValueChange = { },
                            label = "Кінець",
                            modifier = Modifier.weight(1f),
                            enabled = false
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // Потужність / SOC
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SvitField(
                            value = edit.power,
                            onValueChange = { v -> slotEdits[i] = edit.copy(power = v) },
                            label = "Потужність, Вт",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )
                        SvitField(
                            value = edit.soc,
                            onValueChange = { v -> slotEdits[i] = edit.copy(soc = v) },
                            label = "SOC, %",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    // Мережа / Генератор
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Мережа",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = LabelGray,
                                fontFamily = JetBrainsMono
                            )
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = edit.grid,
                                onCheckedChange = { v -> slotEdits[i] = edit.copy(grid = v) }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Генератор",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = LabelGray,
                                fontFamily = JetBrainsMono
                            )
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = edit.gen,
                                onCheckedChange = { v -> slotEdits[i] = edit.copy(gen = v) }
                            )
                        }
                    }
                }
            }

            // --- СТАТУС ---
            Text(
                text = "Дні тижня: ${d.days.joinToString(", ")}",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono
            )
            Text(
                text = "Статус: ${if (d.enabled) "✅ Увімкнено" else "❌ Вимкнено"}",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = if (d.enabled) PVColor else GridColor,
                fontFamily = JetBrainsMono
            )

            if (status.isNotEmpty()) {
                Text(
                    text = status,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = PVColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono
                )
            }

            // --- КНОПКИ ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SvitButton(
                    text = "🔄 Прочитати",
                    modifier = Modifier.weight(1f),
                    highlighted = true,
                    onClick = {
                        status = "✅ Перечитано з сервера"
                        vm.loadData()
                    }
                )
                SvitButton(
                    text = "💾 Записати",
                    modifier = Modifier.weight(1f),
                    highlighted = false,
                    enabled = false,
                    onClick = { }
                )
            }

            // --- ПРЕСЕТИ (локальні шаблони: сервер підтримує лише читання TOU) ---
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val blackoutWindows = remember(bstate.data) {
                bstate.data?.schedule
                    ?.find { it.date == today }
                    ?.slots
                    ?.map { toMinutes(it.start.take(5)) to toMinutes(it.end.take(5)) }
                    ?: emptyList()
            }
            val forecastHours = fstate.data?.hours.orEmpty()

            fun applyPreset(name: String) {
                slotEdits.forEachIndexed { i, edit ->
                    val endTime = slotEdits.getOrNull(i + 1)?.time ?: slotEdits.first().time
                    val sMin = toMinutes(edit.time)
                    var eMin = toMinutes(endTime)
                    if (eMin <= sMin) eMin += 1440
                    val inBlackout = blackoutWindows.any { (bs, be) ->
                        var b2 = be
                        if (b2 <= bs) b2 += 1440
                        sMin < b2 && bs < eMin
                    }
                    val isDay = sMin < 20 * 60 && eMin > 6 * 60
                    val updated = when (name) {
                        // Під час відключень — не заряджати (беремо резерв), вночі — заряд від мережі
                        "Blackout" -> if (inBlackout)
                            edit.copy(power = "0", soc = "100", grid = false, gen = false)
                        else
                            edit.copy(power = "12000", soc = "100", grid = true, gen = false)
                        // Денні слоти — заряд потужністю з прогнозу, нічні — від мережі
                        "Від прогнозу" -> {
                            var pvW = 0f
                            forecastHours.forEach { h ->
                                var hm = h.hour * 60
                                if (hm < sMin) hm += 1440
                                if (hm >= sMin && hm < eMin) pvW += h.predicted_kwh * 1000f
                            }
                            if (isDay)
                                edit.copy(power = pvW.toInt().coerceAtLeast(0).toString(), soc = "100", grid = false, gen = false)
                            else
                                edit.copy(power = "12000", soc = "100", grid = true, gen = false)
                        }
                        "Ніч" -> edit.copy(power = "12000", soc = "100", grid = true, gen = false)
                        else -> edit.copy(power = "12000", soc = "80", grid = false, gen = true)
                    }
                    slotEdits[i] = updated
                }
                status = "Пресет «$name» заповнено локально (сервер не підтримує запис TOU)"
            }

            Text(
                text = "Пресети:",
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Blackout", "Від прогнозу", "Ніч", "День").forEach { preset ->
                    SvitButton(
                        text = preset,
                        highlighted = false,
                        textSize = 12,
                        onClick = { applyPreset(preset) }
                    )
                }
            }
            Text(
                text = "⚠️ Сервер підтримує лише читання розкладу — пресети заповнюють таблицю локально.",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono
            )
        }
    }
}

/** Поле ввода в стиле Svitgrid: подпись и значение JetBrains Mono. */
@Composable
private fun SvitField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = JetBrainsMono,
            fontSize = 12.sp
        )
    )
}

/** Кнопка в стиле Svitgrid: тёмный фон, жёлтая (highlighted) или серая рамка. */
@Composable
private fun SvitButton(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    textSize: Int = 14,
    onClick: () -> Unit
) {
    val borderColor = when {
        !enabled -> Color.White.copy(alpha = 0.12f)
        highlighted -> PVColor
        else -> Color.White.copy(alpha = 0.25f)
    }
    val textColor = when {
        !enabled -> LabelGray
        highlighted -> PVColor
        else -> Color.White
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.06f else 0.03f))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = textSize.sp,
            lineHeight = (textSize + 4).sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = JetBrainsMono,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** "HH:MM" → хвилини від початку доби (для порівняння інтервалів). */
private fun toMinutes(time: String): Int {
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    return h * 60 + m
}
