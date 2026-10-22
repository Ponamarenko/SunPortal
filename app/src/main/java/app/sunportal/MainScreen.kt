package app.sunportal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.data.SettingsRepository
import app.sunportal.ui.about.AboutScreen
import app.sunportal.ui.battery.BatteryScreen
import app.sunportal.ui.charts.ChartsScreen
import app.sunportal.ui.forecast.ForecastScreen
import app.sunportal.ui.live.LiveScreen
import app.sunportal.ui.live.LiveTopBar
import app.sunportal.ui.navigation.tabGroups
import app.sunportal.ui.navigation.tabs
import app.sunportal.ui.overview.OverviewScreen
import app.sunportal.ui.panels.PanelsScreen
import app.sunportal.ui.settings.SettingsScreen
import app.sunportal.ui.tou.TouScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { SettingsRepository(context) }
    var selectedTab by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val menuFont by repo.menuFont.collectAsState(initial = "medium")
    val expandedSaved by repo.drawerExpandedGroups.collectAsState(initial = emptySet())

    val menuFontSp = when (menuFont) {
        "small" -> 14.sp
        "large" -> 18.sp
        else -> 16.sp
    }

    val expandedGroups = remember {
        androidx.compose.runtime.mutableStateMapOf<String, Boolean>().apply {
            tabGroups.forEach { put(it.title, false) }
        }
    }

    LaunchedEffect(expandedSaved) {
        expandedGroups.clear()
        tabGroups.forEach { group ->
            expandedGroups[group.title] = expandedSaved.contains(group.title)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SUN PORTAL",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()

                    tabGroups.forEach { group ->
                        val isExpanded = expandedGroups[group.title] == true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedGroups[group.title] = !isExpanded
                                    scope.launch {
                                        repo.saveDrawerExpandedGroups(expandedGroups.filter { it.value }.keys.toList())
                                    }
                                }
                                .padding(horizontal = 28.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Згорнути" else "Розгорнути",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isExpanded) {
                            group.tabs.forEach { tab ->
                                val index = tabs.indexOf(tab)
                                NavigationDrawerItem(
                                    label = { Text(tab.title, fontSize = menuFontSp) },
                                    selected = selectedTab == index,
                                    onClick = {
                                        selectedTab = index
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = { Icon(tab.icon, contentDescription = null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (selectedTab == 0) {
                    // Live: шапка в стиле Svitgrid — HOME + погода + колокольчик + fullscreen
                    LiveTopBar(onMenu = { scope.launch { drawerState.open() } })
                } else {
                    TopAppBar(
                        title = { Text(tabs[selectedTab].title) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> LiveScreen()
                    1 -> OverviewScreen()
                    2 -> PanelsScreen()
                    3 -> BatteryScreen()
                    4 -> ForecastScreen()
                    5 -> ChartsScreen()
                    6 -> app.sunportal.ui.tariffs.TariffsScreen()
                    7 -> TouScreen()
                    8 -> SettingsScreen()
                    9 -> AboutScreen()
                }
            }
        }
    }
}