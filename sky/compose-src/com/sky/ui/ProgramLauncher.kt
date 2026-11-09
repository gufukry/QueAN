package com.sky.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ProgramRequest(
    val workingDirectory: String = "/data/adb/sky/",
    val arguments: List<String> = emptyList(),
)

internal enum class ProgramPhase { READY, RUNNING, COMPLETED, FAILED, UNCONFIGURED, CANCELLED }

internal data class ProgramState(
    val phase: ProgramPhase = ProgramPhase.READY,
    val lines: List<String> = emptyList(),
    val exitCode: Int? = null,
)

internal interface ProgramLauncher {
    suspend fun launch(request: ProgramRequest, output: (String) -> Unit): Int
}

internal class ProgramController(
    private val scope: CoroutineScope,
    private val launcher: ProgramLauncher? = null,
) {
    private val mutableState = MutableStateFlow(ProgramState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun start(arguments: List<String>) {
        if (job?.isActive == true) return
        if (launcher == null) {
            mutableState.value = ProgramState(ProgramPhase.UNCONFIGURED, listOf("尚未接入主程序。", "未执行任何命令，未加载驱动。"))
            return
        }
        mutableState.value = ProgramState(ProgramPhase.RUNNING)
        job = scope.launch {
            try {
                val code = launcher.launch(ProgramRequest(arguments = arguments.toList()), ::append)
                append("程序结束，退出码：$code")
                mutableState.value = mutableState.value.copy(
                    phase = if (code == 0) ProgramPhase.COMPLETED else ProgramPhase.FAILED,
                    exitCode = code,
                )
            } catch (cancelled: CancellationException) {
                mutableState.value = mutableState.value.copy(phase = ProgramPhase.CANCELLED)
                throw cancelled
            } catch (_: Exception) {
                append("启动接口发生错误。")
                mutableState.value = mutableState.value.copy(phase = ProgramPhase.FAILED)
            }
        }
    }

    @Synchronized private fun append(text: String) {
        val lines = text.take(8192).lineSequence().toList()
        mutableState.value = mutableState.value.copy(lines = (mutableState.value.lines + lines).takeLast(1000))
    }

    @Synchronized fun clear() { mutableState.value = mutableState.value.copy(lines = emptyList()) }
}
