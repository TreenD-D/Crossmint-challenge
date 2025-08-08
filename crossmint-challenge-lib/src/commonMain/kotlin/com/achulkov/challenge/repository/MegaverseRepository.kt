package com.achulkov.challenge.repository

import com.achulkov.challenge.domain.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Megaverse operations.
 * Abstracts the data layer and provides a clean API for the SDK.
 */
interface MegaverseRepository {

    /**
     * Creates a polyanet at the specified position.
     *
     * @param polyanet The polyanet to create
     * @return Result indicating success or failure
     */
    suspend fun createPolyanet(polyanet: Polyanet): MegaverseResult<Unit>

    /**
     * Deletes a polyanet at the specified position.
     *
     * @param position The position to remove the polyanet from
     * @return Result indicating success or failure
     */
    suspend fun deletePolyanet(position: Position): MegaverseResult<Unit>

    /**
     * Creates a soloon at the specified position.
     *
     * @param soloon The soloon to create
     * @return Result indicating success or failure
     */
    suspend fun createSoloon(soloon: Soloon): MegaverseResult<Unit>

    /**
     * Deletes a soloon at the specified position.
     *
     * @param position The position to remove the soloon from
     * @return Result indicating success or failure
     */
    suspend fun deleteSoloon(position: Position): MegaverseResult<Unit>

    /**
     * Creates a cometh at the specified position.
     *
     * @param cometh The cometh to create
     * @return Result indicating success or failure
     */
    suspend fun createCometh(cometh: Cometh): MegaverseResult<Unit>

    /**
     * Deletes a cometh at the specified position.
     *
     * @param position The position to remove the cometh from
     * @return Result indicating success or failure
     */
    suspend fun deleteCometh(position: Position): MegaverseResult<Unit>

    /**
     * Retrieves the goal map for the current candidate.
     *
     * @return Result containing the megaverse map or an error
     */
    suspend fun getGoalMap(): MegaverseResult<MegaverseMap>

    /**
     * Creates multiple astral objects with progress tracking.
     *
     * @param objects The list of astral objects to create
     * @return Flow emitting progress updates and final results
     */
    fun createAstralObjects(objects: List<AstralObject>): Flow<CreationProgress>

    /**
     * Deletes multiple astral objects at the specified positions.
     *
     * @param positions The positions to clear
     * @return Flow emitting progress updates and final results
     */
    fun clearPositions(positions: List<Position>): Flow<DeletionProgress>
}

/**
 * Represents progress during bulk creation operations.
 */
sealed class CreationProgress {
    data class InProgress(val completed: Int, val total: Int) : CreationProgress()
    data class ObjectCreated(val astralObject: AstralObject) : CreationProgress()
    data class ObjectFailed(val astralObject: AstralObject, val error: MegaverseException) :
        CreationProgress()

    data class Completed(val successful: Int, val failed: Int) : CreationProgress()
}

/**
 * Represents progress during bulk deletion operations.
 */
sealed class DeletionProgress {
    data class InProgress(val completed: Int, val total: Int) : DeletionProgress()
    data class PositionCleared(val position: Position) : DeletionProgress()
    data class PositionFailed(val position: Position, val error: MegaverseException) :
        DeletionProgress()

    data class Completed(val successful: Int, val failed: Int) : DeletionProgress()
}