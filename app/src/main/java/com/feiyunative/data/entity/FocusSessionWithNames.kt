package com.feiyunative.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Timeline 展示用：FocusSession + 关联的 Section / Item 名称
 *
 * 仅用于查询/展示，不会单独建表。
 */
data class FocusSessionWithNames(
    @Embedded
    val session: FocusSessionEntity,

    @ColumnInfo(name = "sectionTitle")
    val sectionTitle: String,

    @ColumnInfo(name = "itemTitle")
    val itemTitle: String
)
