package com.deye.mobile.repository

import com.deye.mobile.data.model.InverterData
import com.deye.mobile.data.model.SystemStatus
import com.deye.mobile.data.model.WebSocketMessage
import com.deye.mobile.network.ApiService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с данными инвертора Deye
 */
@Singleton
class InverterRepository @Inject constructor(
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient
) {
    
    private val _inverterDataFlow = MutableStateFlow(InverterData.createEmpty())
    val inverterDataFlow: StateFlow<InverterData> = _inverterDataFlow.asStateFlow()
    
    private val _connectionStatusFlow = MutableStateFlow(false)
    val connectionStatusFlow: StateFlow<Boolean> = _connectionStatusFlow.asStateFlow()
    
    private var webSocket: WebSocket? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    // Конфигурация
    private val config = ApiConfig(
        baseUrl = "http://192.168.25.117:8800", // IP нашего сервера
        timeout = 30L,
        retryAttempts = 3
    )
    
    /**
     * Получить данные инвертора через REST API
     */
    suspend fun getInverterData(): InverterData {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLive()
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        _connectionStatusFlow.value = true
                        data
                    } ?: InverterData.createEmpty()
                } else {
                    _connectionStatusFlow.value = false
                    InverterData.createEmpty()
                }
            } catch (e: Exception) {
                _connectionStatusFlow.value = false
                InverterData.createEmpty()
            }
        }
    }
    
    /**
     * Проверить статус подключения
     */
    suspend fun checkConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStatus()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Запустить WebSocket соединение для real-time обновлений
     */
    fun startWebSocketConnection() {
        if (webSocket != null) return
        
        scope.launch {
            try {
                val request = Request.Builder()
                    .url("${config.baseUrl}/ws")
                    .build()
                
                webSocket = okHttpClient.newWebSocket(request, WebSocketListener())
                
                // Перезапуск соединения при обрыве
                while (webSocket != null) {
                    delay(30000) // Проверять каждые 30 секунд
                    if (webSocket?.closeCode != null) {
                        restartWebSocket()
                    }
                }
            } catch (e: Exception) {
                _connectionStatusFlow.value = false
                restartWebSocket()
            }
        }
    }
    
    /**
     * Перезапустить WebSocket соединение
     */
    private fun restartWebSocket() {
        scope.launch {
            delay(5000) // Задержка перед перезапуском
            if (webSocket?.closeCode != null) {
                webSocket?.close(1000, "Restarting")
                webSocket = null
                startWebSocketConnection()
            }
        }
    }
    
    /**
     * Закрыть WebSocket соединение
     */
    fun closeWebSocketConnection() {
        scope.cancel()
        webSocket?.close(1000, "Closing")
        webSocket = null
    }
    
    /**
     * Получить статус системы
     */
    suspend fun getSystemStatus(): SystemStatus {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStatus()
                response.body() ?: SystemStatus("error", 0, "unknown", "0.0", false, "unknown", "disconnected")
            } catch (e: Exception) {
                SystemStatus("error", 0, "unknown", "0.0", false, "unknown", "disconnected")
            }
        }
    }
    
    /**
     * Вспомогательный класс для конфигурации
     */
    private data class ApiConfig(
        val baseUrl: String,
        val timeout: Long,
        val retryAttempts: Int
    )
    
    /**
     * WebSocket listener для обработки сообщений
     */
    private inner class WebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionStatusFlow.value = true
            println("WebSocket connection opened")
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val json = JSONObject(text)
                val type = json.optString("type")
                
                when (type) {
                    "live_data" -> {
                        val dataJson = json.getJSONObject("data")
                        val inverterData = parseInverterData(dataJson)
                        _inverterDataFlow.value = inverterData
                    }
                    "status" -> {
                        val status = json.optBoolean("connection_status", false)
                        _connectionStatusFlow.value = status
                    }
                }
            } catch (e: Exception) {
                println("Error parsing WebSocket message: ${e.message}")
            }
        }
        
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // Handle binary messages if needed
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            _connectionStatusFlow.value = false
            println("WebSocket closing: $code / $reason")
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionStatusFlow.value = false
            println("WebSocket closed: $code / $reason")
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionStatusFlow.value = false
            println("WebSocket failure: ${t.message}")
            restartWebSocket()
        }
        
        /**
         * Парсинг JSON данных в InverterData
         */
        private fun parseInverterData(json: JSONObject): InverterData {
            return InverterData(
                timestamp = json.optString("timestamp", ""),
                batterySoc = json.optDouble("battery_soc", 0.0),
                batteryVoltage = json.optDouble("battery_voltage", 0.0),
                batteryTemp = json.optDouble("battery_temp", 0.0),
                batteryPower = json.optDouble("battery_power", 0.0),
                batteryCurrent = json.optDouble("battery_current", 0.0),
                pv1Power = json.optDouble("pv1_power", 0.0),
                pv2Power = json.optDouble("pv2_power", 0.0),
                pv3Power = json.optDouble("pv3_power", 0.0),
                totalPvPower = json.optDouble("total_pv_power", 0.0),
                gridVoltage = json.optDouble("grid_voltage", 0.0),
                gridFrequency = json.optDouble("grid_frequency", 0.0),
                gridPower = json.optDouble("grid_power", 0.0),
                loadPower = json.optDouble("load_power", 0.0),
                dailyPvProduction = json.optDouble("daily_pv_production", 0.0),
                totalPvProduction = json.optDouble("total_pv_production", 0.0),
                dailyBuyEnergy = json.optDouble("daily_buy_energy", 0.0),
                dailySellEnergy = json.optDouble("daily_sell_energy", 0.0),
                inverterStatus = json.optString("inverter_status", "offline")
            )
        }
    }
}