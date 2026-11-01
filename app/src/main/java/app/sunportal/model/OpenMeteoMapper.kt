package app.sunportal.model

/**
 * Перевод ответа Open-Meteo в общий формат [WeatherData] — тот же, что отдаёт
 * сервер SunPortal. Благодаря этому карточка погоды (ui/overview/OverviewScreen.kt)
 * и схема потоков (ui/common/FlowScheme.kt) работают с любым источником одинаково.
 */
fun OpenMeteoResponse.toWeatherData(): WeatherData {
    val cur = current
    val code = cur?.weatherCode ?: -1

    val days: List<WeatherDay> = daily?.let { d ->
        d.time?.mapIndexed { i, date ->
            val dcode = d.weatherCode?.getOrNull(i) ?: -1
            WeatherDay(
                date = date.take(10),
                temp_max = d.temperature2mMax?.getOrNull(i) ?: 0f,
                temp_min = d.temperature2mMin?.getOrNull(i) ?: 0f,
                precip = (d.precipitationSum?.getOrNull(i) ?: 0f).toInt(),
                icon = WeatherCodes.icon(dcode),
                desc = WeatherCodes.desc(dcode)
            )
        } ?: emptyList()
    } ?: emptyList()

    return WeatherData(
        ok = true,
        updated = cur?.time?.replace('T', ' ') ?: "",
        current = WeatherCurrent(
            temp = cur?.temperature2m ?: 0f,
            feels = cur?.apparentTemperature ?: 0f,
            humidity = cur?.relativeHumidity2m ?: 0,
            wind = cur?.windSpeed10m ?: 0f,
            code = code,
            icon = WeatherCodes.icon(code),
            desc = WeatherCodes.desc(code)
        ),
        days = days,
        hours = emptyList()
    )
}
