package com.feiyunative.core.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

fun formatDateTime(millis: Long): String {
    return dateFormat.format(Date(millis))
}

/**
 * 将毫秒转换为 HH:mm:ss
 *
 * 例：
 *  - 3_726_000 → 01:02:06
 *  - 65_000    → 00:01:05
 */
fun formatDurationHms(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
