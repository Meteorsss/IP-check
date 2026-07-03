package com.ipcheck.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipcheck.app.data.HistoryEntry
import com.ipcheck.app.data.IpResult
import com.ipcheck.app.data.history.HistoryRepository
import com.ipcheck.app.data.providers.IPPureProvider
import com.ipcheck.app.data.providers.IpDataProvider
import com.ipcheck.app.data.providers.IpProvider
import com.ipcheck.app.data.providers.Ping0Provider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = false,
    val results: List<IpResult> = emptyList(),
    val lastUpdated: Long = 0L
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HistoryRepository(app)

    private val providers: List<IpProvider> = listOf(
        Ping0Provider(),
        IPPureProvider(),
        IpDataProvider()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val history = repo.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val apiKey = repo.apiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        refresh() // 打开 App 自动查询
    }

    /** 并发查询三个数据源，全部返回后写入历史。 */
    fun refresh() {
        if (_uiState.value.loading) return
        _uiState.value = _uiState.value.copy(loading = true)
        viewModelScope.launch {
            val currentKey = apiKey.value
            val results = coroutineFetch(currentKey)
            val now = System.currentTimeMillis()
            _uiState.value = UiState(loading = false, results = results, lastUpdated = now)
            if (results.any { it.success }) {
                repo.addEntry(HistoryEntry(timestamp = now, results = results))
            }
        }
    }

    private suspend fun coroutineFetch(key: String): List<IpResult> = kotlinx.coroutines.coroutineScope {
        val deferreds = providers.map { p ->
            async { p.fetch(key.ifBlank { null }) }
        }
        deferreds.awaitAll()
    }

    fun saveApiKey(key: String) = viewModelScope.launch { repo.setApiKey(key) }
    fun clearHistory() = viewModelScope.launch { repo.clearHistory() }
}
