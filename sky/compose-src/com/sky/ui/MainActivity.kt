package com.sky.ui

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
            try {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use {
                    if (it.moveToFirst()) {
                        val name = it.getString(0) ?: ""
                        val size = if (it.isNull(1)) -1L else it.getLong(1)
                        val error = when {
                            !name.endsWith(".zip", true) && !name.endsWith(".sh", true) -> "请选择 ZIP 或 .sh 文件"
                            size < 0 -> "无法确认文件大小，请选择本地文件"
                            size > 1024L * 1024 * 1024 -> "文件超过 1024 MiB"
                            else -> null
                        }
                        if (error == null) state.driverName = name
                        else android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            } catch (_: RuntimeException) { android.widget.Toast.makeText(this, "无法读取所选文件", android.widget.Toast.LENGTH_LONG).show() }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_Material_Light_NoActionBar)
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        state = ViewModelProvider(this)[SkyState::class.java]
        val version = packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        setContent { SkyApp(state, version, onChooseDriver = { driverPicker.launch(arrayOf("*/*")) }) }
    }
}
