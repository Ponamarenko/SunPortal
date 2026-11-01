package com.sunportal.mobile.network

import com.sunportal.mobile.data.model.InverterData
import com.sunportal.mobile.data.model.SystemStatus
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API сервис для работы с сервером Deye Gateway
 */
interface ApiService {
    
    /**
     * Получить статус системы
     */
    @GET("/api/status")
    suspend fun getStatus(): retrofit2.Response<SystemStatus>
    
    /**
     * Получить live данные
     */
    @GET("/api/live")
    suspend fun getLive(): retrofit2.Response<InverterData>
    
    /**
     * Получить исторические данные
     */
    @GET("/api/history")
    suspend fun getHistory(): retrofit2.Response<Map<String, Any>>
    
    /**
     * Получить данные о погоде
     */
    @GET("/api/weather")
    suspend fun getWeather(): retrofit2.Response<Map<String, Any>>
    
    /**
     * Получить прогноз
     */
    @GET("/api/forecast")
    suspend fun getForecast(): retrofit2.Response<Map<String, Any>>
    
    /**
     * Прочитать конкретные регистры
     */
    @POST("/read")
    suspend fun readRegisters(@Query("addresses") addresses: List<Int>): retrofit2.Response<Map<String, Any>>
    
    /**
     * Записать значение в регистр
     */
    @POST("/write")
    suspend fun writeRegister(
        @Query("address") address: Int,
        @Query("value") value: Double
    ): retrofit2.Response<Map<String, Any>>
    
    /**
     * Аварийная остановка
     */
    @POST("/emergency-stop")
    suspend fun emergencyStop(): retrofit2.Response<Map<String, Any>>
    
    /**
     * Возобновление работы
     */
    @POST("/emergency-resume")
    suspend fun emergencyResume(): retrofit2.Response<Map<String, Any>>
    
    /**
     * Проверить здоровье системы
     */
    @GET("/deye/health")
    suspend fun getHealth(): retrofit2.Response<Map<String, Any>>
}