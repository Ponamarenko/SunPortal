package app.sunportal.ui.overview

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.data.WEATHER_SOURCE_OPEN_METEO
import app.sunportal.ui.common.FlowScheme
import app.sunportal.ui.live.LiveViewModel
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.JetBrainsMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Overview (индекс 1) в стиле Svitgrid:
 * карточка погоды сверху, схема потоков (та же, что в Live) — в центре, блекаут — снизу.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OverviewScreen(
    // Тот же ViewModel, что и на HOME — один опрос сервера на всё приложение
    vm: LiveViewModel = viewModel(),
    bvm: BlackoutViewModel = viewModel(),
    wvm: WeatherViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val bstate by bvm.state.collectAsState()
    val wstate by wvm.state.collectAsState()
    val householdId by vm.householdType.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvitScreenBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ПОГОДА — над схемой
        WeatherCard(wstate)

        // СХЕМА — в центре экрана (общая с Live: ui/common/FlowScheme.kt)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                state.loading && state.data == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null && state.data == null -> Text(
                    text = "Помилка: ${state.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> state.data?.let { d ->
                    FlowScheme(
                        d = d,
                        modifier = Modifier.fillMaxSize(),
                        compact = true,
                        // Дом на схеме отражает погоду (дождь/снег/солнце)
                        weatherCode = wstate.data?.current?.code,
                        // Иконка типа домовладения (Svitgrid)
                        householdId = householdId
                    )
                }
            }
            // Сервер недоступен — показаны последние сохранённые данные
            if (state.offline) {
                Text(
                    text = "⚠️ Немає зв'язку — останні дані",
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = Color(0xFFFFA726),
                    fontFamily = JetBrainsMono,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }

        // БЛЕКАУТ — под схемой
        BlackoutCard(bstate)
    }
}

/**
 * Погода в стиле Svitgrid: карточка с градиентом (#131A24 → #0A0D12), скругление 18.dp,
 * иконка 40sp + температура 28sp (JetBrains Mono), описание/ощущается/влажность/ветер — 12sp,
 * прогноз на 3 дня — Row с тремя колонками.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherCard(wstate: WeatherUiState) {
    SvitgridCard(modifier = Modifier.fillMaxWidth()) {
        // Заголовок + время обновления
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌤 Погода",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono
            )
            Spacer(Modifier.width(8.dp))
            // Бейдж источника данных
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (wstate.source == WEATHER_SOURCE_OPEN_METEO) "Open-Meteo" else "Сервер",
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = wstate.data?.updated ?: "—",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(6.dp))

        val data = wstate.data
        when {
            wstate.loading && data == null -> {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
            data == null -> {
                Column {
                    Text(
                        text = "Немає даних про погоду",
                        fontSize = 12.sp,
                        color = LabelGray,
                        fontFamily = JetBrainsMono
                    )
                    if (!wstate.error.isNullOrBlank()) {
                        Text(
                            text = wstate.error,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = Color(0xFFE74C3C),
                            fontFamily = JetBrainsMono
                        )
                    }
                }
            }
            else -> {
                val cur = data.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Иконка 40sp
                    Text(
                        text = cur.icon.take(3),
                        fontSize = 40.sp,
                        lineHeight = 44.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    // Температура 28sp (JetBrains Mono)
                    Text(
                        text = "${cur.temp.toInt()}°",
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    // desc, feels, humidity, wind — 12sp
                    Column {
                        Text(
                            text = cur.desc,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            color = Color.White,
                            fontFamily = JetBrainsMono,
                            maxLines = 1
                        )
                        Text(
                            text = "Відчувається: ${cur.feels.toInt()}°",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            color = LabelGray,
                            fontFamily = JetBrainsMono,
                            maxLines = 1
                        )
                        Text(
                            text = "Вологість: ${cur.humidity}% · Вітер: ${cur.wind} м/с",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            color = LabelGray,
                            fontFamily = JetBrainsMono,
                            maxLines = 1
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))

                // 3 дня — Row с 3 колонками
                Row(modifier = Modifier.fillMaxWidth()) {
                    data.days.take(3).forEach { day ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.date,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                color = LabelGray,
                                fontFamily = JetBrainsMono,
                                maxLines = 1
                            )
                            Text(
                                text = day.icon.take(3),
                                fontSize = 16.sp,
                                lineHeight = 18.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${"%.1f".format(day.temp_max).replace('.', ',')}°",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = Color(0xFFE74C3C),
                                    fontFamily = JetBrainsMono
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${"%.1f".format(day.temp_min).replace('.', ',')}°",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = Color(0xFF2196F3),
                                    fontFamily = JetBrainsMono
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Блекаут в стиле Svitgrid: карточка с градиентом, заголовок «⚡ Відключення»,
 * бейдж статуса (Зараз світло / Зараз відключення / Скоро) и список слотов.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BlackoutCard(bstate: BlackoutUiState) {
    val statusColor = Color(bstate.statusColor)

    SvitgridCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Відключення",
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            // Бейдж статуса
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = bstate.statusText,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(5.dp))

        val addr = bstate.data?.address
        val grp = bstate.data?.groupInfo
        val line = when {
            grp != null && addr != null -> "Черга: $grp ($addr)"
            grp != null -> "Черга: $grp"
            else -> "Черга: —"
        }
        Text(
            text = line,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono,
            maxLines = 2
        )

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val slots = bstate.data?.schedule?.find { it.date == today }?.slots ?: emptyList()
        if (slots.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            // Список слотов — компактные чипы, по 3 в ряд
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                slots.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { s ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${s.start.take(5)}–${s.end.take(5)}",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = Color.White,
                                    fontFamily = JetBrainsMono,
                                    maxLines = 1
                                )
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        } else if (bstate.error != null || bstate.data == null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Немає даних про відключення",
                fontSize = 11.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono
            )
        }
    }
}
