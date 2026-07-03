package com.ipcheck.app.data.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ipcheck.app.data.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ip_check_store")

/** 历史记录与设置的持久化。历史以 JSON 字符串形式存入 DataStore，避免引入数据库注解处理器。 */
class HistoryRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val historyKey = stringPreferencesKey("history_json")
    private val apiKeyKey = stringPreferencesKey("ipdata_api_key")

    val history: Flow<List<HistoryEntry>> = context.dataStore.data.map { prefs ->
        prefs[historyKey]?.let { raw ->
            runCatching { json.decodeFromString<List<HistoryEntry>>(raw) }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[apiKeyKey].orEmpty() }

    /** 追加一条历史记录，最新的在前，最多保留 100 条。 */
    suspend fun addEntry(entry: HistoryEntry) {
        context.dataStore.edit { prefs ->
            val current = prefs[historyKey]?.let {
                runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = (listOf(entry) + current).take(100)
            prefs[historyKey] = json.encodeToString(updated)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it[historyKey] = "[]" }
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[apiKeyKey] = key.trim() }
    }
}
