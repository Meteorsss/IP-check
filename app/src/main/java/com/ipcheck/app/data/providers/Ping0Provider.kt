package com.ipcheck.app.data.providers

import com.ipcheck.app.data.IpResult
import com.ipcheck.app.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * ping0.cc 数据源。
 *
 * ping0 主页会在 HTML 中渲染访问者 IP 与归属地。这里优先尝试其纯文本 IP 接口，
 * 失败则回退到抓取主页并用正则提取。ping0 有一定反爬机制，附带浏览器 UA 提升成功率。
 */
class Ping0Provider : IpProvider {
    override val name = "ping0.cc"
    override val homeUrl = "https://ping0.cc/"

    override suspend fun fetch(apiKey: String?): IpResult = withContext(Dispatchers.IO) {
        val ts = System.currentTimeMillis()
        try {
            // 1) 优先尝试纯文本 IP 接口
            val quickIp = runCatching { getText("https://ping0.cc/ip") }
                .getOrNull()
                ?.let { ParseUtils.extractIp(it) }

            // 2) 抓取主页用于提取归属地（及在快速接口失败时提取 IP）
            val html = runCatching { getText(homeUrl) }.getOrNull()

            val ip = quickIp ?: html?.let { ParseUtils.extractIp(it) }
            if (ip.isNullOrBlank()) {
                return@withContext fail("未能从 ping0.cc 解析到 IP（可能触发了反爬）", ts)
            }

            val location = html?.let { extractLocation(it) }
            IpResult(
                source = name, sourceUrl = homeUrl, success = true,
                ip = ip, location = location, isp = null, timestamp = ts
            )
        } catch (e: Exception) {
            fail(e.message ?: "请求失败", ts)
        }
    }

    private fun getText(url: String): String {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", HttpClient.BROWSER_UA)
            .header("Accept", "text/html,application/xhtml+xml,*/*")
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .build()
        HttpClient.client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}")
            return resp.body?.string().orEmpty()
        }
    }

    /** ping0 主页归属地通常位于 class="name" 的节点内。 */
    private fun extractLocation(html: String): String? {
        val m = Regex("""class="name"[^>]*>([^<]+)<""").find(html)
        return m?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun fail(msg: String, ts: Long) =
        IpResult(source = name, sourceUrl = homeUrl, success = false, error = msg, timestamp = ts)
}
