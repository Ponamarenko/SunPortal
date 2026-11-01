package app.sunportal.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.ui.live.LiveViewModel
import app.sunportal.ui.overview.BlackoutViewModel

/**
 * Наблюдатель за состоянием станции: шлёт уведомления, когда
 *  - началось отключение («Зараз відключения»);
 *  - до отключения осталось меньше часа («Скоро отключения: 17:00–18:00»);
 *  - аккумулятор заряжен (SOC ≥ 95%);
 *  - во время отключения заряд аккумулятора упал до 20% и ниже.
 *
 * Каждое событие уведомляется один раз: последний статус/уровень запоминается
 * (rememberSaveable — переживает смену вкладок и поворот экрана).
 */
@Composable
fun AlertsWatcher() {
    val context = LocalContext.current
    val blackoutVm: BlackoutViewModel = viewModel()
    val state by blackoutVm.state.collectAsState()
    val liveVm: LiveViewModel = viewModel()
    val liveState by liveVm.uiState.collectAsState()

    var lastStatus by rememberSaveable { mutableStateOf("") }
    var lastSoc by rememberSaveable { mutableStateOf(-1f) }
    var lowNotified by rememberSaveable { mutableStateOf(false) }

    // --- Отключения ---
    LaunchedEffect(state.statusText) {
        val status = state.statusText
        if (status.isBlank() || status == lastStatus) return@LaunchedEffect
        lastStatus = status

        when {
            status.startsWith("Зараз відключення") -> {
                Notifier.show(
                    context,
                    Notifier.ID_BLACKOUT,
                    "⚡ Відключення світла",
                    "Станція перейшла на живлення від акумулятора"
                )
            }
            status.startsWith("Скоро відключення") -> {
                Notifier.show(
                    context,
                    Notifier.ID_BLACKOUT_SOON,
                    "🔌 Скоро відключення",
                    status.removePrefix("Скоро відключення: ").trim()
                )
            }
        }
    }

    // --- Акумулятор заряжен (переход SOC через 95%) ---
    LaunchedEffect(liveState.data?.batt_soc) {
        val soc = liveState.data?.batt_soc ?: return@LaunchedEffect
        val prev = lastSoc
        lastSoc = soc
        if (prev < 0f) return@LaunchedEffect          // первый замер — без уведомления
        if (soc >= 95f && prev < 95f) {
            Notifier.show(
                context,
                Notifier.ID_BATTERY_FULL,
                "🔋 Акумулятор заряджено",
                "SOC ${soc.toInt()}% — заряд завершено"
            )
        }
    }

    // --- Низкий заряд во время отключения ---
    val blackoutActive = state.statusText.startsWith("Зараз відключення")
    LaunchedEffect(liveState.data?.batt_soc, blackoutActive) {
        val soc = liveState.data?.batt_soc ?: return@LaunchedEffect
        if (!blackoutActive) {
            // отключения нет — сбрасываем флаг, чтобы следующий раз сообщить снова
            lowNotified = false
            return@LaunchedEffect
        }
        if (soc <= 20f && !lowNotified) {
            lowNotified = true
            Notifier.show(
                context,
                Notifier.ID_BATTERY_LOW,
                "⚠️ Низький заряд АКБ",
                "SOC ${soc.toInt()}% — відключення світла триває"
            )
        }
    }
}
