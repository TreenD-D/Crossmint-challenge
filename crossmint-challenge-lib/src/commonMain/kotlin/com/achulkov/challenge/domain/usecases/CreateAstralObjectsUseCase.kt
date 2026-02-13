package com.achulkov.challenge.domain.usecases

import com.achulkov.challenge.domain.AstralObject
import com.achulkov.challenge.domain.interfaces.ILogger
import com.achulkov.challenge.repository.CreationProgress
import com.achulkov.challenge.repository.MegaverseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for creating multiple astral objects.
 * Encapsulates the business logic for batch creation operations.
 *
 * @param repository The repository for Megaverse operations
 * @param logger The logger for tracking operations
 */
class CreateAstralObjectsUseCase(
    private val repository: MegaverseRepository,
    private val logger: ILogger
) {
    /**
     * Executes the use case to create multiple astral objects.
     *
     * @param objects The list of astral objects to create
     * @return Flow emitting creation progress updates
     */
    operator fun invoke(objects: List<AstralObject>): Flow<CreationProgress> {
        logger.logOperation(
            "CreateAstralObjectsUseCase",
            "Creating ${objects.size} astral objects"
        )
        return repository.createAstralObjects(objects)
    }
}
