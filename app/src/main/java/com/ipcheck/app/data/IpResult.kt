package com.ipcheck.app.data

import kotlinx.serialization.Serializable

/** 单个数据源的查询结果。用于 UI 展示，同时可序列化后写入历史记录。 */
@Serializable
data class IpResult(
    val source: String,          // 数据源名称，如 "ping0.cc"
    val sourceUrl: String,       // 数据源主页
    val success: Boolean,
    val ip: String? = null,      // 查询到的公网 IP
    val location: String? = null,// 归属地（国家/地区/城市）
    val isp: String? = null,     // 运营商 / ASN
    val error: String? = null,   // 失败时的错误信息
    val timestamp: Long = 0L     // 查询完成时间（毫秒）
)

/** 一次完整查询（三个数据源）的历史记录条目。 */
@Serializable
data class HistoryEntry(
    val timestamp: Long,
    val results: List<IpResult>
) {
    /** 取任一成功源的 IP 作为该条记录的代表 IP。 */
    val primaryIp: String?
        get() = results.firstOrNull { it.success && !it.ip.isNullOrBlank() }?.ip
}
