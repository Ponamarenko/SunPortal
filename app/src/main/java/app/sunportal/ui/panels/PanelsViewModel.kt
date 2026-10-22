package app.sunportal.ui.panels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.model.LiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PanelsUiState(
    val loading: Boolean = true,
    val data: LiveData? = null,
    val error: String? = null
)

class PanelsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PanelsUiState())
    val uiState: StateFlow<PanelsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        autoRefresh()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getLiveData()
                _uiState.value = PanelsUiState(loading = false, data = r.firstOrNull())
            } catch (e: Exception) {
                _uiState.value = PanelsUiState(loading = false, error = e.message)
            }
        }
    }

    private fun autoRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000)
                loadData()
            }
        }
    }
}