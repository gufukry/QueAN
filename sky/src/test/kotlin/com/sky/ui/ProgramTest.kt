package com.sky.ui

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test

class ProgramTest {
    @Test fun unconfiguredDoesNotClaimExecution() = runBlocking {
        val controller = ProgramController(this)
        controller.start(listOf("argument"))
        assertEquals(ProgramPhase.UNCONFIGURED, controller.state.value.phase)
        assertNull(controller.state.value.exitCode)
        controller.clear()
        assertTrue(controller.state.value.lines.isEmpty())
    }
    @Test fun interfaceReceivesIndependentArguments() = runBlocking {
        var received: ProgramRequest? = null
        val controller = ProgramController(this, object : ProgramLauncher {
            override suspend fun launch(request: ProgramRequest, output: (String) -> Unit): Int {
                received = request
                output("test output")
                return 7
            }
        })
        controller.start(listOf("a b", "x;y"))
        yield()
        assertEquals(listOf("a b", "x;y"), received?.arguments)
        assertEquals("/data/adb/sky/", received?.workingDirectory)
        assertEquals(ProgramPhase.FAILED, controller.state.value.phase)
        assertEquals(7, controller.state.value.exitCode)
    }
}
