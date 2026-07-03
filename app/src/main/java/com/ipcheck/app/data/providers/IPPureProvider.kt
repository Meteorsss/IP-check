package com.ipcheck.app.data.providers

import com.ipcheck.app.data.IpResult
import com.ipcheck.app.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * ippure.com 数据源。
 *
 * 优先尝试常见的 JSON 接口，失败则回退抓取主页并用正则提取 IP 与归属信息。
 * 解析对返回结构做了较宽松的兼容（ip / country / city / org 等常见字段）。
 */
class IPPureProvider : IpProvider {
    override val name = "ippure.com"
    override val homeUrl = "https://ippure.com/"

    private val jsonEndpoints = listOf(
        "https://ippure.com/api",
        "https://ippure.com/json",
        "https://api.ippure.com/",
    )

    override suspend fun fetch(apiKey: String?): IpResult = withContext(Dispatchers.IO) {
        val ts = System.currentTimeMillis()
        try {
            // 1) 尝试 JSON 接口
            for (url in jsonEndpoints) {
                val body = runCatching { getText(url) }.getOrNull() ?: continue
                if (body.trimStart().startsWith("{")) {
                    val ip = jsonValue(body, "ip") ?: ParseUtils.extractIp(body)
                    if (!ip.isNullOrBlank()) {
                        return@withContext IpResult(
                            source = name, sourceUrl = homeUrl, success = true,
                            ip = ip,
                            location = buildLocation(body),
                            isp = jsonValue(body, "org") ?: jsonValue(body, "isp") ?: jsonValue(body, "asn"),
                            timestamp = ts
                        )
                    }
                }
            }

            // 2) 回退：抓取主页正则提取
            val html = getText(homeUrl)
            val ip = ParseUtils.extractIp(html)
                ?: return@withContext fail("未能从 ippure.com 解析到 IP", ts)
            IpResult(
                source = name, sourceUrl = homeUrl, success = true,
                ip = ip, location = buildLocation(html), timestamp = ts
            )
        } catch (e: Exception) {
            fail(e.message ?: "请求失败", ts)
        }
    }

    private fun buildLocation(body: String): String? {
        val parts = listOfNotNull(
            jsonValue(body, "country") ?: jsonValue(body, "country_name"),
            jsonValue(body, "region") ?: jsonValue(body, "region_name"),
            jsonValue(body, "city"),
        ).filter { it.isNotBlank() }
        return parts.joinToString(" · ").ifBlank { null }
    }

    private fun jsonValue(body: String, key: String): String? {
        val m = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"").find(body)
        return m?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun getText(url: String): String {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", HttpClient.BROWSER_UA)
            .header("Accept", "application/json,text/html,*/*")
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .build()
        HttpClient.client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}")
            return resp.body?.string().orEmpty()
        }
    }

    private fun fail(msg: String, ts: Long) =
        IpResult(source = name, sourceUrl = homeUrl, success = false, error = msg, timestamp = ts)
}
