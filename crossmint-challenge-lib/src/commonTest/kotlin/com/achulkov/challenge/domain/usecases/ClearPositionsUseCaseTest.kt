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
 * Production-grade tests for ClearPositionsUseCase.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClearPositionsUseCaseTest {

    private lateinit var mockRepository: MockMegaverseRepository
    private lateinit var mockLogger: MockLogger
    private lateinit var useCase: ClearPositionsUseCase

    @BeforeTest
    fun setup() {
        mockRepository = MockMegaverseRepository()
        mockLogger = MockLogger()
        useCase = ClearPositionsUseCase(mockRepository, mockLogger)
    }

    @Test
    fun `should delegate to repository with correct positions`() = runTest {
        // Arrange
        val positions = listOf(
            Position(0, 0),
            Position(1, 1),
            Position(2, 2)
        )

        var capturedPositions: List<Position>? = null
        mockRepository.onClearPositions = { pos ->
            capturedPositions = pos
            flowOf(DeletionProgress.Completed(pos.size, 0))
        }

        // Act
        useCase(positions).collect { /* consume */ }

        // Assert
        assertNotNull(capturedPositions)
        assertEquals(positions, capturedPositions)
    }

    @Test
    fun `should log operation with correct position count`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0), Position(1, 1))

        // Act
        useCase(positions).collect { /* consume */ }

        // Assert
        assertTrue(mockLogger.operations.isNotEmpty())
        assertTrue(mockLogger.operations[0].contains("Clearing 2 positions"))
    }

    @Test
    fun `should emit all deletion progress events from repository`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0), Position(1, 1))
        val expectedProgress = listOf(
            DeletionProgress.InProgress(0, 2),
            DeletionProgress.PositionCleared(positions[0]),
            DeletionProgress.InProgress(1, 2),
            DeletionProgress.PositionCleared(positions[1]),
            DeletionProgress.InProgress(2, 2),
            DeletionProgress.Completed(2, 0)
        )

        mockRepository.setClearPositionsFlow(flowOf(*expectedProgress.toTypedArray()))

        // Act & Assert
        useCase(positions).test {
            expectedProgress.forEach { expected ->
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
            awaitComplete()
        }
    }

    @Test
    fun `should handle empty position list`() = runTest {
        // Arrange
        val emptyList = emptyList<Position>()

        // Act & Assert
        useCase(emptyList).test {
            val initial = awaitItem()
            assertTrue(initial is DeletionProgress.InProgress)
            assertEquals(0, initial.total)

            val completed = awaitItem()
            assertTrue(completed is DeletionProgress.Completed)
            assertEquals(0, completed.successful)
            awaitComplete()
        }

        // Should still log the operation
        assertTrue(mockLogger.operations.any { it.contains("Clearing 0 positions") })
    }

    @Test
    fun `should handle single position`() = runTest {
        // Arrange
        val positions = listOf(Position(5, 5))

        mockRepository.setClearPositionsFlow(
            flowOf(
                DeletionProgress.InProgress(0, 1),
                DeletionProgress.PositionCleared(positions[0]),
                DeletionProgress.Completed(1, 0)
            )
        )

        // Act & Assert
        useCase(positions).test {
            awaitItem() // InProgress
            val cleared = awaitItem()
            assertTrue(cleared is DeletionProgress.PositionCleared)
            assertEquals(Position(5, 5), cleared.position)

            val completed = awaitItem()
            assertTrue(completed is DeletionProgress.Completed)
            assertEquals(1, completed.successful)
            assertEquals(0, completed.failed)

            awaitComplete()
        }
    }

    @Test
    fun `should handle large batch of positions`() = runTest {
        // Arrange
        val positions = (0 until 100).map { Position(it, it) }

        // Act
        useCase(positions).collect { /* consume */ }

        // Assert
        assertTrue(mockLogger.operations.any { it.contains("Clearing 100 positions") })
    }

    @Test
    fun `should propagate failures from repository`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0), Position(1, 1))
        val error = MegaverseException.ApiError(500, "Server error")

        mockRepository.setClearPositionsFlow(
            flowOf(
                DeletionProgress.InProgress(0, 2),
                DeletionProgress.PositionCleared(positions[0]),
                DeletionProgress.InProgress(1, 2),
                DeletionProgress.PositionFailed(positions[1], error),
                DeletionProgress.InProgress(2, 2),
                DeletionProgress.Completed(1, 1)
            )
        )

        // Act & Assert
        useCase(positions).test {
            awaitItem() // InProgress
            awaitItem() // PositionCleared
            awaitItem() // InProgress

            val failedItem = awaitItem()
            assertTrue(failedItem is DeletionProgress.PositionFailed)
            assertEquals(positions[1], failedItem.position)
            assertEquals(error, failedItem.error)

            awaitItem() // InProgress

            val completedItem = awaitItem()
            assertTrue(completedItem is DeletionProgress.Completed)
            assertEquals(1, completedItem.successful)
            assertEquals(1, completedItem.failed)

            awaitComplete()
        }
    }

    @Test
    fun `should handle all positions failing`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0), Position(1, 1))
        val error = MegaverseException.ApiError(404, "Not found")

        mockRepository.setClearPositionsFlow(
            flowOf(
                DeletionProgress.InProgress(0, 2),
                DeletionProgress.PositionFailed(positions[0], error),
                DeletionProgress.InProgress(1, 2),
                DeletionProgress.PositionFailed(positions[1], error),
                DeletionProgress.InProgress(2, 2),
                DeletionProgress.Completed(0, 2)
            )
        )

        // Act & Assert
        useCase(positions).test {
            // Skip emissions until final completion
            var last: DeletionProgress
            do {
                last = awaitItem()
            } while (last !is DeletionProgress.Completed)

            val completedItem = last
            assertTrue(completedItem is DeletionProgress.Completed)
            assertEquals(0, completedItem.successful)
            assertEquals(2, completedItem.failed)

            awaitComplete()
        }
    }

    @Test
    fun `should track progress correctly`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0), Position(1, 1), Position(2, 2))

        mockRepository.setClearPositionsFlow(
            flowOf(
                DeletionProgress.InProgress(0, 3),
                DeletionProgress.PositionCleared(positions[0]),
                DeletionProgress.InProgress(1, 3),
                DeletionProgress.PositionCleared(positions[1]),
                DeletionProgress.InProgress(2, 3),
                DeletionProgress.PositionCleared(positions[2]),
                DeletionProgress.InProgress(3, 3),
                DeletionProgress.Completed(3, 0)
            )
        )

        // Act
        val progressList = mutableListOf<DeletionProgress>()
        useCase(positions).collect { progressList.add(it) }

        // Assert
        val inProgressItems = progressList.filterIsInstance<DeletionProgress.InProgress>()
        assertEquals(4, inProgressItems.size)

        // Verify progress is monotonically increasing
        val completedCounts = inProgressItems.map { it.completed }
        assertEquals(listOf(0, 1, 2, 3), completedCounts)
    }

    @Test
    fun `should handle duplicate positions`() = runTest {
        // Arrange - Repository should handle duplicates, use case just passes through
        val positions = listOf(Position(0, 0), Position(0, 0), Position(1, 1))

        var capturedPositions: List<Position>? = null
        mockRepository.onClearPositions = { pos ->
            capturedPositions = pos
            flowOf(DeletionProgress.Completed(pos.size, 0))
        }

        // Act
        useCase(positions).collect { /* consume */ }

        // Assert - use case should pass duplicates to repository
        assertEquals(positions, capturedPositions)
    }

    // Mock implementations

    private class MockMegaverseRepository : MegaverseRepository {
        private var clearPositionsFlow: Flow<DeletionProgress>? = null
        var onClearPositions: ((List<Position>) -> Flow<DeletionProgress>)? = null

        fun setClearPositionsFlow(flow: Flow<DeletionProgress>) {
            clearPositionsFlow = flow
        }

        override fun clearPositions(positions: List<Position>): Flow<DeletionProgress> {
            return onClearPositions?.invoke(positions) ?: clearPositionsFlow ?: flowOf(
                DeletionProgress.InProgress(0, positions.size),
                DeletionProgress.Completed(positions.size, 0)
            )
        }

        override suspend fun getGoalMap(): MegaverseResult<MegaverseMap> =
            MegaverseResult.Success(MegaverseMap(emptyList()))

        override fun createAstralObjects(objects: List<AstralObject>): Flow<CreationProgress> =
            flowOf()

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
    }
}
