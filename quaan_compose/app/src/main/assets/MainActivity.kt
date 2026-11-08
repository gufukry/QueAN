package com.local.terminal.launcher

import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private lateinit var state: SkyState
    private val driverPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            state.driverName = try {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                    if (it.moveToFirst()) it.getString(0) else "已选择文件"
                } ?: "已选择文件"
            } catch (_: RuntimeException) { "已选择文件" }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        state = ViewModelProvider(this)[SkyState::class.java]
        val version = packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        setContent { SkyApp(state, version, onChooseDriver = { driverPicker.launch(arrayOf("*/*")) }) }
    }
}
