package app.sunportal.model

data class HourlyForecast(
    val hour: Int,
    val predicted_kwh: Float,
    val p90_kwh: Float,
    val p10_kwh: Float
)

data class SocHour(
    val hour: Int,
    val soc: Float
)

data class ForecastData(
    val updated: String,
    val tomorrow_date: String,
    val forecast_solar_kwh: Float? = 0f,
    val open_meteo_kwh: Float? = 0f,
    val solcast_kwh: Float? = 0f,
    val average_kwh: Float? = 0f,
    val tomorrow_kwh: Float? = 0f,
    val sources: Map<String, Float>? = emptyMap(),
    val errors: List<String>? = emptyList(),
    val today_kwh: Float? = 0f,
    val day_after_kwh: Float? = 0f,
    val hours: List<HourlyForecast>? = emptyList(),
    val soc_hours: List<SocHour>? = emptyList()
)