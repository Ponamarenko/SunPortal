package app.sunportal.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import app.sunportal.ui.theme.*

/**
 * Улучшенный компонент статистики с анимациями и трендами
 */
@Composable
fun EnhancedStatsCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    trend: String? = null,
    trendColor: Color = Color.Green,
    icon: (@Composable () -> Unit)? = null,
    progress: Float = 0f,
    showProgress: Boolean = false
) {
    val targetAlpha = if (progress > 0f) 0.2f else 0.05f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(600)
    )
    
    SvitgridEnhancedCard(
        modifier = modifier,
        corner = 16.dp,
        borderColor = color,
        borderWidth = 1.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Заголовок с иконкой
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    icon?.let {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color.copy(alpha = 0.2f), CircleShape)
                                .padding(4.dp)
                        ) {
                            it()
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = SvitCardTitleStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Тренд
                    trend?.let {
                        SvitgridPill(
                            text = it,
                            color = trendColor,
                            active = true
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Значение с единицами измерения
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = value,
                        style = SvitValueStyle,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = SvitSubValueStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                // Прогресс бар
                if (showProgress) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        color.copy(alpha = 0.3f),
                                        color.copy(alpha = 0.1f)
                                    )
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            color,
                                            color.copy(alpha = 0.8f)
                                        )
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                                .width(progress.coerceIn(0f, 1f) * 100.dp)
                        )
                    }
                }
            }
        }
    )
}

/**
 * Компонент для отображения статистики с анимированным графиком
 */
@Composable
fun AnimatedStatsCard(
    title: String,
    currentValue: Float,
    maxValue: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    historicalData: List<Float> = emptyList(),
    icon: (@Composable () -> Unit)? = null
) {
    val progress by animateFloatAsState(
        targetValue = currentValue / maxValue.coerceAtLeast(1f),
        animationSpec = tween(1000, easing = androidx.compose.animation.core.LinearEasing)
    )
    
    SvitgridGlowCard(
        modifier = modifier,
        corner = 16.dp,
        glowColor = color,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Заголовок
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    icon?.let {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color.copy(alpha = 0.2f), CircleShape)
                                .padding(4.dp)
                        ) {
                            it()
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = SvitCardTitleStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Значение
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "%.1f".format(currentValue),
                        style = SvitValueStyle,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = SvitSubValueStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                // Прогресс бар
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    color.copy(alpha = 0.3f),
                                    color.copy(alpha = 0.1f)
                                )
                            ),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        color,
                                        color.copy(alpha = 0.8f)
                                    )
                                ),
                                RoundedCornerShape(4.dp)
                            )
                            .width(progress.coerceIn(0f, 1f) * 100.dp)
                    )
                }
                
                // Исторические данные (мини-график)
                if (historicalData.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        historicalData.take(7).forEach { value ->
                            val barHeight = (value / maxValue.coerceAtLeast(1f)) * 20.dp
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(barHeight.coerceAtLeast(2.dp))
                                    .background(
                                        color.copy(alpha = 0.6f),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    )
}