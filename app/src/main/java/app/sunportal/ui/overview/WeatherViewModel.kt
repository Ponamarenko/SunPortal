package app.sunportal.ui.overview

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.OpenMeteoClient
import app.sunportal.api.RetrofitClient
import app.sunportal.data.SettingsRepository
import app.sunportal.data.WEATHER_SOURCE_OPEN_METEO
import app.sunportal.data.WEATHER_SOURCE_SERVER
import app.sunportal.model.OpenMeteoResponse
import app.sunportal.model.WeatherData
import app.sunportal.model.toWeatherData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Период автообновления погоды, мс (10 минут). */
private const val WEATHER_REFRESH_MS = 10L * 60L * 1000L

data class WeatherUiState(
    val loading: Boolean = false,
    val data: WeatherData? = null,
    val error: String? = null,
    // Какой источник дал текущие данные: "server" | "open-meteo"
    val source: String = WEATHER_SOURCE_SERVER,
    // Сырой ответ Open-Meteo (для ручного запроса и отладки)
    val openMeteo: OpenMeteoResponse? = null,
    val openMeteoError: String? = null
)

/**
 * Погода для карточки и схемы потоков.
 *
 * Источники:
 *  - сервер SunPortal (`weather_read.php`) — по умолчанию;
 *  - Open-Meteo (https://api.open-meteo.com) — без ключа, координаты из настроек.
 *
 * Режим "auto": сначала сервер, при его недоступности — Open-Meteo (если заданы координаты).
 */
class WeatherViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        load()
        startAutoRefresh()
    }

    /** Автообновление погоды каждые 10 минут (данные сервера обновляются редко). */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(WEATHER_REFRESH_MS)
                load()
            }
        }
    }

    /** Загрузить погоду согласно выбранному в настройках источнику. */
    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            val source = repo.weatherSource.first()
            val lat = repo.weatherLat.first().toDoubleOrNull()
            val lon = repo.weatherLon.first().toDoubleOrNull()

            // --- Явный выбор Open-Meteo ---
            if (source == WEATHER_SOURCE_OPEN_METEO) {
                if (lat == null || lon == null) {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Open-Meteo: не задано координати"
                    )
                    return@launch
                }
                val r = fetchOpenMeteo(lat, lon)
                if (r != null) {
                    _state.value = _state.value.copy(
                        loading = false,
                        data = r.toWeatherData(),
                        source = WEATHER_SOURCE_OPEN_METEO,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Open-Meteo: немає відповіді"
                    )
                }
                return@launch
            }

            try {
                val r = RetrofitClient.api.getWeatherData()
                _state.value = _state.value.copy(
                    loading = false,
                    data = r,
                    source = WEATHER_SOURCE_SERVER,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("Weather", "load failed: $e", e)
                // В режиме "auto" при недоступном сервере — запасной Open-Meteo
                val fallback = if (source == WEATHER_SOURCE_AUTO && lat != null && lon != null) {
                    fetchOpenMeteo(lat, lon)?.toWeatherData()
                } else null

                if (fallback != null) {
                    _state.value = _state.value.copy(
                        loading = false,
                        data = fallback,
                        source = WEATHER_SOURCE_OPEN_METEO,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
            }
        }
    }

    /** Явный запрос к Open-Meteo по заданным координатам. */
    fun loadOpenMeteo(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val r = fetchOpenMeteo(latitude, longitude)
            _state.value = if (r != null) {
                _state.value.copy(
                    loading = false,
                    data = r.toWeatherData(),
                    source = WEATHER_SOURCE_OPEN_METEO,
                    error = null
                )
            } else {
                _state.value.copy(loading = false, error = "Open-Meteo: немає відповіді")
            }
        }
    }

    private suspend fun fetchOpenMeteo(lat: Double, lon: Double): OpenMeteoResponse? {
        return try {
            val r = OpenMeteoClient.api.forecast(latitude = lat, longitude = lon)
            _state.value = _state.value.copy(openMeteo = r, openMeteoError = null)
            r
        } catch (e: Exception) {
            Log.e("Weather", "open-meteo load failed: $e", e)
            _state.value = _state.value.copy(openMeteo = null, openMeteoError = e.message)
            null
        }
    }
}
