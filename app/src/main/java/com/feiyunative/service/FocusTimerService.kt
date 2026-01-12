package com.feiyunative.service

import android.app.*
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.feiyunative.R
import com.feiyunative.core.db.DbProvider
import com.feiyunative.core.util.newId
import com.feiyunative.data.entity.FocusSessionEntity
import kotlinx.coroutines.*

class FocusTimerService : Service() {

    companion object {
        const val ACTION_START = "FOCUS_START"
        const val ACTION_STOP = "FOCUS_STOP"
        const val EXTRA_PLAN_ITEM_ID = "PLAN_ITEM_ID"

        const val CHANNEL_ID = "focus_timer"
        const val NOTIFICATION_ID = 1001

        /** ⭐ 全局锁：当前正在计时的 itemId */
        @Volatile
        var currentItemId: String? = null

        /** ⭐ 当前正在计时的 sessionId（用于更新结束时间） */
        @Volatile
        var currentSessionId: String? = null
    }

    private var startElapsed: Long = 0L
    private var startWallTime: Long = 0L

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Android 13+：如果没有通知权限，前台服务无法展示通知。
        // Android 14 上这会导致 startForeground 直接抛 ForegroundServiceStartNotAllowedException。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        when (intent?.action) {

            ACTION_START -> {
                val newItemId =
                    intent.getStringExtra(EXTRA_PLAN_ITEM_ID) ?: return START_NOT_STICKY

                // ⭐ 如果已有任务在跑，而且不是同一个 → 先停
                if (currentItemId != null && currentItemId != newItemId) {
                    stopAndSaveInternal()
                }

                currentItemId = newItemId
                startElapsed = SystemClock.elapsedRealtime()
                startWallTime = System.currentTimeMillis()

                // ✅ 立即写入一条「进行中」session，避免：
                // 1) App crash / 进程被杀后丢失开始记录
                // 2) Timeline 无法显示“正在进行”的专注
                val sessionId = newId()
                currentSessionId = sessionId
                serviceScope.launch {
                    val db = DbProvider.get(applicationContext)
                    db.focusSessionDao().insert(
                        FocusSessionEntity(
                            id = sessionId,
                            planItemId = newItemId,
                            startAt = startWallTime,
                            endAt = startWallTime, // 进行中：先写同值
                            durationMillis = 0L,
                            source = "service_running"
                        )
                    )
                }

                val notification = buildNotification("专注中")
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }

            ACTION_STOP -> {
                stopAndSaveInternal()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun stopAndSaveInternal() {
        val itemId = currentItemId ?: return
        val sessionId = currentSessionId

        val endElapsed = SystemClock.elapsedRealtime()
        val duration = endElapsed - startElapsed
        val endWall = System.currentTimeMillis()

        currentItemId = null
        currentSessionId = null

        serviceScope.launch {
            val db = DbProvider.get(applicationContext)

            // ✅ 更新当前 session 的结束时间（如果找不到就兜底插入一条）
            val dao = db.focusSessionDao()
            if (sessionId != null) {
                dao.updateEndAndDuration(
                    id = sessionId,
                    endAt = endWall,
                    durationMillis = duration,
                    source = "service_stop"
                )
            } else {
                dao.insert(
                    FocusSessionEntity(
                        id = newId(),
                        planItemId = itemId,
                        startAt = startWallTime,
                        endAt = endWall,
                        durationMillis = duration,
                        source = "service_stop_fallback"
                    )
                )
            }

            // ✅ 自动标记完成
            db.planItemDao().setDone(itemId, true)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---------- Notification ----------

    private fun buildNotification(text: String): Notification {
        val stopIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_STOP
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("飞鱼计划 · 专注中")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(
                NotificationCompat.Action(
                    0,
                    "停止",
                    stopPendingIntent
                )
            )
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "专注计时",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
