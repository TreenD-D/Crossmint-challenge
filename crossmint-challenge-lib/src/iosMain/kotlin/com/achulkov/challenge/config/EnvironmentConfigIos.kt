package com.achulkov.challenge.config

import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

/**
 * iOS implementation of environment variable access.
 *
 * On iOS, environment variables are accessed through [NSProcessInfo].
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    return try {
        val environment = NSProcessInfo.processInfo.environment
        val value = environment[name]
        value?.toString()
    } catch (e: Exception) {
        null
    }
}

/**
 * iOS implementation of system property access.
 *
 * iOS doesn't have a traditional system property concept like JVM.
 * We use NSUserDefaults or fallback to empty.
 */
internal actual fun getSystemProperty(name: String): String? {
    // On iOS, system properties aren't commonly used
    // Return null as there's no direct equivalent
    return null
}
