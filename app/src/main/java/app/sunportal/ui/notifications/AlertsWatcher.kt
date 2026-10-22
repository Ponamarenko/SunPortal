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
import app.sunportal.ui.overview.BlackoutViewModel

/**
 * Наблюдатель за состоянием отключений: шлёт уведомления, когда
 *  - началось отключение («Зараз відключення»);
 *  - до отключения осталось меньше часа («Скоро відключення: 17:00–18:00»).
 *
 * Каждое событие уведомляется один раз: последний статус запоминается
 * (rememberSaveable — переживает смену вкладок и поворот экрана).
 */
@Composable
fun AlertsWatcher() {
    val context = LocalContext.current
    val blackoutVm: BlackoutViewModel = viewModel()
    val state by blackoutVm.state.collectAsState()

    var lastStatus by rememberSaveable { mutableStateOf("") }

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
}
