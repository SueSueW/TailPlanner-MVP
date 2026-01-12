package com.feiyunative.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feiyunative.ui.component.SectionCard
import com.feiyunative.ui.vm.PlanViewModel
import android.util.Log
import com.feiyunative.ui.component.PlanItemRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState



@Composable
fun PlanScreen(
    vm: PlanViewModel = viewModel()
) {
    val plans by vm.plans.collectAsState()
    val currentPlan by vm.currentPlan.collectAsState()
    val sections by vm.sections.collectAsState()
    val itemsBySection by vm.itemsBySection.collectAsState()

    var showAddPlan by remember { mutableStateOf(false) }
    var showAddSectionForPlanId by remember { mutableStateOf<String?>(null) }
    var showAddItemForSectionId by remember { mutableStateOf<String?>(null) }

    var editingSection by remember { mutableStateOf<com.feiyunative.data.entity.PlanSectionEntity?>(null) }
    var editingItem by remember { mutableStateOf<com.feiyunative.data.entity.PlanItemEntity?>(null) }

    val isFocusRunning by vm.isFocusRunning.collectAsState()
    val runningItemId by vm.runningItemId.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {

        // ---------- 顶部操作（MVP：新 Plan / 新 Section） ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { showAddPlan = true }) {
                Text("+ Plan")
            }
            OutlinedButton(
                onClick = {
                    val pid = currentPlan?.id
                    if (pid != null) showAddSectionForPlanId = pid
                },
                enabled = currentPlan != null
            ) {
                Text("+ Section")
            }
        }

        // ✅【关键修复】没有 Plan 时，不渲染 TabRow
        if (plans.isEmpty()) {

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "还没有 Plan",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = { vm.createDefaultPlan() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("创建第一个 Plan")
            }

            // ⛔ 非常重要：直接 return，下面的 UI 不执行
            return
        }

        // ---------- 顶部 Plan Tab ----------
        val selectedIndex = plans.indexOfFirst { it.id == currentPlan?.id }
            .takeIf { it >= 0 } ?: 0

        ScrollableTabRow(
            selectedTabIndex = selectedIndex
        ) {
            plans.forEach { plan ->
                Tab(
                    selected = plan.id == currentPlan?.id,
                    onClick = { vm.selectPlan(plan.id) },
                    text = { Text(plan.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------- 当前 Plan ----------
        currentPlan?.let { plan ->
            Text(
                text = "当前 Plan：${plan.title}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LaunchedEffect(plan.id) {
                vm.ensureDefaultSectionAndItems(plan.id)
            }
        }

        // ---------- Section / Item ----------
        sections.forEach { section ->
            SectionCard(
                section = section,
                onToggle = { vm.toggleSection(section) }
            ) {
                itemsBySection[section.id].orEmpty().forEach { item ->
                    PlanItemRow(
                        item = item,
                        isRunning = runningItemId == item.id
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingItem = item }) {
                            Text("重命名")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { vm.deleteItem(item) }) {
                            Text("删除")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (itemsBySection[section.id].orEmpty().isEmpty()) {
                        Text("暂无 Item", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    TextButton(onClick = { showAddItemForSectionId = section.id }) {
                        Text("+ Item")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { editingSection = section }) {
                        Text("重命名")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { vm.deleteSection(section) }) {
                        Text("删除 Section")
                    }
                }
            }
        }

        // ✅ Bottom padding（与 Timeline 对齐）
        Spacer(modifier = Modifier.height(120.dp))
    }

    // ---------- Dialog：新 Plan ----------
    if (showAddPlan) {
        SimpleTextInputDialog(
            title = "新建 Plan",
            hint = "Plan 名称",
            onConfirm = {
                vm.addPlan(it)
                showAddPlan = false
            },
            onDismiss = { showAddPlan = false }
        )
    }

    // ---------- Dialog：新 Section ----------
    showAddSectionForPlanId?.let { planId ->
        SimpleTextInputDialog(
            title = "新建 Section",
            hint = "Section 名称",
            onConfirm = {
                vm.addSection(planId, it)
                showAddSectionForPlanId = null
            },
            onDismiss = { showAddSectionForPlanId = null }
        )
    }

    // ---------- Dialog：重命名 Section ----------
    editingSection?.let { section ->
        SimpleTextInputDialog(
            title = "重命名 Section",
            hint = "Section 名称",
            initialText = section.title,
            onConfirm = {
                vm.renameSection(section, it)
                editingSection = null
            },
            onDismiss = { editingSection = null }
        )
    }

    // ---------- Dialog：新 Item ----------
    showAddItemForSectionId?.let { sectionId ->
        SimpleTextInputDialog(
            title = "新建 PlanItem",
            hint = "Item 名称",
            onConfirm = {
                vm.addItem(sectionId, it)
                showAddItemForSectionId = null
            },
            onDismiss = { showAddItemForSectionId = null }
        )
    }

    // ---------- Dialog：重命名 Item ----------
    editingItem?.let { item ->
        SimpleTextInputDialog(
            title = "重命名 Item",
            hint = "Item 名称",
            initialText = item.title,
            onConfirm = {
                vm.renameItem(item, it)
                editingItem = null
            },
            onDismiss = { editingItem = null }
        )
    }

}

@Composable
private fun SimpleTextInputDialog(
    title: String,
    hint: String,
    initialText: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(hint) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val v = text.trim()
                    if (v.isNotEmpty()) onConfirm(v) else onDismiss()
                }
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
