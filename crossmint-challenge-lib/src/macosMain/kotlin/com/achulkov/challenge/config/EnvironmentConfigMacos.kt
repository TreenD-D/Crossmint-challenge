package com.achulkov.challenge.config

import platform.Foundation.NSProcessInfo

/**
 * macOS implementation of environment variable access.
 *
 * On macOS, environment variables are accessed through [NSProcessInfo].
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    return try {
        val environment = NSProcessInfo.processInfo.environment
        environment[name]?.toString()
    } catch (e: Exception) {
        null
    }
}

/**
 * macOS implementation of system property access.
 *
 * macOS doesn't have a traditional system property concept like JVM.
 * We return null as there's no direct equivalent.
 */
internal actual fun getSystemProperty(name: String): String? {
    // On macOS, system properties aren't commonly used outside JVM
    // Return null as there's no direct equivalent
    return null
}
