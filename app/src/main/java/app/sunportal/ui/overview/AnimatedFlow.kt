package app.sunportal.ui.overview

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

data class FlowLine(
    val start: Offset,
    val end: Offset,
    val color: Color,
    val active: Boolean,
    val reverse: Boolean = false
)

@Composable
fun AnimatedFlowDiagram(
    lines: List<FlowLine>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flow")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Canvas(modifier = modifier) {
        lines.forEach { line ->
            // Статичная линия (rail)
            drawLine(
                color = line.color.copy(alpha = 0.2f),
                start = line.start,
                end = line.end,
                strokeWidth = 4f
            )

            // Активная анимированная линия
            if (line.active) {
                val start = if (line.reverse) line.end else line.start
                val end = if (line.reverse) line.start else line.end
                val currentPos = Offset(
                    x = start.x + (end.x - start.x) * offset,
                    y = start.y + (end.y - start.y) * offset
                )

                // Точка
                drawCircle(
                    color = line.color,
                    radius = 8f,
                    center = currentPos
                )

                // Свечение
                drawCircle(
                    color = line.color.copy(alpha = 0.3f),
                    radius = 16f,
                    center = currentPos
                )
            }
        }
    }
}
