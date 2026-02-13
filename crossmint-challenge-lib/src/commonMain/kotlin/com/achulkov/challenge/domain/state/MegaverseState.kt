package com.achulkov.challenge.domain.state

import com.achulkov.challenge.domain.MegaverseMap
import com.achulkov.challenge.domain.Position

/**
 * Sealed class representing the various states of Megaverse operations.
 */
sealed class MegaverseState {
    /**
     * Initial state before any operations.
     */
    data object Idle : MegaverseState()

    /**
     * Loading state during operations.
     *
     * @param message Optional message describing what's being loaded
     */
    data class Loading(val message: String? = null) : MegaverseState()

    /**
     * Success state after a successful operation.
     *
     * @param message Optional success message
     * @param data Optional data payload
     */
    data class Success(
        val message: String? = null,
        val data: Any? = null
    ) : MegaverseState()

    /**
     * Error state when an operation fails.
     *
     * @param message Error message
     * @param exception Optional exception that caused the error
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : MegaverseState()

    /**
     * Progress state for bulk operations.
     *
     * @param completed Number of completed operations
     * @param total Total number of operations
     * @param currentItem Optional description of current item being processed
     */
    data class Progress(
        val completed: Int,
        val total: Int,
        val currentItem: String? = null
    ) : MegaverseState()
}

/**
 * Represents the overall state of the Megaverse.
 *
 * @property goalMap The goal map if loaded
 * @property createdPositions Set of positions where objects have been created
 * @property operationState Current state of operations
 */
data class MegaverseEnvironmentState(
    val goalMap: MegaverseMap? = null,
    val createdPositions: Set<Position> = emptySet(),
    val operationState: MegaverseState = MegaverseState.Idle
)
