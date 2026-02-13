package com.achulkov.challenge.domain.interfaces

/**
 * Logger interface for abstracting logging implementation.
 * This allows for easier testing and different logging implementations per platform.
 */
interface ILogger {
    /**
     * Log an informational message.
     *
     * @param tag The tag to identify the source of the log message
     * @param message The message to log
     */
    fun info(tag: String, message: String)

    /**
     * Log a debug message.
     *
     * @param tag The tag to identify the source of the log message
     * @param message The message to log
     */
    fun debug(tag: String, message: String)

    /**
     * Log a warning message.
     *
     * @param tag The tag to identify the source of the log message
     * @param message The message to log
     */
    fun warn(tag: String, message: String)

    /**
     * Log an error message.
     *
     * @param tag The tag to identify the source of the log message
     * @param message The message to log
     * @param throwable Optional throwable to include with the error
     */
    fun error(tag: String, message: String, throwable: Throwable? = null)

    /**
     * Log an operation.
     *
     * @param operation The name of the operation
     * @param details Details about the operation
     */
    fun logOperation(operation: String, details: String)

    /**
     * Log a configuration change.
     *
     * @param key The configuration key
     * @param value The configuration value
     */
    fun logConfig(key: String, value: String)

    /**
     * Set whether debug logging is enabled.
     *
     * @param enabled true to enable debug logging, false otherwise
     */
    fun setDebugEnabled(enabled: Boolean)
}
