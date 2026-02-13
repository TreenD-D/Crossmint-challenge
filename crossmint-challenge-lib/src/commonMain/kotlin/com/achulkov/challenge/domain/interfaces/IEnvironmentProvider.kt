package com.achulkov.challenge.domain.interfaces

/**
 * Environment provider interface for abstracting environment variable access.
 * This allows for different implementations per platform and easier testing.
 */
interface IEnvironmentProvider {
    /**
     * Gets a value from environment variables or system properties.
     *
     * @param envVar The environment variable name
     * @param property The system property name
     * @return The value, or null if not found
     */
    fun getEnvOrProperty(envVar: String, property: String): String?

    /**
     * Gets an environment variable value.
     *
     * @param name The environment variable name
     * @return The value, or null if not set
     */
    fun getEnvironmentVariable(name: String): String?

    /**
     * Gets a system property value.
     *
     * @param name The system property name
     * @return The value, or null if not set
     */
    fun getSystemProperty(name: String): String?
}
