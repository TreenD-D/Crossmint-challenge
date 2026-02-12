package com.achulkov.challenge.repository

import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.domain.MegaverseException
import com.achulkov.challenge.domain.MegaverseResult
import com.achulkov.challenge.domain.Position
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class ResilientApiExecutorTest {

    private class MockSettings : Settings {
        private val map = mutableMapOf<String, Any>()
        override val keys: Set<String> get() = map.keys
        override val size: Int get() = map.size
        override fun clear() = map.clear()
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean = map[key] as? Boolean ?: defaultValue
        override fun getBooleanOrNull(key: String): Boolean? = map[key] as? Boolean
        override fun getDouble(key: String, defaultValue: Double): Double = map[key] as? Double ?: defaultValue
        override fun getDoubleOrNull(key: String): Double? = map[key] as? Double
        override fun getFloat(key: String, defaultValue: Float): Float = map[key] as? Float ?: defaultValue
        override fun getFloatOrNull(key: String): Float? = map[key] as? Float
        override fun getInt(key: String, defaultValue: Int): Int = map[key] as? Int ?: defaultValue
        override fun getIntOrNull(key: String): Int? = map[key] as? Int
        override fun getLong(key: String, defaultValue: Long): Long = map[key] as? Long ?: defaultValue
        override fun getLongOrNull(key: String): Long? = map[key] as? Long
        override fun getString(key: String, defaultValue: String): String = map[key] as? String ?: defaultValue
        override fun getStringOrNull(key: String): String? = map[key] as? String
        override fun hasKey(key: String): Boolean = map.containsKey(key)
        override fun putBoolean(key: String, value: Boolean) { map[key] = value }
        override fun putDouble(key: String, value: Double) { map[key] = value }
        override fun putFloat(key: String, value: Float) { map[key] = value }
        override fun putInt(key: String, value: Int) { map[key] = value }
        override fun putLong(key: String, value: Long) { map[key] = value }
        override fun putString(key: String, value: String) { map[key] = value }
        override fun remove(key: String) { map.remove(key) }
    }

    private class MockConfig(
        private val maxRetries: Int = 2,
        private val baseDelay: Long = 10L,
        private val rateLimit: Int = 100
    ) : MegaverseConfig(settings = MockSettings()) {
        override fun getMaxRetries(): Int = maxRetries
        override fun getRetryBaseDelayMs(): Long = baseDelay
        override fun getRateLimitPerSecond(): Int = rateLimit
    }

    @Test
    fun `executeWithResilience succeeds on first attempt`() = runTest {
        val config = MockConfig()
        val executor = ResilientApiExecutor(config)

        val result = executor.executeWithResilience("test") {
            MegaverseResult.Success("success")
        }

        assertTrue(result is MegaverseResult.Success)
        assertEquals("success", result.data)
    }

    @Test
    fun `executeWithResilience retries on failure and eventually succeeds`() = runTest {
        val config = MockConfig(maxRetries = 3)
        val executor = ResilientApiExecutor(config)
        var attempts = 0

        val result = executor.executeWithResilience("test") {
            attempts++
            if (attempts < 3) {
                MegaverseResult.Error(MegaverseException.NetworkError("fail"))
            } else {
                MegaverseResult.Success("success")
            }
        }

        assertTrue(result is MegaverseResult.Success)
        assertEquals("success", result.data)
        assertEquals(3, attempts)
    }

    @Test
    fun `executeWithResilience fails after max retries`() = runTest {
        val config = MockConfig(maxRetries = 2)
        val executor = ResilientApiExecutor(config)
        var attempts = 0

        val result = executor.executeWithResilience("test") {
            attempts++
            MegaverseResult.Error(MegaverseException.NetworkError("fail"))
        }

        assertTrue(result is MegaverseResult.Error)
        // 1 initial + 2 retries = 3 attempts
        assertEquals(3, attempts)
    }

    @Test
    fun `executeWithResilience does not retry on non-retryable error`() = runTest {
        val config = MockConfig(maxRetries = 3)
        val executor = ResilientApiExecutor(config)
        var attempts = 0
        val position = Position(0, 0)

        val result = executor.executeWithResilience("test") {
            attempts++
            MegaverseResult.Error(MegaverseException.InvalidPosition(position))
        }

        assertTrue(result is MegaverseResult.Error)
        assertTrue(result.exception is MegaverseException.InvalidPosition)
        assertEquals(1, attempts)
    }

    @Test
    fun `circuit breaker opens after threshold failures`() = runTest {
        val config = MockConfig(maxRetries = 0) // No retries to speed up failure count
        val executor = ResilientApiExecutor(config)
        
        // Threshold is 5 in ResilientApiExecutor.companion
        repeat(5) {
            val result = executor.executeWithResilience<Unit>("test") {
                MegaverseResult.Error(MegaverseException.NetworkError("fail"))
            }
            assertTrue(result is MegaverseResult.Error)
        }
        
        // 6th attempt should be rejected by circuit breaker immediately
        val result = executor.executeWithResilience<Unit>("test") {
            fail("Should not execute when circuit is open")
        }
        
        assertTrue(result is MegaverseResult.Error)
        assertEquals("Service temporarily unavailable. Circuit breaker is OPEN.", result.exception.message)
    }
}
