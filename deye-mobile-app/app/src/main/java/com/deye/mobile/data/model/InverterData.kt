package com.deye.mobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Модель данных для инвертора Deye
 */
data class InverterData(
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("battery_soc")
    val batterySoc: Double?,
    
    @SerializedName("battery_voltage")
    val batteryVoltage: Double?,
    
    @SerializedName("battery_temp")
    val batteryTemp: Double?,
    
    @SerializedName("battery_power")
    val batteryPower: Double?,
    
    @SerializedName("battery_current")
    val batteryCurrent: Double?,
    
    @SerializedName("pv1_power")
    val pv1Power: Double?,
    
    @SerializedName("pv2_power")
    val pv2Power: Double?,
    
    @SerializedName("pv3_power")
    val pv3Power: Double?,
    
    @SerializedName("total_pv_power")
    val totalPvPower: Double?,
    
    @SerializedName("grid_voltage")
    val gridVoltage: Double?,
    
    @SerializedName("grid_frequency")
    val gridFrequency: Double?,
    
    @SerializedName("grid_power")
    val gridPower: Double?,
    
    @SerializedName("load_power")
    val loadPower: Double?,
    
    @SerializedName("daily_pv_production")
    val dailyPvProduction: Double?,
    
    @SerializedName("total_pv_production")
    val totalPvProduction: Double?,
    
    @SerializedName("daily_buy_energy")
    val dailyBuyEnergy: Double?,
    
    @SerializedName("daily_sell_energy")
    val dailySellEnergy: Double?,
    
    @SerializedName("inverter_status")
    val inverterStatus: String?
) {
    // Вычисляемые свойства
    val totalSolarPower: Double
        get() = (pv1Power ?: 0.0) + (pv2Power ?: 0.0) + (pv3Power ?: 0.0)
    
    val isOnline: Boolean
        get() = inverterStatus == "online"
    
    fun getBatteryLevel(): String {
        return when {
            batterySoc == null -> "N/A"
            batterySoc >= 80 -> "Высокий"
            batterySoc >= 50 -> "Средний"
            batterySoc >= 20 -> "Низкий"
            else -> "Критический"
        }
    }
    
    fun getTemperatureStatus(): String {
        return when {
            batteryTemp == null -> "N/A"
            batteryTemp >= 40 -> "Высокая"
            batteryTemp >= 25 -> "Нормальная"
            batteryTemp >= 10 -> "Низкая"
            else -> "Критическая"
        }
    }
    
    companion object {
        fun createEmpty(): InverterData {
            return InverterData(
                timestamp = Date().toString(),
                batterySoc = null,
                batteryVoltage = null,
                batteryTemp = null,
                batteryPower = null,
                batteryCurrent = null,
                pv1Power = null,
                pv2Power = null,
                pv3Power = null,
                totalPvPower = null,
                gridVoltage = null,
                gridFrequency = null,
                gridPower = null,
                loadPower = null,
                dailyPvProduction = null,
                totalPvProduction = null,
                dailyBuyEnergy = null,
                dailySellEnergy = null,
                inverterStatus = "offline"
            )
        }
    }
}

/**
 * Модель статуса системы
 */
data class SystemStatus(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("app")
    val app: String,
    
    @SerializedName("version")
    val version: String,
    
    @SerializedName("inverter_connected")
    val inverterConnected: Boolean,
    
    @SerializedName("inverter_model")
    val inverterModel: String,
    
    @SerializedName("modbus_status")
    val modbusStatus: String
)

/**
 * Модель для WebSocket сообщений
 */
data class WebSocketMessage(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("data")
    val data: InverterData,
    
    @SerializedName("timestamp")
    val timestamp: String
)