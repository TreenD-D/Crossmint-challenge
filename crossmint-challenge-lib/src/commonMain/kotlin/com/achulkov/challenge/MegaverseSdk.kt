package com.achulkov.challenge

import com.achulkov.challenge.domain.*
import com.achulkov.challenge.domain.interfaces.IConfiguration
import com.achulkov.challenge.domain.interfaces.ILogger
import com.achulkov.challenge.domain.state.MegaverseStateManager
import com.achulkov.challenge.domain.usecases.ClearPositionsUseCase
import com.achulkov.challenge.domain.usecases.CreateAstralObjectsUseCase
import com.achulkov.challenge.domain.usecases.GetGoalMapUseCase
import com.achulkov.challenge.domain.usecases.SolveChallengeUseCase
import com.achulkov.challenge.extensions.*
import com.achulkov.challenge.repository.CreationProgress
import com.achulkov.challenge.repository.DeletionProgress
import com.achulkov.challenge.repository.MegaverseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Production-grade SDK for interacting with the Megaverse API.
 *
 *
 * ## Usage with Koin (Recommended):
 * ```kotlin
 * // Initialize Koin once at app startup
 * KoinInitializer.init()
 * 
 * // Get SDK instance from Koin
 * val sdk: MegaverseSdk by inject()
 * 
 * // Or create manually with injected dependencies
 * val sdk = MegaverseSdk(
 *     configuration = get(),
 *     logger = get(),
 *     repository = get(),
 *     // ... other dependencies
 * )
 * ```
 *
 * @param configuration Configuration provider for API settings
 * @param logger Logger for tracking operations
 * @param repository Repository for Megaverse operations
 * @param createAstralObjectsUseCase Use case for creating astral objects
 * @param getGoalMapUseCase Use case for fetching goal map
 * @param clearPositionsUseCase Use case for clearing positions
 * @param solveChallengeUseCase Use case for solving the challenge
 * @param stateManager Optional state manager for reactive state management
 */
