package app.sunportal.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val versionName = packageInfo.versionName ?: "?"
    val versionCode = if (android.os.Build.VERSION.SDK_INT >= 28)
        packageInfo.longVersionCode else packageInfo.versionCode.toLong()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ℹ️ Про додаток", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SunPortal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Версія: $versionName (build $versionCode)", fontSize = 14.sp)
                Text("Місцевий портал для моніторингу сонячної станції", fontSize = 13.sp)
                HorizontalDivider()
                Text("Сервер: ${app.sunportal.api.RetrofitClient.getBaseUrl()}", fontSize = 12.sp)
                Text("Інвертор: Deye SUN-12K-SG02LP1-EU-AM3", fontSize = 12.sp)
                Text("Батарея: 32 кВт·год (2 × 16)", fontSize = 12.sp)
                Text("Панелі: 16.19 кВт (Пд 8.37 + Зх 4.14 + Сх 3.68)", fontSize = 12.sp)
            }
        }

        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("📋 Історія версій:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("• 1.0.0 — Live, Огляд, Налаштування, Прогноз, TOU, Про додаток", fontSize = 12.sp)
            }
        }
    }
}