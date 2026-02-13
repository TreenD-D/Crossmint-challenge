package com.achulkov.challenge.domain.state

import com.achulkov.challenge.domain.MegaverseMap
import com.achulkov.challenge.domain.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State manager for Megaverse operations using Kotlin StateFlow.
 * Provides reactive state updates for UI components and observers.
 */
class MegaverseStateManager {
    private val _state = MutableStateFlow(MegaverseEnvironmentState())
    
    /**
     * Observable state of the Megaverse environment.
     */
    val state: StateFlow<MegaverseEnvironmentState> = _state.asStateFlow()

    /**
     * Updates the goal map in the state.
     *
     * @param goalMap The goal map to set
     */
    fun setGoalMap(goalMap: MegaverseMap) {
        _state.update { it.copy(goalMap = goalMap) }
    }

    /**
     * Adds a position to the set of created positions.
     *
     * @param position The position where an object was created
     */
    fun addCreatedPosition(position: Position) {
        _state.update {
            it.copy(createdPositions = it.createdPositions + position)
        }
    }

    /**
     * Removes a position from the set of created positions.
     *
     * @param position The position that was cleared
     */
    fun removeCreatedPosition(position: Position) {
        _state.update {
            it.copy(createdPositions = it.createdPositions - position)
        }
    }

    /**
     * Updates the operation state.
     *
     * @param operationState The new operation state
     */
    fun setOperationState(operationState: MegaverseState) {
        _state.update { it.copy(operationState = operationState) }
    }

    /**
     * Clears all state back to initial values.
     */
    fun clear() {
        _state.value = MegaverseEnvironmentState()
    }

    /**
     * Gets the current state value (non-reactive).
     *
     * @return Current state snapshot
     */
    fun currentState(): MegaverseEnvironmentState = _state.value
}
