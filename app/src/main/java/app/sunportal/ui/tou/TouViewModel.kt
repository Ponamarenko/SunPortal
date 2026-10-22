package app.sunportal.ui.tou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.model.TouData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TouUiState(
    val loading: Boolean = true,
    val data: TouData? = null,
    val error: String? = null
)

class TouViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TouUiState())
    val uiState: StateFlow<TouUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getTouData()
                _uiState.value = TouUiState(loading = false, data = r)
            } catch (e: Exception) {
                _uiState.value = TouUiState(loading = false, error = e.message)
            }
        }
    }
}