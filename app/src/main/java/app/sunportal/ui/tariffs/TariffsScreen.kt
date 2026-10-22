package app.sunportal.ui.tariffs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.BatteryColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.LoadColor
import app.sunportal.ui.theme.PVColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

private val Context.dataStore by preferencesDataStore(name = "tariffs_prefs")

/**
 * Tariffs (индекс 6) в стиле Svitgrid:
 * тёмный фон, карточки SvitgridCard (градиент #131A24 → #0A0D12, скругление 18.dp),
 * шрифт JetBrains Mono, калькулятор вартості.
 */
@Composable
fun TariffsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val (accountType, setAccountType) = remember { mutableStateOf("Однотарифний") }
    val (season, setSeason) = remember { mutableStateOf("Літо") }
    val (hasHeating, setHasHeating) = remember { mutableStateOf("Ні") }
    val (zone, setZone) = remember { mutableStateOf("6.2 (Бориспільський р-н, с. Іванків)") }

    val (consumption, setConsumption) = remember { mutableStateOf("") }
    val (nightPercent, setNightPercent) = remember { mutableStateOf("50") }

    val accountTypes = listOf("Однотарифний", "Двохтарифний")
    val seasons = listOf("Літо", "Зима")
    val heatingOptions = listOf("Так", "Ні")

    val prefs = context.dataStore

    LaunchedEffect(Unit) {
        prefs.data.first().let { p ->
            p[stringPreferencesKey("accountType")]?.let { setAccountType(it) }
            p[stringPreferencesKey("season")]?.let { setSeason(it) }
            p[stringPreferencesKey("hasHeating")]?.let { setHasHeating(it) }
            p[stringPreferencesKey("zone")]?.let { setZone(it) }
            p[stringPreferencesKey("consumption")]?.let { setConsumption(it) }
            p[stringPreferencesKey("nightPercent")]?.let { setNightPercent(it) }
        }
    }

    fun save() {
        scope.launch {
            prefs.edit { e ->
                e[stringPreferencesKey("accountType")] = accountType
                e[stringPreferencesKey("season")] = season
                e[stringPreferencesKey("hasHeating")] = hasHeating
                e[stringPreferencesKey("zone")] = zone
                e[stringPreferencesKey("consumption")] = consumption
                e[stringPreferencesKey("nightPercent")] = nightPercent
            }
        }
    }

    val baseSingle = BigDecimal("4.32")
    val dayTwo = BigDecimal("4.32")
    val nightTwo = BigDecimal("2.16")
    val heatUpTo = BigDecimal("2.64")
    val heatAbove = BigDecimal("4.32")

    val calcResult = remember(consumption, nightPercent, accountType, season, hasHeating) {
        val cons = consumption.toDoubleOrNull() ?: 0.0
        if (cons <= 0.0) null else {
            val nightPct = (nightPercent.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
            val dayCons = cons * (1.0 - nightPct / 100.0)
            val nightCons = cons * (nightPct / 100.0)
            when (accountType) {
                "Однотарифний" -> {
                    val total = BigDecimal(cons).multiply(baseSingle).setScale(2, RoundingMode.HALF_UP)
                    mapOf(
                        "total" to total.toString(),
                        "day" to "0.00",
                        "night" to "0.00",
                        "heat" to "0.00",
                        "over" to "0.00",
                        "saveVsSingle" to "0.00",
                        "isSave" to false
                    )
                }
                else -> {
                    val daySum = BigDecimal(dayCons).multiply(dayTwo).setScale(2, RoundingMode.HALF_UP)
                    val nightSum = BigDecimal(nightCons).multiply(nightTwo).setScale(2, RoundingMode.HALF_UP)
                    var heatSum = BigDecimal("0.00")
                    var overSum = BigDecimal("0.00")
                    if (season == "Зима" && hasHeating == "Так") {
                        val upTo = 2000.0
                        if (cons <= upTo) {
                            heatSum = BigDecimal(cons).multiply(heatUpTo).setScale(2, RoundingMode.HALF_UP)
                        } else {
                            heatSum = BigDecimal(upTo).multiply(heatUpTo).setScale(2, RoundingMode.HALF_UP)
                            overSum = BigDecimal(cons - upTo).multiply(heatAbove).setScale(2, RoundingMode.HALF_UP)
                        }
                    }
                    val totalTwo = daySum.add(nightSum).add(heatSum).add(overSum).setScale(2, RoundingMode.HALF_UP)
                    val totalSingle = BigDecimal(cons).multiply(baseSingle).setScale(2, RoundingMode.HALF_UP)
                    val save = if (totalSingle > totalTwo) totalSingle.subtract(totalTwo).setScale(2, RoundingMode.HALF_UP) else BigDecimal("0.00")
                    mapOf(
                        "total" to totalTwo.toString(),
                        "day" to daySum.toString(),
                        "night" to nightSum.toString(),
                        "heat" to heatSum.toString(),
                        "over" to overSum.toString(),
                        "saveVsSingle" to save.toString(),
                        "isSave" to (totalSingle > totalTwo)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvitScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Тарифи",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono,
            color = Color.White
        )

        // --- ТИП УЧЁТА ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Тип учёта")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                accountTypes.forEach { t ->
                    SvitChip(
                        label = t,
                        selected = accountType == t,
                        onClick = { setAccountType(t) }
                    )
                }
            }
        }

        // --- СЕЗОН ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Сезон")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                seasons.forEach { s ->
                    SvitChip(
                        label = s,
                        selected = season == s,
                        onClick = { setSeason(s) }
                    )
                }
            }
        }

        // --- Є ЕЛЕКТРООПАЛЕННЯ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Є електроопалення")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                heatingOptions.forEach { h ->
                    SvitChip(
                        label = h,
                        selected = hasHeating == h,
                        onClick = { setHasHeating(h) }
                    )
                }
            }
        }

        // --- ЗОНА БЛЕКАУТУ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Зона блекауту")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = zone,
                onValueChange = { setZone(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp
                ),
                label = {
                    Text(
                        text = "Зона",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono
                    )
                }
            )
        }

        // --- ТАРИФЫ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Тарифы (грн/кВт·ч)")
            Spacer(Modifier.height(8.dp))
            TariffLine("Базовый (однотарифний)", "4.32 грн/кВт·ч", PVColor)
            TariffLine("Двохтарифний · День 07:00–23:00", "4.32", LoadColor)
            TariffLine("Двохтарифний · Ніч 23:00–07:00", "2.16", LoadColor)
            if (season == "Зима" && hasHeating == "Так") {
                Spacer(Modifier.height(2.dp))
                TariffLine("Електроопалення · до 2000 кВт·ч/міс", "2.64", BatteryColor)
                TariffLine("Електроопалення · понад 2000 кВт·ч/міс", "4.32", BatteryColor)
            }
        }

        // --- КАЛЬКУЛЯТОР ВАРТОСТІ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Калькулятор вартості")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = consumption,
                onValueChange = { setConsumption(it) },
                label = {
                    Text(
                        text = "Споживання за місяць, кВт·год",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (accountType == "Двохтарифний") {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nightPercent,
                    onValueChange = { setNightPercent(it) },
                    label = {
                        Text(
                            text = "% нічного споживання",
                            fontSize = 12.sp,
                            fontFamily = JetBrainsMono
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            calcResult?.let { r ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Разом: ${r["total"]} грн",
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PVColor,
                    fontFamily = JetBrainsMono
                )
                if (accountType == "Двохтарифний") {
                    ResultLine("День", "${r["day"]} грн")
                    ResultLine("Ніч", "${r["night"]} грн")
                }
                if (season == "Зима" && hasHeating == "Так") {
                    ResultLine("Електроопалення (до 2000)", "${r["heat"]} грн")
                    if (r["over"] != "0.00") ResultLine("Понад 2000", "${r["over"]} грн")
                }
                val isSave = r["isSave"] as? Boolean ?: false
                if (isSave) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Економія vs однотарифний: ${r["saveVsSingle"]} грн",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BatteryColor,
                        fontFamily = JetBrainsMono
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { save() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Зберегти",
                fontSize = 14.sp,
                fontFamily = JetBrainsMono
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Заголовок карточки — JetBrains Mono 14.sp, Bold. */
@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = JetBrainsMono,
        color = Color.White
    )
}

/** FilterChip в стиле Svitgrid: подпись 12.sp, рамка выбранного — PVColor. */
@Composable
private fun SvitChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono,
                color = Color.White
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            labelColor = Color.White,
            selectedLabelColor = Color.White,
            selectedContainerColor = Color.White.copy(alpha = 0.16f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.White.copy(alpha = 0.25f),
            selectedBorderColor = PVColor
        )
    )
}

/** Строка тарифа: название слева, значение справа. */
@Composable
private fun TariffLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = JetBrainsMono
        )
    }
}

/** Строка результата калькулятора. */
@Composable
private fun ResultLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = JetBrainsMono
        )
    }
}
