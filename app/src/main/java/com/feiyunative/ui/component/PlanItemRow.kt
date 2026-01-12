package com.feiyunative.ui.component

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.feiyunative.data.entity.PlanItemEntity
import com.feiyunative.service.FocusTimerService

@Composable
fun PlanItemRow(
    item: PlanItemEntity,
    isRunning: Boolean
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isRunning) "专注中…" else "未计时",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = {
                val intent = Intent(context, FocusTimerService::class.java).apply {
                    action = if (isRunning) {
                        FocusTimerService.ACTION_STOP
                    } else {
                        putExtra(
                            FocusTimerService.EXTRA_PLAN_ITEM_ID,
                            item.id
                        )
                        FocusTimerService.ACTION_START
                    }
                }

                if (isRunning) {
                    // STOP：普通 service 调用
                    context.startService(intent)
                } else {
                    // START：前台 service
                    ContextCompat.startForegroundService(context, intent)
                }
            }
        ) {
            Text(if (isRunning) "停止" else "开始")
        }
    }
}
