package app.sunportal.ui.live

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.model.BlackoutResponse
import app.sunportal.model.BlackoutSlot
import app.sunportal.model.LiveData
import app.sunportal.ui.common.BatteryStatusLine
import app.sunportal.ui.common.FlowScheme
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.overview.BlackoutViewModel
import app.sunportal.ui.overview.WeatherViewModel
import app.sunportal.ui.theme.JetBrainsMono
import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun LiveScreen(vm: LiveViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF131A24), Color(0xFF0A0D12))
                )
            )
            .padding(16.dp)
    ) {
        when {
            state.loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            state.error != null -> {
                Text(
                    "Помилка: ${state.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                state.data?.let { d -> InverterScheme(d) }
            }
        }
    }
}

/**
 * Шапка Live в стиле Svitgrid:
 * «HOME», под ней погода («13° Невеликий дощ») + «Щойно»,
 * справа — колокольчик и fullscreen (переключает системные бары).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTopBar(onMenu: () -> Unit) {
    val weatherVm: WeatherViewModel = viewModel()
    val weatherState by weatherVm.state.collectAsState()
    val cur = weatherState.data?.current
    val view = LocalView.current
    var fullscreen by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "HOME",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                if (cur != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${cur.temp.toInt()}° ${cur.desc}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Щойно",
                            fontSize = 11.sp,
                            color = LabelGray
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Меню")
            }
        },
        actions = {
            IconButton(onClick = { /* колокольчик: іконка сповіщень */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Сповіщення")
            }
            IconButton(onClick = {
                fullscreen = !fullscreen
                val activity = view.context as? Activity
                if (activity != null) {
                    val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                    if (fullscreen) {
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        controller.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
            }) {
                Icon(Icons.Default.Fullscreen, contentDescription = "На весь екран")
            }
        }
    )
}

/**
 * Таймлайн суток: 24 сегмента по часу, высота 20.dp, скругление 4.dp.
 * Зелёный (#66BB6A, как «Зараз світло») — світло є,
 * красный (#E74C3C, как «Зараз відключення») — блекаут (по слотам BlackoutViewModel).
 */
@Composable
private fun BlackoutTimeline(slots: List<BlackoutSlot>?) {
    val redHours = remember(slots) { computeRedHours(slots) }
    val green = Color(0xFF66BB6A)
    val red = Color(0xFFE74C3C)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (hour in 0 until 24) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (hour in redHours) red else green)
            )
        }
    }
}

/** Часы (0..23), пересекающиеся со слотами блекаута (слот [start,end) → часы красные). */
private fun computeRedHours(slots: List<BlackoutSlot>?): Set<Int> {
    slots ?: return emptySet()
    val red = mutableSetOf<Int>()
    for (s in slots) {
        try {
            val p1 = s.start.split(":")
            val p2 = s.end.split(":")
            val startMin = p1[0].toInt() * 60 + p1[1].toInt()
            val endMin = p2[0].toInt() * 60 + p2[1].toInt()
            for (h in 0..23) {
                if (startMin < (h + 1) * 60 && endMin > h * 60) red.add(h)
            }
        } catch (_: Exception) {
        }
    }
    return red
}

/** Жёлтая плашка блекаута (только при активном отключении). */
@Composable
private fun BlackoutBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x33FFAD00), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFFFFC107),
            fontSize = 12.sp,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Если сейчас идёт блекаут — "Без світла X хв · на акумуляторі",
 * иначе null (плашка скрыта). Данные — BlackoutViewModel (тот же, что в Overview).
 */
private fun activeBlackoutText(resp: BlackoutResponse?): String? {
    resp ?: return null
    return try {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val now = LocalTime.now()
        val slots = resp.schedule?.find { it.date == today }?.slots ?: return null
        for (s in slots) {
            val st = LocalTime.parse(s.start)
            val en = LocalTime.parse(s.end)
            if ((now.isAfter(st) || now == st) && now.isBefore(en)) {
                val mins = ChronoUnit.MINUTES.between(st, now)
                return "Без світла $mins хв · на акумуляторі"
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}

/**
 * Live: таймлайн суток + плашка блекаута + статус батареи,
 * ниже — общая схема потоков (ui/common/FlowScheme.kt, та же, что в Overview).
 */
@Composable
private fun InverterScheme(d: LiveData) {
    // Блекаут (та же BlackoutViewModel, що в Overview)
    val blackoutVm: BlackoutViewModel = viewModel()
    val blackoutState by blackoutVm.state.collectAsState()
    val blackoutText = activeBlackoutText(blackoutState.data)
    val todayKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val todaySlots = blackoutState.data?.schedule?.find { it.date == todayKey }?.slots
    
    // Погода над домом (ШАГ 3)
    val weatherVm: WeatherViewModel = viewModel()
    val weatherState by weatherVm.state.collectAsState()
    val weatherCode = weatherState.data?.current?.code

    Column(modifier = Modifier.fillMaxSize()) {
        // Таймлайн суток (24 сегмента): над плашкой блекаута
        BlackoutTimeline(todaySlots)
        if (blackoutText != null) {
            Spacer(Modifier.height(6.dp))
            BlackoutBanner(blackoutText)
        }
        Spacer(Modifier.height(6.dp))
        BatteryStatusLine(d)
        Spacer(Modifier.height(8.dp))

        // Схема — общая для Live и Overview
        FlowScheme(
            d = d,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            weatherCode = weatherCode
        )
    }
}
