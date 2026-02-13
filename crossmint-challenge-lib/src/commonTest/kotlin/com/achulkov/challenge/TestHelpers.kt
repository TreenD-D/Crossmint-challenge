package com.achulkov.challenge

import com.achulkov.challenge.domain.interfaces.ILogger

/**
 * Mock logger for testing. Captures all log messages for verification.
 */
class MockLogger : ILogger {
    val operations = mutableListOf<String>()
    val infos = mutableListOf<String>()
    val debugs = mutableListOf<String>()
    val warns = mutableListOf<String>()
    val errors = mutableListOf<String>()
    val configs = mutableListOf<Pair<String, String>>()
    var isDebugEnabled = false
        private set

    override fun logOperation(operation: String, details: String) {
        operations.add("[$operation] $details")
    }

    override fun info(tag: String, message: String) {
        infos.add("[$tag] $message")
    }

    override fun debug(tag: String, message: String) {
        debugs.add("[$tag] $message")
    }

    override fun warn(tag: String, message: String) {
        warns.add("[$tag] $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        errors.add("[$tag] $message: ${throwable?.message}")
    }

    override fun logConfig(key: String, value: String) {
        configs.add(key to value)
    }

    override fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }

    fun clear() {
        operations.clear()
        infos.clear()
        debugs.clear()
        warns.clear()
        errors.clear()
        configs.clear()
    }
}
