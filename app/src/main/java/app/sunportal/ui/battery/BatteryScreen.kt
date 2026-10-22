package app.sunportal.ui.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitCardGradient
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.components.LottieArt
import app.sunportal.ui.components.LottieArtView
import app.sunportal.ui.live.LiveViewModel
import app.sunportal.ui.theme.JetBrainsMono

@Composable
fun BatteryScreen(vm: LiveViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val d = state.data

    val soc = d?.batt_soc ?: 0f
    var socColor: Color = Color.Gray
    var socStatus = "Невідомо"
    when {
        soc >= 50f -> { socColor = Color(0xFF2ECC71); socStatus = "Заряд/Очікування" }
        soc >= 30f -> { socColor = Color(0xFFFFB74D); socStatus = "Розряд/Очікування" }
        else -> { socColor = Color(0xFFE74C3C); socStatus = "Критичний" }
    }

    val voltage = d?.battery_voltage ?: 0f
    val current = d?.battery_current ?: 0f
    val power = d?.p_batt ?: 0f
    val temp = d?.battery_temp ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF131A24), Color(0xFF0A0D12))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lottie battery + SOC overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieArtView(art = LottieArt.BATTERY, size = 200.dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${soc.toInt()}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = socColor,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = socStatus,
                    fontSize = 14.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
        }

        // 2×2 grid cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SvitgridCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Напруга", color = LabelGray, fontSize = 12.sp, fontFamily = JetBrainsMono)
                    Text("${String.format("%.1f", voltage)} V", fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = JetBrainsMono)
                }
            }
            SvitgridCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Струм", color = LabelGray, fontSize = 12.sp, fontFamily = JetBrainsMono)
                    val sign = if (current >= 0) "+" else ""
                    Text("${sign}${String.format("%.1f", current)} A", fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = JetBrainsMono)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SvitgridCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Потужність", color = LabelGray, fontSize = 12.sp, fontFamily = JetBrainsMono)
                    Text("${String.format("%.0f", power)} Вт", fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = JetBrainsMono)
                }
            }
            SvitgridCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Температура", color = LabelGray, fontSize = 12.sp, fontFamily = JetBrainsMono)
                    Text(if (temp == 0f) "—" else "${String.format("%.1f", temp)} °C", fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = JetBrainsMono)
                }
            }
        }

        // Health section — remaining capacity
        val remainingKwh = d?.battery_remaining_kwh

        if (remainingKwh != null && remainingKwh > 0f) {
            SvitgridCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ємність акумулятора", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("Залишок", color = LabelGray, fontSize = 12.sp, fontFamily = JetBrainsMono)
                            Text("${String.format("%.1f", remainingKwh)} kWh", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = JetBrainsMono)
                        }
                    }
                }
            }
        }
    }
}