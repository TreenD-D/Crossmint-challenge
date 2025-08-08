package com.achulkov.challenge

import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.domain.*
import com.achulkov.challenge.extensions.*
import com.achulkov.challenge.network.MegaverseApi
import com.achulkov.challenge.network.MegaverseApiImpl
import com.achulkov.challenge.repository.CreationProgress
import com.achulkov.challenge.repository.DeletionProgress
import com.achulkov.challenge.repository.MegaverseRepository
import com.achulkov.challenge.repository.MegaverseRepositoryImpl
import com.achulkov.challenge.utils.MegaverseLogger
import io.ktor.client.*
import kotlinx.coroutines.flow.Flow

/**
 * Main SDK class for interacting with the Megaverse API.
 * Provides a high-level, easy-to-use interface for all Megaverse operations.
 *
 * @param config Optional custom configuration, if null a default will be created
 * @param httpClient Optional custom HTTP client, if null a default will be created
 * @param enableDebugLogging Whether to enable debug logging (should be true only in debug builds)
 */
class MegaverseSdk(
    private val config: MegaverseConfig = MegaverseConfig(),
    httpClient: HttpClient? = null,
    enableDebugLogging: Boolean = false
) {

    private val api: MegaverseApi = MegaverseApiImpl(config.getBaseUrl(), httpClient)
    private val repository: MegaverseRepository = MegaverseRepositoryImpl(api, config)

    init {
        // Enable debug logging if requested
        MegaverseLogger.setDebugEnabled(enableDebugLogging)

        MegaverseLogger.info("MegaverseSdk", "SDK initialized")
        MegaverseLogger.debug("MegaverseSdk", "Debug logging enabled: $enableDebugLogging")
        MegaverseLogger.debug("MegaverseSdk", "Base URL: ${config.getBaseUrl()}")
    }

    /**
     * Enable or disable debug logging at runtime.
     * This should be set to true only in debug builds.
     *
     * @param enabled Whether to enable debug logging
     */
    fun setDebugLogging(enabled: Boolean) {
        MegaverseLogger.setDebugEnabled(enabled)
        MegaverseLogger.info(
            "MegaverseSdk",
            "Debug logging ${if (enabled) "enabled" else "disabled"}"
        )
    }

    /**
     * Sets the candidate ID for all subsequent API calls.
     * This must be called before using any other methods.
     *
     * @param candidateId Your unique candidate identifier
     */
    fun setCandidateId(candidateId: String) {
        MegaverseLogger.logOperation("setCandidateId", "Setting candidate ID")
        config.setCandidateId(candidateId)
    }

    /**
     * Gets the currently configured candidate ID.
     *
     * @return The candidate ID, or null if not set
     */
    fun getCandidateId(): String? {
        val candidateId = config.getCandidateId()
        MegaverseLogger.debug(
            "MegaverseSdk",
            "Retrieved candidate ID: ${candidateId?.let { "****" } ?: "null"}")
        return candidateId
    }

    /**
     * Sets a custom base URL for the API (useful for testing).
     *
     * @param baseUrl The base URL to use
     */
    fun setBaseUrl(baseUrl: String) {
        MegaverseLogger.logOperation("setBaseUrl", "Setting custom base URL: $baseUrl")
        config.setBaseUrl(baseUrl)
    }

    // Individual object operations

    /**
     * Creates a single polyanet at the specified position.
     *
     * @param position The position to place the polyanet
     * @return Result indicating success or failure
     */
    suspend fun createPolyanet(position: Position): MegaverseResult<Unit> {
        MegaverseLogger.logOperation("createPolyanet", "Creating polyanet at $position")
        return repository.createPolyanet(Polyanet(position))
    }

    /**
     * Creates a single soloon at the specified position.
     *
     * @param position The position to place the soloon
     * @param color The color of the soloon
     * @return Result indicating success or failure
     */
    suspend fun createSoloon(position: Position, color: SoloonColor): MegaverseResult<Unit> {
        MegaverseLogger.logOperation("createSoloon", "Creating ${color.value} soloon at $position")
        return repository.createSoloon(Soloon(position, color))
    }

    /**
     * Creates a single cometh at the specified position.
     *
     * @param position The position to place the cometh
     * @param direction The direction of the cometh
     * @return Result indicating success or failure
     */
    suspend fun createCometh(
        position: Position,
        direction: ComethDirection
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "createCometh",
            "Creating ${direction.value}-facing cometh at $position"
        )
        return repository.createCometh(Cometh(position, direction))
    }

    /**
     * Deletes any object at the specified position.
     *
     * @param position The position to clear
     * @return Flow emitting deletion progress
     */
    fun clearPosition(position: Position): Flow<DeletionProgress> {
        MegaverseLogger.logOperation("clearPosition", "Clearing position $position")
        return repository.clearPositions(listOf(position))
    }

    // Bulk operations

    /**
     * Creates multiple astral objects with progress tracking.
     *
     * @param objects The list of astral objects to create
     * @return Flow emitting progress updates
     */
    fun createAstralObjects(objects: List<AstralObject>): Flow<CreationProgress> {
        MegaverseLogger.logOperation(
            "createAstralObjects",
            "Creating ${objects.size} astral objects"
        )
        return repository.createAstralObjects(objects)
    }

    /**
     * Clears multiple positions with progress tracking.
     *
     * @param positions The positions to clear
     * @return Flow emitting progress updates
     */
    fun clearPositions(positions: List<Position>): Flow<DeletionProgress> {
        MegaverseLogger.logOperation("clearPositions", "Clearing ${positions.size} positions")
        return repository.clearPositions(positions)
    }

    // Pattern-based operations

    /**
     * Creates an X-shaped pattern of polyanets for Phase 1 of the challenge.
     *
     * @param size The size of the grid (e.g., 11 for an 11x11 grid)
     * @return Flow emitting creation progress
     */
    fun createXPatternChallenge(size: Int = 11): Flow<CreationProgress> {
        MegaverseLogger.logOperation(
            "createXPatternChallenge",
            "Creating X-pattern for ${size}x${size} grid"
        )
        val polyanets = createXPatternPolyanets(size)
        MegaverseLogger.info("MegaverseSdk", "X-pattern will create ${polyanets.size} polyanets")
        return repository.createAstralObjects(polyanets)
    }

    /**
     * Creates a border pattern of the specified astral objects.
     *
     * @param width The width of the grid
     * @param height The height of the grid
     * @param objectCreator Function to create objects for each position
     * @return Flow emitting creation progress
     */
    fun createBorderPattern(
        width: Int,
        height: Int,
        objectCreator: (Position) -> AstralObject
    ): Flow<CreationProgress> {
        MegaverseLogger.logOperation(
            "createBorderPattern",
            "Creating border pattern for ${width}x${height} grid"
        )
        val positions = createBorderPattern(width, height)
        val objects = positions.map(objectCreator)
        MegaverseLogger.info("MegaverseSdk", "Border pattern will create ${objects.size} objects")
        return repository.createAstralObjects(objects)
    }

    /**
     * Creates a plus (+) pattern of the specified astral objects.
     *
     * @param width The width of the grid
     * @param height The height of the grid
     * @param objectCreator Function to create objects for each position
     * @return Flow emitting creation progress
     */
    fun createPlusPattern(
        width: Int,
        height: Int,
        objectCreator: (Position) -> AstralObject
    ): Flow<CreationProgress> {
        MegaverseLogger.logOperation(
            "createPlusPattern",
            "Creating plus pattern for ${width}x${height} grid"
        )
        val positions = createPlusPattern(width, height)
        val objects = positions.map(objectCreator)
        MegaverseLogger.info("MegaverseSdk", "Plus pattern will create ${objects.size} objects")
        return repository.createAstralObjects(objects)
    }

    // Map operations

    /**
     * Retrieves the goal map for the current candidate.
     *
     * @return Result containing the goal map or an error
     */
    suspend fun getGoalMap(): MegaverseResult<MegaverseMap> {
        MegaverseLogger.logOperation("getGoalMap", "Fetching goal map")
        return repository.getGoalMap()
    }

    /**
     * Solves the challenge by creating all objects according to the goal map.
     * This is a high-level operation that handles the entire challenge automatically.
     * Creates objects in the correct order: POLYanets first, then SOLoons, then COMETHs.
     *
     * @return Flow emitting progress updates for the entire challenge
     */
    suspend fun solveChallenge(): Flow<CreationProgress> = kotlinx.coroutines.flow.flow {
        MegaverseLogger.logOperation("solveChallenge", "Starting automatic challenge solution")

        val goalMapResult = getGoalMap()

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

                MegaverseLogger.info("MegaverseSdk", "Found ${polyanets.size} polyanets to create")
                MegaverseLogger.info("MegaverseSdk", "Found ${soloons.size} soloons to create")
                MegaverseLogger.info("MegaverseSdk", "Found ${comeths.size} comeths to create")
                MegaverseLogger.info("MegaverseSdk", "Total objects to create: $totalObjects")

                emit(CreationProgress.InProgress(0, totalObjects))

                // Phase 1: Create all POLYanets first
                if (polyanets.isNotEmpty()) {
                    MegaverseLogger.info(
                        "MegaverseSdk",
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
                                MegaverseLogger.info(
                                    "MegaverseSdk",
                                    "Phase 1 complete: ${progress.successful} POLYanets created, ${progress.failed} failed"
                                )
                            }
                        }
                    }
                }

                // Phase 2: Create all SOLoons (now that POLYanets exist)
                if (soloons.isNotEmpty()) {
                    MegaverseLogger.info(
                        "MegaverseSdk",
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
                                MegaverseLogger.info(
                                    "MegaverseSdk",
                                    "Phase 2 complete: ${progress.successful} SOLoons created, ${progress.failed} failed"
                                )
                            }
                        }
                    }
                }

                // Phase 3: Create all COMETHs (independent of other objects)
                if (comeths.isNotEmpty()) {
                    MegaverseLogger.info(
                        "MegaverseSdk",
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
                                MegaverseLogger.info(
                                    "MegaverseSdk",
                                    "Phase 3 complete: ${progress.successful} COMETHs created, ${progress.failed} failed"
                                )
                            }
                        }
                    }
                }

                val totalSuccessful = completedObjects
                val totalFailed = totalObjects - completedObjects

                MegaverseLogger.info(
                    "MegaverseSdk",
                    "Challenge solving completed! Total successful: $totalSuccessful, Total failed: $totalFailed"
                )
                emit(CreationProgress.Completed(totalSuccessful, totalFailed))
            }

            is MegaverseResult.Error -> {
                MegaverseLogger.error(
                    "MegaverseSdk",
                    "Failed to get goal map for challenge solving",
                    goalMapResult.exception
                )
                emit(CreationProgress.Completed(0, 1))
            }
        }
    }

    /**
     * Clears the entire megaverse by attempting to delete all possible objects.
     *
     * @param width The width of the grid to clear
     * @param height The height of the grid to clear
     * @return Flow emitting deletion progress
     */
    fun clearEntireMegaverse(width: Int, height: Int): Flow<DeletionProgress> {
        MegaverseLogger.logOperation(
            "clearEntireMegaverse",
            "Clearing entire ${width}x${height} megaverse"
        )

        val allPositions = mutableListOf<Position>()
        for (row in 0 until height) {
            for (col in 0 until width) {
                allPositions.add(Position(row, col))
            }
        }

        MegaverseLogger.warn(
            "MegaverseSdk",
            "This will attempt to clear ${allPositions.size} positions"
        )
        return repository.clearPositions(allPositions)
    }
}