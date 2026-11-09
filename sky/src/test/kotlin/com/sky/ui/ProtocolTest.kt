package com.sky.ui

import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.toArgb

class ProtocolTest {
    @Test fun rc4RoundTripAndVector() {
        assertEquals("bbf316e8d940af0ad3", Rc4Codec.encryptHex("Plaintext", "Key"))
        assertEquals("中文内容", Rc4Codec.decryptHex(Rc4Codec.encryptHex("中文内容", "test-key"), "test-key"))
    }
    @Test fun compareVersions() {
        assertTrue(newerVersion("2.2.1", "2.2.0"))
        assertFalse(newerVersion("2.1", "2.2.0"))
        assertFalse(newerVersion("v2.2.0", "2.2.0"))
        assertFalse(newerVersion("invalid", "2.0"))
    }
    @Test fun configAndFilters() {
        val config = parseRemoteConfig("""{"maintenance":{"enabled":true,"message":"维护"},"features":{"downloads":true},"drivers":[{"id":"test","name":"测试","url":"https://example.invalid/test.zip","size":123,"abis":["arm64-v8a"],"minApi":33,"kernelPrefix":"6.12","cardTypes":["code"]}]}""")
        assertTrue(config.maintenance.enabled)
        assertTrue(config.featureEnabled("downloads"))
        assertFalse(config.featureEnabled("missing"))
        assertEquals(1, config.compatibleDrivers(DriverDevice(setOf("arm64-v8a"), 37, "6.12.69"), "code").size)
        assertTrue(config.compatibleDrivers(DriverDevice(setOf("x86"), 37, "6.12.69"), "code").isEmpty())
        assertTrue(config.compatibleDrivers(DriverDevice(setOf("arm64-v8a"), 37, "6.12.69"), null).isEmpty())
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInsecureDriver() {
        validateDriverEntry(DriverEntry("test", "test", "http://example.invalid/a.zip", 1))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidId() {
        validateDriverEntry(DriverEntry("../test", "test", "https://example.invalid/a.zip", 1))
    }
    @Test fun trimsKernel() { assertEquals("6.12.69-android16-6", shortKernel("6.12.69-android16-6-g123-test")) }
    @Test fun brandColors() {
        assertEquals(0xFF3B82F6L, skyColors(false).primary.toArgb().toLong() and 0xffffffffL)
    }
}
