package app.sunportal.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sunportal.api.RetrofitClient
import app.sunportal.data.SettingsRepository
import android.widget.Toast
import kotlinx.coroutines.launch

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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚙️ Налаштування", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Сервер", fontWeight = FontWeight.Bold, fontSize = 16.sp)



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
                    Text(if (status.isEmpty()) "Не перевірено" else status, fontSize = 13.sp)
                }

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP-адреса") },
                    placeholder = { Text("ha4a.pp.ua") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Порт") },
                    placeholder = { Text("8080") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val portNum = port.toIntOrNull()
                        if (ip.isBlank() || portNum == null || portNum !in 1..65535) {
                            status = "❌ Некоректний IP або порт"
                            return@Button
                        }
                        scope.launch {
                            repo.saveServer(ip, port)
                            RetrofitClient.updateBaseUrl("http://$ip:$port/")
                            status = "✅ Збережено: http://$ip:$port/"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💾 Зберегти")
                }

                OutlinedButton(
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
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔍 Перевірити з'єднання")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Розмір шрифту меню", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val fontOptions = listOf("small" to "Малий (14sp)", "medium" to "Середній (16sp)", "large" to "Великий (18sp)")
                val currentFont by repo.menuFont.collectAsState(initial = "medium")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fontOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = currentFont == key,
                            onClick = { scope.launch { repo.saveMenuFont(key) } },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Про застосунок", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Версія: 1.0.1", fontSize = 14.sp)
                Text("Оновлено: 2026-10-11", fontSize = 12.sp, color = Color.Gray)
                Button(
                    onClick = { Toast.makeText(context, "У вас актуальна версія 1.0.1", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Перевірити оновлення")
                }
            }
        }

        Text("Поточна адреса: ${RetrofitClient.getBaseUrl()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
