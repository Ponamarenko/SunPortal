package app.sunportal.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.model.LiveData
import app.sunportal.ui.components.LottieArt
import app.sunportal.ui.components.LottieArtView
import app.sunportal.ui.theme.BatteryColor
import app.sunportal.ui.theme.GridColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.LoadColor
import app.sunportal.ui.theme.PVColor
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// --- Общие цвета/константы схемы (используются Live и Overview) ---

val LabelGray = Color(0xB3FFFFFF)        // серый для подписей
val GeneratorColor = Color(0xFFFF9800)   // #FF9800 — генератор
val SmartLoadColor = Color(0xFF9C27B0)   // #9C27B0 — смарт-нагрузка

/** Тёмная палитра Svitgrid: фон экрана и градиент карточек #131A24 → #0A0D12. */
val SvitScreenBg = Color(0xFF0A0D12)
val SvitCardTop = Color(0xFF131A24)
val SvitCardBottom = Color(0xFF0A0D12)
val SvitCardGradient = listOf(SvitCardTop, SvitCardBottom)

/** Ёмкость батареи, кВт·ч (2 × 16, см. AboutScreen) */
private const val BATTERY_CAPACITY_KWH = 32f

/**
 * Карточка в стиле Svitgrid: градиент #131A24 → #0A0D12, скругление 18.dp.
 */
