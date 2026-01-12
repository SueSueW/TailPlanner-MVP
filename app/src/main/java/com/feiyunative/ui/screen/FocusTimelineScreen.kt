package com.feiyunative.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feiyunative.ui.component.TodaySummaryCard
import com.feiyunative.ui.vm.FocusTimelineViewModel
import com.feiyunative.data.entity.FocusSessionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.Application
import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyunative.ui.theme.PieChartColors
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Color
import com.feiyunative.ui.component.SummaryContent



@Composable
fun FocusTimelineScreen() {
    val context = LocalContext.current
    val vm: FocusTimelineViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )

    val sessions by vm.sessions.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<FocusSessionEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
/*
        // ---------- 今日总结 ----------
        TodaySummaryCard()

 */

        // ---------- 日期切换（MVP：前后一天 + 点击打开日期选择） ----------
        val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { vm.selectDate(selectedDate.minusDays(1)) }) {
                Text("← 前一天")
            }

            TextButton(
                onClick = {
                    val y = selectedDate.year
                    val m = selectedDate.monthValue - 1
                    val d = selectedDate.dayOfMonth
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            vm.selectDate(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        y,
                        m,
                        d
                    ).show()
                }
            ) {
                Text(selectedDate.format(dateFormatter))
            }

            TextButton(onClick = { vm.selectDate(selectedDate.plusDays(1)) }) {
                Text("后一天 →")
            }
        }

        // ---------- 追加时间段 ----------
        Button(
            onClick = {
                editingSession = null
                showEditDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("+ 追加时间段")
        }

        Spacer(modifier = Modifier.height(12.dp))

        val itemDurations = aggregateByItem(sessions)
        val totalMillis = itemDurations.sumOf { it.totalMillis }

        TodaySummary(
            totalMillis = totalMillis,
            items = itemDurations
        )

        // ---------- 时间线 ----------
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(sessions, key = { it.session.id }) { session ->
                FocusSessionRow(
                    session = session,
                    onEdit = {
                        editingSession = session.session
                        showEditDialog = true
                    },
                    onDelete = {
                        vm.deleteSession(session.session.id)
                    }
                )
            }
        }
    }

    // ---------- 编辑 / 新增 Dialog ----------
    if (showEditDialog) {
        FocusSessionEditDialog(
            session = editingSession,
            date = selectedDate,
            onConfirm = { startAt, endAt ->
                vm.saveSession(editingSession, startAt, endAt)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

data class ItemDuration(
    val itemTitle: String,
    val totalMillis: Long
)

private fun aggregateByItem(
    sessions: List<com.feiyunative.data.entity.FocusSessionWithNames>
): List<ItemDuration> {
    return sessions
        .groupBy { it.itemTitle }
        .map { (itemTitle, list) ->
            ItemDuration(
                itemTitle = itemTitle,
                totalMillis = list.sumOf { it.session.durationMillis }
            )
        }
        .sortedByDescending { it.totalMillis }
}

@Composable
private fun TodaySummary(
    totalMillis: Long,
    items: List<ItemDuration>
) {
    val totalText = com.feiyunative.core.util.formatDurationHms(totalMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Today Summary",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "总专注时长：$totalText",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryContent(
                totalMillis = totalMillis,
                items = items
            )

        }
    }
}

@Composable
fun SimplePieChart(
    items: List<ItemDuration>,
    totalMillis: Long
) {
    val colors = PieChartColors.palette

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {

            // ✅ 空数据：画一个“空饼图”（整圆）
            if (totalMillis <= 0L || items.isEmpty()) {
                drawArc(
                    color = Color(0xFFB0B0B0), // 灰色空态
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true
                )
                return@Canvas
            }

            // ✅ 有数据：正常画饼图
            var startAngle = -90f
            items.forEachIndexed { index, item ->
                val sweep =
                    item.totalMillis.toFloat() / totalMillis * 360f

                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )

                startAngle += sweep
            }
        }
    }
}


@Composable
fun PieChartLegend(
    items: List<ItemDuration>,
    totalMillis: Long
) {
    val colors = PieChartColors.palette

    Column(
        modifier = Modifier.padding(start = 8.dp) // 轻微左缩进，更好看
    ) {
        items.forEachIndexed { index, item ->
            val color = colors[index % colors.size]
            val percent =
                if (totalMillis > 0)
                    (item.totalMillis * 100f / totalMillis).toInt()
                else 0

            val durationText =
                com.feiyunative.core.util.formatDurationHms(item.totalMillis)

            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 色点
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, shape = MaterialTheme.shapes.small)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // item 名称
                Text(
                    text = item.itemTitle,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 时长 + 百分比（紧跟在后面）
                Text(
                    text = "$durationText (${percent}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
