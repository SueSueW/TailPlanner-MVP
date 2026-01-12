package com.feiyunative.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feiyunative.core.db.DbProvider
import com.feiyunative.core.util.newId
import com.feiyunative.data.entity.FocusSessionEntity
import com.feiyunative.data.entity.PlanItemEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import com.feiyunative.data.entity.FocusSessionWithNames


class FocusTimelineViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DbProvider.get(app)
    private val sessionDao = db.focusSessionDao()
    private val planItemDao = db.planItemDao()
    private val planDao = db.planDao()
    private val sectionDao = db.planSectionDao()

    /** 当前选中的日期 */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /** 当天的起止时间 */
    private fun dayRange(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    /** 当前日期的 Session 列表 */
    val sessions: StateFlow<List<FocusSessionWithNames>> =
        selectedDate
            .flatMapLatest { date ->
                val (from, to) = dayRange(date)
                sessionDao.observeBetweenWithNames(from, to)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )


    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun saveSession(
        editing: FocusSessionEntity?,
        startAt: Long,
        endAt: Long
    ) {
        viewModelScope.launch {

            val duration = endAt - startAt
            if (duration <= 0) return@launch

            val safeItemId =
                editing?.planItemId
                    ?: planItemDao.getAnyId()
                    ?: createTempInboxItem()

            val session = FocusSessionEntity(
                id = editing?.id ?: newId(),
                planItemId = safeItemId,
                startAt = startAt,
                endAt = endAt,
                durationMillis = duration,
                source = "manual_edit"
            )

            sessionDao.insert(session)
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            sessionDao.deleteById(id)
        }
    }

    /**
     * 当系统中还没有任何 PlanItem 时，
     * 创建一个临时 Inbox Item，保证外键不崩
     */
    private suspend fun createTempInboxItem(): String {
        // ✅ 保证外键链路存在：Plan -> Section -> Item
        val now = System.currentTimeMillis()
        val planId = newId()
        planDao.upsert(
            com.feiyunative.data.entity.PlanEntity(
                id = planId,
                title = "Inbox",
                position = 0,
                createdAt = now,
                archived = false
            )
        )

        val sectionId = newId()
        sectionDao.upsert(
            com.feiyunative.data.entity.PlanSectionEntity(
                id = sectionId,
                planId = planId,
                title = "Inbox",
                position = 0,
                collapsed = false
            )
        )

        val itemId = newId()
        planItemDao.upsert(
            PlanItemEntity(
                id = itemId,
                sectionId = sectionId,
                title = "Inbox",
                type = "timer",
                target = null,
                completed = null,
                done = false,
                createdAt = now
            )
        )
        return itemId
    }
}
