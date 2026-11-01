package app.sunportal.api

import app.sunportal.model.BlackoutResponse
import app.sunportal.model.ForecastData
import app.sunportal.model.HistoryResponse
import app.sunportal.model.LiveData
import app.sunportal.model.TouData
import app.sunportal.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/live/raw")
    suspend fun getLiveData(): List<LiveData>

    @GET("tou_read.php")
    suspend fun getTouData(): TouData

    @GET("forecast_read.php")
    suspend fun getForecastData(): ForecastData

    @GET("weather_read.php")
    suspend fun getWeatherData(): WeatherData

    @GET("api/")
    suspend fun getHistory(
        @Query("action") action: String = "history",
        @Query("period") period: String,
        @Query("fields") fields: String? = null
    ): HistoryResponse

    @GET("api/")
    suspend fun getBlackout(
        @Query("action") action: String = "blackout"
    ): BlackoutResponse
}
