package com.feiyunative.core.db

import android.content.Context
import androidx.room.Room

object DbProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "feiyu.db"
            ).build().also { INSTANCE = it }
        }
    }
    fun close() {
        INSTANCE?.close()
        INSTANCE = null
    }
}