class MegaverseSdk(
    private val configuration: IConfiguration,
    private val logger: ILogger,
    private val repository: MegaverseRepository,
    private val createAstralObjectsUseCase: CreateAstralObjectsUseCase,
    private val getGoalMapUseCase: GetGoalMapUseCase,
    private val clearPositionsUseCase: ClearPositionsUseCase,
    private val solveChallengeUseCase: SolveChallengeUseCase,
    private val stateManager: MegaverseStateManager? = null
) {

    init {
        logger.info("MegaverseSdk", "SDK initialized")
        logger.debug("MegaverseSdk", "Base URL: ${configuration.getBaseUrl()}")
    }

    /**
     * Enable or disable debug logging at runtime.
     * This should be set to true only in debug builds.
     *
     * @param enabled Whether to enable debug logging
     */
    fun setDebugLogging(enabled: Boolean) {
        logger.setDebugEnabled(enabled)
        logger.info(
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
        logger.logOperation("setCandidateId", "Setting candidate ID")
        configuration.setCandidateId(candidateId)
    }

    /**
     * Gets the currently configured candidate ID.
     *
     * @return The candidate ID, or null if not set
     */
    fun getCandidateId(): String? {
        val candidateId = configuration.getCandidateId()
        logger.debug(
            "MegaverseSdk",
            "Retrieved candidate ID: ${candidateId?.let { "****" } ?: "null"}"
        )
        return candidateId
    }

    /**
     * Sets a custom base URL for the API (useful for testing).
     *
     * @param baseUrl The base URL to use
     */
    fun setBaseUrl(baseUrl: String) {
        logger.logOperation("setBaseUrl", "Setting custom base URL: $baseUrl")
        configuration.setBaseUrl(baseUrl)
    }

    // Individual object operations

    /**
     * Creates a single polyanet at the specified position.
     *
     * @param position The position to place the polyanet
     * @return Result indicating success or failure
     */
    suspend fun createPolyanet(position: Position): MegaverseResult<Unit> {
        logger.logOperation("createPolyanet", "Creating polyanet at $position")
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
        logger.logOperation("createSoloon", "Creating ${color.value} soloon at $position")
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
        logger.logOperation(
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
        logger.logOperation("clearPosition", "Clearing position $position")
        return clearPositionsUseCase(listOf(position))
    }

    // Bulk operations

    /**
     * Creates multiple astral objects with progress tracking.
     *
     * @param objects The list of astral objects to create
     * @return Flow emitting progress updates
     */
    fun createAstralObjects(objects: List<AstralObject>): Flow<CreationProgress> {
        logger.logOperation(
            "createAstralObjects",
            "Creating ${objects.size} astral objects"
        )
        return createAstralObjectsUseCase(objects)
    }

    /**
     * Clears multiple positions with progress tracking.
     *
     * @param positions The positions to clear
     * @return Flow emitting progress updates
     */
    fun clearPositions(positions: List<Position>): Flow<DeletionProgress> {
        logger.logOperation("clearPositions", "Clearing ${positions.size} positions")
        return clearPositionsUseCase(positions)
    }

    // Pattern-based operations

    /**
     * Creates an X-shaped pattern of polyanets for Phase 1 of the challenge.
     *
     * @param size The size of the grid (e.g., 11 for an 11x11 grid)
     * @return Flow emitting creation progress
     */
    fun createXPatternChallenge(size: Int = 11): Flow<CreationProgress> {
        logger.logOperation(
            "createXPatternChallenge",
            "Creating X-pattern for ${size}x${size} grid"
        )
        val polyanets = createXPatternPolyanets(size)
        logger.info("MegaverseSdk", "X-pattern will create ${polyanets.size} polyanets")
        return createAstralObjectsUseCase(polyanets)
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
        logger.logOperation(
            "createBorderPattern",
            "Creating border pattern for ${width}x${height} grid"
        )
        val positions = createBorderPattern(width, height)
        val objects = positions.map(objectCreator)
        logger.info("MegaverseSdk", "Border pattern will create ${objects.size} objects")
        return createAstralObjectsUseCase(objects)
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
        logger.logOperation(
            "createPlusPattern",
            "Creating plus pattern for ${width}x${height} grid"
        )
        val positions = createPlusPattern(width, height)
        val objects = positions.map(objectCreator)
        logger.info("MegaverseSdk", "Plus pattern will create ${objects.size} objects")
        return createAstralObjectsUseCase(objects)
    }

    // Map operations

    /**
     * Retrieves the goal map for the current candidate.
     *
     * @return Result containing the goal map or an error
     */
    suspend fun getGoalMap(): MegaverseResult<MegaverseMap> {
        logger.logOperation("getGoalMap", "Fetching goal map")
        return getGoalMapUseCase()
    }

    /**
     * Solves the challenge by creating all objects according to the goal map.
     * This is a high-level operation that handles the entire challenge automatically.
     * Creates objects in the correct order: POLYanets first, then SOLoons, then COMETHs.
     *
     * @return Flow emitting progress updates for the entire challenge
     */
    suspend fun solveChallenge(): Flow<CreationProgress> {
        logger.logOperation("solveChallenge", "Starting automatic challenge solution")
        return solveChallengeUseCase()
    }

    /**
     * Clears the entire megaverse by attempting to delete all possible objects.
     *
     * @param width The width of the grid to clear
     * @param height The height of the grid to clear
     * @return Flow emitting deletion progress
     */
    fun clearEntireMegaverse(width: Int, height: Int): Flow<DeletionProgress> {
        logger.logOperation(
            "clearEntireMegaverse",
            "Clearing entire ${width}x${height} megaverse"
        )

        val allPositions = mutableListOf<Position>()
        for (row in 0 until height) {
            for (col in 0 until width) {
                allPositions.add(Position(row, col))
            }
        }

        logger.warn(
            "MegaverseSdk",
            "This will attempt to clear ${allPositions.size} positions"
        )
        return clearPositionsUseCase(allPositions)
    }
}