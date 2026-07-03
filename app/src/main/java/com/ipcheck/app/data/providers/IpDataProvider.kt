package com.ipcheck.app.data.providers

import com.ipcheck.app.data.IpResult
import com.ipcheck.app.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * ipdata.co 数据源。
 *
 * ipdata.co 是需要 API Key 的商业接口：https://api.ipdata.co/?api-key=YOUR_KEY
 * 用户可在 App 设置中填入自己的免费 Key。未配置 Key 时给出明确提示。
 */
class IpDataProvider : IpProvider {
    override val name = "ipdata.co"
    override val homeUrl = "https://ipdata.co/"

    override suspend fun fetch(apiKey: String?): IpResult = withContext(Dispatchers.IO) {
        val ts = System.currentTimeMillis()
        if (apiKey.isNullOrBlank()) {
            return@withContext fail("需要在设置中填写 ipdata.co 的 API Key（官网可免费申请）", ts)
        }
        try {
            val body = getText("https://api.ipdata.co/?api-key=$apiKey")
            val ip = jsonValue(body, "ip") ?: ParseUtils.extractIp(body)
            if (ip.isNullOrBlank()) {
                val apiMsg = jsonValue(body, "message")
                return@withContext fail(apiMsg ?: "未能从 ipdata.co 解析到 IP", ts)
            }
            IpResult(
                source = name, sourceUrl = homeUrl, success = true,
                ip = ip,
                location = buildLocation(body),
                isp = extractAsnName(body),
                timestamp = ts
            )
        } catch (e: Exception) {
            fail(e.message ?: "请求失败", ts)
        }
    }

    private fun buildLocation(body: String): String? {
        val parts = listOfNotNull(
            jsonValue(body, "country_name"),
            jsonValue(body, "region"),
            jsonValue(body, "city"),
        ).filter { it.isNotBlank() }
        return parts.joinToString(" · ").ifBlank { null }
    }

    /** asn 字段是嵌套对象，取其中的 name。 */
    private fun extractAsnName(body: String): String? {
        val asnBlock = Regex("\"asn\"\\s*:\\s*\\{([^}]*)}").find(body)?.groupValues?.get(1)
            ?: return jsonValue(body, "org")
        return jsonValue(asnBlock, "name") ?: jsonValue(asnBlock, "asn")
    }

    private fun jsonValue(body: String, key: String): String? {
        val m = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"").find(body)
        return m?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun getText(url: String): String {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", HttpClient.BROWSER_UA)
            .header("Accept", "application/json")
            .build()
        HttpClient.client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            // ipdata 在鉴权失败时也返回 JSON body（含 message），此处保留 body 供上层解析
            if (!resp.isSuccessful && !text.trimStart().startsWith("{")) {
                throw RuntimeException("HTTP ${resp.code}")
            }
            return text
        }
    }

    private fun fail(msg: String, ts: Long) =
        IpResult(source = name, sourceUrl = homeUrl, success = false, error = msg, timestamp = ts)
}
