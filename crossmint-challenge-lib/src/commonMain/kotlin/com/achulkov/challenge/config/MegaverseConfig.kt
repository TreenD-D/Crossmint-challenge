package com.achulkov.challenge.config

import com.achulkov.challenge.utils.MegaverseLogger
import com.russhwolf.settings.Settings

/**
 * Configuration manager for the Megaverse SDK.
 *
 * @param settings Optional custom settings instance, if null a default will be created
 */
class MegaverseConfig(
    private val settings: Settings = Settings()
) {

    companion object {
        private const val KEY_CANDIDATE_ID = "megaverse_candidate_id"
        private const val KEY_BASE_URL = "megaverse_base_url"
        private const val DEFAULT_BASE_URL = "https://challenge.crossmint.io/api"
    }

    init {
        MegaverseLogger.info("MegaverseConfig", "Configuration manager initialized")
        // Check if there's existing configuration
        val existingCandidateId = getCandidateId()
        if (existingCandidateId != null) {
            MegaverseLogger.info("MegaverseConfig", "Found existing candidate ID configuration")
        }
    }

    /**
     * Gets the currently configured candidate ID.
     *
     * @return The candidate ID, or null if not set
     */
    fun getCandidateId(): String? {
        val candidateId = settings.getStringOrNull(KEY_CANDIDATE_ID)
        MegaverseLogger.debug(
            "MegaverseConfig",
            "Retrieved candidate ID: ${candidateId?.let { "****" } ?: "null"}")
        return candidateId
    }

    /**
     * Sets the candidate ID for API requests.
     *
     * @param candidateId The candidate ID to use
     * @throws IllegalArgumentException if candidateId is blank
     */
    fun setCandidateId(candidateId: String) {
        require(candidateId.isNotBlank()) { "Candidate ID cannot be blank" }

        val previousId = getCandidateId()
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
     * @return The base URL
     */
    fun getBaseUrl(): String {
        val baseUrl = settings.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
        MegaverseLogger.debug("MegaverseConfig", "Retrieved base URL: $baseUrl")
        return baseUrl
    }

    /**
     * Sets a custom base URL for the Megaverse API.
     *
     * @param baseUrl The base URL to use
     * @throws IllegalArgumentException if baseUrl is blank
     */
    fun setBaseUrl(baseUrl: String) {
        require(baseUrl.isNotBlank()) { "Base URL cannot be blank" }

        val previousUrl = getBaseUrl()
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
     * Clears all configuration settings.
     */
    fun clear() {
        MegaverseLogger.warn("MegaverseConfig", "Clearing all configuration settings")
        settings.clear()
        MegaverseLogger.info("MegaverseConfig", "All configuration settings cleared")
    }

    /**
     * Validates that the configuration is ready for API calls.
     *
     * @throws IllegalStateException if candidate ID is not set
     */
    fun validateConfiguration() {
        val candidateId = getCandidateId()

        if (candidateId == null) {
            MegaverseLogger.error("MegaverseConfig", "Validation failed: Candidate ID is not set")
            throw IllegalStateException("Candidate ID must be set before using the API")
        }

        if (candidateId.isBlank()) {
            MegaverseLogger.error("MegaverseConfig", "Validation failed: Candidate ID is blank")
            throw IllegalStateException("Candidate ID cannot be blank")
        }

        MegaverseLogger.debug("MegaverseConfig", "Configuration validation successful")
    }
}