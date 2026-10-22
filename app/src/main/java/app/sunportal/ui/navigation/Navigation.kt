package app.sunportal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import app.sunportal.R

data class Tab(
    val id: String,
    val title: String,
    val icon: ImageVector
)

data class TabGroup(
    val title: String,
    val tabs: List<Tab>
)

val tabGroups = listOf(
    TabGroup(
        "Головне",
        listOf(
            Tab("live", "HOME", Icons.Default.PlayArrow),
            Tab("overview", "Огляд", Icons.Default.Home),
            Tab("panels", "Панелі", Icons.Default.Star),
            Tab("battery", "Батарея", Icons.Default.BatteryFull),
        )
    ),
    TabGroup(
        "Аналітика",
        listOf(
            Tab("forecast", "Прогноз", Icons.Default.DateRange),
            Tab("charts", "Графіки", Icons.Default.Info),
        )
    ),
    TabGroup(
        "Налаштування",
        listOf(
            Tab("tariffs", "Тарифи", Icons.Default.AccountBox),
            Tab("tou", "TOU", Icons.Default.Settings),
            Tab("settings", "Налаштування", Icons.Default.Build),
            Tab("about", "Про додаток", Icons.Default.Info),
        )
    )
)

val tabs = tabGroups.flatMap { it.tabs }