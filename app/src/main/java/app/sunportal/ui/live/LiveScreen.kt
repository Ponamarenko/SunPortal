package app.sunportal.ui.live

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.model.BlackoutResponse
import app.sunportal.model.BlackoutSlot
import app.sunportal.model.LiveData
import app.sunportal.model.householdById
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.PVColor
import app.sunportal.ui.theme.BatteryColor
import app.sunportal.ui.theme.GridColor
import app.sunportal.ui.common.BatteryStatusLine
import app.sunportal.ui.common.FlowScheme
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.buildGridStatus
import app.sunportal.ui.notifications.Notifier
import app.sunportal.ui.overview.BlackoutViewModel
import app.sunportal.ui.overview.WeatherViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun LiveScreen(vm: LiveViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val householdId by vm.householdType.collectAsState()
    var selectedNodeState by rememberSaveable { mutableStateOf<String?>(null) }

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
            state.loading && state.data == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            state.error != null && state.data == null -> {
                Text(
                    "Помилка: ${state.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                // При недоступном сервері state.data — останні збережені дані
                state.data?.let { d ->
                    InverterScheme(
                        d = d,
                        householdId = householdId,
                        onNodeClick = { selectedNodeState.value = it },
                        selectedNode = selectedNodeState
                    )
                }
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
    val liveVm: LiveViewModel = viewModel()
    val liveState by liveVm.uiState.collectAsState()
    // Тип домовладения — иконка + название в шапке главной
    val householdId by liveVm.householdType.collectAsState()
    val household = householdById(householdId)
    val cur = weatherState.data?.current
    val view = LocalView.current
    var fullscreen by remember { mutableStateOf(false) }
    var showAlerts by remember { mutableStateOf(false) }
    val alerts by Notifier.history.collectAsState()

    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HOME",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    // Иконка и название типа домовладения — контекст главной
                    Image(
                        painter = painterResource(household.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = household.title,
                        fontSize = 10.sp,
                        color = LabelGray
                    )
                }
                if (cur != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${cur.temp.toInt()}° ${cur.desc}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        // возраст данных: «Щойно» / «5 хв тому» / «2 год тому»
                        val ageMin = remember(liveState.cachedAt) {
                            if (liveState.cachedAt <= 0L) -1L
                            else (System.currentTimeMillis() - liveState.cachedAt) / 60000L
                        }
                        val ageText = when {
                            ageMin < 0 -> ""
                            ageMin < 1 -> "Щойно"
                            ageMin < 60 -> "$ageMin хв тому"
                            else -> "${ageMin / 60} год тому"
                        }
                        if (ageText.isNotEmpty()) {
                            Text(
                                text = ageText,
                                fontSize = 11.sp,
                                color = LabelGray
                            )
                        }
                    }
                }
                if (liveState.offline) {
                    // Сервер недоступен — показаны последние сохранённые данные
                    Text(
                        text = "⚠️ Немає зв'язку — останні дані",
                        fontSize = 11.sp,
                        color = Color(0xFFFFA726)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Меню")
            }
        },
        actions = {
            // Колокольчик → история оповещений (и настройки уведомлений в диалоге)
            IconButton(onClick = { showAlerts = true }) {
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

    // --- История оповещений ---
    if (showAlerts) {
        AlertDialog(
            onDismissRequest = { showAlerts = false },
            title = {
                Text(
                    text = "Сповіщення",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono
                )
            },
            text = {
                if (alerts.isEmpty()) {
                    Text(
                        text = "Поки що немає сповіщень",
                        fontSize = 12.sp,
                        color = LabelGray,
                        fontFamily = JetBrainsMono
                    )
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alerts.forEach { a ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${formatAlertTime(a.time)}  ${a.title}",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = JetBrainsMono
                                )
                                Text(
                                    text = a.text,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = LabelGray,
                                    fontFamily = JetBrainsMono
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAlerts = false }) {
                    Text("Закрити", fontFamily = JetBrainsMono)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAlerts = false
                    val ctx = view.context
                    try {
                        ctx.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                            }
                        )
                    } catch (e: Exception) {
                        // на части прошивок экран настроек уведомлений недоступен
                    }
                }) {
                    Text("Налаштування", fontFamily = JetBrainsMono)
                }
            }
        )
    }
}