@Composable
fun SvitgridCard(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    contentPadding: Dp = 12.dp,
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
 * Схема потоков энергии — точная копия Svitgrid:
 * PV сверху (0.50w, 0.15h), инвертор в центре (0.50w, 0.50h),
 * батарея слева (0.15w, 0.50h), сеть справа (0.85w, 0.50h), дом снизу (0.50w, 0.85h).
 * Линии, бегущие точки и стрелки — общие для Live и Overview.
 *
 * @param compact узкий экран (Overview): блок PV приподнимается, узел «Дім» поджимается
 *                вверх, чтобы подписи не выходили за пределы области. Live — compact = false.
 */
@Composable
fun FlowScheme(
    d: LiveData,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    weatherCode: Int? = null
) {
    val hour = LocalTime.now().hour
    val isDay = hour >= 6 && hour < 20

    val pvPower = d.p_sun
    val loadPower = d.p_load
    val battPower = d.p_batt
    val gridPower = d.p_grid
    val gridVoltage = d.grid_voltage_l1
    val genPower = d.p_gen ?: 0f
    val smartPower = d.p_smart_load ?: 0f

    // Анимации прогресса (0..1, бесконечно)
    val pvProgress = rememberFlowProgress(pvPower)
    val battProgress = rememberFlowProgress(battPower)
    val gridProgress = rememberFlowProgress(gridPower)
    val loadProgress = rememberFlowProgress(loadPower)
    val genProgress = rememberFlowProgress(genPower)
    val smartProgress = rememberFlowProgress(smartPower)

    BoxWithConstraints(modifier = modifier) {
        val w: Dp = maxWidth
        val h: Dp = maxHeight
        val density = LocalDensity.current

        // --- Позиции узлов (центры, dp) ---
        val invX = w * 0.50f;  val invY = h * 0.50f   // инвертор — центр
        val pvX = w * 0.50f;   val pvY = h * 0.15f    // PV — сверху
        val battX = w * 0.15f; val battY = h * 0.50f  // батарея — слева
        val gridX = w * 0.85f; val gridY = h * 0.50f  // сеть — справа
        val homeX = w * 0.50f; val homeY = h * 0.85f  // дом — снизу
        val genX = w * 0.15f;  val genY = h * 0.75f   // генератор — слева-снизу
        val smartX = w * 0.85f; val smartY = h * 0.75f // смарт-нагрузка — справа-снизу

        // --- Размеры элементов: Live — как было, Overview (compact) — меньше ---
        val nodeSize = if (compact) 56.dp else 80.dp        // батарея/сеть/дом
        val smallNodeSize = if (compact) 46.dp else 60.dp   // генератор/смарт-нагрузка
        val invSize = if (compact) 40.dp else 80.dp         // инвертор (Live: 80.dp)
        val valueSize = if (compact) 15.sp else 18.sp       // значение узла
        val subSize = if (compact) 10.sp else 12.sp         // доп. строка узла
        val labelSize = if (compact) 10.sp else 11.sp       // подпись узла
        // явные lineHeight только в compact: в Live тексты наследуют 24.sp (как было)
        val valueLine = if (compact) 19.sp else TextUnit.Unspecified
        val subLine = if (compact) 13.sp else TextUnit.Unspecified
        val labelLine = if (compact) 13.sp else TextUnit.Unspecified
        // высота текста под иконкой узла (значение + подпись) — для прижатия узла «Дім»
        val nodeTextH = if (compact) 34.dp else 44.dp

        // --- PV-блок, точки отвода линий (по одной от каждой панели) ---
        val pvBlockW = if (compact) 156.dp else 160.dp
        val pvBlockH = if (compact) 78.dp else 100.dp
        val pvPanelSize = if (compact) 34.dp else 45.dp
        val pvBlockCx = pvX
        // compact: не даём блоку выйти за верх области
        val pvCenterY = if (compact) maxOf(pvY, pvBlockH * 0.5f + 2.dp) else pvY
        val pvBlockBottom = pvCenterY + pvBlockH * 0.5f
        // центры панелей PV1..PV3 внутри блока (равные расстояния, SpaceEvenly)
        val pvColW = (pvBlockW - 12.dp) / 3f
        val pvPanelXs = (0 until 3).map { i ->
            pvBlockCx - pvBlockW * 0.5f + 6.dp + pvColW * (i + 0.5f)
        }

        // --- Точки отрезков линий (обрезаны у инвертора) ---
        // Батарея ↔ Инвертор: горизонтальная y=0.50h, от 0.20w до 0.45w
        val battLineLeft = px(density, w * 0.20f, h * 0.50f)
        val battLineRight = px(density, w * 0.45f, h * 0.50f)
        // Сеть ↔ Инвертор: горизонтальная y=0.50h, от 0.80w до 0.55w
        val gridLineRight = px(density, w * 0.80f, h * 0.50f)
        val gridLineLeft = px(density, w * 0.55f, h * 0.50f)
        // Инвертор → Дом: вертикальная x=0.50w; нижний конец подтыкается под иконку дома
        val homeNodeTop = if (compact)
            minOf(homeY - nodeSize * 0.5f, h - (nodeSize + nodeTextH + 2.dp))
        else homeY - 40.dp
        val homeLineTop = px(density, w * 0.50f, invY + 20.dp)
        val homeLineBottom = px(
            density,
            w * 0.50f,
            maxOf(homeNodeTop + 4.dp, invY + 24.dp)
        )
        // Позиции узлов (px для Canvas)
        val invPos = px(density, invX, invY)
        val genPos = px(density, genX, genY)
        val smartPos = px(density, smartX, smartY)
        // Линии от каждой панели PV: начало — низ PV-блока под центром панели
        val pvLineStarts = pvPanelXs.map { cx -> px(density, cx, pvBlockBottom) }

        // --- Фон: линии + бегущие точки + стрелки у конца ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 2.dp.toPx()   // линии 2.dp
            val dotRadiusPx = 4.dp.toPx()     // точки 4.dp
            val arrowSizePx = 8.dp.toPx()     // стрелки 8.dp (у конца линии)

            // 1) PV1/PV2/PV3 → Инвертор: плавные кривые Безье (Path + cubicTo)
            // Начало — низ PV-блока под панелью, конец — верх инвертора;
            // control-точки тянут по вертикали → линия плавно загибается к центру
            val invTopPx = 20.dp.toPx()
            val pvCtrl = 60.dp.toPx()
            val invTop = Offset(invPos.x, invPos.y - invTopPx)
            pvLineStarts.forEach { start ->
                val pvPath = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(
                        start.x, start.y + pvCtrl,
                        invTop.x, invTop.y - pvCtrl,
                        invTop.x, invTop.y
                    )
                }
                drawPath(
                    path = pvPath,
                    color = if (pvPower > 0f) PVColor else Color.Gray,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                if (pvPower > 0f) {
                    // точки бегут вдоль Path (PathMeasure), стрелка — у конца Path
                    drawFlowDotsOnPath(pvPath, PVColor, pvProgress, dotRadiusPx)
                    drawFlowArrowOnPath(pvPath, PVColor, arrowSizePx)
                }
            }

            // 2) Батарея ↔ Инвертор (горизонтальная; направление по знаку p_batt)
            val battStart = if (battPower > 0f) battLineLeft else battLineRight
            val battEnd = if (battPower > 0f) battLineRight else battLineLeft
            drawFlowLine(battStart, battEnd, abs(battPower) > 0f, BatteryColor, strokeWidthPx)
            if (abs(battPower) > 0f) {
                drawFlowDots(battStart, battEnd, BatteryColor, battProgress, dotRadiusPx)
                drawFlowArrow(battStart, battEnd, BatteryColor, arrowSizePx)
            }

            // 3) Сеть ↔ Инвертор (горизонтальная; направление по знаку p_grid)
            val gridStart = if (gridPower > 0f) gridLineRight else gridLineLeft
            val gridEnd = if (gridPower > 0f) gridLineLeft else gridLineRight
            drawFlowLine(gridStart, gridEnd, abs(gridPower) > 0f, GridColor, strokeWidthPx)
            if (abs(gridPower) > 0f) {
                drawFlowDots(gridStart, gridEnd, GridColor, gridProgress, dotRadiusPx)
                drawFlowArrow(gridStart, gridEnd, GridColor, arrowSizePx)
            }

            // 4) Инвертор → Дом (вертикальная, сверху вниз)
            drawFlowLine(homeLineTop, homeLineBottom, loadPower > 0f, LoadColor, strokeWidthPx)
            if (loadPower > 0f) {
                drawFlowDots(homeLineTop, homeLineBottom, LoadColor, loadProgress, dotRadiusPx)
                drawFlowArrow(homeLineTop, homeLineBottom, LoadColor, arrowSizePx)
            }

            // 5) Генератор → Инвертор (оранжевая #FF9800, только при p_gen > 0)
            if (genPower > 0f) {
                val gl = hypot((invPos.x - genPos.x).toDouble(), (invPos.y - genPos.y).toDouble()).toFloat()
                val gux = (invPos.x - genPos.x) / gl
                val guy = (invPos.y - genPos.y) / gl
                val genTrim = 20.dp.toPx()
                val genStart = Offset(genPos.x + gux * genTrim, genPos.y + guy * genTrim)
                val genEnd = Offset(invPos.x - gux * genTrim, invPos.y - guy * genTrim)
                drawFlowLine(genStart, genEnd, true, GeneratorColor, strokeWidthPx)
                drawFlowDots(genStart, genEnd, GeneratorColor, genProgress, dotRadiusPx)
                drawFlowArrow(genStart, genEnd, GeneratorColor, arrowSizePx)
            }

            // 6) Инвертор → Смарт-нагрузка (фиолетовая #9C27B0, только при p_smart_load > 0)
            if (smartPower > 0f) {
                val sl = hypot((smartPos.x - invPos.x).toDouble(), (smartPos.y - invPos.y).toDouble()).toFloat()
                val sux = (smartPos.x - invPos.x) / sl
                val suy = (smartPos.y - invPos.y) / sl
                val smartTrim = 20.dp.toPx()
                val smStart = Offset(invPos.x + sux * smartTrim, invPos.y + suy * smartTrim)
                val smEnd = Offset(smartPos.x - sux * smartTrim, smartPos.y - suy * smartTrim)
                drawFlowLine(smStart, smEnd, true, SmartLoadColor, strokeWidthPx)
                drawFlowDots(smStart, smEnd, SmartLoadColor, smartProgress, dotRadiusPx)
                drawFlowArrow(smStart, smEnd, SmartLoadColor, arrowSizePx)
            }
        }

        // --- PV-блок сверху: пунктирный фон, 3 панели, у каждой своя линия ---
        PvBlock(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = pvBlockCx - pvBlockW * 0.5f, y = pvCenterY - pvBlockH * 0.5f)
                .size(width = pvBlockW, height = pvBlockH),
            strings = listOf(d.p_string1, d.p_string2, d.p_string3),
            total = pvPower,
            isDay = isDay,
            panelSize = pvPanelSize
        )

        // Инвертор (центр — маленький, без подписи)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = invX - invSize * 0.5f, y = invY - invSize * 0.5f)
                .size(invSize),
            contentAlignment = Alignment.Center
        ) {
            LottieArtView(art = LottieArt.INVERTER, size = invSize)
        }

        // Батарея слева
        SchemeNode(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = battX - 55.dp, y = battY - nodeSize * 0.5f),
            columnWidth = 110.dp,
            lottieArt = LottieArt.BATTERY,
            lottieSize = nodeSize,
            valueText = "${battPower.toInt()} Вт",
            subText = buildBatterySub(d.batt_soc, d.battery_voltage),
            extraText = "%.1f°C".format(d.battery_temp),
            smallText = null,
            labelText = null,
            valueSize = valueSize,
            subSize = subSize,
            labelSize = labelSize,
            valueLine = valueLine,
            subLine = subLine,
            labelLine = labelLine,
            color = BatteryColor
        )

        // Сеть справа — трансформатор svitgrid (столб), при p_grid = 0: "OFF" + "Вимк."
        SchemeNode(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = gridX - 45.dp, y = gridY - nodeSize * 0.5f),
            columnWidth = 90.dp,
            lottieArt = if (isDay) LottieArt.TRANSFORMER else LottieArt.TRANSFORMER_NIGHT,
            lottieSize = nodeSize,
            valueText = "${gridPower.toInt()} Вт",
            subText = null,
            smallText = if (abs(gridPower) < 0.01f) "OFF" else buildGridStatus(gridPower, gridVoltage),
            labelText = if (abs(gridPower) < 0.01f) "Вимк." else null,
            valueSize = valueSize,
            subSize = subSize,
            labelSize = labelSize,
            valueLine = valueLine,
            subLine = subLine,
            labelLine = labelLine,
            color = GridColor
        )

        // Дом снизу
        SchemeNode(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = homeX - 55.dp, y = homeNodeTop),
            columnWidth = 110.dp,
            lottieArt = houseArtFor(weatherCode, isDay),
            lottieSize = nodeSize,
            valueText = "${loadPower.toInt()} Вт",
            subText = null,
            smallText = null,
            labelText = "Споживання",
            valueSize = valueSize,
            subSize = subSize,
            labelSize = labelSize,
            valueLine = valueLine,
            subLine = subLine,
            labelLine = labelLine,
            color = LoadColor
        )

        // Иконка погоды над домом (ШАГ 3): emoji по current.code
        val weatherIconStr = when (weatherCode) {
            0, 1 -> "☀️"
            2, 3 -> "☁️"
            in 51..67 -> "🌧"
            in 71..100 -> "❄️"
            else -> ""
        }
        if (weatherIconStr.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = homeX - 30.dp, y = homeNodeTop - 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weatherIconStr,
                    color = Color.White,
                    fontSize = 40.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Генератор слева-снизу (только при p_gen > 0)
        if (genPower > 0f) {
            SchemeNode(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = genX - 48.dp, y = genY - smallNodeSize * 0.5f),
                columnWidth = 96.dp,
                lottieArt = LottieArt.GENERATOR,
                lottieSize = smallNodeSize,
                valueText = "${genPower.toInt()} Вт",
                subText = null,
                smallText = null,
                labelText = "Генератор",
                valueSize = valueSize,
                subSize = subSize,
                labelSize = labelSize,
                valueLine = valueLine,
                subLine = subLine,
                labelLine = labelLine,
                color = GeneratorColor
            )
        }

        // Смарт-нагрузка справа-снизу (только при p_smart_load > 0)
        if (smartPower > 0f) {
            SchemeNode(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = smartX - 48.dp, y = smartY - smallNodeSize * 0.5f),
                columnWidth = 96.dp,
                lottieArt = LottieArt.SMART_LOAD,
                lottieSize = smallNodeSize,
                valueText = "${smartPower.toInt()} Вт",
                subText = null,
                smallText = null,
                labelText = "Смарт-навантаження",
                valueSize = valueSize,
                subSize = subSize,
                labelSize = labelSize,
                valueLine = valueLine,
                subLine = subLine,
                labelLine = labelLine,
                color = SmartLoadColor
            )
        }
    }
}

