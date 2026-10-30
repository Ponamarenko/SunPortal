package app.sunportal.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.R
import app.sunportal.model.LiveData
import app.sunportal.model.householdById
import app.sunportal.ui.components.LottieArt
import app.sunportal.ui.components.LottieArtView
import app.sunportal.ui.theme.*
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// --- Точная цветовая схема SvitGrid ---

// Основные цвета из SvitGrid
val LabelGray = Color(0xB3FFFFFF)        // серый для подписей
val GeneratorColor = Color(0xFF9E9E9E)   // Сделали нейтральным
val SmartLoadColor = Color(0xFF9E9E9E)   // Сделали нейтральным

/** Зелёный индикатор выработки PV (точный цвет SvitGrid). */
val PvActiveColor = Color(0xFF00A326)

/** Светлые линии потока (монохромный стиль). */
val LineColorActive = Color(0xCCFFFFFF)
val LineColorInactive = Color(0x33FFFFFF)

/** Точная тёмная палитра SvitGrid: фон экрана и градиент карточек #1B232E → #080B10. */
val SvitScreenBg = Color(0xFF0A0D12)
val SvitCardTop = Color(0xFF1B232E)
val SvitCardBottom = Color(0xFF080B10)
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
        // Позиция для UPS-баджа под инвертором
        val upsBadgeX = invX; val upsBadgeY = invY + invSize * 0.6f

        // --- Размеры элементов: Live — крупные иконки, Overview (compact) — меньше ---
        val nodeSize = if (compact) 54.dp else 72.dp        // батарея/сеть
        val smallNodeSize = if (compact) 42.dp else 56.dp   // генератор/смарт-нагрузка
        val battSize = if (compact) 62.dp else 84.dp          // батарея — крупнее (Svitgrid)
        val invSize = if (compact) 40.dp else 64.dp         // инвертор
        val badgeSize = if (compact) 20.dp else 28.dp       // иконка типа домовладения
        val upsBadgeSize = if (compact) 18.dp else 24.dp    // UPS-бадж под инвертором
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

        // --- Толщина линий передачи: очень тонкие 0.8.dp (как в Svitgrid) ---
        val wireWidth = 0.8.dp
        val dotRadius = 1.2.dp
        val arrowSize = 0.dp // Убираем стрелки, если они не нужны для чистоты

        // --- PV-ряд: 2 колонки (PV1 слева, PV2 справа) в стиле Svitgrid ---
        // ширина колонок адаптивная — на узких экранах не вылезают за край
        val pvColW = if (compact) minOf(92.dp, w * 0.26f) else minOf(104.dp, w * 0.28f)
        val pvColXs = listOf(w * 0.25f, w * 0.75f) // PV1 слева, PV2 справа
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

        // --- Точки отрезков линий: концы у краёв иконок, чтобы стрелки были видны ---
        // Батарея ↔ Инвертор: горизонтальная y=0.50h, от края батареи до края инвертора
        val battLineLeft = px(density, battX + battSize * 0.5f + 4.dp, h * 0.50f)
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
        // Grid T-branch: точка разветвления от сети к дому и к UPS-баджу
        val tBranchX = px(density, gridX - 40.dp, gridY + 30.dp)
        val gridToHomeX = px(density, w * 0.50f, gridY + 30.dp)
        val gridToUpsX = px(density, upsBadgeX, gridY + 30.dp)
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

            // 1) PV: от каждого поля — своя линия к инвертору (под углом, к как в Svitgrid)
            pvColXs.forEach { cx ->
                val panelBottom = px(density, cx, pvRowH)
                val invTop = px(density, invX, invY - invSize * 0.5f + 2.dp)
                val isActive = pvPower > 0f
                drawFlowLine(panelBottom, invTop, isActive, LineColorActive, strokeWidthPx, dashPathEffect, curved = false)
                if (isActive) {
                    drawFlowDots(panelBottom, invTop, LineColorActive, pvProgress, dotRadiusPx)
                }
            }

            // 2) Батарея ↔ Инвертор (направление по знаку p_batt)
            val battStart = if (battPower > 0f) battLineLeft else battLineRight
            val battEnd = if (battPower > 0f) battLineRight else battLineLeft
            drawFlowLine(battStart, battEnd, abs(battPower) > 0f, Color.White, strokeWidthPx, dashPathEffect)
            if (abs(battPower) > 0f) {
                drawFlowDots(battStart, battEnd, Color.White, battProgress, dotRadiusPx)
            }

            // 3) Сеть → T-branch → Дом и UPS-бадж
            if (abs(gridPower) > 0f) {
                // Сеть → T-branch
                drawFlowLine(gridLineRight, tBranchX, true, Color.White, strokeWidthPx, dashPathEffect)
                drawFlowDots(gridLineRight, tBranchX, Color.White, gridProgress, dotRadiusPx)
                
                // T-branch → Дом
                drawFlowLine(gridToHomeX, homeLineTop, true, Color.White, strokeWidthPx, dashPathEffect)
                drawFlowDots(gridToHomeX, homeLineTop, Color.White, gridProgress, dotRadiusPx)
                
                // T-branch → UPS-бадж
                drawFlowLine(gridToUpsX, px(density, upsBadgeX, upsBadgeY), true, Color.White, strokeWidthPx, dashPathEffect)
                drawFlowDots(gridToUpsX, px(density, upsBadgeX, upsBadgeY), Color.White, gridProgress, dotRadiusPx)
            }

            // 4) Инвертор → Дом
            drawFlowLine(homeLineTop, homeLineBottom, loadPower > 0f, Color.White, strokeWidthPx, dashPathEffect)
            if (loadPower > 0f) {
                drawFlowDots(homeLineTop, homeLineBottom, Color.White, loadProgress, dotRadiusPx)
            }

            // 5) Генератор → Инвертор
            if (genPower > 0f) {
                val gl = hypot((invPos.x - genPos.x).toDouble(), (invPos.y - genPos.y).toDouble()).toFloat()
                val gux = (invPos.x - genPos.x) / gl
                val guy = (invPos.y - genPos.y) / gl
                val genTrim = 8f
                val genStart = Offset(genPos.x + gux * genTrim, genPos.y + guy * genTrim)
                val genEnd = Offset(invPos.x - gux * genTrim, invPos.y - guy * genTrim)
                drawFlowLine(genStart, genEnd, true, Color.White, strokeWidthPx, dashPathEffect)
                drawFlowDots(genStart, genEnd, Color.White, genProgress, dotRadiusPx)
            }

            // 6) Инвертор → Смарт-нагрузка
            if (smartPower > 0f) {
                val sl = hypot((smartPos.x - invPos.x).toDouble(), (smartPos.y - invPos.y).toDouble()).toFloat()
                val sux = (smartPos.x - invPos.x) / sl
                val suy = (smartPos.y - invPos.y) / sl
                val smartTrim = 8f
                val smStart = Offset(invPos.x + sux * smartTrim, invPos.y + suy * smartTrim)
                val smEnd = Offset(smartPos.x - sux * smartTrim, smartPos.y - suy * smartTrim)
                drawFlowLine(smStart, smEnd, true, Color.White, strokeWidthPx, dashPathEffect)
                drawFlowDots(smStart, smEnd, Color.White, smartProgress, dotRadiusPx)
            }
        }

        // --- Итог PV (над рядом колонок) ---
        if (showPvTotal) {
            Text(
                text = "☀ ${"%.1f".format(pvPower / 1000f).replace('.', ',')} кВт",
                color = Color.White,
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
        // --- PV-ряд: 2 колонки (PV1 и PV2) ---
        val pvStrings = listOf(d.p_string1, d.p_string2)
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
                    voltage = listOf(d.pv1_voltage, d.pv2_voltage).getOrElse(i) { 0f },
                    current = listOf(d.pv1_current, d.pv2_current).getOrElse(i) { 0f },
                    onNodeClick = onNodeClick
                )
            }
        }

        // Инвертор (центр — маленький, с подписью "UPS")
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
        
        // UPS подпись под инвертором
        Text(
            text = "UPS",
            color = LabelGray,
            fontSize = if (compact) 10.sp else 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = invX - 20.dp, y = invY + invSize * 0.5f + 4.dp)
                .width(40.dp)
        )
        
        // UPS-бадж под инвертором
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = upsBadgeX - upsBadgeSize * 0.5f, y = upsBadgeY)
                .size(upsBadgeSize)
                .background(Color.Green.copy(alpha = 0.8f), RoundedCornerShape(upsBadgeSize * 0.4f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${"%.1f".format(abs(gridPower) / 1000f).replace('.', ',')} кВт",
                color = Color.White,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Батарея слева
        SchemeNode(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = battX - battColW * 0.5f, y = battY - battSize * 0.5f),
            columnWidth = battColW,
            clickKey = "batt",
            lottieArt = if (battPower > 0f) LottieArt.BATTERY_CHARGING else LottieArt.BATTERY,
            lottieSize = battSize,
            valueText = "${"%.1f".format(d.battery_voltage)} В · ${d.batt_soc.toInt()}%",
            subText = if (battPower > 0f) "Зарядка · ${"%.0f".format(abs(battPower))} Вт" else "%.1f°C".format(d.battery_temp),
            extraText = null,
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
            valueText = "${abs(gridPower).toInt()} Вт",
            subText = buildGridVoltages(d.grid_voltage_l1, d.grid_voltage_l2, d.grid_voltage_l3),
            smallText = if (abs(gridPower) < 0.01f) "OFF" else if (gridPower < 0f) "Експорт" else "Імпорт",
            labelText = if (abs(gridPower) < 0.01f) "Вимк." else null,
            valueSize = valueSize,
            subSize = subSize,
            labelSize = labelSize,
            valueLine = valueLine,
            subLine = subLine,
            labelLine = labelLine,
            color = GridColor
        )
        
        // 423 W Grid pill badge (T-branch connection to house)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = w * 0.50f - 30.dp, y = gridY + 40.dp)
                .size(60.dp, 24.dp)
                .background(Color.Blue.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "423 В",
                color = Color.White,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

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
            smallText = "Споживання",
            labelText = null,
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
            "~${hPart}h ${mPart}m до 100%" to LabelGray
        }
        battPower < 0f -> {
            val remainingKwh = battSoc.coerceAtLeast(0f) / 100f * BATTERY_CAPACITY_KWH
            val hours = remainingKwh / (abs(battPower) / 1000f)
            val hPart = hours.toInt()
            val mPart = ((hours - hPart) * 60).toInt()
            "~${hPart}h ${mPart}m до 0%" to LabelGray
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

fun buildGridVoltages(voltageL1: Float?, voltageL2: Float?, voltageL3: Float?): String {
    val v1 = voltageL1?.toInt() ?: 0
    val v2 = voltageL2?.toInt() ?: 0  
    val v3 = voltageL3?.toInt() ?: 0
    return "$v1/$v2/$v3 В"
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
    voltage: Float = 0f,
    current: Float = 0f,
    onNodeClick: ((String) -> Unit)? = null
) {
    val active = power > 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { if (onNodeClick != null) onNodeClick?.invoke("pv$index") }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок PV
        Text(
            text = "PV$index",
            style = SvitCardTitleStyle,
            color = LabelGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Напряжение и ток
        Text(
            text = if (voltage > 0f) "${voltage.toInt()} В · ${current.toInt()} А" else "— В · — А",
            style = SvitLabelStyle,
            color = LabelGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
        
        // Панель PV — іконка з APK Svitgrid (svit_household_solar_array)
        Box(
            modifier = Modifier.size(panelSize)
        ) {
            Image(
                painter = painterResource(R.drawable.svit_household_solar_array),
                contentDescription = null,
                modifier = Modifier
                    .size(panelSize * 0.9f)
                    .align(Alignment.Center)
            )
            
            // Индикатор активности с пульсацией
            if (active) {
                val pulse by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(dotSize * pulse)
                        .background(PvActiveColor, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        
        // Мощность
        Text(
            text = "${"%.1f".format(power / 1000f).replace('.', ',')} кВт",
            style = SvitValueStyle,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
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
    pathEffect: PathEffect? = null,
    curved: Boolean = false
) {
    // неактивная линия — приглушённый серый (аккуратнее, чем яркий серый)
    val lineColor = if (active) color else Color.Gray.copy(alpha = 0.35f)
    
    val path = Path().apply {
        moveTo(start.x, start.y)
        if (curved) {
            val (c1, c2) = bezierControl(start, end)
            cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
        } else {
            lineTo(end.x, end.y)
        }
    }
    
    val stroke = Stroke(
        width = strokeWidthPx,
        cap = StrokeCap.Round,
        pathEffect = pathEffect
    )
    
    // Основная линия
    drawPath(path = path, color = lineColor, style = stroke)
}

/**
 * Контрольные точки кривой Безье для линии PV -> инвертор
 * (кривая с плавным "схлопыванием" от прямой линии).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.bezierControl(start: Offset, end: Offset): Pair<Offset, Offset> {
    val bend = with(this) { 48.dp.toPx() }
    val dx = end.x - start.x
    val dy = end.y - start.y
    val c1 = Offset(start.x + dx * 0.40f, start.y + dy * 0.40f + bend)
    val c2 = Offset(end.x - dx * 0.40f, end.y - dy * 0.40f + bend)
    return c1 to c2
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowDots(
    start: Offset,
    end: Offset,
    color: Color,
    progress: Float,
    radiusPx: Float,
    curved: Boolean = false
) {
    // 7 точек с разными размерами и эффектом пульсации для SvitGrid стиля
    listOf(0f, 0.16f, 0.33f, 0.5f, 0.66f, 0.83f, 1f).forEach { offset ->
        val t = (progress + offset) % 1f
        val pos = if (curved) {
            val (c1, c2) = bezierControl(start, end)
            bezierPoint(start, c1, c2, end, t)
        } else {
            Offset(
                x = start.x + (end.x - start.x) * t,
                y = start.y + (end.y - start.y) * t
            )
        }
        
        // Пульсация для активных линий
        val pulse = if (color != Color.Gray) {
            val pulseProgress = (progress * 3f + offset) % 1f
            1f + sin(pulseProgress * Math.PI.toFloat()) * 0.3f
        } else 1f
        
        // Точки с разным размером для эффекта движения
        val size = radiusPx.times(pulse).times(1.3f - abs(t - 0.5f).times(0.4f)) // Центральные точки крупнее
        
        // Основная точка
        drawCircle(color = color, radius = size, center = pos)
        
            // Свечение для активных линий (эффект SvitGrid)
        if (color != Color.Gray) {
            drawCircle(
                color = color.copy(alpha = 0.6f),
                radius = size * 2.0f,
                center = pos
            )
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = size * 3.5f,
                center = pos
            )
        }
        
        // Дополнительная точка для эффекта яркого ядра
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = size * 0.4f,
            center = pos
        )
    }
}

/** Точка кривої Безье по параметру t (0..1). */
private fun bezierPoint(start: Offset, c1: Offset, c2: Offset, end: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        x = u * u * u * start.x + 3f * u * u * t * c1.x + 3f * u * t * t * c2.x + t * t * t * end.x,
        y = u * u * u * start.y + 3f * u * u * t * c1.y + 3f * u * t * t * c2.y + t * t * t * end.y
    )
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

/** Треугольник-стрелка по известному углу направления (angle, tip) - стиль SvitGrid. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowArrowAt(
    angle: Float,
    tip: Offset,
    color: Color,
    sizePx: Float
) {
    val ux = cos(angle); val uy = sin(angle)

    val base = Offset(tip.x - ux * sizePx, tip.y - uy * sizePx)
    // перпендикуляр к направлению — увеличенное основание для SvitGrid стиля
    val halfBase = sizePx * 0.7f
    val left = Offset(base.x - uy * halfBase, base.y + ux * halfBase)
    val right = Offset(base.x + uy * halfBase, base.y - ux * halfBase)

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    
    // Стрелка с эффектом свечения для SvitGrid стиля
    if (color != Color.Gray) {
        // Внешнее свечение
        drawPath(
            path = path,
            color = color.copy(alpha = 0.2f),
            style = Stroke(sizePx * 1.5f)
        )
        // Основная стрелка
        drawPath(path = path, color = color)
        // Внутреннее свечение
        drawPath(
            path = path,
            color = color.copy(alpha = 0.4f),
            style = Stroke(1f)
        )
    } else {
        // Неактивная стрелка
        drawPath(path = path, color = color)
    }
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
    color: Color,
    active: Boolean = true
) {
    Column(
        modifier = modifier
            .width(columnWidth)
            .clickable(onClick = { if (clickKey != null) onNodeClick?.invoke(clickKey!!) }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lottieArt != null) {
            Box(modifier = Modifier.size(lottieSize).padding(4.dp)) {
                LottieArtView(
                    art = lottieArt,
                    size = lottieSize * 0.9f,
                    // BATTERY_CHARGING — анімація акумулятора в стані зарядки (швидший темп)
                    speed = if (lottieArt == LottieArt.BATTERY_CHARGING) 1.5f else if (active) 1f else 0.5f
                )
            }
        } else {
            Spacer(Modifier.size(lottieSize))
        }
        
        // Значение узла
        if (valueText != null) {
            Text(
                text = valueText,
                style = SvitValueStyle,
                color = if (active) Color.White else LabelGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Дополнительный текст
        if (subText != null) {
            Text(
                text = subText,
                style = SvitSubValueStyle,
                color = LabelGray,
                textAlign = TextAlign.Center
            )
        }
        
        // Дополнительная информация
        if (extraText != null) {
            Text(
                text = extraText,
                style = SvitLabelStyle,
                color = LabelGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Малый текст (статус)
        if (smallText != null) {
            Text(
                text = smallText,
                style = SvitLabelStyle,
                color = LabelGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Подпись и бейдж
        if (labelText != null || badgeRes != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (badgeRes != null) {
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
                        style = SvitLabelStyle,
                        color = LabelGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
