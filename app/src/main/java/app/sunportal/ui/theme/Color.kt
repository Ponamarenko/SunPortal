package app.sunportal.ui.theme

import androidx.compose.ui.graphics.Color

val Sun80 = Color(0xFFFFD8A8)
val SunGrey80 = Color(0xFFE0C3A0)
val Sunrise80 = Color(0xFFFFB4A2)

val Sun40 = Color(0xFF8A5000)
val SunGrey40 = Color(0xFF6F5B3E)
val Sunrise40 = Color(0xFF8C4A3C)

// Точная цветовая схема SvitGrid
val SvitScreenBg = Color(0xFF0A0D12)           // Фон экрана
val SvitCardTop = Color(0xFF131A24)             // Верхняя часть карточки
val SvitCardBottom = Color(0xFF0A0D12)          // Нижняя часть карточки
val SvitCardGradient = listOf(SvitCardTop, SvitCardBottom)

// Основные цвета энергии (точные значения из SvitGrid)
val PVColor = Color(0xFF00A326)                // Солнечная энергия - ярко-зеленый
val LoadColor = Color(0xFF2196F3)               // Потребление - ярко-синий
val BatteryColor = Color(0xFFFF9800)            // Батарея - оранжевый
val GridColor = Color(0xFF9C27B0)              // Сеть - фиолетовый
val GeneratorColor = Color(0xFFFF5722)          // Генератор - темно-оранжевый
val SmartLoadColor = Color(0xFF9C27B0)          // Умная нагрузка - фиолетовый

// Дополнительные цвета для точного соответствия
val PvActiveColor = Color(0xFF00A326)           // Активный PV - ярко-зеленый
val LabelGray = Color(0xB3FFFFFF)              // Серый для подписей
val ActiveGlow = Color(0x40FFFFFF)              // Свечение для активных элементов
val InactiveGray = Color(0xFF808080)            // Неактивный серый

// Цвета для статусов
val StatusOnline = Color(0xFF4CAF50)           // Онлайн - зеленый
val StatusOffline = Color(0xFFFF9800)           // Оффлайн - оранжевый
val StatusError = Color(0xFFF44336)             // Ошибка - красный
val StatusWarning = Color(0xFFFFC107)           // Предупреждение - желтый
