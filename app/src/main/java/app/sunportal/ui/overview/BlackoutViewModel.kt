package app.sunportal.ui.overview

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.model.BlackoutResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class BlackoutUiState(
    val loading: Boolean = true,
    val data: BlackoutResponse? = null,
    val error: String? = null,
    val statusText: String = "Немає даних про відключення",
    val statusColor: Long = 0xFF9E9E9E
)

@RequiresApi(Build.VERSION_CODES.O)
class BlackoutViewModel : ViewModel() {
    private val _state = MutableStateFlow(BlackoutUiState())
    val state: StateFlow<BlackoutUiState> = _state.asStateFlow()

    init {
        loadData()
        startAutoRefresh()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getBlackout(action = "blackout")
                val status = computeStatus(r)
                _state.value = BlackoutUiState(loading = false, data = r, error = null, statusText = status.first, statusColor = status.second)
            } catch (e: Exception) {
                _state.value = BlackoutUiState(loading = false, data = null, error = e.message, statusText = "Немає даних про відключення", statusColor = 0xFF9E9E9E)
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000)
                loadData()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun computeStatus(resp: BlackoutResponse): Pair<String, Long> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val now = LocalTime.now()
        val day = resp.schedule?.find { it.date == today }
        val slots = day?.slots ?: emptyList()
        for (s in slots) {
            try {
                val st = LocalTime.parse(s.start)
                val en = LocalTime.parse(s.end)
                val minsToStart = ChronoUnit.MINUTES.between(now, st)
                if (now.isAfter(st) && now.isBefore(en)) {
                    return Pair("Зараз відключення", 0xFFE74C3C)
                }
                if (minsToStart >= 0 && minsToStart <= 60) {
                    return Pair("Скоро відключення: ${s.start}–${s.end}", 0xFFFFA726)
                }
            } catch (ex: Exception) {
            }
        }
        return Pair("Зараз світло", 0xFF66BB6A)
    }
}