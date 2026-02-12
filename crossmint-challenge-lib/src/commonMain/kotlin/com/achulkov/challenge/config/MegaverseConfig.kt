package com.achulkov.challenge.config

import com.achulkov.challenge.utils.MegaverseLogger
import com.russhwolf.settings.Settings

/**
 * Configuration manager for the Megaverse SDK.
 *
 * This class manages SDK configuration with support for both persistent storage
 * and environment variables. It implements a hierarchical configuration lookup:
 *
 * 1. Explicitly set values (highest priority)
 * 2. Environment variables
 * 3. Persistent settings storage (lowest priority)
 *
 * @param settings Optional custom settings instance for persistent storage
 * @param envConfig Optional environment configuration provider
 */
open class MegaverseConfig(
    private val settings: Settings = Settings(),
    private val envConfig: EnvironmentConfig = EnvironmentConfig
) {

    companion object {
        private const val KEY_CANDIDATE_ID = "megaverse_candidate_id"
        private const val KEY_BASE_URL = "megaverse_base_url"
        private const val DEFAULT_BASE_URL = "https://challenge.crossmint.io/api"

        /**
         * Creates a MegaverseConfig from environment variables.
         *
         * This factory method initializes the configuration with values
         * from environment variables, providing a secure way to manage
         * credentials without hardcoding them.
         *
         * @return A MegaverseConfig populated from environment variables
         * @throws IllegalStateException if the candidate ID is not set in environment
         *
         * Example:
         * ```kotlin
         * // Set environment variable:
         * // export MEGAVERSE_CANDIDATE_ID="your-candidate-id"
         *
         * // Use in code:
         * val config = MegaverseConfig.fromEnvironment()
         * val sdk = MegaverseSdk(config)
         * ```
         */
        fun fromEnvironment(): MegaverseConfig {
            val envConfiguration = EnvironmentConfiguration.fromEnvironment()

            val settings = Settings()

            // Populate settings from environment
            envConfiguration.candidateId?.let { id ->
                settings.putString(KEY_CANDIDATE_ID, id)
                MegaverseLogger.info(
                    "MegaverseConfig",
                    "Candidate ID loaded from environment"
                )
            }

            settings.putString(KEY_BASE_URL, envConfiguration.baseUrl)

            MegaverseLogger.info(
                "MegaverseConfig",
                "Configuration initialized from environment variables"
            )

            return MegaverseConfig(settings)
        }
    }

    init {
        MegaverseLogger.info("MegaverseConfig", "Configuration manager initialized")

        // Check for environment-based configuration
        val envCandidateId = envConfig.getCandidateId()
        val storedCandidateId = settings.getStringOrNull(KEY_CANDIDATE_ID)

        when {
            envCandidateId != null && envCandidateId != storedCandidateId -> {
                MegaverseLogger.info(
                    "MegaverseConfig",
                    "Found candidate ID in environment, will use it"
                )
                settings.putString(KEY_CANDIDATE_ID, envCandidateId)
            }

            storedCandidateId != null -> {
                MegaverseLogger.info("MegaverseConfig", "Found existing candidate ID in storage")
            }

            else -> {
                MegaverseLogger.warn(
                    "MegaverseConfig",
                    "No candidate ID configured. Set via environment (MEGAVERSE_CANDIDATE_ID) or setCandidateId()"
                )
            }
        }
    }

    /**
     * Gets the currently configured candidate ID.
     *
     * The value is retrieved in the following order:
     * 1. Environment variable MEGAVERSE_CANDIDATE_ID
     * 2. Persistent settings storage
     *
     * @return The candidate ID, or null if not set
     */
    fun getCandidateId(): String? {
        // Check environment first
        val envCandidateId = envConfig.getCandidateId()
        if (envCandidateId != null) {
            MegaverseLogger.debug(
                "MegaverseConfig",
                "Using candidate ID from environment"
            )
            return envCandidateId
        }

        // Fall back to settings storage
        val candidateId = settings.getStringOrNull(KEY_CANDIDATE_ID)
        MegaverseLogger.debug(
            "MegaverseConfig",
            "Retrieved candidate ID: ${candidateId?.let { "****" } ?: "null"}"
        )
        return candidateId
    }

    /**
     * Sets the candidate ID for API requests.
     *
     * Note: Environment variables take precedence over explicitly set values.
     * To use a specific candidate ID, ensure the MEGAVERSE_CANDIDATE_ID
     * environment variable is not set.
     *
     * @param candidateId The candidate ID to use
     * @throws IllegalArgumentException if candidateId is blank
     */
    fun setCandidateId(candidateId: String) {
        require(candidateId.isNotBlank()) { "Candidate ID cannot be blank" }

        // Check if environment variable is set (it takes precedence)
        if (envConfig.getCandidateId() != null) {
            MegaverseLogger.warn(
                "MegaverseConfig",
                "Environment variable MEGAVERSE_CANDIDATE_ID is set and takes precedence. " +
                    "Unset it to use setCandidateId() value."
            )
            return
        }

        val previousId = settings.getStringOrNull(KEY_CANDIDATE_ID)
        settings.putString(KEY_CANDIDATE_ID, candidateId)

        MegaverseLogger.logConfig("candidateId", candidateId)

        if (previousId != null && previousId != candidateId) {
            MegaverseLogger.info(
                "MegaverseConfig",
                "Candidate ID changed from existing configuration"
            )
        } else if (previousId == null) {
            MegaverseLogger.info("MegaverseConfig", "Candidate ID set for the first time")
        }
    }

    /**
     * Gets the base URL for the Megaverse API.
     *
     * The value is retrieved in the following order:
     * 1. Environment variable MEGAVERSE_BASE_URL
     * 2. Persistent settings storage
     * 3. Default URL (https://challenge.crossmint.io/api)
     *
     * @return The base URL
     */
    fun getBaseUrl(): String {
        // Check environment first
        val envBaseUrl = envConfig.getBaseUrl()
        if (envBaseUrl != DEFAULT_BASE_URL) {
            MegaverseLogger.debug(
                "MegaverseConfig",
                "Using custom base URL from environment"
            )
            return envBaseUrl
        }

        // Fall back to settings storage
        val baseUrl = settings.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
        MegaverseLogger.debug("MegaverseConfig", "Retrieved base URL: $baseUrl")
        return baseUrl
    }

    /**
     * Sets a custom base URL for the Megaverse API.
     *
     * Note: Environment variables take precedence over explicitly set values.
     *
     * @param baseUrl The base URL to use
     * @throws IllegalArgumentException if baseUrl is blank
     */
    fun setBaseUrl(baseUrl: String) {
        require(baseUrl.isNotBlank()) { "Base URL cannot be blank" }

        // Check if environment variable is set (it takes precedence)
        val envBaseUrl = envConfig.getBaseUrl()
        if (envBaseUrl != DEFAULT_BASE_URL) {
            MegaverseLogger.warn(
                "MegaverseConfig",
                "Environment variable MEGAVERSE_BASE_URL is set and takes precedence. " +
                    "Unset it to use setBaseUrl() value."
            )
            return
        }

        val previousUrl = settings.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
        settings.putString(KEY_BASE_URL, baseUrl)

        MegaverseLogger.logConfig("baseUrl", baseUrl)

        if (previousUrl != baseUrl) {
            MegaverseLogger.info(
                "MegaverseConfig",
                "Base URL changed from $previousUrl to $baseUrl"
            )
        }
    }

    /**
     * Checks if debug logging is enabled.
     *
     * @return true if debug logging is enabled
     */
    fun isDebugLoggingEnabled(): Boolean {
        return envConfig.isDebugLoggingEnabled()
    }

    /**
     * Gets the configured request delay in milliseconds.
     *
     * @return The delay between requests in milliseconds
     */
    fun getRequestDelayMs(): Long {
        return envConfig.getRequestDelayMs()
    }

    /**
     * Gets the maximum number of retries for failed requests.
     *
     * @return The maximum number of retries
     */
    open fun getMaxRetries(): Int {
        return envConfig.getMaxRetries()
    }

    /**
     * Gets the base delay for exponential backoff in milliseconds.
     *
     * @return The base retry delay in milliseconds
     */
    open fun getRetryBaseDelayMs(): Long {
        return envConfig.getRetryBaseDelayMs()
    }

    /**
     * Gets the rate limit (requests per second).
     *
     * @return The maximum requests per second
     */
    open fun getRateLimitPerSecond(): Int {
        return envConfig.getRateLimitPerSecond()
    }

    /**
     * Clears all configuration settings from persistent storage.
     *
     * Note: This does not affect environment variables.
     */
    fun clear() {
        MegaverseLogger.warn("MegaverseConfig", "Clearing all persistent configuration settings")
        settings.clear()
        MegaverseLogger.info("MegaverseConfig", "All persistent configuration settings cleared")
    }

    /**
     * Validates that the configuration is ready for API calls.
     *
     * @throws IllegalStateException if candidate ID is not set
     */
    fun validateConfiguration() {
        val candidateId = getCandidateId()

        if (candidateId == null) {
            MegaverseLogger.error(
                "MegaverseConfig",
                "Validation failed: Candidate ID is not set"
            )
            throw IllegalStateException(
                "Candidate ID must be set before using the API. " +
                    "Set the MEGAVERSE_CANDIDATE_ID environment variable " +
                    "or call setCandidateId()."
            )
        }

        if (candidateId.isBlank()) {
            MegaverseLogger.error(
                "MegaverseConfig",
                "Validation failed: Candidate ID is blank"
            )
            throw IllegalStateException("Candidate ID cannot be blank")
        }

        MegaverseLogger.debug("MegaverseConfig", "Configuration validation successful")
    }
}
