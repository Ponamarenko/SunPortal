package app.sunportal.model

data class WeatherData(
    val ok: Boolean,
    val updated: String,
    val current: WeatherCurrent,
    val days: List<WeatherDay>,
    val hours: List<WeatherHour>
)

data class WeatherCurrent(
    val temp: Float,
    val feels: Float,
    val humidity: Int,
    val wind: Float,
    val code: Int,
    val icon: String,
    val desc: String
)

data class WeatherDay(
    val date: String,
    val temp_max: Float,
    val temp_min: Float,
    val precip: Int,
    val icon: String,
    val desc: String
)

data class WeatherHour(
    val hour: Int,
    val temp: Float,
    val icon: String,
    val precip: Int
)
