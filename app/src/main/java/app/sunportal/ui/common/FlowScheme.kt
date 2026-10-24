package app.sunportal.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.model.LiveData
import app.sunportal.model.householdById
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
val SmartLoadColor = Color(0x9C27B0)   // #9C27B0 — смарт-нагрузка

/** Зелёный индикатор выработки PV (как в Svitgrid). */
val PvActiveColor = Color(0xFF00A326)

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
 * Схема потоков энергии — по мотивам Svitgrid:
 * сверху ряд из 3 PV-колонок (подпись «PV1», мощность, панель, зелёный индикатор),
 * под ними общая шина (bus) с отводами к панелям и вертикалью к инвертору,
 * ниже — батарея (слева), инвертор (центр), сеть (справа), дом — снизу по центру.
 * Линии, бегущие точки и стрелки — общие для Live и Overview.
 *
 * @param compact узкий экран (Overview): размеры панелей и подписей меньше
 * @param weatherCode текущий код погоды WMO — выбор варианта дома
 * @param windSpeed скорость ветра, м/с — при сильном ветре дом «ветреный»
 * @param householdId тип домовладения (иконка Svitgrid) — показывается в узле «Дом»
 * @param onNodeClick клик на узел → (String) — id узла (batt/grid/home/inv/pv1/pv2/pv3)
 */
