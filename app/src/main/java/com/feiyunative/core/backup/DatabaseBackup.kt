package com.feiyunative.core.backup

import android.content.Context
import android.net.Uri
import com.feiyunative.core.db.DbProvider
import java.io.File

object DatabaseBackup {

    private const val DB_NAME = "feiyu.db" // ⚠️ 如不确定，下一步我可帮你确认

    fun exportDatabase(context: Context, uri: Uri) {
        // ✅ 关键：先关闭数据库，触发 WAL checkpoint
        DbProvider.close()

        val dbFile = context.getDatabasePath("feiyu.db")
        if (!dbFile.exists()) return

        context.contentResolver.openOutputStream(uri)?.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }


    fun importDatabase(context: Context, uri: Uri) {
        val dbFile: File = context.getDatabasePath(DB_NAME)

        // ⚠️ 关键：先关闭 Room
        DbProvider.close()

        context.contentResolver.openInputStream(uri)?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
