package com.ipcheck.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipcheck.app.MainViewModel
import com.ipcheck.app.data.IpResult
import com.ipcheck.app.ui.glassSurface
import com.ipcheck.app.ui.theme.IosGreen
import com.ipcheck.app.ui.theme.IosOrange
import com.ipcheck.app.ui.theme.TextPrimary
import com.ipcheck.app.ui.theme.TextSecondary
import com.ipcheck.app.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenHistory: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val clipboard = LocalClipboardManager.current
    var showSettings by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val primaryIp = state.results.firstOrNull { it.success && !it.ip.isNullOrBlank() }?.ip

    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        // 顶部栏
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("IP Check", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("公网 IP 多源查询", color = TextSecondary, fontSize = 14.sp)
            }
            GlassIconButton(Icons.Rounded.History) { onOpenHistory() }
            Spacer(Modifier.width(10.dp))
            GlassIconButton(Icons.Rounded.Settings) { showSettings = true }
        }

        Spacer(Modifier.height(20.dp))

        // 当前 IP 主卡片
        HeroCard(
            ip = primaryIp,
            loading = state.loading,
            onCopy = {
                primaryIp?.let { clipboard.setText(AnnotatedString(it)) }
            }
        )

        Spacer(Modifier.height(16.dp))

        // 刷新按钮
        RefreshButton(loading = state.loading) { viewModel.refresh() }

        Spacer(Modifier.height(22.dp))

        Text(
            "数据源",
            color = TextTertiary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
        )

        // 三个数据源结果
        val sources = listOf("ping0.cc", "ippure.com", "ipdata.co")
        sources.forEach { srcName ->
            val result = state.results.firstOrNull { it.source == srcName }
            SourceCard(name = srcName, result = result, loading = state.loading && result == null)
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(30.dp))
    }

    if (showSettings) {
        ApiKeyDialog(
            current = apiKey,
            onDismiss = { showSettings = false },
            onSave = {
                viewModel.saveApiKey(it)
                showSettings = false
                viewModel.refresh()
            }
        )
    }
}

@Composable
private fun HeroCard(ip: String?, loading: Boolean, onCopy: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .then(glassSurface(cornerRadius = 32.dp, strong = true))
            .padding(24.dp)
    ) {
        Column {
            Text("当前公网 IP", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when {
                        ip != null -> ip
                        loading -> "获取中…"
                        else -> "暂无结果"
                    },
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                if (ip != null) {
                    GlassIconButton(Icons.Rounded.ContentCopy, size = 44.dp) { onCopy() }
                }
            }
        }
    }
}

@Composable
private fun RefreshButton(loading: Boolean, onClick: () -> Unit) {
    val rotation = if (loading) {
        val t = rememberInfiniteTransition(label = "spin")
        val a by t.animateFloat(
            0f, 360f,
            infiniteRepeatable(tween(900), RepeatMode.Restart),
            label = "a"
        )
        a
    } else 0f

    Box(
        Modifier
            .fillMaxWidth()
            .then(glassSurface(cornerRadius = 22.dp))
            .clickable(enabled = !loading) { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = "刷新",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp).rotate(rotation)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (loading) "查询中…" else "重新查询",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SourceCard(name: String, result: IpResult?, loading: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .then(glassSurface(cornerRadius = 24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(success = result?.success, loading = loading)
                Spacer(Modifier.width(10.dp))
                Text(name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = TextSecondary
                    )
                }
            }

            when {
                result == null && loading -> {
                    Spacer(Modifier.height(8.dp))
                    Text("查询中…", color = TextTertiary, fontSize = 14.sp)
                }
                result?.success == true -> {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        result.ip.orEmpty(),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    result.location?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("📍 $it", color = TextSecondary, fontSize = 13.sp)
                    }
                    result.isp?.let {
                        Spacer(Modifier.height(2.dp))
                        Text("🛰 $it", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                result != null -> {
                    Spacer(Modifier.height(8.dp))
                    Text(result.error ?: "查询失败", color = IosOrange, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusDot(success: Boolean?, loading: Boolean) {
    val color = when {
        loading || success == null -> TextTertiary
        success -> IosGreen
        else -> IosOrange
    }
    Box(
        Modifier
            .size(10.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(size)
            .then(glassSurface(cornerRadius = size / 2))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(size * 0.46f))
    }
}
