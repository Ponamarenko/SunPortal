package com.deye.mobile.ui.screens.main

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
import com.deye.mobile.data.model.InverterData
import com.deye.mobile.ui.components.InverterStatusCard
import com.deye.mobile.ui.components.SolarPowerCard
import com.deye.mobile.ui.components.BatteryCard
import com.deye.mobile.ui.components.GridLoadCard
import com.deye.mobile.viewmodel.MainViewModel

/**
 * Главный экран приложения
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val inverterData by viewModel.inverterData.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deye Solar Monitor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                    inverterData = inverterData,
                    connectionStatus = connectionStatus,
                    onRefresh = { viewModel.refreshData() }
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
    inverterData: InverterData,
    connectionStatus: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статус подключения
        InverterStatusCard(
            isConnected = connectionStatus,
            isOnline = inverterData.isOnline,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Основные показатели
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SolarPowerCard(
                pv1Power = inverterData.pv1Power ?: 0.0,
                pv2Power = inverterData.pv2Power ?: 0.0,
                pv3Power = inverterData.pv3Power ?: 0.0,
                totalPower = inverterData.totalSolarPower,
                modifier = Modifier.weight(1f)
            )
            
            BatteryCard(
                soc = inverterData.batterySoc,
                voltage = inverterData.batteryVoltage,
                temperature = inverterData.batteryTemp,
                power = inverterData.batteryPower,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Сеть и нагрузка
        GridLoadCard(
            gridVoltage = inverterData.gridVoltage,
            gridFrequency = inverterData.gridFrequency,
            gridPower = inverterData.gridPower,
            loadPower = inverterData.loadPower,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Энергетическая статистика
        EnergyStatsCard(
            dailyPv = inverterData.dailyPvProduction ?: 0.0,
            totalPv = inverterData.totalPvProduction ?: 0.0,
            dailyBuy = inverterData.dailyBuyEnergy ?: 0.0,
            dailySell = inverterData.dailySellEnergy ?: 0.0,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Кнопка обновления
        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Обновить данные")
        }
    }
}

/**
 * Карточка с энергетической статистикой
 */
@Composable
private fun EnergyStatsCard(
    dailyPv: Double,
    totalPv: Double,
    dailyBuy: Double,
    dailySell: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Энергетическая статистика",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Дневная генерация:")
                Text(
                    text = String.format("%.2f kWh", dailyPv),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Всего генерация:")
                Text(
                    text = String.format("%.2f kWh", totalPv),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Дневная покупка:")
                Text(
                    text = String.format("%.2f kWh", dailyBuy),
                    fontWeight = FontWeight.Medium,
                    color = Color.Red
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Дневная продажа:")
                Text(
                    text = String.format("%.2f kWh", dailySell),
                    fontWeight = FontWeight.Medium,
                    color = Color.Green
                )
            }
        }
    }
}