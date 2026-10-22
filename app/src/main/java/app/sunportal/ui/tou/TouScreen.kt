package app.sunportal.ui.tou

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private data class SlotEdit(
    val index: Int,
    val time: String,
    val power: String,
    val soc: String,
    val grid: Boolean,
    val gen: Boolean
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TouScreen(vm: TouViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⚡ TOU (Time of Use)", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (state.loading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text("Помилка: ${state.error}", color = Color.Red)
        } else state.data?.let { d ->
            Text("Оновлено: ${d.updated}", fontSize = 12.sp, color = Color.Gray)

            val slotEdits = remember(d) {
                mutableStateListOf<SlotEdit>().apply {
                    d.slots.forEach {
                        add(
                            SlotEdit(
                                index = it.index,
                                time = it.time,
                                power = it.power.toString(),
                                soc = it.soc.toString(),
                                grid = it.grid,
                                gen = it.gen
                            )
                        )
                    }
                }
            }

            // Кожен слот — окрема картка
            slotEdits.forEachIndexed { i, edit ->
                // Кінець слота = час наступного (останній замикається на перший)
                val endTime = slotEdits.getOrNull(i + 1)?.time ?: slotEdits.first().time

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${edit.index}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "${edit.time} – $endTime",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = edit.time,
                                onValueChange = { },
                                label = { Text("Початок", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = false
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { },
                                label = { Text("Кінець", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = false
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = edit.power,
                                onValueChange = { v -> slotEdits[i] = edit.copy(power = v) },
                                label = { Text("Потужність, Вт") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = edit.soc,
                                onValueChange = { v -> slotEdits[i] = edit.copy(soc = v) },
                                label = { Text("SOC, %") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Мережа", fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Switch(
                                    checked = edit.grid,
                                    onCheckedChange = { v -> slotEdits[i] = edit.copy(grid = v) }
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Генератор", fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Switch(
                                    checked = edit.gen,
                                    onCheckedChange = { v -> slotEdits[i] = edit.copy(gen = v) }
                                )
                            }
                        }
                    }
                }
            }

            Text("Дні тижня: ${d.days.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray)
            Text("Статус: ${if (d.enabled) "✅ Увімкнено" else "❌ Вимкнено"}", fontSize = 12.sp, color = Color.Gray)

            if (status.isNotEmpty()) {
                Text(status, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        status = "✅ Перечитано з сервера"
                        vm.loadData()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("🔄 Прочитати") }

                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                ) { Text("💾 Записати") }
            }

            Text("Пресети:", fontSize = 13.sp, color = Color.Gray)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Blackout", "Від прогнозу", "Зима", "Весна", "Літо", "Осінь").forEach { preset ->
                    OutlinedButton(
                        onClick = { status = "Пресет «$preset» вибрано (збереження ще не реалізовано)" }
                    ) { Text(preset, fontSize = 13.sp) }
                }
            }
        }
    }
}