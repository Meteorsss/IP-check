package com.ipcheck.app.data.providers

import com.ipcheck.app.data.IpResult

/** 单个 IP 数据源的抽象。每个实现独立处理自己的抓取与解析逻辑。 */
interface IpProvider {
    val name: String
    val homeUrl: String

    /** 执行查询。实现应捕获自身异常并以 IpResult(success=false) 返回，不要抛出。 */
    suspend fun fetch(apiKey: String?): IpResult
}

/** 抓取解析用的通用工具。 */
object ParseUtils {
    // 兼容压缩形式的 IPv6，同时匹配 IPv4
    private val IPV4 = Regex("""\b(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)\b""")
    private val IPV6 = Regex("""\b(?:[A-Fa-f0-9]{1,4}:){2,7}[A-Fa-f0-9]{0,4}\b""")

    /** 从任意文本中提取第一个 IPv4；找不到再尝试 IPv6。 */
    fun extractIp(text: String): String? {
        IPV4.find(text)?.let { return it.value }
        // 过滤明显不是地址的片段（至少两段十六进制才算）
        IPV6.find(text)?.let {
            val v = it.value
            if (v.contains(":") && v.count { c -> c == ':' } >= 2) return v
        }
        return null
    }
}
