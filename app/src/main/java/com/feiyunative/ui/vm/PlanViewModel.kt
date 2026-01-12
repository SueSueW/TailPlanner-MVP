package com.feiyunative.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feiyunative.core.db.DbProvider
import com.feiyunative.data.entity.PlanEntity
import com.feiyunative.data.repo.PlanRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.feiyunative.data.entity.PlanSectionEntity
import com.feiyunative.data.entity.PlanItemEntity
import com.feiyunative.core.util.newId
import com.feiyunative.core.util.nowMillis


@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlanViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DbProvider.get(app)
    private val planDao = db.planDao()

    private val repo = PlanRepository(
        db.planDao(),
        db.planSectionDao(),
        db.planItemDao()
    )

    /** 所有未归档 Plan（顶部 Tab 数据源） */
    val plans: StateFlow<List<PlanEntity>> =
        repo.observeActivePlans()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** 当前选中的 PlanId */
    private val _currentPlanId = MutableStateFlow<String?>(null)
    val currentPlanId: StateFlow<String?> = _currentPlanId.asStateFlow()

    /** 当前选中的 Plan（给 UI 用） */
    val currentPlan: StateFlow<PlanEntity?> =
        combine(plans, currentPlanId) { list, id ->
            when {
                list.isEmpty() -> null
                id == null -> list.first()
                else -> list.find { it.id == id } ?: list.first()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** 当前 Plan 的 Section 列表  */
    val sections: StateFlow<List<PlanSectionEntity>> =
        currentPlan
            .filterNotNull()
            .flatMapLatest { plan ->
                repo.observeSections(plan.id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** 选择item  */
    val itemsBySection: StateFlow<Map<String, List<PlanItemEntity>>> =
        sections
            .flatMapLatest { sectionList ->
                if (sectionList.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    combine(
                        sectionList.map { section ->
                            repo.observeItems(section.id)
                                .map { items -> section.id to items }
                        }
                    ) { pairs -> pairs.toMap() }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyMap()
            )

    private val focusSessionDao = db.focusSessionDao()

    val isFocusRunning: StateFlow<Boolean> =
        focusSessionDao.observeIsRunning()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val runningItemId: StateFlow<String?> =
        focusSessionDao.observeRunningItemId()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )


    fun selectPlan(id: String) {
        _currentPlanId.value = id
    }

    fun addPlan(title: String) {
        viewModelScope.launch {
            val nextPosition = plans.value.size
            repo.upsert(
                PlanEntity(
                    id = com.feiyunative.core.util.newId(),
                    title = title,
                    position = nextPosition,
                    createdAt = com.feiyunative.core.util.nowMillis(),
                    archived = false
                )
            )
        }
    }

    fun addSection(planId: String, title: String) {
        viewModelScope.launch {
            val nextPosition = sections.value.size
            repo.upsertSection(
                PlanSectionEntity(
                    id = newId(),
                    planId = planId,
                    title = title,
                    position = nextPosition,
                    collapsed = false
                )
            )
        }
    }

    fun addItem(sectionId: String, title: String) {
        viewModelScope.launch {
            repo.upsertItem(
                PlanItemEntity(
                    id = newId(),
                    sectionId = sectionId,
                    title = title,
                    type = "timer",
                    target = null,
                    completed = null,
                    done = false,
                    createdAt = nowMillis()
                )
            )
        }
    }

    fun deleteItem(item: PlanItemEntity) {
        viewModelScope.launch {
            db.planItemDao().delete(item)
        }
    }

    fun deleteSection(section: PlanSectionEntity) {
        viewModelScope.launch {
            db.planSectionDao().delete(section)
        }
    }

    fun toggleSection(section: PlanSectionEntity) {
        viewModelScope.launch {
            repo.setSectionCollapsed(
                id = section.id,
                collapsed = !section.collapsed
            )
        }
    }


    fun toggleItem(item: PlanItemEntity) {
        viewModelScope.launch {
            repo.toggleItemDone(item.id, !item.done)
        }
    }

    fun renameSection(section: PlanSectionEntity, newTitle: String) {
        viewModelScope.launch {
            repo.renameSectionTitle(section.id, newTitle)
        }
    }

    fun renameItem(item: PlanItemEntity, newTitle: String) {
        viewModelScope.launch {
            repo.renameItemTitle(item.id, newTitle)
        }
    }


    fun ensureDefaultSectionAndItems(planId: String) {
        viewModelScope.launch {
            val section = repo.ensureDefaultSection(planId)
            repo.ensureDefaultItems(section.id)
        }
    }

    fun createDefaultPlan() {
        viewModelScope.launch {
            val planId = newId()

            val plan = PlanEntity(
                id = planId,
                title = "我的第一个 Plan",
                position = 0,                          // 第一个 Plan
                createdAt = System.currentTimeMillis(),
                archived = false
            )

            planDao.upsert(plan)
            selectPlan(planId)
        }
    }


}
