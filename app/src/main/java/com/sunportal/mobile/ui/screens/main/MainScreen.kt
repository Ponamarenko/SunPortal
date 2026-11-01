package com.sunportal.mobile.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunportal.mobile.ui.theme.SolarYellow
import com.sunportal.mobile.ui.theme.BatteryGreen
import com.sunportal.mobile.ui.theme.BatteryOrange
import com.sunportal.mobile.ui.theme.BatteryRed
import com.sunportal.mobile.ui.theme.GridGreen
import com.sunportal.mobile.ui.theme.GridRed
import com.sunportal.mobile.ui.theme.LoadPurple

/**
 * Главный экран приложения SunPortal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val appVersion by remember { mutableStateOf("1.0.0") }
    val buildNumber by remember { mutableStateOf("1") }
    val isLoading by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SunPortal Solar Monitor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.svitgridPrimary,
                    titleContentColor = MaterialTheme.colorScheme.svitgridOnPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                MainContent(
                    appVersion = appVersion,
                    buildNumber = buildNumber,
                    onRefresh = { 
                        // TODO: Implement refresh logic
                    }
                )
            }
        }
    }
}

/**
 * Основной контент экрана
 */
@Composable
private fun MainContent(
    appVersion: String,
    buildNumber: String,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Информация о приложении
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SunPortal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Версия: $appVersion",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Билд: $buildNumber",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "SunPortal - система мониторинга солнечной энергетики",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Кнопка проверки обновлений
        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.svitgridPrimary,
                contentColor = MaterialTheme.colorScheme.svitgridOnPrimary
            )
        ) {
            Text("Перевірка оновлення")
        }
        
        // Будущие функции
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Майбутні функції",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "🌞 Моніторинг сонячної енергії\\n" +
                           "🔋 Управління акумулятором\\n" +
                           "⚡ Параметри мережі\\n" +
                           "📊 Енергетична статистика\\n" +
                           "📱 Мобільний контроль",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )
            }
        }
    }
}