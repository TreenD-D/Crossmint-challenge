package com.achulkov.challenge.config

import com.achulkov.challenge.utils.MegaverseLogger

/**
 * Configuration provider that supports environment variables for secure credential management.
 *
 * This provider reads configuration from environment variables, falling back to
 * system properties if environment variables are not set. This approach ensures

 * that sensitive data like candidate IDs are never hardcoded in the codebase.
 *
 * Supported environment variables:
 * - `MEGAVERSE_CANDIDATE_ID`: The candidate ID for API authentication
 * - `MEGAVERSE_BASE_URL`: Optional custom base URL for the API
 * - `MEGAVERSE_DEBUG_LOGGING`: Enable debug logging ("true" or "false")
 * - `MEGAVERSE_REQUEST_DELAY_MS`: Delay between requests in milliseconds
 * - `MEGAVERSE_MAX_RETRIES`: Maximum number of retries for failed requests
 * - `MEGAVERSE_RETRY_BASE_DELAY_MS`: Base delay for exponential backoff in milliseconds
 *
 * Example usage:
 * ```kotlin
 * // From environment variables
 * val candidateId = EnvironmentConfig.getCandidateId()
 *
 * // Or use the SDK with environment-based configuration
 * val sdk = MegaverseSdk(
 *     config = MegaverseConfig.fromEnvironment()
 * )
 * ```
 */
object EnvironmentConfig {

    private const val ENV_CANDIDATE_ID = "MEGAVERSE_CANDIDATE_ID"
    private const val ENV_BASE_URL = "MEGAVERSE_BASE_URL"
    private const val ENV_DEBUG_LOGGING = "MEGAVERSE_DEBUG_LOGGING"
    private const val ENV_REQUEST_DELAY_MS = "MEGAVERSE_REQUEST_DELAY_MS"
    private const val ENV_MAX_RETRIES = "MEGAVERSE_MAX_RETRIES"
    private const val ENV_RETRY_BASE_DELAY_MS = "MEGAVERSE_RETRY_BASE_DELAY_MS"
    private const val ENV_RATE_LIMIT_PER_SECOND = "MEGAVERSE_RATE_LIMIT_PER_SECOND"

    private const val DEFAULT_BASE_URL = "https://challenge.crossmint.io/api"
    private const val DEFAULT_REQUEST_DELAY_MS = 500L
    private const val DEFAULT_MAX_RETRIES = 3
    private const val DEFAULT_RETRY_BASE_DELAY_MS = 1000L
    private const val DEFAULT_RATE_LIMIT_PER_SECOND = 2

    /**
     * Retrieves the candidate ID from environment variables.
     *
     * Checks the following sources in order:
     * 1. Environment variable `MEGAVERSE_CANDIDATE_ID`
     * 2. System property `megaverse.candidate.id`
     *
     * @return The candidate ID, or null if not configured
     */
    fun getCandidateId(): String? {
        val candidateId = getEnvOrProperty(ENV_CANDIDATE_ID, "megaverse.candidate.id")

        MegaverseLogger.debug(
            "EnvironmentConfig",
            "Candidate ID ${if (candidateId != null) "found" else "not found"} in environment"
        )

        return candidateId?.trim()?.takeIf { it.isNotBlank() }
    }

    /**
     * Retrieves the base URL from environment variables.
     *
     * Checks the following sources in order:
     * 1. Environment variable `MEGAVERSE_BASE_URL`
     * 2. System property `megaverse.base.url`
     *
     * @return The base URL, or the default if not configured
     */
    fun getBaseUrl(): String {
        val baseUrl = getEnvOrProperty(ENV_BASE_URL, "megaverse.base.url")
            ?: DEFAULT_BASE_URL

        MegaverseLogger.debug("EnvironmentConfig", "Using base URL: $baseUrl")
        return baseUrl
    }

    /**
     * Checks if debug logging should be enabled.
     *
     * Checks the following sources in order:
     * 1. Environment variable `MEGAVERSE_DEBUG_LOGGING`
     * 2. System property `megaverse.debug.logging`
     *
     * @return true if debug logging is enabled, false otherwise
     */
    fun isDebugLoggingEnabled(): Boolean {
        val value = getEnvOrProperty(ENV_DEBUG_LOGGING, "megaverse.debug.logging")
        val enabled = value?.lowercase() == "true"

        MegaverseLogger.debug("EnvironmentConfig", "Debug logging enabled: $enabled")
        return enabled
    }