@Composable
fun FlowScheme(
    d: LiveData,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    weatherCode: Int? = null,
    windSpeed: Float? = null,
    householdId: String? = null,
    onNodeClick: ((String) -> Unit)? = null
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

    // Иконка типа домовладения (Svitgrid) — в узле «Дом»
    val household = householdById(householdId)

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
        val battX = w * 0.16f; val battY = h * 0.50f  // батарея — слева
        val gridX = w * 0.84f; val gridY = h * 0.50f  // сеть — справа
        val homeX = w * 0.50f; val homeY = h * 0.85f  // дом — снизу
        val genX = w * 0.16f;  val genY = h * 0.75f   // генератор — слева-снизу
        val smartX = w * 0.84f; val smartY = h * 0.75f // смарт-нагрузка — справа-снизу

        // --- Размеры элементов: Live — крупные иконки, Overview (compact) — меньше ---
        val nodeSize = if (compact) 58.dp else 88.dp        // батарея/сеть
        val smallNodeSize = if (compact) 46.dp else 64.dp   // генератор/смарт-нагрузка
        val invSize = if (compact) 44.dp else 84.dp         // инвертор
        val badgeSize = if (compact) 20.dp else 28.dp       // иконка типа домовладения
        val valueSize = if (compact) 15.sp else 18.sp       // значение узла
        val subSize = if (compact) 10.sp else 12.sp         // доп. строка узла
        val labelSize = if (compact) 10.sp else 11.sp       // подпись узла
        // явные lineHeight только в compact: в Live тексты наследуют 24.sp (как было)
        val valueLine = if (compact) 19.sp else TextUnit.Unspecified
        val subLine = if (compact) 13.sp else TextUnit.Unspecified
        val labelLine = if (compact) 13.sp else TextUnit.Unspecified
        // высота текста под иконкой узла (значение + подпись) — для прижатия узла «Дім»
        val nodeTextH = if (compact) 32.dp else 48.dp
        // Дом — крупнее всех (как в Svitgrid), но на низких экранах ужимается,
        // чтобы не налезать на инвертор и не уйти за нижний край
        val homeSize = if (compact) {
            58.dp
        } else {
            minOf(
                104.dp,
                h * 0.22f,
                maxOf(48.dp, h - (invY + invSize * 0.5f + 8.dp) - nodeTextH - 2.dp)
            )
        }

        // --- Толщина линий передачи: пунктир 1.5.dp (как в Svitgrid) ---
        val wireWidth = if (compact) 1.5.dp else 1.5.dp
        val dotRadius = if (compact) 2.dp else 2.5.dp
        val arrowSize = if (compact) 5.dp else 6.dp

        // --- PV-ряд: 3 колонки (как в Svitgrid) над батареей / инвертором / сетью ---
        // ширины колонок адаптивные — на узких экранах (320 dp) не вылезают за край
        val battColW = minOf(110.dp, w * 0.31f)
        val gridColW = minOf(90.dp, w * 0.25f)
        val homeColW = minOf(110.dp, w * 0.31f)
        val smallColW = minOf(96.dp, w * 0.27f)
        val pvColW = if (compact) minOf(92.dp, w * 0.26f) else minOf(104.dp, w * 0.28f)
        val pvColXs = listOf(battX, w * 0.50f, gridX)
        val pvCaptionSize = if (compact) 8.sp else 9.sp
        val pvValueSize = if (compact) 10.sp else 11.sp
        val pvCaptionLine = if (compact) 9.sp else 10.sp
        val pvValueLine = if (compact) 11.sp else 12.sp
        val pvPanelSize = if (compact) 36.dp else 52.dp
        val pvDotSize = if (compact) 5.dp else 7.dp
        // общий итог PV — над рядом (только в Live, в Overview места мало)
        val showPvTotal = !compact
        val pvRowTop = if (showPvTotal) 14.dp else 0.dp
        // высота PV-ряда: подпись + мощность + панель + индикатор
        val pvRowH = pvRowTop +
            pvCaptionLine.value.dp + pvValueLine.value.dp + pvPanelSize + pvDotSize + 6.dp
        // шина PV — под рядом панелей (но выше инвертора)
        val busY = maxOf(
            minOf(h * 0.34f, maxOf(pvRowH + 14.dp, h * 0.20f)),
            pvRowH + 4.dp
        )

        // --- Точки отрезков линий: концы у краёв иконок, чтобы стрелки были видны ---
        // Батарея ↔ Инвертор: горизонтальная y=0.50h, от края батареи до края инвертора
        val battLineLeft = px(density, battX + nodeSize * 0.5f + 4.dp, h * 0.50f)
        val battLineRight = px(density, invX - invSize * 0.5f - 4.dp, h * 0.50f)
        // Сеть ↔ Инвертор: горизонтальная y=0.50h, от края инвертора до края сети
        val gridLineRight = px(density, gridX - nodeSize * 0.5f - 4.dp, h * 0.50f)
        val gridLineLeft = px(density, invX + invSize * 0.5f + 4.dp, h * 0.50f)
        // Инвертор → Дом: вертикальная x=0.50w; нижний конец подтыкается под иконку дома
        // Дом снизу: прижат к низу, чтобы значение и подпись не уходили за край
        val homeNodeTop = minOf(homeY - homeSize * 0.5f, h - (homeSize + nodeTextH + 2.dp))
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

        // --- Фон: линии + бегущие точки + стрелки у конца ---
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = wireWidth.toPx()     // тонкие линии передачи
            val dotRadiusPx = dotRadius.toPx()        // мелкие бегущие точки
            val arrowSizePx = arrowSize.toPx()        // аккуратные стрелки у конца

            // 1) PV-шина: горизонтальная линия под рядом панелей + узлы (квадраты)
            val busLeft = px(density, pvColXs[0], busY)
            val busRight = px(density, pvColXs[2], busY)
            val busColor = if (pvPower > 0f) PVColor else Color.Gray
            drawLine(
                color = busColor,
                start = busLeft,
                end = busRight,
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round,
                pathEffect = dashPathEffect
            )
            // отводы: панель → шина (с бегущими точками, если есть выработка)
            pvColXs.forEach { cx ->
                val top = px(density, cx, pvRowH)
                val bot = px(density, cx, busY)
                drawFlowLine(top, bot, pvPower > 0f, PVColor, strokeWidthPx, dashPathEffect)
                if (pvPower > 0f) {
                    drawFlowDots(top, bot, PVColor, pvProgress, dotRadiusPx)
                }
                // узел на шине
                val s = 4.dp.toPx()
                drawRect(
                    color = busColor,
                    topLeft = Offset(bot.x - s / 2f, bot.y - s / 2f),
                    size = Size(s, s)
                )
            }
            // шина → инвертор (центральная вертикаль, поток вниз).
            // Линия заканчивается над верхним краем иконки инвертора, чтобы стрелка
            // была видна, а не пряталась под анимацией.
            val invTopY = invY - invSize * 0.5f
            val busInvEndY =
                if (invTopY - 6.dp >= busY + 16.dp) invTopY - 6.dp else busY + 16.dp
            val busInvTop = px(density, w * 0.50f, busY)
            val busInvBottom = px(density, w * 0.50f, busInvEndY)
            drawFlowLine(busInvTop, busInvBottom, pvPower > 0f, PVColor, strokeWidthPx, dashPathEffect)
            if (pvPower > 0f) {
                drawFlowDots(busInvTop, busInvBottom, PVColor, pvProgress, dotRadiusPx)
                // стрелка только если отрезок не слишком короткий (иначе прячется под иконкой)
                if (busInvEndY - busY > 20.dp) {
                    drawFlowArrow(busInvTop, busInvBottom, PVColor, arrowSizePx)
                }
            }

            // 2) Батарея ↔ Инвертор (горизонтальная; направление по знаку p_batt)
            val battStart = if (battPower > 0f) battLineLeft else battLineRight
            val battEnd = if (battPower > 0f) battLineRight else battLineLeft
            drawFlowLine(battStart, battEnd, abs(battPower) > 0f, BatteryColor, strokeWidthPx, dashPathEffect)
            if (abs(battPower) > 0f) {
                drawFlowDots(battStart, battEnd, BatteryColor, battProgress, dotRadiusPx)
                drawFlowArrow(battStart, battEnd, BatteryColor, arrowSizePx)
            }

            // 3) Сеть ↔ Инвертор (горизонтальная; направление по знаку p_grid)
            val gridStart = if (gridPower > 0f) gridLineRight else gridLineLeft
            val gridEnd = if (gridPower > 0f) gridLineLeft else gridLineRight
            drawFlowLine(gridStart, gridEnd, abs(gridPower) > 0f, GridColor, strokeWidthPx, dashPathEffect)
            if (abs(gridPower) > 0f) {
                drawFlowDots(gridStart, gridEnd, GridColor, gridProgress, dotRadiusPx)
                drawFlowArrow(gridStart, gridEnd, GridColor, arrowSizePx)
            }

            // 4) Инвертор → Дом (вертикальная, сверху вниз)
            drawFlowLine(homeLineTop, homeLineBottom, loadPower > 0f, LoadColor, strokeWidthPx, dashPathEffect)
            if (loadPower > 0f) {
                drawFlowDots(homeLineTop, homeLineBottom, LoadColor, loadProgress, dotRadiusPx)
                drawFlowArrow(homeLineTop, homeLineBottom, LoadColor, arrowSizePx)
            }

            // 5) Генератор → Инвертор (оранжевая #FF9800, только при p_gen > 0)
            if (genPower > 0f) {
                val gl = hypot((invPos.x - genPos.x).toDouble(), (invPos.y - genPos.y).toDouble()).toFloat()
                val gux = (invPos.x - genPos.x) / gl
                val guy = (invPos.y - genPos.y) / gl
                val genTrim = 8f
                val genStart = Offset(genPos.x + gux * genTrim, genPos.y + guy * genTrim)
                val genEnd = Offset(invPos.x - gux * genTrim, invPos.y - guy * genTrim)
                drawFlowLine(genStart, genEnd, true, GeneratorColor, strokeWidthPx, dashPathEffect)
                drawFlowDots(genStart, genEnd, GeneratorColor, genProgress, dotRadiusPx)
                drawFlowArrow(genStart, genEnd, GeneratorColor, arrowSizePx)
            }

            // 6) Инвертор → Смарт-нагрузка (фиолетовая #9C27B0, только при p_smart_load > 0)
            if (smartPower > 0f) {
                val sl = hypot((smartPos.x - invPos.x).toDouble(), (smartPos.y - invPos.y).toDouble()).toFloat()
                val sux = (smartPos.x - invPos.x) / sl
                val suy = (smartPos.y - invPos.y) / sl
                val smartTrim = 8f
                val smStart = Offset(invPos.x + sux * smartTrim, invPos.y + suy * smartTrim)
                val smEnd = Offset(smartPos.x - sux * smartTrim, smartPos.y - suy * smartTrim)
                drawFlowLine(smStart, smEnd, true, SmartLoadColor, strokeWidthPx, dashPathEffect)
                drawFlowDots(smStart, smEnd, SmartLoadColor, smartProgress, dotRadiusPx)
                drawFlowArrow(smStart, smEnd, SmartLoadColor, arrowSizePx)
            }
        }

        // --- Итог PV (над рядом колонок) ---
        if (showPvTotal) {
            Text(
                text = "☀ ${"%.1f".format(pvPower / 1000f).replace('.', ',')} кВт",
                color = PVColor,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = w * 0.50f - 40.dp, y = 0.dp)
                    .width(80.dp),
                textAlign = TextAlign.Center
            )
        }

        // --- PV-ряд: 3 колонки ---
        val pvStrings = listOf(d.p_string1, d.p_string2, d.p_string3)
        pvColXs.forEachIndexed { i, cx ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = cx - pvColW * 0.5f, y = pvRowTop)
                    .width(pvColW)
            ) {
                PvColumn(
                    index = i + 1,
                    power = pvStrings.getOrElse(i) { 0f },
                    isDay = isDay,
                    panelSize = pvPanelSize,
                    dotSize = pvDotSize,
                    captionSize = pvCaptionSize,
                    valueSize = pvValueSize,
                    captionLine = pvCaptionLine,
                    valueLine = pvValueLine,
                    onNodeClick = onNodeClick
                )
            }
        }

        // Инвертор (центр — маленький, без подписи)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = invX - invSize * 0.5f, y = invY - invSize * 0.5f)
                .size(invSize)
                .clickable(onClick = { onNodeClick?.let { cb -> cb("inv") } }),
            contentAlignment = Alignment.Center
        ) {
            LottieArtView(art = LottieArt.INVERTER, size = invSize)
        }

        // Батарея слева
        SchemeNode(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = battX - battColW * 0.5f, y = battY - nodeSize * 0.5f),
            columnWidth = battColW,
            clickKey = "batt",
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
                .offset(x = gridX - gridColW * 0.5f, y = gridY - nodeSize * 0.5f),
            columnWidth = gridColW,
            clickKey = "grid",
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

        // Дом снизу (с иконкой типа домовладения)
        SchemeNode(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = homeX - homeColW * 0.5f, y = homeNodeTop),
            columnWidth = homeColW,
            clickKey = "home",
            lottieArt = houseArtFor(weatherCode, isDay, windSpeed),
            lottieSize = homeSize,
            valueText = "${loadPower.toInt()} Вт",
            subText = null,
            smallText = null,
            labelText = "Споживання",
            badgeRes = household.iconRes,
            badgeSize = badgeSize,
            valueSize = valueSize,
            subSize = subSize,
            labelSize = labelSize,
            valueLine = valueLine,
            subLine = subLine,
            labelLine = labelLine,
            color = LoadColor
        )

        // Генератор слева-снизу (только при p_gen > 0)
        if (genPower > 0f) {
            SchemeNode(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = genX - smallColW * 0.5f, y = genY - smallNodeSize * 0.5f),
                columnWidth = smallColW,
                clickKey = "gen",
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
                    .offset(x = smartX - smallColW * 0.5f, y = smartY - smallNodeSize * 0.5f),
                columnWidth = smallColW,
                clickKey = "smart",
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

fun buildGridStatus(pgrid: Float, voltage: Float?): String {
    val status = when {
        pgrid > 0f -> "Імпорт"
        pgrid < 0f -> "Експорт"
        else -> "Очікування"
    }
    val v = if (voltage != null && voltage > 0f) " · ${voltage.toInt()}V" else ""
    return "$status$v"
}

/**
 * Одна PV-колонка (как в Svitgrid): подпись «PV1», мощность в кВт,
 * панель (Lottie solar) и зелёный индикатор выработки.
 */
@Composable
private fun PvColumn(
    index: Int,
    power: Float,
    isDay: Boolean,
    panelSize: Dp,
    dotSize: Dp,
    captionSize: TextUnit,
    valueSize: TextUnit,
    captionLine: TextUnit,
    valueLine: TextUnit,
    onNodeClick: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { if (onNodeClick != null) onNodeClick?.invoke("pv$index") }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PV$index",
            color = LabelGray,
            fontSize = captionSize,
            lineHeight = captionLine,
            fontFamily = JetBrainsMono,
            maxLines = 1
        )
        Text(
            text = "${"%.1f".format(power / 1000f).replace('.', ',')} кВт",
            color = PVColor,
            fontSize = valueSize,
            lineHeight = valueLine,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        // Панель с подписью мощности (W)
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${"%.0f".format(power)} Вт",
                color = PVColor,
                fontSize = 9.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
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
    strokeWidthPx: Float,
    pathEffect: PathEffect? = null
) {
    // неактивная линия — приглушённый серый (аккуратнее, чем яркий серый)
    val lineColor = if (active) color else Color.Gray.copy(alpha = 0.35f)
    drawLine(
        color = lineColor,
        start = start,
        end = end,
        strokeWidth = strokeWidthPx,
        cap = StrokeCap.Round,
        pathEffect = pathEffect
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
 * Варианты из assets/lottie (вытянуты из svitgrid):
 * солнце / дождь / снег / ветер + ночные версии.
 * При сильном ветре (≥ 8 м/с) — «ветреный» дом.
 */
private fun houseArtFor(weatherCode: Int?, isDay: Boolean, windSpeed: Float?): LottieArt =
    when (weatherCode ?: -1) {
        in 51..67, in 80..82, in 95..99 ->
            if (isDay) LottieArt.HOME_RAIN else LottieArt.HOME_RAIN_NIGHT
        in 71..77, in 85..86 ->
            if (isDay) LottieArt.HOME_SNOW else LottieArt.HOME_SNOW_NIGHT
        else ->
            if ((windSpeed ?: 0f) >= 8f) {
                if (isDay) LottieArt.HOME_WINDY else LottieArt.HOME_WINDY_NIGHT
            } else {
                if (isDay) LottieArt.HOME_SUN else LottieArt.HOME_NIGHT
            }
    }

@Composable
private fun SchemeNode(
    modifier: Modifier = Modifier,
    clickKey: String? = null,
    onNodeClick: ((String) -> Unit)? = null,
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
    badgeRes: Int? = null,
    badgeSize: Dp = 18.dp,
    color: Color
) {
    Column(
        modifier = modifier
            .width(columnWidth)
            .clickable(onClick = { if (clickKey != null) onNodeClick?.invoke(clickKey!!) }),
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
                fontFamily = JetBrainsMono,
                textAlign = TextAlign.Center
            )
        }
        if (labelText != null || badgeRes != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeRes != null) {
                    // Иконка типа домовладения (Svitgrid)
                    Image(
                        painter = painterResource(badgeRes),
                        contentDescription = null,
                        modifier = Modifier.size(badgeSize)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                if (labelText != null) {
                    Text(
                        text = labelText,
                        color = LabelGray,
                        fontSize = labelSize,
                        lineHeight = labelLine,
                        fontFamily = JetBrainsMono,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
