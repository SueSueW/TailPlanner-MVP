package com.feiyunative.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feiyunative.ui.screen.ItemDuration
import com.feiyunative.ui.screen.PieChartLegend
import com.feiyunative.ui.screen.SimplePieChart

@Composable
fun SummaryContent(
    totalMillis: Long,
    items: List<ItemDuration>
) {
    SimplePieChart(
        items = items,
        totalMillis = totalMillis
    )

    Spacer(modifier = Modifier.height(8.dp))

    PieChartLegend(
        items = items,
        totalMillis = totalMillis
    )
}
