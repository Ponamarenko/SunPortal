package app.sunportal.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Доступні Lottie-анімації (витягнуті з svitgrid, формат bodymovin 5.12.2).
 * Лежать у app/src/main/assets/lottie/
 */
enum class LottieArt(val asset: String) {
    HOME_SUN("lottie/home_sun.json"),
    HOME_NIGHT("lottie/home_night.json"),
    HOME_RAIN("lottie/home_rain.json"),
    HOME_RAIN_NIGHT("lottie/home_rain_night.json"),
    HOME_SNOW("lottie/home_snow.json"),
    HOME_SNOW_NIGHT("lottie/home_snow_night.json"),
    HOME_WINDY("lottie/home_windy.json"),
    HOME_WINDY_NIGHT("lottie/home_windy_night.json"),
    SOLAR("lottie/solar.json"),
    SOLAR_NIGHT("lottie/solar_night.json"),
    SOLAR_PANEL("lottie/solar_panel.json"),
    BATTERY("lottie/battery.json"),
    CHARGER("lottie/charger.json"),
    GENERATOR("lottie/generator.json"),
    INVERTER("lottie/inverter.json"),
    SMART_LOAD("lottie/smart_load.json"),
    TRANSFORMER("lottie/transformer.json"),
    TRANSFORMER_NIGHT("lottie/transformer_night.json"),
    TRANSFORMER_3P("lottie/transformer_3p.json"),
    TRANSFORMER_3P_NIGHT("lottie/transformer_3p_night.json"),
    HOUSEHOLD("lottie/household.json");

    companion object {
        /** Вибір анімації будинку залежно від погоди й часу доби */
        fun homeFor(weather: String?, night: Boolean): LottieArt = when {
            weather == null -> if (night) HOME_NIGHT else HOME_SUN
            weather.contains("rain", true) -> if (night) HOME_RAIN_NIGHT else HOME_RAIN
            weather.contains("snow", true) -> if (night) HOME_SNOW_NIGHT else HOME_SNOW
            weather.contains("wind", true) -> if (night) HOME_WINDY_NIGHT else HOME_WINDY
            night -> HOME_NIGHT
            else -> HOME_SUN
        }
    }
}

/**
 * Показ Lottie-анімації з assets. За замовчуванням — безкінечний цикл.
 */
@Composable
fun LottieArtView(
    art: LottieArt,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    loop: Boolean = true,
    speed: Float = 1f,
    contentScale: ContentScale = ContentScale.Fit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(art.asset)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        speed = speed
    )

    val m = if (size != null) modifier.size(size) else modifier
    Box(modifier = m, contentAlignment = Alignment.Center) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = contentScale,
            modifier = Modifier.size(size ?: 120.dp)
        )
    }
}
