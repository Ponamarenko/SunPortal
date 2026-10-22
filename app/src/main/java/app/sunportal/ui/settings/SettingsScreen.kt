package app.sunportal.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.api.RetrofitClient
import app.sunportal.data.SettingsRepository
import app.sunportal.ui.common.LabelGray
import app.sunportal.ui.common.SvitScreenBg
import app.sunportal.ui.common.SvitgridCard
import app.sunportal.ui.theme.JetBrainsMono
import app.sunportal.ui.theme.PVColor
import kotlinx.coroutines.launch

private const val APP_VERSION = "1.0.3"
private const val APP_UPDATED = "2026-10-22"

/**
 * Settings (индекс 8) в стиле Svitgrid:
 * тёмный фон, карточки SvitgridCard (градиент #131A24 → #0A0D12, скругление 18.dp),
 * шрифт JetBrains Mono.
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val repo = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val savedIp by repo.serverIp.collectAsState(initial = "ha4a.pp.ua")
    val savedPort by repo.serverPort.collectAsState(initial = "8080")

    var ip by remember { mutableStateOf(savedIp) }
    var port by remember { mutableStateOf(savedPort) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(savedIp, savedPort) {
        ip = savedIp
        port = savedPort
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
            text = "⚙️ Налаштування",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono,
            color = Color.White
        )

        // --- СЕРВЕР ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Сервер")
            Spacer(Modifier.height(8.dp))

            // Індикатор стану
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = when {
                                status.startsWith("✅") -> Color(0xFF2ECC71)
                                status.startsWith("❌") -> Color(0xFFE74C3C)
                                status.startsWith("⏳") -> Color(0xFFF5B800)
                                else -> Color.Gray
                            },
                            shape = CircleShape
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (status.isEmpty()) "Не перевірено" else status,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LabelGray,
                    fontFamily = JetBrainsMono
                )
            }
            Spacer(Modifier.height(10.dp))

            SvitField(
                value = ip,
                onValueChange = { ip = it },
                label = "IP-адреса",
                placeholder = "ha4a.pp.ua"
            )
            Spacer(Modifier.height(8.dp))
            SvitField(
                value = port,
                onValueChange = { port = it },
                label = "Порт",
                placeholder = "8080",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(10.dp))

            SvitButton(
                text = "💾 Зберегти",
                modifier = Modifier.fillMaxWidth(),
                highlighted = true,
                onClick = {
                    val portNum = port.toIntOrNull()
                    if (ip.isBlank() || portNum == null || portNum !in 1..65535) {
                        status = "❌ Некоректний IP або порт"
                        return@SvitButton
                    }
                    scope.launch {
                        repo.saveServer(ip, port)
                        RetrofitClient.updateBaseUrl("http://$ip:$port/")
                        status = "✅ Збережено: http://$ip:$port/"
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            SvitButton(
                text = "🔍 Перевірити з'єднання",
                modifier = Modifier.fillMaxWidth(),
                highlighted = false,
                onClick = {
                    scope.launch {
                        status = "⏳ Перевірка..."
                        try {
                            RetrofitClient.updateBaseUrl("http://$ip:$port/")
                            val response = RetrofitClient.api.getLiveData()
                            status = "✅ З'єднання OK (отримано ${response.size} записів)"
                        } catch (e: Exception) {
                            status = "❌ Помилка: ${e.message}"
                        }
                    }
                }
            )
        }

        // --- РОЗМІР ШРИФТУ МЕНЮ ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Розмір шрифту меню")
            Spacer(Modifier.height(8.dp))
            val fontOptions = listOf(
                "small" to "Малий (14sp)",
                "medium" to "Середній (16sp)",
                "large" to "Великий (18sp)"
            )
            val currentFont by repo.menuFont.collectAsState(initial = "medium")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fontOptions.forEach { (key, label) ->
                    val sel = currentFont == key
                    FilterChip(
                        selected = sel,
                        onClick = { scope.launch { repo.saveMenuFont(key) } },
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
                            selected = sel,
                            borderColor = Color.White.copy(alpha = 0.25f),
                            selectedBorderColor = PVColor
                        )
                    )
                }
            }
        }

        // --- ПРО ЗАСТОСУНОК ---
        SvitgridCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle("Про застосунок")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Версія: $APP_VERSION",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color.White,
                fontFamily = JetBrainsMono
            )
            Text(
                text = "Оновлено: $APP_UPDATED",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = LabelGray,
                fontFamily = JetBrainsMono
            )
            Spacer(Modifier.height(10.dp))
            SvitButton(
                text = "Перевірити оновлення",
                modifier = Modifier.fillMaxWidth(),
                highlighted = false,
                onClick = {
                    Toast.makeText(context, "У вас актуальна версія $APP_VERSION", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Text(
            text = "Поточна адреса: ${RetrofitClient.getBaseUrl()}",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = LabelGray,
            fontFamily = JetBrainsMono
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

/** Поле ввода в стиле Svitgrid: подпись и значение JetBrains Mono. */
@Composable
private fun SvitField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono
            )
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = JetBrainsMono,
            fontSize = 12.sp
        )
    )
}

/** Кнопка в стиле Svitgrid: тёмный фон, жёлтая (highlighted) или серая рамка. */
@Composable
private fun SvitButton(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (highlighted) PVColor else Color.White.copy(alpha = 0.25f)
    val textColor = if (highlighted) PVColor else Color.White
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = JetBrainsMono,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
