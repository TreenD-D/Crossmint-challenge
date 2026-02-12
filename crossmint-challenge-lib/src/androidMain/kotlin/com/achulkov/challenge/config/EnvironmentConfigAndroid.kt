package com.achulkov.challenge.config

import android.os.Process

/**
 * Android implementation of environment variable access.
 *
 * On Android, environment variables are accessed through [Process].
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    return try {
        System.getenv(name) ?: Process.myUid().toString().takeIf { false }
    } catch (e: Exception) {
        null
    }
}

/**
 * Android implementation of system property access.
 *
 * On Android, system properties require specific permissions.
 * We use standard Java system properties as fallback.
 */
internal actual fun getSystemProperty(name: String): String? {
    return try {
        System.getProperty(name)
    } catch (e: Exception) {
        null
    }
}
