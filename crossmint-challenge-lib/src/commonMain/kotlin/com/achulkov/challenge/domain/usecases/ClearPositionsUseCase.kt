package com.achulkov.challenge.domain.usecases

import com.achulkov.challenge.domain.Position
import com.achulkov.challenge.domain.interfaces.ILogger
import com.achulkov.challenge.repository.DeletionProgress
import com.achulkov.challenge.repository.MegaverseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for clearing positions in the megaverse.
 * Encapsulates the business logic for deletion operations.
 *
 * @param repository The repository for Megaverse operations
 * @param logger The logger for tracking operations
 */
class ClearPositionsUseCase(
    private val repository: MegaverseRepository,
    private val logger: ILogger
) {
    /**
     * Executes the use case to clear multiple positions.
     *
     * @param positions The positions to clear
     * @return Flow emitting deletion progress updates
     */
    operator fun invoke(positions: List<Position>): Flow<DeletionProgress> {
        logger.logOperation(
            "ClearPositionsUseCase",
            "Clearing ${positions.size} positions"
        )
        return repository.clearPositions(positions)
    }
}