/** Время события в формате HH:mm. */
private fun formatAlertTime(ms: Long): String =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

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

/** Часы (0..23), пересекающиеся со слотами блекаута (слот [start,end)) → часы красные. */
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FFAD00))
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
 * ниже — общая схема потоков (ui/common/FlowScheme.kt, та же, що в Overview),
 * нижче — ряд статистики (Сьогодні / Всього) та модальна панель по кліку на вузол.
 */
@Composable
private fun InverterScheme(
    d: LiveData,
    householdId: String,
    onNodeClick: ((String) -> Unit)? = null,
    selectedNode: MutableState<String?>? = null
) {
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
    val windSpeed = weatherState.data?.current?.wind

    Column(modifier = Modifier.fillMaxSize()) {
        // Таймлайн суток (24 сегмента): над плашкою блекаута
        BlackoutTimeline(todaySlots)
        if (blackoutText != null) {
            Spacer(Modifier.height(6.dp))
            BlackoutBanner(blackoutText)
        }
        Spacer(Modifier.height(6.dp))
        BatteryStatusLine(d)
        Spacer(Modifier.height(8.dp))

        // Схема — общая для Live і Overview
        FlowScheme(
            d = d,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            weatherCode = weatherCode,
            windSpeed = windSpeed,
            householdId = householdId,
            onNodeClick = onNodeClick
        )

        // --- Ряд статистики під схемою (Сьогодні / Всього) ---
        StatsRow(d.pv_today, d.pv_total)
    }

    // --- Модальна панель з деталями вузла (Material3, поза Column) ---
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedNode?.value) {
        if (selectedNode?.value != null) {
            scope.launch { sheetState.show() }
        } else {
            scope.launch { sheetState.hide() }
        }
    }
    ModalBottomSheet(
        onDismissRequest = { selectedNode?.value = null },
        sheetState = sheetState
    ) {
        if (selectedNode?.value != null) {
            NodeDetails(d, selectedNode.value!!)
        }
    }
}

/**
 * Ряд статистики під схемою потоків: «Сьогодні» (накопичення за день) та «Всього» (накопичення на жаль).
 * Розрахунок накопичень — PV-колонки: п.с + батарея + сім'я сім'янок / заряд / віддача.
 */
@Composable
private fun StatsRow(pvToday: Float, pvTotal: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SvitgridCard(
            modifier = Modifier.weight(1f),
            title = "Сьогодні",
            value = "${"%.1f".format(pvToday)} кВт",
            accent = PVColor
        )
        SvitgridCard(
            modifier = Modifier.weight(1f),
            title = "Всього",
            value = "${"%.1f".format(pvTotal)} кВт",
            accent = BatteryColor
        )
    }
}

/**
 * Деталі вузла (після кліку) — вміст модальної панелі Material3.
 * Секції за type: дом, батарея, інвертор, сім'я PV-панелей, сім'я генератора/смарт-навантаження.
 */
@Composable
private fun NodeDetails(d: LiveData, nodeKey: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (nodeKey) {
            "home" -> {
                Text(
                    text = "Дом",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = Color.White
                )
                Text(
                    text = "Витрати: ${"%.1f".format(d.p_load)} Вт",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "Енергія: ~${"%.1f".format(d.pv_today)} кВт",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            "battery" -> {
                Text(
                    text = "Батарея",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = BatteryColor
                )
                Text(
                    text = "SOC: ${d.batt_soc}%",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "Напруга: ${"%.1f".format(d.battery_voltage)} В",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            "grid" -> {
                Text(
                    text = "Сім'я",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = GridColor
                )
                Text(
                    text = "Статус: ${buildGridStatus(d.p_grid, d.grid_voltage_l1)}",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            "inv" -> {
                Text(
                    text = "Інвертор",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = PVColor
                )
                Text(
                    text = "Потужність: ${"%.1f".format(d.p_gen ?: 0f)} Вт",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            "pv1", "pv2", "pv3" -> {
                Text(
                    text = "PV${if (nodeKey == "pv1") "1" else if (nodeKey == "pv2") "2" else "3"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = PVColor
                )
                Text(
                    text = "До: ${"%.1f".format(d.p_string1)} Вт",
                    fontSize = 13.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            else -> {
                Text(
                    text = "Вузол: $nodeKey",
                    fontSize = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
        }
    }
}
