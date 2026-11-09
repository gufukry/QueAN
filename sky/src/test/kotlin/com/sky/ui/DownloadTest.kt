package com.sky.ui

import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DownloadTest {
    @Test fun emptyRepositoryAndInvalidEntry() = runBlocking {
        val directory = Files.createTempDirectory("sky-download-test").toFile()
        val repository = DriverDownloadRepository(directory)
        try {
            repository.awaitReady()
            assertTrue(repository.downloads.value.isEmpty())
            try {
                repository.download(DriverEntry("../unsafe", "unsafe", "https://example.invalid/a.zip", 1))
                fail("unsafe ID accepted")
            } catch (_: IllegalArgumentException) { }
            assertTrue(repository.downloads.value.isEmpty())
        } finally { repository.close(); directory.deleteRecursively() }
    }
}
