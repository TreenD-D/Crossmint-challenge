package com.achulkov.challenge.repository

import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.domain.*
import com.achulkov.challenge.network.MegaverseApi
import com.achulkov.challenge.utils.MegaverseLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * Implementation of MegaverseRepository with resilience patterns.
 *
 * This repository provides:
 * - Exponential backoff retry with jitter
 * - Circuit breaker pattern for fault tolerance
 * - Rate limiting with token bucket algorithm
 * - Comprehensive progress tracking for bulk operations
 *
 * @param api The API client for network operations
 * @param config The configuration manager
 * @param resilientExecutor Optional custom resilient executor
 */
class MegaverseRepositoryImpl(
    private val api: MegaverseApi,
    private val config: MegaverseConfig,
    private val resilientExecutor: ResilientApiExecutor = ResilientApiExecutor(config)
) : MegaverseRepository {

    init {
        MegaverseLogger.info(
            "MegaverseRepository",
            "Initialized with resilience patterns: " +
                "maxRetries=${config.getMaxRetries()}, " +
                "baseDelay=${config.getRetryBaseDelayMs()}ms"
        )
    }

    override suspend fun createPolyanet(polyanet: Polyanet): MegaverseResult<Unit> {
        MegaverseLogger.debug("Repository", "Creating polyanet at ${polyanet.position}")
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("createPolyanet") {
            api.createPolyanet(polyanet.position, config.getCandidateId()!!)
        }
    }

    override suspend fun deletePolyanet(position: Position): MegaverseResult<Unit> {
        MegaverseLogger.debug("Repository", "Deleting polyanet at $position")
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("deletePolyanet") {
            api.deletePolyanet(position, config.getCandidateId()!!)
        }
    }

    override suspend fun createSoloon(soloon: Soloon): MegaverseResult<Unit> {
        MegaverseLogger.debug(
            "Repository",
            "Creating ${soloon.color.value} soloon at ${soloon.position}"
        )
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("createSoloon") {
            api.createSoloon(soloon.position, soloon.color, config.getCandidateId()!!)
        }
    }

    override suspend fun deleteSoloon(position: Position): MegaverseResult<Unit> {
        MegaverseLogger.debug("Repository", "Deleting soloon at $position")
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("deleteSoloon") {
            api.deleteSoloon(position, config.getCandidateId()!!)
        }
    }

    override suspend fun createCometh(cometh: Cometh): MegaverseResult<Unit> {
        MegaverseLogger.debug(
            "Repository",
            "Creating ${cometh.direction.value}-facing cometh at ${cometh.position}"
        )
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("createCometh") {
            api.createCometh(cometh.position, cometh.direction, config.getCandidateId()!!)
        }
    }

    override suspend fun deleteCometh(position: Position): MegaverseResult<Unit> {
        MegaverseLogger.debug("Repository", "Deleting cometh at $position")
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("deleteCometh") {
            api.deleteCometh(position, config.getCandidateId()!!)
        }
    }

    override suspend fun getGoalMap(): MegaverseResult<MegaverseMap> {
        MegaverseLogger.debug("Repository", "Fetching goal map")
        config.validateConfiguration()

        return resilientExecutor.executeWithResilience("getGoalMap") {
            api.getGoalMap(config.getCandidateId()!!)
        }
    }

    override fun createAstralObjects(objects: List<AstralObject>): Flow<CreationProgress> = flow {
        MegaverseLogger.logOperation(
            "createAstralObjects",
            "Starting bulk creation of ${objects.size} objects with resilience"
        )

        var successful = 0
        var failed = 0
        val total = objects.size
        val startTime = Clock.System.now().toEpochMilliseconds()

        emit(CreationProgress.InProgress(0, total))

        for ((index, astralObject) in objects.withIndex()) {
            try {
                MegaverseLogger.debug(
                    "Repository",
                    "Creating object ${index + 1}/$total: ${astralObject::class.simpleName} at ${astralObject.position}"
                )

                val result = when (astralObject) {
                    is Polyanet -> createPolyanet(astralObject)
                    is Soloon -> createSoloon(astralObject)
                    is Cometh -> createCometh(astralObject)
                }

                when (result) {
                    is MegaverseResult.Success -> {
                        successful++
                        MegaverseLogger.debug(
                            "Repository",
                            "Successfully created ${astralObject::class.simpleName} at ${astralObject.position}"
                        )
                        emit(CreationProgress.ObjectCreated(astralObject))
                    }

                    is MegaverseResult.Error -> {
                        failed++
                        MegaverseLogger.warn(
                            "Repository",
                            "Failed to create ${astralObject::class.simpleName} at ${astralObject.position}: ${result.exception.message}"
                        )
                        emit(CreationProgress.ObjectFailed(astralObject, result.exception))
                    }
                }
            } catch (e: Exception) {
                failed++
                MegaverseLogger.error(
                    "Repository",
                    "Unexpected error creating ${astralObject::class.simpleName} at ${astralObject.position}",
                    e
                )
                emit(
                    CreationProgress.ObjectFailed(
                        astralObject,
                        MegaverseException.Unknown("Unexpected error", e)
                    )
                )
            }

            emit(CreationProgress.InProgress(index + 1, total))
        }

        val duration = Clock.System.now().toEpochMilliseconds() - startTime
        MegaverseLogger.logOperation(
            "createAstralObjects",
            "Completed in ${duration}ms. Successful: $successful, Failed: $failed"
        )
        emit(CreationProgress.Completed(successful, failed))
    }

    override fun clearPositions(positions: List<Position>): Flow<DeletionProgress> = flow {
        MegaverseLogger.logOperation(
            "clearPositions",
            "Starting bulk deletion of ${positions.size} positions with resilience"
        )

        var successful = 0
        var failed = 0
        val total = positions.size
        val startTime = Clock.System.now().toEpochMilliseconds()

        emit(DeletionProgress.InProgress(0, total))

        for ((index, position) in positions.withIndex()) {
            try {
                MegaverseLogger.debug(
                    "Repository",
                    "Clearing position ${index + 1}/$total: $position"
                )

                // Try deleting each type of object at the position
                var deleted = false

                // Try polyanet first
                deletePolyanet(position).let { result ->
                    when (result) {
                        is MegaverseResult.Success -> {
                            deleted = true
                            successful++
                            MegaverseLogger.debug(
                                "Repository",
                                "Successfully deleted polyanet at $position"
                            )
                            emit(DeletionProgress.PositionCleared(position))
                        }

                        is MegaverseResult.Error -> {
                            MegaverseLogger.debug("Repository", "No polyanet found at $position")
                            // Object might not be a polyanet, try others
                        }
                    }
                }

                // Try soloon if polyanet deletion failed
                if (!deleted) {
                    deleteSoloon(position).let { result ->
                        when (result) {
                            is MegaverseResult.Success -> {
                                deleted = true
                                successful++
                                MegaverseLogger.debug(
                                    "Repository",
                                    "Successfully deleted soloon at $position"
                                )
                                emit(DeletionProgress.PositionCleared(position))
                            }

                            is MegaverseResult.Error -> {
                                MegaverseLogger.debug("Repository", "No soloon found at $position")
                                // Object might not be a soloon, try cometh
                            }
                        }
                    }
                }

                // Try cometh if soloon deletion failed
                if (!deleted) {
                    deleteCometh(position).let { result ->
                        when (result) {
                            is MegaverseResult.Success -> {
                                deleted = true
                                successful++
                                MegaverseLogger.debug(
                                    "Repository",
                                    "Successfully deleted cometh at $position"
                                )
                                emit(DeletionProgress.PositionCleared(position))
                            }

                            is MegaverseResult.Error -> {
                                failed++
                                MegaverseLogger.warn(
                                    "Repository",
                                    "No object found at $position or deletion failed: ${result.exception.message}"
                                )
                                emit(DeletionProgress.PositionFailed(position, result.exception))
                            }
                        }
                    }
                }

                if (!deleted) {
                    failed++
                    MegaverseLogger.warn("Repository", "No objects found to delete at $position")
                    emit(
                        DeletionProgress.PositionFailed(
                            position,
                            MegaverseException.Unknown("No object found at position")
                        )
                    )
                }
            } catch (e: Exception) {
                failed++
                MegaverseLogger.error(
                    "Repository",
                    "Unexpected error clearing position $position",
                    e
                )
                emit(
                    DeletionProgress.PositionFailed(
                        position,
                        MegaverseException.Unknown("Unexpected error", e)
                    )
                )
            }

            emit(DeletionProgress.InProgress(index + 1, total))
        }

        val duration = Clock.System.now().toEpochMilliseconds() - startTime
        MegaverseLogger.logOperation(
            "clearPositions",
            "Completed in ${duration}ms. Successful: $successful, Failed: $failed"
        )
        emit(DeletionProgress.Completed(successful, failed))
    }
}
