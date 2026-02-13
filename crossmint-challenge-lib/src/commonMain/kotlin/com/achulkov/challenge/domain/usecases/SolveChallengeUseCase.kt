package com.achulkov.challenge.domain.usecases

import com.achulkov.challenge.domain.*
import com.achulkov.challenge.domain.interfaces.ILogger
import com.achulkov.challenge.extensions.*
import com.achulkov.challenge.repository.CreationProgress
import com.achulkov.challenge.repository.MegaverseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for automatically solving the Megaverse challenge.
 * Encapsulates the complex business logic for:
 * 1. Fetching the goal map
 * 2. Parsing and organizing objects by type
 * 3. Creating objects in the correct order (POLYanets -> SOLoons -> COMETHs)
 * 4. Tracking overall progress
 *
 * @param repository The repository for Megaverse operations
 * @param logger The logger for tracking operations
 */
class SolveChallengeUseCase(
    private val repository: MegaverseRepository,
    private val logger: ILogger
) {
    /**
     * Executes the use case to solve the challenge automatically.
     *
     * @return Flow emitting creation progress for the entire challenge
     */
    suspend operator fun invoke(): Flow<CreationProgress> = flow {
        logger.logOperation("SolveChallengeUseCase", "Starting automatic challenge solution")

        val goalMapResult = repository.getGoalMap()

        when (goalMapResult) {
            is MegaverseResult.Success -> {
                val map = goalMapResult.data

                // Separate objects by type for ordered creation
                val polyanetPositions = map.getPolyanetPositions()
                val soloonsByColor = map.getSoloonPositions()
                val comethsByDirection = map.getComethPositions()

                val polyanets = polyanetPositions.toPolyanets()
                val soloons =
                    soloonsByColor.flatMap { (color, positions) -> positions.toSoloons(color) }
                val comeths = comethsByDirection.flatMap { (direction, positions) ->
                    positions.toComeths(direction)
                }

                val totalObjects = polyanets.size + soloons.size + comeths.size
                var completedObjects = 0

                logger.info("SolveChallengeUseCase", "Found ${polyanets.size} polyanets to create")
                logger.info("SolveChallengeUseCase", "Found ${soloons.size} soloons to create")
                logger.info("SolveChallengeUseCase", "Found ${comeths.size} comeths to create")
                logger.info("SolveChallengeUseCase", "Total objects to create: $totalObjects")

                emit(CreationProgress.InProgress(0, totalObjects))

                // Phase 1: Create all POLYanets first
                if (polyanets.isNotEmpty()) {
                    logger.info(
                        "SolveChallengeUseCase",
                        "Phase 1: Creating ${polyanets.size} POLYanets..."
                    )

                    repository.createAstralObjects(polyanets).collect { progress ->
                        when (progress) {
                            is CreationProgress.InProgress -> {
                                emit(
                                    CreationProgress.InProgress(
                                        completedObjects + progress.completed,
                                        totalObjects
                                    )
                                )
                            }

                            is CreationProgress.ObjectCreated -> {
                                emit(progress)
                            }

                            is CreationProgress.ObjectFailed -> {
                                emit(progress)
                            }

                            is CreationProgress.Completed -> {
                                completedObjects += progress.successful
                                logger.info(
                                    "SolveChallengeUseCase",
                                    "Phase 1 complete: ${progress.successful} POLYanets created, ${progress.failed} failed"
                                )
                            }
                        }
                    }
                }

                // Phase 2: Create all SOLoons (now that POLYanets exist)
                if (soloons.isNotEmpty()) {
                    logger.info(
                        "SolveChallengeUseCase",
                        "Phase 2: Creating ${soloons.size} SOLoons..."
                    )

                    repository.createAstralObjects(soloons).collect { progress ->
                        when (progress) {
                            is CreationProgress.InProgress -> {
                                emit(
                                    CreationProgress.InProgress(
                                        completedObjects + progress.completed,
                                        totalObjects
                                    )
                                )
                            }

                            is CreationProgress.ObjectCreated -> {
                                emit(progress)
                            }

                            is CreationProgress.ObjectFailed -> {
                                emit(progress)
                            }

                            is CreationProgress.Completed -> {
                                completedObjects += progress.successful
                                logger.info(
                                    "SolveChallengeUseCase",
                                    "Phase 2 complete: ${progress.successful} SOLoons created, ${progress.failed} failed"
                                )
                            }
                        }
                    }
                }

                // Phase 3: Create all COMETHs (independent of other objects)
                if (comeths.isNotEmpty()) {
                    logger.info(
                        "SolveChallengeUseCase",
                        "Phase 3: Creating ${comeths.size} COMETHs..."
                    )

                    repository.createAstralObjects(comeths).collect { progress ->
                        when (progress) {
                            is CreationProgress.InProgress -> {
                                emit(
                                    CreationProgress.InProgress(
                                        completedObjects + progress.completed,
                                        totalObjects
                                    )
                                )
                            }

                            is CreationProgress.ObjectCreated -> {
                                emit(progress)
                            }

                            is CreationProgress.ObjectFailed -> {
                                emit(progress)
                            }

                            is CreationProgress.Completed -> {
                                completedObjects += progress.successful
                                logger.info(
                                    "SolveChallengeUseCase",
                                    "Phase 3 complete: ${progress.successful} COMETHs created, ${progress.failed} failed"
                                )
                            }
                        }
                    }
                }

                val totalSuccessful = completedObjects
                val totalFailed = totalObjects - completedObjects

                logger.info(
                    "SolveChallengeUseCase",
                    "Challenge solving completed! Total successful: $totalSuccessful, Total failed: $totalFailed"
                )
                emit(CreationProgress.Completed(totalSuccessful, totalFailed))
            }

            is MegaverseResult.Error -> {
                logger.error(
                    "SolveChallengeUseCase",
                    "Failed to get goal map for challenge solving",
                    goalMapResult.exception
                )
                emit(CreationProgress.Completed(0, 1))
            }
        }
    }
}
