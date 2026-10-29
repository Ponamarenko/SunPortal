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
import androidx.compose.material3.Icon
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Power
import app.sunportal.ui.theme.*

/**
 * Улучшенный компонент уведомления с анимациями
 */
@Composable
fun EnhancedNotificationItem(
    title: String,
    message: String,
    time: String,
    type: NotificationType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    read: Boolean = false
) {
    val targetAlpha = if (read) 0.6f else 1f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(300)
    )
    
    val targetBorderAlpha = if (read) 0.1f else 0.3f
    val borderAlpha by animateFloatAsState(
        targetValue = targetBorderAlpha,
        animationSpec = tween(300)
    )
    
    SvitgridCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick ?: {})
            .border(
                1.dp,
                type.color.copy(alpha = borderAlpha),
                RoundedCornerShape(12.dp)
            ),
        corner = 12.dp,
        contentPadding = PaddingValues(16.dp),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Иконка уведомления
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    type.color.copy(alpha = 0.2f),
                                    type.color.copy(alpha = 0.1f)
                                )
                            ),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            type.color.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        tint = type.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Контент уведомления
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Заголовок и время
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = title,
                            style = SvitCardTitleStyle,
                            color = if (read) Color.Gray else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = time,
                            style = SvitLabelStyle,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Сообщение
                    Text(
                        text = message,
                        style = SvitSubValueStyle,
                        color = if (read) Color.Gray.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
                
                // Индикатор непрочитанности
                if (!read) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(type.color, CircleShape)
                    )
                }
            }
        }
    )
}

/**
 * Тип уведомления с цветами и иконками
 */
enum class NotificationType(
    val color: Color,
    val icon: androidx.compose.material.icons.Icons.Filled
) {
    INFO(Color.Blue, androidx.compose.material.icons.IIcons.Default.Info),
    WARNING(Color(0xFFFFC107), androidx.compose.material.icons.Icons.Default.Warning),
    ERROR(Color.Red, androidx.compose.material.icons.Icons.Default.Error),
    SUCCESS(Color.Green, androidx.compose.material.icons.Icons.Default.CheckCircle),
    BATTERY(Color(0xFFFF9800), androidx.compose.material.icons.Icons.Default.BatteryAlert),
    GRID(Color(0xFF9C27B0), androidx.compose.material.icons.Icons.Default.Power)
}

/**
 * Компонент для отображения истории уведомлений
 */
@Composable
fun NotificationHistory(
    notifications: List<NotificationItem>,
    modifier: Modifier = Modifier,
    onNotificationClick: (NotificationItem) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "История уведомлений",
            style = SvitTitleStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
        )
        
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет уведомлений",
                    style = SvitSubValueStyle,
                    color = Color.Gray
                )
            }
        } else {
            notifications.forEach { notification ->
                EnhancedNotificationItem(
                    title = notification.title,
                    message = notification.message,
                    time = notification.time,
                    type = notification.type,
                    onClick = { onNotificationClick(notification) },
                    read = notification.read,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Данные уведомления
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val read: Boolean = false
)