package app.sunportal.model

import com.google.gson.annotations.SerializedName

/**
 * Ответ Open-Meteo (https://api.open-meteo.com) — альтернативный источник погоды.
 * Бесплатно, без ключа и без авторизации.
 *
 * Перенесено из телефонной сессии 21.10. `current`/`daily` помечены nullable:
 * Gson создаёт объект минуя конструктор, и при неполном ответе поля остались бы null.
 */
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: OpenMeteoCurrent? = null,
    val daily: OpenMeteoDaily? = null
)

data class OpenMeteoCurrent(
    val time: String? = null,
    @SerializedName("temperature_2m")
    val temperature2m: Float? = null,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Int? = null,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Float? = null,
    @SerializedName("precipitation")
    val precipitation: Float? = null,
    @SerializedName("weather_code")
    val weatherCode: Int? = null,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Float? = null,
    @SerializedName("is_day")
    val isDay: Int? = null
)

data class OpenMeteoDaily(
    val time: List<String>? = null,
    @SerializedName("weather_code")
    val weatherCode: List<Int>? = null,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Float>? = null,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Float>? = null,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Float>? = null,
    @SerializedName("sunrise")
    val sunrise: List<String>? = null,
    @SerializedName("sunset")
    val sunset: List<String>? = null
)
