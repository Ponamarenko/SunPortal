package app.sunportal.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sunportal_settings")

class SettingsRepository(private val context: Context) {
    private val KEY_IP = stringPreferencesKey("server_ip")
    private val KEY_PORT = stringPreferencesKey("server_port")
    private val KEY_MENU_FONT = stringPreferencesKey("menu_font_size")
    private val KEY_MENU_PADDING = stringPreferencesKey("menu_item_padding")
    private val KEY_DRAWER_EXPANDED = stringPreferencesKey("drawer_expanded_groups")

    private val defaultFont = "medium"
    private val defaultPadding = "12.dp"

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        val ip = prefs[KEY_IP] ?: "ha4a.pp.ua"
        val port = prefs[KEY_PORT] ?: "8080"
        "http://$ip:$port/"
    }

    val serverIp: Flow<String> = context.dataStore.data.map { it[KEY_IP] ?: "ha4a.pp.ua" }
    val serverPort: Flow<String> = context.dataStore.data.map { it[KEY_PORT] ?: "8080" }
    val menuFont: Flow<String> = context.dataStore.data.map { it[KEY_MENU_FONT] ?: defaultFont }
    val menuPadding: Flow<String> = context.dataStore.data.map { it[KEY_MENU_PADDING] ?: defaultPadding }
    val drawerExpandedGroups: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_DRAWER_EXPANDED] ?: ""
        if (json.isEmpty()) emptySet() else {
            try {
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotEmpty() }
                    .toSet()
            } catch (e: Exception) { emptySet() }
        }
    }

    suspend fun saveServer(ip: String, port: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IP] = ip
            prefs[KEY_PORT] = port
        }
    }

    suspend fun saveMenuFont(font: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MENU_FONT] = font
            when (font) {
                "small" -> prefs[KEY_MENU_PADDING] = "8.dp"
                "large" -> prefs[KEY_MENU_PADDING] = "16.dp"
                else -> prefs[KEY_MENU_PADDING] = "12.dp"
            }
        }
    }

    suspend fun saveDrawerExpandedGroups(groups: List<String>) {
        context.dataStore.edit { prefs ->
            if (groups.isEmpty()) {
                prefs.remove(KEY_DRAWER_EXPANDED)
            } else {
                val json = groups.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                prefs[KEY_DRAWER_EXPANDED] = json
            }
        }
    }
}
