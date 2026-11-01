package com.sunportal.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sunportal.mobile.ui.theme.GridGreen
import com.sunportal.mobile.ui.theme.GridRed
import com.sunportal.mobile.ui.theme.LoadPurple
import java.text.DecimalFormat

/**
 * Карточка с информацией о сети и нагрузке
 */
@Compose
fun GridLoadCard(
    gridVoltage: Double?,
    gridFrequency: Double?,
    gridPower: Double?,
    loadPower: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                    text = "⚡ Сеть и нагрузка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Параметры сети
            GridParameterRow(
                label = "Напряжение",
                value = gridVoltage?.let { DecimalFormat("#,##0.0").format(it) } ?: "N/A",
                unit = "В"
            )
            
            GridParameterRow(
                label = "Частота",
                value = gridFrequency?.let { DecimalFormat("#,##0.0").format(it) } ?: "N/A",
                unit = "Гц"
            )
            
            // Мощность сети
            GridParameterRow(
                label = "Мощность сети",
                value = gridPower?.let { DecimalFormat("#,##0").format(it) } ?: "N/A",
                unit = "Вт",
                color = if (gridPower != null) {
                    if (gridPower > 0) GridRed else GridGreen
                } else Color.Gray
            )
            
            // Мощность нагрузки
            GridParameterRow(
                label = "Нагрузка",
                value = loadPower?.let { DecimalFormat("#,##0").format(it) } ?: "N/A",
                unit = "Вт",
                color = LoadPurple
            )
        }
    }
}

/**
 * Строка с параметром сети
 */
@Composable
private fun GridParameterRow(
    label: String,
    value: String,
    unit: String,
    color: Color = Color.Unspecified
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
            modifier = Modifier.width(80.dp)
        )
        
        // Пробел для выравнивания
        Spacer(modifier = Modifier.weight(1f))
        
        // Значение
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(60.dp),
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