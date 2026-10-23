package app.sunportal.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.BuildConfig
import app.sunportal.api.RetrofitClient
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.BatteryColor
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.LoadColor
import app.sunportal.ui.theme.PVColor

/**
 * About (индекс 9) в стиле Svitgrid:
 * фон SvitScreenBg, карточки SvitgridCard (градиент #131A24 → #0A0D12, 18.dp),
 * вся типографика — JetBrains Mono.
 *
 * Версия берётся из BuildConfig (versionName/versionCode из build.gradle.kts),
 * поэтому больше не отстаёт от реальной сборки.
 */
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    // Установленный build (может отличаться от BuildConfig, если APK не переустановлен)
    @Suppress("DEPRECATION")
    val installedBuild = remember {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        if (android.os.Build.VERSION.SDK_INT >= 28) pi.longVersionCode
        else pi.versionCode.toLong()
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
            text = "ℹ️ Про додаток",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono,
            color = Color.White
        )

        // --- ДОДАТОК ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Додаток")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SunPortal",
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono,
                color = Color.White
            )
            Text(
                text = "Версія: ${BuildConfig.VERSION_NAME} (build $installedBuild)",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = JetBrainsMono,
                color = PVColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Місцевий портал для моніторингу сонячної станції",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = JetBrainsMono,
                color = LabelGray
            )
        }

        // --- СЕРВЕР ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Сервер")
            Spacer(Modifier.height(8.dp))
            InfoLine("Адреса", RetrofitClient.getBaseUrl(), PVColor)
        }

        // --- ОБЛАДНАННЯ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Обладнання")
            Spacer(Modifier.height(8.dp))
            InfoLine("Інвертор", "Deye SUN-12K-SG02LP1-EU-AM3")
            InfoLine("Батарея", "32 кВт·год (2 × 16)", BatteryColor)
            InfoLine("Панелі", "16.19 кВт", PVColor)
            InfoLine("  Пд / Зх / Сх", "8.37 / 4.14 / 3.68 кВт", LoadColor)
        }

        // --- ІСТОРІЯ ВЕРСІЙ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Історія версій")
            Spacer(Modifier.height(8.dp))
            VersionLine("1.0.4", "SDK 36, Kotlin 2.0.21, Open-Meteo, сповіщення", true)
            VersionLine("1.0.3", "Налаштування в стилі Svitgrid")
            VersionLine("1.0.2", "TOU Svitgrid, тарифи")
            VersionLine("1.0.1", "Схема потоків, прогноз, графіки")
            VersionLine("1.0.0", "Live, Огляд, Прогноз, TOU, Про додаток")
        }

        Text(
            text = "© SunPortal · локальний проєкт",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = JetBrainsMono,
            color = LabelGray
        )
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

/** Строка «подпись — значение» (12.sp, значение цветом). */
@Composable
private fun InfoLine(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = JetBrainsMono,
            color = LabelGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = JetBrainsMono,
            color = valueColor
        )
    }
}

/** Строка истории версий: «1.0.4 — …», текущая — жёлтым. */
@Composable
private fun VersionLine(
    version: String,
    note: String,
    current: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = version,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
            fontFamily = JetBrainsMono,
            color = if (current) PVColor else Color.White,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = note,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = JetBrainsMono,
            color = LabelGray
        )
    }
}
