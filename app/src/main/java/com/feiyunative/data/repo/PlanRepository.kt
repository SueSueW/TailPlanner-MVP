package com.feiyunative.data.repo

import com.feiyunative.core.util.newId
import com.feiyunative.core.util.nowMillis
import com.feiyunative.data.dao.PlanDao
import com.feiyunative.data.dao.PlanItemDao
import com.feiyunative.data.dao.PlanSectionDao
import com.feiyunative.data.entity.PlanEntity
import com.feiyunative.data.entity.PlanItemEntity
import com.feiyunative.data.entity.PlanSectionEntity
import kotlinx.coroutines.flow.Flow

class PlanRepository(
    private val planDao: PlanDao,
    private val sectionDao: PlanSectionDao,
    private val itemDao: PlanItemDao
) {

    /* ---------- Plan ---------- */

    fun observeActivePlans(): Flow<List<PlanEntity>> =
        planDao.observeActive()

    suspend fun upsert(plan: PlanEntity) =
        planDao.upsert(plan)

    /* ---------- Section ---------- */

    fun observeSections(planId: String): Flow<List<PlanSectionEntity>> =
        sectionDao.observeByPlan(planId)

    suspend fun upsertSection(section: PlanSectionEntity) =
        sectionDao.upsert(section)

    suspend fun setSectionCollapsed(id: String, collapsed: Boolean) =
        sectionDao.setCollapsed(id, collapsed)

    suspend fun renameSectionTitle(id: String, title: String) =
        sectionDao.renameTitle(id, title)

    /**
     * ⭐ 保证一个 Plan 至少有一个 Section
     */
    suspend fun ensureDefaultSection(planId: String): PlanSectionEntity {
        val existing = sectionDao.getFirstByPlan(planId)
        if (existing != null) return existing

        val section = PlanSectionEntity(
            id = newId(),
            planId = planId,
            title = "阶段一",
            position = 0,
            collapsed = false
        )
        sectionDao.upsert(section)
        return section
    }

    /* ---------- Item ---------- */

    fun observeItems(sectionId: String): Flow<List<PlanItemEntity>> =
        itemDao.observeBySection(sectionId)

    suspend fun upsertItem(item: PlanItemEntity) =
        itemDao.upsert(item)

    suspend fun toggleItemDone(id: String, done: Boolean) =
        itemDao.setDone(id, done)

    suspend fun renameItemTitle(id: String, title: String) =
        itemDao.renameTitle(id, title)

    /**
     * ⭐ 保证一个 Section 至少有一个 Item
     */
    suspend fun ensureDefaultItems(sectionId: String) {
        val count = itemDao.countBySection(sectionId)
        if (count > 0) return

        itemDao.upsert(
            PlanItemEntity(
                id = newId(),
                sectionId = sectionId,
                title = "示例计时任务",
                type = "timer",
                target = null,
                completed = null,
                done = false,
                createdAt = nowMillis()
            )
        )
    }
}
