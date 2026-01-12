package com.feiyunative.data.dao

import androidx.room.*
import com.feiyunative.data.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanItemDao {

    @Query(
        """
        SELECT * FROM plan_item
        WHERE sectionId = :sectionId
        ORDER BY createdAt ASC
        """
    )
    fun observeBySection(sectionId: String): Flow<List<PlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PlanItemEntity)

    @Query("UPDATE plan_item SET done = :done WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean)

    @Query("UPDATE plan_item SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Int?)

    @Query("""
    UPDATE plan_item
    SET title = :title
    WHERE id = :id
""")
    suspend fun renameTitle(id: String, title: String)

    @Query("""
        SELECT COUNT(*) FROM plan_item
        WHERE sectionId = :sectionId
    """)
    suspend fun countBySection(sectionId: String): Int

    @Query("SELECT * FROM plan_item WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PlanItemEntity?

    /** ✅ 新增：拿任意一个 planItem 的 id（用于兜底） */
    @Query("SELECT id FROM plan_item LIMIT 1")
    suspend fun getAnyId(): String?

    @Delete
    suspend fun delete(item: PlanItemEntity)
}
