package com.feiyunative

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.feiyunative.ui.navigation.AppScaffold
import com.feiyunative.ui.theme.FeiyuNativeTheme

class MainActivity : ComponentActivity() {

    /**
     * Android 13+ 需要运行时授权 POST_NOTIFICATIONS。
     * 没有该权限时，启动前台服务时无法展示通知，Android 14 上会直接抛
     * ForegroundServiceStartNotAllowedException（看起来像“前台服务启动不合法”）。
     */
    private val requestPostNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            FeiyuNativeTheme {
                AppScaffold()
            }
        }
    }
}
