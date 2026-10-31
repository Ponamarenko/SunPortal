package com.deye.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deye.mobile.ui.theme.SolarOrange
import com.deye.mobile.ui.theme.SolarYellow
import java.text.DecimalFormat

/**
 * Карточка с мощностью солнечных панелей
 */
@Composable
fun SolarPowerCard(
    pv1Power: Double,
    pv2Power: Double,
    pv3Power: Double,
    totalPower: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☀️ Солнечные панели",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${DecimalFormat("#,###").format(totalPower.toInt())} Вт",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SolarYellow
                )
            }
            
            // Индикаторы мощности для каждой панели
            SolarPowerIndicator(
                label = "PV1",
                power = pv1Power,
                maxPower = 5000.0,
                color = SolarOrange
            )
            
            SolarPowerIndicator(
                label = "PV2",
                power = pv2Power,
                maxPower = 5000.0,
                color = SolarOrange
            )
            
            SolarPowerIndicator(
                label = "PV3",
                power = pv3Power,
                maxPower = 5000.0,
                color = SolarOrange
            )
        }
    }
}

/**
 * Индикатор мощности для одной панели
 */
@Composable
private fun SolarPowerIndicator(
    label: String,
    power: Double,
    maxPower: Double,
    color: Color
) {
    val percentage = (power / maxPower).coerceIn(0.0, 1.0)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Лейбл
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(30.dp)
        )
        
        // Прогресс бар
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(8.dp)
                    .background(
                        color = color,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
        
        // Значение мощности
        Text(
            text = DecimalFormat("#,##0").format(power.toInt()),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
        
        // Единицы измерения
        Text(
            text = "Вт",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}