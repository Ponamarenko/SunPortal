package app.sunportal.ui.overview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.OpenMeteoClient
import app.sunportal.api.RetrofitClient
import app.sunportal.model.OpenMeteoResponse
import app.sunportal.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val loading: Boolean = false,
    val data: WeatherData? = null,
    val error: String? = null,
    // Альтернативный источник погоды — Open-Meteo (без ключа). Заполняется по запросу.
    val openMeteo: OpenMeteoResponse? = null,
    val openMeteoError: String? = null
)

class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val r = RetrofitClient.api.getWeatherData()
                _state.value = _state.value.copy(loading = false, data = r)
            } catch (e: Exception) {
                Log.e("Weather", "load failed: $e", e)
                _state.value = WeatherUiState(loading = false, error = e.message)
            }
        }
    }

    /**
     * Погода с Open-Meteo — резервный источник, когда сервер SunPilot недоступен.
     * Координаты передаются вручную: сервер SunPortal их не отдаёт.
     */
    fun loadOpenMeteo(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val r = OpenMeteoClient.api.forecast(latitude = latitude, longitude = longitude)
                _state.value = _state.value.copy(openMeteo = r, openMeteoError = null)
            } catch (e: Exception) {
                Log.e("Weather", "open-meteo load failed: $e", e)
                _state.value = _state.value.copy(openMeteo = null, openMeteoError = e.message)
            }
        }
    }
}