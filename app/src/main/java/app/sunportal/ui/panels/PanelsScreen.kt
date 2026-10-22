package app.sunportal.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.GridColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.LoadColor
import app.sunportal.ui.theme.PVColor

/**
 * Panels (индекс 2) в стиле Svitgrid:
 * тёмный фон, карточки SvitgridCard (градиент #131A24 → #0A0D12, скругление 18.dp),
 * шрифт JetBrains Mono, bar chart распределения мощности.
 */
@Composable
fun PanelsScreen(vm: PanelsViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvitScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "☀️ Панелі",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono,
            color = Color.White
        )

        if (state.loading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text(
                text = "Помилка: ${state.error}",
                color = Color.Red,
                fontFamily = JetBrainsMono
            )
        } else state.data?.let { d ->
            fun formatPower(v: Float): String = if (v < 1f) "<1" else "${v.toInt()}"

            PanelCard("🌄 Схід (PV1)", formatPower(d.p_string1), "Вт", PVColor)
            PanelCard("🌞 Південь (PV2)", formatPower(d.p_string2), "Вт", LoadColor)
            PanelCard("🌅 Захід (PV3)", formatPower(d.p_string3), "Вт", GridColor)
            PanelCard("⚡ Загальна потужність", formatPower(d.p_sun), "Вт", PVColor)

            val s1 = d.p_string1
            val s2 = d.p_string2
            val s3 = d.p_string3
            val maxP = maxOf(s1, s2, s3, 1f)
            val totalP = s1 + s2 + s3
            val pct1 = if (totalP > 0) ((s1 / totalP) * 100f).toInt() else 0
            val pct2 = if (totalP > 0) ((s2 / totalP) * 100f).toInt() else 0
            val pct3 = if (totalP > 0) ((s3 / totalP) * 100f).toInt() else 0

            // --- BAR CHART: розподіл потужності ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Розподіл потужності",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))

                PowerBarRow("Схід", s1, maxP, PVColor, formatPower(s1), pct1)
                Spacer(Modifier.height(10.dp))
                PowerBarRow("Південь", s2, maxP, LoadColor, formatPower(s2), pct2)
                Spacer(Modifier.height(10.dp))
                PowerBarRow("Захід", s3, maxP, GridColor, formatPower(s3), pct3)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

            StatCard("Сьогодні", String.format("%.2f", d.pv_today), "кВт·год", PVColor)
            StatCard("Всього", String.format("%.2f", d.pv_total), "кВт·год", PVColor)
            StatCard("Пік сьогодні", "${(d.pv_peak_today ?: 0f).toInt()} Вт", "", PVColor)

            // --- ОПИСАНИЕ ПАНЕЛЕЙ ---
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Склад системи",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Кількість панелей: 35 шт",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "Схід: 8 шт × 460 Вт = 3.68 кВт",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "Південь: 18 шт × 465 Вт = 8.37 кВт",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "Захід: 9 шт × 460 Вт = 4.14 кВт",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Всього: 16.19 кВт",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Одна полоса bar chart: подпись слева, полоса по центру, значение и процент справа.
 * Высота полосы 24.dp, скругление 8.dp.
 */
@Composable
private fun PowerBarRow(
    label: String,
    value: Float,
    maxValue: Float,
    color: Color,
    valueText: String,
    percent: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value / maxValue).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(8.dp))
            )
        }
        Text(
            text = "$valueText Вт ($percent%)",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono
        )
    }
}

/**
 * Карточка панели в стиле Svitgrid: иконка + название + значение + единица.
 */
@Composable
fun PanelCard(title: String, value: String, unit: String, color: Color) {
    SvitgridCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = JetBrainsMono
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
        }
    }
}

/**
 * Карточка статистики в стиле Svitgrid: заголовок сверху, значение снизу.
 */
@Composable
fun StatCard(title: String, value: String, unit: String, color: Color) {
    SvitgridCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = JetBrainsMono
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
        }
    }
}
