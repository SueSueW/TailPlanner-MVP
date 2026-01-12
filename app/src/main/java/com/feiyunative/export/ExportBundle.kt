package com.feiyunative.export

/**
 * 顶层导出结构
 * 仅用于 JSON 导出 / 导入协议
 */
data class ExportBundle(
    val meta: ExportMeta,
    val plans: ExportPlans,
    val focusSessions: List<ExportFocusSession>
)

data class ExportMeta(
    val app: String,           // FeiyuNative / LingWeiJi
    val version: Int,          // 协议版本
    val exportedAt: Long       // epoch millis
)

data class ExportPlans(
    val sections: List<ExportSection>,
    val items: List<ExportItem>
)

data class ExportSection(
    val id: Long,
    val title: String,
    val order: Int
)

data class ExportItem(
    val id: Long,
    val sectionId: Long,
    val title: String,
    val order: Int
)

data class ExportFocusSession(
    val id: Long,
    val planItemId: Long,
    val startAt: Long,
    val endAt: Long,
    val durationMillis: Long
)
