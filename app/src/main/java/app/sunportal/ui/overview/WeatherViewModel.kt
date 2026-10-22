package app.sunportal.ui.overview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val loading: Boolean = false,
    val data: WeatherData? = null,
    val error: String? = null
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
                _state.value = WeatherUiState(loading = false, data = r)
            } catch (e: Exception) {
                Log.e("Weather", "load failed: $e", e)
                _state.value = WeatherUiState(loading = false, error = e.message)
            }
        }
    }
}