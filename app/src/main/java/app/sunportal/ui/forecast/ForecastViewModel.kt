package app.sunportal.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.model.ForecastData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForecastUiState(
    val loading: Boolean = true,
    val data: ForecastData? = null,
    val error: String? = null
)

class ForecastViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getForecastData()
                val data = r?.let { ForecastData(
                    updated = it.updated,
                    tomorrow_date = it.tomorrow_date,
                    forecast_solar_kwh = it.forecast_solar_kwh ?: 0f,
                    open_meteo_kwh = it.open_meteo_kwh ?: 0f,
                    solcast_kwh = it.solcast_kwh ?: 0f,
                    average_kwh = it.average_kwh ?: 0f,
                    tomorrow_kwh = it.average_kwh ?: it.tomorrow_kwh ?: 0f,
                    sources = it.sources?.orEmpty() ?: emptyMap(),
                    errors = it.errors?.orEmpty() ?: emptyList(),
                    today_kwh = it.today_kwh ?: 0f,
                    day_after_kwh = it.day_after_kwh ?: 0f,
                    hours = it.hours?.orEmpty() ?: emptyList(),
                    soc_hours = it.soc_hours?.orEmpty() ?: emptyList()
                ) }
                _uiState.value = ForecastUiState(loading = false, data = data)
            } catch (e: Exception) {
                _uiState.value = ForecastUiState(loading = false, error = e.message)
            }
        }
    }
}
