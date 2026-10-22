package app.sunportal.ui.overview

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

data class FlowLine(
    val start: Offset,
    val end: Offset,
    val color: Color,
    val active: Boolean,
    val reverse: Boolean = false
)

/**
 * Анімований потік енергії: "рейка" + кілька точок, що біжать уздовж лінії.
 */
@Composable
fun AnimatedFlowDiagram(
    lines: List<FlowLine>,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "flow")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val dotCount = 3
        lines.forEach { line ->
            // "рейка"
            drawLine(
                color = line.color.copy(alpha = 0.18f),
                start = line.start,
                end = line.end,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            if (line.active) {
                val from = if (line.reverse) line.end else line.start
                val to = if (line.reverse) line.start else line.end

                // легке світіння під лінією
                drawLine(
                    color = line.color.copy(alpha = 0.28f),
                    start = line.start,
                    end = line.end,
                    strokeWidth = 12f,
                    cap = StrokeCap.Round
                )

                repeat(dotCount) { i ->
                    val t = ((phase + i.toFloat() / dotCount) % 1f)
                    val pos = Offset(
                        x = from.x + (to.x - from.x) * t,
                        y = from.y + (to.y - from.y) * t
                    )
                    // ореол
                    drawCircle(color = line.color.copy(alpha = 0.25f), radius = 14f, center = pos)
                    // ядро
                    drawCircle(color = line.color, radius = 6f, center = pos)
                }
            }
        }
    }
}
