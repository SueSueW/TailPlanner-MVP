package com.feiyunative.data.dao

import androidx.room.*
import com.feiyunative.data.entity.PlanSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanSectionDao {

    @Query("""
        SELECT * FROM plan_section
        WHERE planId = :planId
        ORDER BY position ASC
    """)
    fun observeByPlan(planId: String): Flow<List<PlanSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(section: PlanSectionEntity)

    @Query("""
        UPDATE plan_section
        SET collapsed = :collapsed
        WHERE id = :id
    """)
    suspend fun setCollapsed(id: String, collapsed: Boolean)

    @Query("""
    UPDATE plan_section
    SET title = :title
    WHERE id = :id
""")
    suspend fun renameTitle(id: String, title: String)


    // ⭐ 关键补齐：Repository 依赖的方法
    @Query("""
        SELECT * FROM plan_section
        WHERE planId = :planId
        ORDER BY position ASC
        LIMIT 1
    """)
    suspend fun getFirstByPlan(planId: String): PlanSectionEntity?

    @Delete
    suspend fun delete(section: PlanSectionEntity)
}
