package app.sunportal.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.ui.theme.*

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = SvitTitleStyle,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/**
 * Карточка в точном стиле SvitGrid
 * Градиент #131A24 → #0A0D12, скругление 18.dp
 */
@Composable
fun SvitgridCard(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(SvitCardGradient))
            .padding(contentPadding),
        content = content
    )
}

/**
 * Улучшенная карточка SvitGrid с тенью и границей
 */
@Composable
fun SvitgridEnhancedCard(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    contentPadding: Dp = 16.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    shadow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(SvitCardGradient))
            .border(
                width = borderWidth,
                color = borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(corner)
            )
            .then(if (shadow) Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(corner)) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    glow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (glow) Color.Unspecified else container
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.large
                )
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Улучшенная карточка с эффектом свечения в стиле SvitGrid
 */
@Composable
fun SvitgridGlowCard(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    glowColor: Color = ActiveGlow,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(
            containerColor = Color.Unspecified
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = glowColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(corner)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            glowColor.copy(alpha = 0.05f),
                            glowColor.copy(alpha = 0.01f)
                        )
                    )
                )
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun StatTile(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    SvitgridEnhancedCard(
        modifier = modifier,
        corner = 12.dp,
        borderColor = color,
        borderWidth = 1.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = SvitLabelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = SvitValueStyle,
                        color = color
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        style = SvitSubValueStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    )
}

/**
 * Улучшенная статистическая карточка в стиле SvitGrid
 */
@Composable
fun SvitgridStatTile(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    trend: String? = null, // "+12%" или "-5%"
    trendColor: Color = Color.Green
) {
    SvitgridGlowCard(
        modifier = modifier,
        corner = 12.dp,
        glowColor = color,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = SvitLabelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = value,
                        style = SvitValueStyle,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        style = SvitSubValueStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    if (trend != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = trend,
                            style = SvitSubValueStyle,
                            color = trendColor,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.2f),
                        color.copy(alpha = 0.1f)
                    )
                ), RoundedCornerShape(50)
            )
            .border(
                1.dp,
                color.copy(alpha = 0.3f),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = SvitSubValueStyle,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Улучшенная таблетка в стиле SvitGrid с анимацией
 */
@Composable
fun SvitgridPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    animated: Boolean = true
) {
    val targetAlpha = if (active) 0.2f else 0.05f
    val targetBorderAlpha = if (active) 0.3f else 0.1f
    
    val alpha = animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = if (animated) tween(300) else tween(0)
    ).value
    val borderAlpha = animateFloatAsState(
        targetValue = targetBorderAlpha,
        animationSpec = if (animated) tween(300) else tween(0)
    ).value
    
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.5f)
                    )
                ), RoundedCornerShape(50)
            )
            .border(
                1.dp,
                color.copy(alpha = borderAlpha),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = SvitSubValueStyle,
            fontWeight = FontWeight.SemiBold,
            color = if (active) color else color.copy(alpha = 0.6f)
        )
    }
}
