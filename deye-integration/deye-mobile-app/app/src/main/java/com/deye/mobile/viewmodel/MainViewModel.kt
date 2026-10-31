package com.deye.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deye.mobile.data.model.InverterData
import com.deye.mobile.repository.InverterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для главного экрана
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InverterRepository
) : ViewModel() {
    
    // Private mutable state
    private val _inverterData = MutableStateFlow(InverterData.createEmpty())
    private val _connectionStatus = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    
    // Public immutable state
    val inverterData: StateFlow<InverterData> = _inverterData.asStateFlow()
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // WebSocket connection state
    private val _isWebSocketConnected = MutableStateFlow(false)
    val isWebSocketConnected: StateFlow<Boolean> = _isWebSocketConnected.asStateFlow()
    
    init {
        // Start initial data loading
        refreshData()
        
        // Start WebSocket connection for real-time updates
        viewModelScope.launch {
            repository.startWebSocketConnection()
        }
        
        // Listen for WebSocket updates
    viewModelScope.launch {
        repository.inverterDataFlow
            .collect { data ->
                _inverterData.value = data
                _isLoading.value = false
            }
    }
    
    viewModelScope.launch {
        repository.connectionStatusFlow
            .collect { status ->
                _connectionStatus.value = status
                _isWebSocketConnected.value = status
            }
    }
    }
    
    /**
     * Обновить данные вручную
     */
    fun refreshData() {
        if (_isLoading.value) return
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val data = repository.getInverterData()
                _inverterData.value = data
                _connectionStatus.value = true
            } catch (e: Exception) {
                // Keep current data, just update connection status
                _connectionStatus.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Получить live данные (для тестирования)
     */
    fun getLiveData() = repository.getInverterData()
    
    /**
     * Проверить статус подключения
     */
    fun checkConnectionStatus() {
        viewModelScope.launch {
            val isConnected = repository.checkConnection()
            _connectionStatus.value = isConnected
        }
    }
    
    /**
     * Очистка ресурсов при уничтожении ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.closeWebSocketConnection()
        }
    }
}