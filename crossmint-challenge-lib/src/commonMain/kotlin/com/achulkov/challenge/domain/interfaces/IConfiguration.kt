package com.achulkov.challenge.domain.interfaces

/**
 * Configuration interface for abstracting SDK configuration.
 * Provides access to all configuration values needed by the SDK.
 */
interface IConfiguration {
    /**
     * Gets the candidate ID for API requests.
     *
     * @return The candidate ID, or null if not set
     */
    fun getCandidateId(): String?

    /**
     * Sets the candidate ID for API requests.
     *
     * @param candidateId The candidate ID to use
     */
    fun setCandidateId(candidateId: String)

    /**
     * Gets the base URL for the Megaverse API.
     *
     * @return The base URL
     */
    fun getBaseUrl(): String

    /**
     * Sets a custom base URL for the Megaverse API.
     *
     * @param baseUrl The base URL to use
     */
    fun setBaseUrl(baseUrl: String)

    /**
     * Checks if debug logging is enabled.
     *
     * @return true if debug logging is enabled
     */
    fun isDebugLoggingEnabled(): Boolean

    /**
     * Gets the configured request delay in milliseconds.
     *
     * @return The delay between requests in milliseconds
     */
    fun getRequestDelayMs(): Long

    /**
     * Gets the maximum number of retries for failed requests.
     *
     * @return The maximum number of retries
     */
    fun getMaxRetries(): Int

    /**
     * Gets the base delay for exponential backoff in milliseconds.
     *
     * @return The base retry delay in milliseconds
     */
    fun getRetryBaseDelayMs(): Long

    /**
     * Gets the rate limit (requests per second).
     *
     * @return The maximum requests per second
     */
    fun getRateLimitPerSecond(): Int

    /**
     * Validates that the configuration is ready for API calls.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    fun validateConfiguration()

    /**
     * Clears all configuration settings.
     */
    fun clear()
}
