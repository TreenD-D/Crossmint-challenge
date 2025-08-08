package com.achulkov.challenge.utils

/**
 * Logging utility for the Megaverse SDK.
 * Logs are only shown in DEBUG builds to avoid noise in production.
 */
object MegaverseLogger {

    private var isDebugEnabled = false

    /**
     * Enable or disable debug logging.
     * This should be set to true only in debug builds.
     *
     * @param enabled Whether to enable debug logging
     */
    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }

    /**
     * Log debug information.
     *
     * @param tag The tag for the log message
     * @param message The message to log
     */
    fun debug(tag: String, message: String) {
        if (isDebugEnabled) {
            println("🔍 [DEBUG] $tag: $message")
        }
    }

    /**
     * Log info information.
     *
     * @param tag The tag for the log message
     * @param message The message to log
     */
    fun info(tag: String, message: String) {
        if (isDebugEnabled) {
            println("ℹ️ [INFO] $tag: $message")
        }
    }

    /**
     * Log warning information.
     *
     * @param tag The tag for the log message
     * @param message The message to log
     */
    fun warn(tag: String, message: String) {
        if (isDebugEnabled) {
            println("⚠️ [WARN] $tag: $message")
        }
    }

    /**
     * Log error information.
     *
     * @param tag The tag for the log message
     * @param message The message to log
     * @param throwable Optional throwable for stack trace
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebugEnabled) {
            println("❌ [ERROR] $tag: $message")
            throwable?.let {
                println("Stack trace: ${it.stackTraceToString()}")
            }
        }
    }

    /**
     * Log network request details.
     *
     * @param method HTTP method (GET, POST, DELETE, etc.)
     * @param url Request URL
     * @param body Request body (optional)
     * @param headers Request headers (optional)
     */
    fun logRequest(
        method: String,
        url: String,
        body: String? = null,
        headers: Map<String, List<String>>? = null
    ) {
        if (isDebugEnabled) {
            println("🌐 [NETWORK] → $method $url")
            headers?.let { headerMap ->
                if (headerMap.isNotEmpty()) {
                    println("📋 Request Headers:")
                    headerMap.forEach { (key, values) ->
                        println("   $key: ${values.joinToString(", ")}")
                    }
                }
            }
            body?.let {
                println("📤 Request Body:")
                println("   ${formatJson(it)}")
            }
        }
    }

    /**
     * Log network response details.
     *
     * @param statusCode HTTP status code
     * @param url Request URL
     * @param responseBody Response body (optional)
     * @param duration Request duration in milliseconds
     * @param headers Response headers (optional)
     */
    fun logResponse(
        statusCode: Int,
        url: String,
        responseBody: String? = null,
        duration: Long? = null,
        headers: Map<String, List<String>>? = null
    ) {
        if (isDebugEnabled) {
            val statusEmoji = when (statusCode) {
                in 200..299 -> "✅"
                in 300..399 -> "↗️"
                in 400..499 -> "❌"
                in 500..599 -> "💥"
                else -> "❓"
            }
            val durationText = duration?.let { " (${it}ms)" } ?: ""
            println("🌐 [NETWORK] ← $statusEmoji $statusCode $url$durationText")

            headers?.let { headerMap ->
                if (headerMap.isNotEmpty()) {
                    println("📋 Response Headers:")
                    headerMap.forEach { (key, values) ->
                        println("   $key: ${values.joinToString(", ")}")
                    }
                }
            }
            responseBody?.let {
                println("📥 Response Body:")
                println("   ${formatJson(it)}")
            }
        }
    }

    /**
     * Log SDK operations and progress.
     *
     * @param operation The operation being performed
     * @param details Additional details about the operation
     */
    fun logOperation(operation: String, details: String) {
        if (isDebugEnabled) {
            println("⚙️ [OPERATION] $operation: $details")
        }
    }

    /**
     * Log configuration changes.
     *
     * @param key The configuration key that changed
     * @param value The new value (will be masked if sensitive)
     */
    fun logConfig(key: String, value: String) {
        if (isDebugEnabled) {
            val maskedValue = if (key.contains("id", ignoreCase = true) ||
                key.contains("key", ignoreCase = true) ||
                key.contains("token", ignoreCase = true)
            ) {
                value.take(4) + "****"
            } else {
                value
            }
            println("🔧 [CONFIG] $key = $maskedValue")
        }
    }

    /**
     * Attempts to format JSON string for better readability.
     * Falls back to original string if formatting fails.
     */
    private fun formatJson(jsonString: String): String {
        return try {
            // Simple JSON formatting for better readability
            if (jsonString.trim().startsWith("{") || jsonString.trim().startsWith("[")) {
                // Basic indentation for JSON
                jsonString.replace(",", ",\n  ")
                    .replace("{", "{\n  ")
                    .replace("}", "\n}")
                    .replace("[", "[\n  ")
                    .replace("]", "\n]")
            } else {
                jsonString
            }
        } catch (e: Exception) {
            jsonString
        }
    }
}