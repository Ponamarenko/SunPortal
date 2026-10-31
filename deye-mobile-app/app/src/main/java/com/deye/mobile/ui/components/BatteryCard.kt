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
import com.deye.mobile.ui.theme.BatteryGreen
import com.deye.mobile.ui.theme.BatteryOrange
import com.deye.mobile.ui.theme.BatteryRed
import java.text.DecimalFormat

/**
 * Карточка с информацией об аккумуляторе
 */
@Composable
fun BatteryCard(
    soc: Double?,
    voltage: Double?,
    temperature: Double?,
    power: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок
            Text(
                text = "🔋 Аккумулятор",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // SOC (уровень заряда)
            BatteryParameterRow(
                label = "Заряд",
                value = soc?.let { DecimalFormat("#,##0").format(it) } ?: "N/A",
                unit = "%",
                color = getBatteryColor(soc),
                showProgress = true,
                progress = soc?.div(100.0) ?: 0.0
            )
            
            // Напряжение
            BatteryParameterRow(
                label = "Напряжение",
                value = voltage?.let { DecimalFormat("#,##0.0").format(it) } ?: "N/A",
                unit = "В"
            )
            
            // Температура
            BatteryParameterRow(
                label = "Температура",
                value = temperature?.let { DecimalFormat("#,##0.0").format(it) } ?: "N/A",
                unit = "°C",
                color = getTemperatureColor(temperature)
            )
            
            // Мощность
            BatteryParameterRow(
                label = "Мощность",
                value = power?.let { DecimalFormat("#,##0").format(it) } ?: "N/A",
                unit = "Вт",
                color = if (power != null) {
                    if (power > 0) BatteryGreen else BatteryOrange
                } else Color.Gray
            )
        }
    }
}

/**
 * Строка с параметром аккумулятора
 */
@Composable
private fun BatteryParameterRow(
    label: String,
    value: String,
    unit: String,
    color: Color = Color.Unspecified,
    showProgress: Boolean = false,
    progress: Double = 0.0
) {
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
            modifier = Modifier.width(60.dp)
        )
        
        if (showProgress) {
            // Прогресс бар для SOC
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.0, 1.0))
                        .height(6.dp)
                        .background(
                            color = color,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        } else {
            // Пробел для выравнивания
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Значение
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
        
        // Единицы измерения
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Получить цвет для уровня заряда
 */
private fun getBatteryColor(soc: Double?): Color {
    return when {
        soc == null -> Color.Gray
        soc >= 80 -> BatteryGreen
        soc >= 50 -> BatteryOrange
        soc >= 20 -> BatteryOrange
        else -> BatteryRed
    }
}

/**
 * Получить цвет для температуры
 */
private fun getTemperatureColor(temperature: Double?): Color {
    return when {
        temperature == null -> Color.Gray
        temperature >= 40 -> Color.Red
        temperature >= 25 -> Color.Green
        temperature >= 10 -> Color.Blue
        else -> Color.Blue
    }
}