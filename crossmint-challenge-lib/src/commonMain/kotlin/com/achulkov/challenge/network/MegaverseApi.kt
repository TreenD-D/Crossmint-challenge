package com.achulkov.challenge.network

import com.achulkov.challenge.domain.*

/**
 * Interface defining the Megaverse API operations.
 */
interface MegaverseApi {

    /**
     * Creates a polyanet at the specified position.
     *
     * @param position The position to place the polyanet
     * @param candidateId The candidate identifier
     * @return Result indicating success or failure
     */
    suspend fun createPolyanet(position: Position, candidateId: String): MegaverseResult<Unit>

    /**
     * Deletes a polyanet at the specified position.
     *
     * @param position The position to remove the polyanet from
     * @param candidateId The candidate identifier
     * @return Result indicating success or failure
     */
    suspend fun deletePolyanet(position: Position, candidateId: String): MegaverseResult<Unit>

    /**
     * Creates a soloon at the specified position with the given color.
     *
     * @param position The position to place the soloon
     * @param color The color of the soloon
     * @param candidateId The candidate identifier
     * @return Result indicating success or failure
     */
    suspend fun createSoloon(
        position: Position,
        color: SoloonColor,
        candidateId: String
    ): MegaverseResult<Unit>

    /**
     * Deletes a soloon at the specified position.
     *
     * @param position The position to remove the soloon from
     * @param candidateId The candidate identifier
     * @return Result indicating success or failure
     */
    suspend fun deleteSoloon(position: Position, candidateId: String): MegaverseResult<Unit>

    /**
     * Creates a cometh at the specified position with the given direction.
     *
     * @param position The position to place the cometh
     * @param direction The direction of the cometh
     * @param candidateId The candidate identifier
     * @return Result indicating success or failure
     */
    suspend fun createCometh(
        position: Position,
        direction: ComethDirection,
        candidateId: String
    ): MegaverseResult<Unit>

    /**
     * Deletes a cometh at the specified position.
     *
     * @param position The position to remove the cometh from
     * @param candidateId The candidate identifier
     * @return Result indicating success or failure
     */
    suspend fun deleteCometh(position: Position, candidateId: String): MegaverseResult<Unit>

    /**
     * Retrieves the goal map for the specified candidate.
     *
     * @param candidateId The candidate identifier
     * @return Result containing the megaverse map or an error
     */
    suspend fun getGoalMap(candidateId: String): MegaverseResult<MegaverseMap>
}