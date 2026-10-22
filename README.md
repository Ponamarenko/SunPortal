# SunPortal

Android-додаток для моніторингу сонячної станції: схема потоків енергії (PV → інвертор →
батарея / мережа → дім), погода, блекаути, прогноз виробітку, тарифи та TOU.

Стиль інтерфейсу — як у «Svitgrid» (Flutter): темна тема, моноширинний шрифт JetBrains Mono,
градієнтні картки `#131A24 → #0A0D12`, анімації Lottie з оригінальних ассетів svitgrid.

## Стек

| Частина | Версія |
|---|---|
| Kotlin | 1.9.24 |
| AGP | 8.5.2 |
| Gradle | 8.7 |
| compileSdk / targetSdk | 34 |
| minSdk | 26 (Android 8.0) |
| Compose BOM | 2024.06.00 |
| Retrofit / Gson | 2.11.0 |
| Lottie Compose | 6.4.0 |
| DataStore Preferences | 1.1.1 |

Актуальные версии тулчейна (SDK 36 / Kotlin 2.0.21 / Gradle 8.9) — в ветке
`feat/build-sdk36-kotlin2` (PR #2), ждёт сборки на ПК.

## Сборка (Windows)

```bat
build_now.bat                       :: assembleDebug
build_now.bat clean assembleDebug   ;; полная пересборка
```

Скрипт выставляет `JAVA_HOME=C:\Users\xAnder\.jdks\jbr-21.0.11` (JDK 21).
APK: `app\build\outputs\apk\debug\app-debug.apk`.

## Структура

```
app/src/main/java/app/sunportal/
├── MainActivity.kt, MainScreen.kt      — вход + вкладки (10 экранов)
├── api/          RetrofitClient, ApiService, OpenMeteoApi/Client
├── model/        LiveData, WeatherData, OpenMeteo*, WeatherCodes
├── data/         SettingsRepository (DataStore)
└── ui/
    ├── live/         HOME — таймлайн суток, блекаут, схема потоков
    ├── overview/     Огляд — карточка погоды, схема, блекаут
    ├── common/       FlowScheme.kt — общая схема потоков (Live + Огляд)
    ├── components/   LottieArt.kt, Components.kt (SectionTitle, GlowCard, StatTile, Pill)
    ├── panels/, battery/, forecast/, charts/, tariffs/, tou/, settings/, about/
```

## Источники погоды

`Налаштування → Джерело погоди`:

| Режим | Поведение |
|---|---|
| **Авто** | сначала сервер (`weather_read.php`), при его недоступности — Open-Meteo |
| **Сервер** | только сервер SunPortal |
| **Open-Meteo** | только Open-Meteo (https://api.open-meteo.com), без ключа |

Для Open-Meteo нужны координаты объекта (широта/долгота) — вводятся там же.
Ответ Open-Meteo переводится в общий формат `WeatherData` (`model/OpenMeteoMapper.kt`),
поэтому карточка погоды и схема потоков работают одинаково для обоих источников.
Погода обновляется автоматически каждые 10 минут; источник показан бейджем в карточке.

## Документация

- `docs/lottie-svitgrid.md` — список Lottie-ассетов и как их использовать.
- `AGENTS.md` — правила работы с репозиторием (git-синхронизация, сборка, релиз).

## Лицензия ассетов

Lottie-файлы в `app/src/main/assets/lottie/` — художественная графика из приложения
svitgrid (Flutter). Перед публикацией в Google Play нужно проверить лицензию оригинала.
