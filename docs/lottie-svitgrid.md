# Lottie-анимации (извлечены из svitgrid)

Извлечены из `svitgrid.ver.1.0.242.build.300.apks` → `assets/flutter_assets/assets/lottie/`.
Формат: **bodymovin 5.12.2**, совместим с `lottie-compose` для Android.

Файлы лежат в `app/src/main/assets/lottie/` (22 шт., 2.4 МБ).
Компонент доступа — `app/src/main/java/app/sunportal/ui/components/LottieArt.kt`.

> Документ вынесен из `assets/`, чтобы README не попадал в APK.

## Использование в Compose

```kotlin
import app.sunportal.ui.components.LottieArt
import app.sunportal.ui.components.LottieArtView

// простая анимация дома (день)
LottieArtView(
    art = LottieArt.HOME_SUN,
    size = 160.dp
)

// умный выбор по погоде/времени суток
val art = LottieArt.homeFor(weather = "rain", night = true) // -> HOME_RAIN_NIGHT
LottieArtView(art = art, size = 200.dp)

// однократное проигрывание с ускорением
LottieArtView(
    art = LottieArt.BATTERY,
    loop = false,
    speed = 1.5f,
    size = 96.dp
)
```

## Список анимаций

| Ассет | Enum | Что показывает |
|---|---|---|
| `home_sun` / `home_night` | `HOME_SUN` / `HOME_NIGHT` | Дом, солнце/ночь |
| `home_rain` / `home_rain_night` | `HOME_RAIN` / `HOME_RAIN_NIGHT` | Дом в дождь |
| `home_snow` / `home_snow_night` | `HOME_SNOW` / `HOME_SNOW_NIGHT` | Дом в снег |
| `home_windy` / `home_windy_night` | `HOME_WINDY` / `HOME_WINDY_NIGHT` | Дом, ветер |
| `solar` / `solar_night` | `SOLAR` / `SOLAR_NIGHT` | Солнечная панель |
| `solar_panel` | `SOLAR_PANEL` | Панель отдельно |
| `battery` | `BATTERY` | Батарея |
| `charger` | `CHARGER` | Зарядное устройство |
| `generator` | `GENERATOR` | Генератор |
| `inverter` | `INVERTER` | Инвертор |
| `smart_load` | `SMART_LOAD` | Умная нагрузка |
| `transformer` / `_night` | `TRANSFORMER` / `TRANSFORMER_NIGHT` | Трансформатор (1 фаза) |
| `transformer_3p` / `_night` | `TRANSFORMER_3P` / `TRANSFORMER_3P_NIGHT` | Трансформатор (3 фазы) |
| `household` | `HOUSEHOLD` | Домохозяйство |

## Зависимость

В `app/build.gradle.kts` уже есть:
```
implementation("com.airbnb.android:lottie-compose:6.4.0")
```
Версия 6.4.0 поддерживает весь используемый API (`LottieCompositionSpec.Asset`,
`animateLottieCompositionAsState`) — обновление не требуется.

## Примечание

- `test_minimal.json` — служебный, можно удалить.
- Часть анимаций (`solar`, `solar_night`, `home_sun`, `home_night`, `battery`, `inverter`)
  продублирована в `app/src/main/res/raw/` — там же на них ссылается `FlowScheme.kt`.
  Дубли в `assets/lottie/` оставлены, чтобы `LottieArt` был рабочим для всех значений enum.
- Файлы — художественная графика svitgrid; при публикации в маркет проверьте лицензию оригинала.
