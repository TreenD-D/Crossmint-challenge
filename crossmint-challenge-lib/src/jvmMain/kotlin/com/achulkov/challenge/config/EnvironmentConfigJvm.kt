package com.achulkov.challenge.config

/**
 * JVM implementation of environment variable access.
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    return System.getenv(name)
}

/**
 * JVM implementation of system property access.
 */
internal actual fun getSystemProperty(name: String): String? {
    return System.getProperty(name)
}
