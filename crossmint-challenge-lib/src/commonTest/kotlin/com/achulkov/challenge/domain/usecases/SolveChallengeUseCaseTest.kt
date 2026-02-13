package com.achulkov.challenge.domain.usecases

import app.cash.turbine.test
import com.achulkov.challenge.MockLogger
import com.achulkov.challenge.domain.*
import com.achulkov.challenge.repository.CreationProgress
import com.achulkov.challenge.repository.DeletionProgress
import com.achulkov.challenge.repository.MegaverseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Production-grade tests for SolveChallengeUseCase.
 * Tests all business logic paths, error scenarios, and edge cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SolveChallengeUseCaseTest {

    private lateinit var mockRepository: MockMegaverseRepository
    private lateinit var mockLogger: MockLogger
    private lateinit var useCase: SolveChallengeUseCase

    @BeforeTest
    fun setup() {
        mockRepository = MockMegaverseRepository()
        mockLogger = MockLogger()
        useCase = SolveChallengeUseCase(mockRepository, mockLogger)
    }

    @Test
    fun `should successfully solve challenge with all object types`() = runTest {
        // Arrange
        val goalMap = createGoalMapWithAllTypes()
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        val calls = mutableListOf<List<AstralObject>>()
        mockRepository.onCreateObjects = { objects ->
            calls.add(objects)
            flowOf(
                CreationProgress.InProgress(0, objects.size),
                CreationProgress.Completed(objects.size, 0)
            )
        }

        // Act & Assert
        useCase().test {
            val initial = awaitItem()
            assertTrue(initial is CreationProgress.InProgress)
            assertEquals(0, initial.completed)
            assertEquals(6, initial.total)

            var last: CreationProgress
            do {
                last = awaitItem()
            } while (last !is CreationProgress.Completed)

            assertEquals(6, last.successful)
            assertEquals(0, last.failed)

            awaitComplete()
        }

        // Repository should be called once per phase (polyanets -> soloons -> comeths)
        assertEquals(3, calls.size)
        assertTrue(calls[0].all { it is Polyanet })
        assertTrue(calls[1].all { it is Soloon })
        assertTrue(calls[2].all { it is Cometh })

        // Verify logging
        assertTrue(mockLogger.operations.any { it.contains("automatic challenge solution") })
        assertTrue(mockLogger.infos.any { it.contains("2 polyanets") })
        assertTrue(mockLogger.infos.any { it.contains("2 soloons") })
        assertTrue(mockLogger.infos.any { it.contains("2 comeths") })
    }

    @Test
    fun `should handle empty goal map gracefully`() = runTest {
        // Arrange
        val emptyGoalMap = MegaverseMap(emptyList())
        mockRepository.setGoalMapResult(MegaverseResult.Success(emptyGoalMap))

        // Act & Assert
        useCase().test {
            val item1 = awaitItem()
            assertTrue(item1 is CreationProgress.InProgress)
            assertEquals(0, item1.completed)
            assertEquals(0, item1.total)

            val finalItem = awaitItem()
            assertTrue(finalItem is CreationProgress.Completed)
            assertEquals(0, finalItem.successful)
            assertEquals(0, finalItem.failed)

            awaitComplete()
        }
    }

    @Test
    fun `should handle goal map fetch failure`() = runTest {
        // Arrange
        val exception = MegaverseException.NetworkError("Connection failed")
        mockRepository.setGoalMapResult(MegaverseResult.Error(exception))

        // Act & Assert
        useCase().test {
            val item = awaitItem()
            assertTrue(item is CreationProgress.Completed)
            assertEquals(0, item.successful)
            assertEquals(1, item.failed)

            awaitComplete()
        }

        // Verify error logging
        assertTrue(mockLogger.errors.isNotEmpty())
        assertTrue(mockLogger.errors[0].contains("Failed to get goal map"))
    }

    @Test
    fun `should track progress correctly across multiple phases`() = runTest {
        // Arrange
        val goalMap = createGoalMapWithAllTypes()
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        val progressList = mutableListOf<CreationProgress>()

        // Act
        useCase().collect { progress ->
            progressList.add(progress)
        }

        // Assert
        val inProgressItems = progressList.filterIsInstance<CreationProgress.InProgress>()
        assertTrue(inProgressItems.isNotEmpty())

        // Verify progress is monotonically increasing
        val completedCounts = inProgressItems.map { it.completed }
        assertEquals(completedCounts.sorted(), completedCounts)

        // Verify final completion
        val finalItem = progressList.last()
        assertTrue(finalItem is CreationProgress.Completed)
    }

    @Test
    fun `should handle partial failures in polyanet phase`() = runTest {
        // Arrange
        val goalMap = createSimpleGoalMap()
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        mockRepository.onCreateObjects = { objects ->
            flowOf(
                CreationProgress.InProgress(0, objects.size),
                CreationProgress.ObjectCreated(objects[0]),
                CreationProgress.ObjectFailed(
                    objects[1],
                    MegaverseException.ApiError(500, "Server error")
                ),
                CreationProgress.InProgress(objects.size, objects.size),
                CreationProgress.Completed(1, 1)
            )
        }

        // Act & Assert
        useCase().test {
            val initial = awaitItem()
            assertTrue(initial is CreationProgress.InProgress)
            assertEquals(2, initial.total)

            // Consume until we see an object failure
            var failed: CreationProgress.ObjectFailed? = null
            var completed: CreationProgress.Completed? = null
            while (failed == null || completed == null) {
                when (val item = awaitItem()) {
                    is CreationProgress.ObjectFailed -> failed = item
                    is CreationProgress.Completed -> completed = item
                    else -> Unit
                }
            }

            assertEquals(Position(0, 1), failed!!.astralObject.position)
            assertEquals(1, completed!!.successful)
            assertEquals(1, completed!!.failed)

            awaitComplete()
        }
    }

    @Test
    fun `should create objects in correct order - polyanets first, then soloons, then comeths`() = runTest {
        // Arrange
        val goalMap = createGoalMapWithAllTypes()
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        val createdObjects = mutableListOf<AstralObject>()

        mockRepository.onCreateObjects = { objects ->
            createdObjects.addAll(objects)
            flowOf(
                CreationProgress.InProgress(0, objects.size),
                CreationProgress.Completed(objects.size, 0)
            )
        }

        // Act
        useCase().collect { /* consume flow */ }

        // Assert - verify order
        assertTrue(createdObjects.size >= 6)

        val polyanetCount = createdObjects.takeWhile { it is Polyanet }.size
                    val soloonStartIndex = polyanetCount
            val soloonCount = createdObjects.drop(soloonStartIndex).takeWhile { it is Soloon }.size
            val comethStartIndex = polyanetCount + soloonCount

        // All polyanets should come first
        assertTrue(polyanetCount > 0)
        assertTrue(createdObjects.take(polyanetCount).all { it is Polyanet })

        // All soloons should come after polyanets
        assertTrue(soloonCount > 0)
        assertTrue(
            createdObjects.drop(soloonStartIndex).take(soloonCount).all { it is Soloon }
        )

        // All comeths should come last
        assertTrue(
            createdObjects.drop(comethStartIndex).all { it is Cometh }
        )
    }

    @Test
    fun `should log all phases correctly`() = runTest {
        // Arrange
        val goalMap = createGoalMapWithAllTypes()
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        // Act
        useCase().collect { /* consume flow */ }

        // Assert
        val hasStartMessage = mockLogger.operations.any { msg -> msg.contains("Starting automatic challenge solution") }
        assertTrue(hasStartMessage)
        val hasPhase1 = mockLogger.infos.any { msg -> msg.contains("Phase 1: Creating") }
        assertTrue(hasPhase1)
        val hasPhase2 = mockLogger.infos.any { msg -> msg.contains("Phase 2: Creating") }
        assertTrue(hasPhase2)
        val hasPhase3 = mockLogger.infos.any { msg -> msg.contains("Phase 3: Creating") }
        assertTrue(hasPhase3)
        val hasCompletedMessage = mockLogger.infos.any { msg -> msg.contains("Challenge solving completed") }
        assertTrue(hasCompletedMessage)
    }

    @Test
    fun `should handle only polyanets in goal map`() = runTest {
        // Arrange
        val goalMap = MegaverseMap(
            listOf(
                listOf("POLYANET", "SPACE"),
                listOf("SPACE", "POLYANET")
            )
        )
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        mockRepository.onCreateObjects = { objects ->
            flowOf(
                CreationProgress.InProgress(0, objects.size),
                CreationProgress.Completed(objects.size, 0)
            )
        }

        // Act & Assert
        useCase().test {
            val initial = awaitItem()
            assertTrue(initial is CreationProgress.InProgress)
            assertEquals(2, initial.total)

            var last: CreationProgress
            do {
                last = awaitItem()
            } while (last !is CreationProgress.Completed)

            assertEquals(2, last.successful)

            awaitComplete()
        }
    }

    @Test
    fun `should aggregate failures across all phases correctly`() = runTest {
        // Arrange
        val goalMap = createGoalMapWithAllTypes()
        mockRepository.setGoalMapResult(MegaverseResult.Success(goalMap))

        // Simulate failures in each phase
        mockRepository.setCreateObjectsFlow(
            flowOf(
                CreationProgress.InProgress(0, 2),
                CreationProgress.ObjectCreated(Polyanet(Position(0, 0))),
                CreationProgress.ObjectFailed(
                    Polyanet(Position(1, 1)),
                    MegaverseException.ApiError(500, "Error")
                ),
                CreationProgress.Completed(1, 1) // 1 success, 1 failure
            )
        )

        // Act
        val progressList = mutableListOf<CreationProgress>()
        useCase().collect { progressList.add(it) }

        // Assert
        val finalItem = progressList.last() as CreationProgress.Completed
        // 1 polyanet succeeded, rest failed
        assertTrue(finalItem.successful > 0)
        assertTrue(finalItem.failed > 0)
        assertEquals(6, finalItem.successful + finalItem.failed)
    }

    // Helper functions

    private fun createGoalMapWithAllTypes(): MegaverseMap {
        return MegaverseMap(
            listOf(
                listOf("POLYANET", "BLUE_SOLOON", "UP_COMETH"),
                listOf("POLYANET", "RED_SOLOON", "DOWN_COMETH")
            )
        )
    }

    private fun createSimpleGoalMap(): MegaverseMap {
        return MegaverseMap(
            listOf(
                listOf("POLYANET", "POLYANET")
            )
        )
    }

    // Mock implementations

    private class MockMegaverseRepository : MegaverseRepository {
        private var goalMapResult: MegaverseResult<MegaverseMap>? = null
        private var createObjectsFlow: Flow<CreationProgress>? = null
        var onCreateObjects: ((List<AstralObject>) -> Flow<CreationProgress>)? = null

        fun setGoalMapResult(result: MegaverseResult<MegaverseMap>) {
            goalMapResult = result
        }

        fun setCreateObjectsFlow(flow: Flow<CreationProgress>) {
            createObjectsFlow = flow
        }

        override suspend fun getGoalMap(): MegaverseResult<MegaverseMap> {
            return goalMapResult ?: MegaverseResult.Error(
                MegaverseException.NetworkError("Not configured")
            )
        }

        override fun createAstralObjects(
            objects: List<AstralObject>
        ): Flow<CreationProgress> {
            return onCreateObjects?.invoke(objects) ?: createObjectsFlow ?: flowOf(
                CreationProgress.InProgress(0, objects.size),
                CreationProgress.Completed(objects.size, 0)
            )
        }

        override suspend fun createPolyanet(polyanet: Polyanet): MegaverseResult<Unit> =
            MegaverseResult.Success(Unit)

        override suspend fun createSoloon(soloon: Soloon): MegaverseResult<Unit> =
            MegaverseResult.Success(Unit)

        override suspend fun createCometh(cometh: Cometh): MegaverseResult<Unit> =
            MegaverseResult.Success(Unit)

        override suspend fun deletePolyanet(position: Position): MegaverseResult<Unit> =
            MegaverseResult.Success(Unit)

        override suspend fun deleteSoloon(position: Position): MegaverseResult<Unit> =
            MegaverseResult.Success(Unit)

        override suspend fun deleteCometh(position: Position): MegaverseResult<Unit> =
            MegaverseResult.Success(Unit)

        override fun clearPositions(
            positions: List<Position>
        ): Flow<DeletionProgress> =
            flowOf()
    }
}
