package app.sunportal.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sunportal.api.RetrofitClient
import app.sunportal.model.LiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveUiState(
    val loading: Boolean = true,
    val data: LiveData? = null,
    val error: String? = null
)

class LiveViewModel : ViewModel() {
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
                _uiState.value = LiveUiState(loading = false, data = response.firstOrNull())
            } catch (e: Exception) {
                _uiState.value = LiveUiState(loading = false, error = e.message)
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000)
                loadData()
            }
        }
    }
}