    /**
     * Retrieves the delay between requests from environment variables.
     *
     * @return The delay in milliseconds, or the default if not configured or invalid
     */
    fun getRequestDelayMs(): Long {
        val value = getEnvOrProperty(ENV_REQUEST_DELAY_MS, "megaverse.request.delay.ms")
        return value?.toLongOrNull()?.coerceAtLeast(0) ?: DEFAULT_REQUEST_DELAY_MS
    }

    /**
     * Retrieves the maximum number of retries from environment variables.
     *
     * @return The maximum retries, or the default if not configured or invalid
     */
    fun getMaxRetries(): Int {
        val value = getEnvOrProperty(ENV_MAX_RETRIES, "megaverse.max.retries")
        return value?.toIntOrNull()?.coerceAtLeast(0) ?: DEFAULT_MAX_RETRIES
    }

    /**
     * Retrieves the base delay for exponential backoff from environment variables.
     *
     * @return The base delay in milliseconds, or the default if not configured or invalid
     */
    fun getRetryBaseDelayMs(): Long {
        val value = getEnvOrProperty(ENV_RETRY_BASE_DELAY_MS, "megaverse.retry.base.delay.ms")
        return value?.toLongOrNull()?.coerceAtLeast(0) ?: DEFAULT_RETRY_BASE_DELAY_MS
    }

    /**
     * Retrieves the rate limit per second from environment variables.
     *
     * @return The rate limit (requests per second), or the default if not configured or invalid
     */
    fun getRateLimitPerSecond(): Int {
        val value = getEnvOrProperty(ENV_RATE_LIMIT_PER_SECOND, "megaverse.rate.limit.per.second")
        return value?.toIntOrNull()?.coerceAtLeast(1) ?: DEFAULT_RATE_LIMIT_PER_SECOND
    }

    /**
     * Validates that all required configuration is present.
     *
     * @throws IllegalStateException if the candidate ID is not configured
     */
    fun validateConfiguration() {
        val candidateId = getCandidateId()

        if (candidateId == null) {
            throw IllegalStateException(
                "Candidate ID is not configured. " +
                    "Please set the $ENV_CANDIDATE_ID environment variable " +
                    "or the megaverse.candidate.id system property."
            )
        }

        MegaverseLogger.info("EnvironmentConfig", "Configuration validated successfully")
    }

    /**
     * Gets an environment variable or system property value.
     *
     * @param envVar The environment variable name
     * @param property The system property name
     * @return The value, or null if not found in either source
     */
    private fun getEnvOrProperty(envVar: String, property: String): String? {
        // First check environment variable
        val envValue = getEnvironmentVariable(envVar)
        if (!envValue.isNullOrBlank()) {
            return envValue
        }

        // Fall back to system property
        return getSystemProperty(property)
    }

}

/**
 * Platform-specific function to get environment variables.
 * Implemented differently for each target platform.
 *
 * @param name The environment variable name
 * @return The value, or null if not set
 */
internal expect fun getEnvironmentVariable(name: String): String?

/**
 * Platform-specific function to get system properties.
 * Implemented differently for each target platform.
 *
 * @param name The system property name
 * @return The value, or null if not set
 */
internal expect fun getSystemProperty(name: String): String?

/**
 * Data class holding all environment-based configuration values.
 *
 * @property candidateId The candidate ID for API authentication
 * @property baseUrl The base URL for the API
 * @property debugLoggingEnabled Whether debug logging is enabled
 * @property requestDelayMs Delay between requests in milliseconds
 * @property maxRetries Maximum number of retries for failed requests
 * @property retryBaseDelayMs Base delay for exponential backoff
 * @property rateLimitPerSecond Rate limit for API requests
 */
data class EnvironmentConfiguration(
    val candidateId: String?,
    val baseUrl: String,
    val debugLoggingEnabled: Boolean,
    val requestDelayMs: Long,
    val maxRetries: Int,
    val retryBaseDelayMs: Long,
    val rateLimitPerSecond: Int
) {
    companion object {
        /**
         * Creates an EnvironmentConfiguration from environment variables.
         *
         * @return A populated EnvironmentConfiguration instance
         */
        fun fromEnvironment(): EnvironmentConfiguration {
            return EnvironmentConfiguration(
                candidateId = EnvironmentConfig.getCandidateId(),
                baseUrl = EnvironmentConfig.getBaseUrl(),
                debugLoggingEnabled = EnvironmentConfig.isDebugLoggingEnabled(),
                requestDelayMs = EnvironmentConfig.getRequestDelayMs(),
                maxRetries = EnvironmentConfig.getMaxRetries(),
                retryBaseDelayMs = EnvironmentConfig.getRetryBaseDelayMs(),
                rateLimitPerSecond = EnvironmentConfig.getRateLimitPerSecond()
            )
        }
    }
}
