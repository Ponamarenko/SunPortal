package app.sunportal.ui.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.data.SettingsRepository
import app.sunportal.model.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LiveUiState(
    val loading: Boolean = true,
    val data: LiveData? = null,
    val error: String? = null,
    /** true — данные из локального кэша (сервер недоступен). */
    val offline: Boolean = false,
    /** Время последнего успешного ответа сервера, мс (0 — неизвестно). */
    val cachedAt: Long = 0L
)

/**
 * Данные станции в реальном времени (опрос каждые 5 с).
 *
 * Последний успешный ответ сохраняется в DataStore — если сервер недоступен,
 * показываются последние известные данные с пометкой «Немає зв'язку».
 */
class LiveViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)
    private val gson = Gson()

    /** true — приложение на экране (опрос 5 с), false — в фоне (опрос 60 с). */
    private var foreground = true

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startAutoRefresh()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getLiveData()
                val d = response.firstOrNull()
                if (d != null) {
                    try {
                        repo.saveLiveCache(gson.toJson(d))
                    } catch (e: Exception) {
                        // кэш — необязателен, его ошибку игнорируем
                    }
                }
                _uiState.value = LiveUiState(
                    loading = false,
                    data = d,
                    cachedAt = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                // Сервер недоступен — показываем последние сохранённые данные
                val cached = repo.liveCache.first()
                val parsed = if (cached.isBlank()) null else
                    runCatching { gson.fromJson(cached, LiveData::class.java) }.getOrNull()
                _uiState.value = LiveUiState(
                    loading = false,
                    data = parsed,
                    error = e.message,
                    offline = parsed != null,
                    cachedAt = parsed?.timestamp?.times(1000L) ?: 0L
                )
            }
        }
    }

    /**
     * Переключить частоту опроса: пока приложение на экране — раз в 5 с,
     * в фоне — раз в минуту (меньше расход батареи и нагрузка на сервер).
     */
    fun setForeground(value: Boolean) {
        if (foreground == value) return
        foreground = value
        if (value) loadData()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(if (foreground) 5000L else 60000L)
                loadData()
            }
        }
    }
}