/** Статус батареи: «~11h 34m до 100%» / «~5h 12m до 0%» / «Очікування». */
@Composable
fun BatteryStatusLine(d: LiveData, modifier: Modifier = Modifier) {
    val (text, color) = batteryStatusText(d.p_batt, d.batt_soc)
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        color = color,
        fontSize = 14.sp,
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

private fun batteryStatusText(battPower: Float, battSoc: Float): Pair<String, Color> {
    return when {
        battPower > 0f -> {
            val remainingKwh = (100f - battSoc).coerceAtLeast(0f) / 100f * BATTERY_CAPACITY_KWH
            val hours = remainingKwh / (battPower / 1000f)
            val hPart = hours.toInt()
            val mPart = ((hours - hPart) * 60).toInt()
            "~${hPart}h ${mPart}m до 100%" to BatteryColor
        }
        battPower < 0f -> {
            val remainingKwh = battSoc.coerceAtLeast(0f) / 100f * BATTERY_CAPACITY_KWH
            val hours = remainingKwh / (abs(battPower) / 1000f)
            val hPart = hours.toInt()
            val mPart = ((hours - hPart) * 60).toInt()
            "~${hPart}h ${mPart}m до 0%" to LoadColor
        }
        else -> "Очікування" to LabelGray
    }
}

private fun px(
    density: androidx.compose.ui.unit.Density,
    x: Dp,
    y: Dp
): Offset = with(density) { Offset(x.toPx(), y.toPx()) }

private fun buildBatterySub(soc: Float, voltage: Float?): String {
    val v = if (voltage != null && voltage > 0f) " · ${"%.1f".format(voltage)}V" else ""
    return "${soc.toInt()}%$v"
}

private fun buildGridStatus(pgrid: Float, voltage: Float?): String {
    val status = when {
        pgrid > 0f -> "Імпорт"
        pgrid < 0f -> "Експорт"
        else -> "Очікування"
    }
    val v = if (voltage != null && voltage > 0f) " · ${voltage.toInt()}V" else ""
    return "$status$v"
}

/**
 * PV-блок Svitgrid: сверху общая мощность «1,2 кВт», далее 3 панели в ряд (45.dp):
 * над каждой — мощность, под каждой — «PV1 · 0,6» (название · кВт). Пунктирная рамка.
 */
@Composable
private fun PvBlock(
    modifier: Modifier = Modifier,
    strings: List<Float>,
    total: Float,
    isDay: Boolean,
    panelSize: Dp
) {
    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = PVColor.copy(alpha = 0.45f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                    )
                )
            }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // unbounded: не даём родителю «съесть» последнюю строку (подписи PV1..PV3)
                .wrapContentHeight(unbounded = true),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Общая PV-мощность, lineHeight явный — иначе наследуется 24.sp из Typography
            Text(
                text = "${"%.1f".format(total / 1000f).replace('.', ',')} кВт",
                color = PVColor,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                strings.forEachIndexed { i, power ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Над панелью: V·I в API нет — только мощность
                        Text(
                            text = "${power.toInt()} Вт",
                            color = PVColor,
                            fontSize = 8.5.sp,
                            lineHeight = 11.sp,
                            fontFamily = JetBrainsMono
                        )
                        // Панель: чем больше выработка PV — тем быстрее анимация
                        LottieArtView(
                            art = if (isDay) LottieArt.SOLAR else LottieArt.SOLAR_NIGHT,
                            size = panelSize,
                            speed = if (total > 0f) 1f else 0.15f
                        )
                        // Под панелью: «PV1 · 0,6»
                        Text(
                            text = "PV${i + 1} · ${"%.1f".format(power / 1000f).replace('.', ',')}",
                            color = LabelGray,
                            fontSize = 8.5.sp,
                            lineHeight = 11.sp,
                            fontFamily = JetBrainsMono,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberFlowProgress(power: Float): Float {
    val transition = rememberInfiniteTransition(label = "flow")
    // Точки замедлены: 4000 / (|power|/1000).coerceIn(0.5f, 3f)
    // 1 кВт -> 4000мс, 3 кВт -> 1333мс, 5 кВт -> 1333мс
    val powerAbs = abs(power)
    val speedFactor = (powerAbs / 1000f).coerceAtLeast(0.5f).coerceAtMost(3f)
    val durationMs = (4000f / speedFactor).toInt()

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing)
        ),
        label = "progress"
    )
    return progress
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowLine(
    start: Offset,
    end: Offset,
    active: Boolean,
    color: Color,
    strokeWidthPx: Float
) {
    val lineColor = if (active) color else Color.Gray
    drawLine(
        color = lineColor,
        start = start,
        end = end,
        strokeWidth = strokeWidthPx,
        cap = StrokeCap.Round
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowDots(
    start: Offset,
    end: Offset,
    color: Color,
    progress: Float,
    radiusPx: Float
) {
    // 3 точки со смещением 0, 0.33, 0.66
    listOf(0f, 0.33f, 0.66f).forEach { offset ->
        val t = (progress + offset) % 1f
        val pos = Offset(
            x = start.x + (end.x - start.x) * t,
            y = start.y + (end.y - start.y) * t
        )
        drawCircle(color = color, radius = radiusPx, center = pos)
    }
}

/** Точки вдоль Path (PathMeasure): 3 точки со смещением 0, 0.33, 0.66. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowDotsOnPath(
    path: Path,
    color: Color,
    progress: Float,
    radiusPx: Float
) {
    val pm = PathMeasure().apply { setPath(path, false) }
    val len = pm.length
    if (len <= 0f) return
    listOf(0f, 0.33f, 0.66f).forEach { offset ->
        val t = (progress + offset) % 1f
        drawCircle(color = color, radius = radiusPx, center = pm.getPosition(len * t))
    }
}

/** Стрелка у КОНЦА Path: направление — касательная в конце (tan от PathMeasure). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowArrowOnPath(
    path: Path,
    color: Color,
    sizePx: Float
) {
    val pm = PathMeasure().apply { setPath(path, false) }
    val len = pm.length
    if (len <= 0f) return
    val tan = pm.getTangent(len)
    drawFlowArrowAt(atan2(tan.y, tan.x), pm.getPosition(len), color, sizePx)
}

/**
 * Стрелка (треугольник) у КОНЦА линии (у инвертора), направленная по потоку.
 * tip — на конце линии, основание — вглубь линии на sizePx.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowArrow(
    start: Offset,
    end: Offset,
    color: Color,
    sizePx: Float
) {
    drawFlowArrowAt(atan2(end.y - start.y, end.x - start.x), end, color, sizePx)
}

/** Треугольник-стрелка по известному углу направления (angle, tip). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowArrowAt(
    angle: Float,
    tip: Offset,
    color: Color,
    sizePx: Float
) {
    val ux = cos(angle); val uy = sin(angle)

    val base = Offset(tip.x - ux * sizePx, tip.y - uy * sizePx)
    // перпендикуляр к направлению — половина основания
    val halfBase = sizePx * 0.55f
    val left = Offset(base.x - uy * halfBase, base.y + ux * halfBase)
    val right = Offset(base.x + uy * halfBase, base.y - ux * halfBase)

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Анимация дома по погоде (WMO-код) и времени суток.
 * Варианты из assets/lottie: солнце / дождь / снег (+ ночные версии).
 */
private fun houseArtFor(weatherCode: Int?, isDay: Boolean): LottieArt =
    when (weatherCode ?: -1) {
        in 51..67, in 80..82, in 95..99 ->
            if (isDay) LottieArt.HOME_RAIN else LottieArt.HOME_RAIN_NIGHT
        in 71..77, in 85..86 ->
            if (isDay) LottieArt.HOME_SNOW else LottieArt.HOME_SNOW_NIGHT
        else ->
            if (isDay) LottieArt.HOME_SUN else LottieArt.HOME_NIGHT
    }

@Composable
private fun SchemeNode(
    modifier: Modifier = Modifier,
    columnWidth: Dp,
    lottieArt: LottieArt? = null,
    lottieSize: Dp,
    valueText: String?,
    subText: String?,
    extraText: String? = null,
    smallText: String?,
    labelText: String?,
    valueSize: TextUnit = 18.sp,
    subSize: TextUnit = 12.sp,
    labelSize: TextUnit = 11.sp,
    valueLine: TextUnit = TextUnit.Unspecified,
    subLine: TextUnit = TextUnit.Unspecified,
    labelLine: TextUnit = TextUnit.Unspecified,
    color: Color
) {
    Column(
        modifier = modifier.width(columnWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lottieArt != null) {
            // Анимация из assets/lottie (с погодными вариантами дома)
            LottieArtView(
                art = lottieArt,
                size = lottieSize
            )
        } else {
            // Резерв: пустое место, если анимация не задана
            Spacer(Modifier.size(lottieSize))
        }
        if (valueText != null) {
            Text(
                text = valueText,
                color = color,
                fontSize = valueSize,
                lineHeight = valueLine,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        if (subText != null) {
            Text(
                text = subText,
                color = Color.White,
                fontSize = subSize,
                lineHeight = subLine,
                fontFamily = JetBrainsMono,
                textAlign = TextAlign.Center
            )
        }
        if (extraText != null) {
            Text(
                text = extraText,
                color = LabelGray,
                fontSize = labelSize,
                lineHeight = labelLine,
                fontFamily = JetBrainsMono,
                textAlign = TextAlign.Center
            )
        }
        if (smallText != null) {
            Text(
                text = smallText,
                color = LabelGray,
                fontSize = labelSize,
                lineHeight = labelLine,
                textAlign = TextAlign.Center
            )
        }
        if (labelText != null) {
            Text(
                text = labelText,
                color = LabelGray,
                fontSize = labelSize,
                lineHeight = labelLine,
                textAlign = TextAlign.Center
            )
        }
    }
}
