package com.achulkov.challenge.domain.usecases

import com.achulkov.challenge.domain.MegaverseMap
import com.achulkov.challenge.domain.MegaverseResult
import com.achulkov.challenge.domain.interfaces.ILogger
import com.achulkov.challenge.repository.MegaverseRepository

/**
 * Use case for retrieving the goal map.
 * Encapsulates the business logic for fetching the goal map.
 *
 * @param repository The repository for Megaverse operations
 * @param logger The logger for tracking operations
 */
class GetGoalMapUseCase(
    private val repository: MegaverseRepository,
    private val logger: ILogger
) {
    /**
     * Executes the use case to get the goal map.
     *
     * @return Result containing the goal map or an error
     */
    suspend operator fun invoke(): MegaverseResult<MegaverseMap> {
        logger.logOperation("GetGoalMapUseCase", "Fetching goal map")
        return repository.getGoalMap()
    }
}
