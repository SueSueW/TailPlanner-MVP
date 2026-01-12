package com.feiyunative.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.feiyunative.core.backup.DatabaseBackup

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var showRestartHint by remember { mutableStateOf(false) }

    // 📤 导出
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            DatabaseBackup.exportDatabase(context, uri)
        }
    }

    // 📥 导入
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            DatabaseBackup.importDatabase(context, uri)
            showRestartHint = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                exportLauncher.launch("FeiyuNative_backup.db")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📤 导出数据")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                importLauncher.launch(arrayOf("*/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📥 导入数据")
        }

        if (showRestartHint) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "数据已导入，请手动重启 App 以生效。",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
