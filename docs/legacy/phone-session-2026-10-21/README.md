# Архив телефонной сессии 21.10 (ветка `archive/phone-session-2026-10-21`)

Здесь лежат версии файлов, которые телефонная сессия «Проверка root-доступа к системе»
(21.10 2026) переписала **до** того, как ПК-агент сделал редизайн экранов в стиле Svitgrid.
Они не входят в сборку приложения — это справочный архив, чтобы наработка не потерялась.

## Что здесь

| Файл | Что это |
|---|---|
| `OverviewScreen.kt` | Версия главного экрана с отдельной карточкой `EnergyFlowCard`: Lottie-узлы (солнечная панель, дом, батарея, трансформатор, инвертор) + `AnimatedEnergyPath` с параметрическими кривыми |
| `AnimatedFlow.kt` | Версия анимации потока («рейка» + бегущие точки) с доработками телефонной сессии |

## Почему они не в `app/src`

- `ui/common/FlowScheme.kt` в `main` — более поздняя и общая реализация схемы потоков
  (её используют и Live, и Overview): кривые Безье через `PathMeasure`, бегущие точки со
  стрелками, compact-режим, Svitgrid-тема. Своя `EnergyFlowCard` её бы дублировала.
- Из телефонной версии в `main` перенесено всё, чего в `FlowScheme` не было:
  погодные варианты дома (дождь/снег), управление скоростью Lottie для панелей PV,
  `LottieArt` + 22 анимации из `app/src/main/assets/lottie/`.
- `AnimatedEnergyPath` (`createCurvePath`, `getPointOnCurve`, `quadraticBezierPoint`,
  `CurveType`) — альтернативная математика кривых, не используется: в `main` кривые
  считаются штатным `PathMeasure`.

## Остальная наработка сессии — уже в `main`

- Lottie-ассеты (22 шт.) → `app/src/main/assets/lottie/` + `docs/lottie-svitgrid.md`
- Компонент `LottieArt` → `app/src/main/java/app/sunportal/ui/components/LottieArt.kt`
- Обновление сборки (SDK 36, JVM 17, Compose plugin, `libs.versions.toml`) — совпало
  с версией в репозитории, дублировать не пришлось
- Open-Meteo → `model/OpenMeteo.kt`, `api/OpenMeteoApi.kt`, `api/OpenMeteoClient.kt`
- Общие хелперы UI → `ui/components/Components.kt`
