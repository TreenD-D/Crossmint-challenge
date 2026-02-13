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
 * Production-grade tests for CreateAstralObjectsUseCase.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateAstralObjectsUseCaseTest {

    private lateinit var mockRepository: MockMegaverseRepository
    private lateinit var mockLogger: MockLogger
    private lateinit var useCase: CreateAstralObjectsUseCase

    @BeforeTest
    fun setup() {
        mockRepository = MockMegaverseRepository()
        mockLogger = MockLogger()
        useCase = CreateAstralObjectsUseCase(mockRepository, mockLogger)
    }

    @Test
    fun `should delegate to repository with correct objects`() = runTest {
        // Arrange
        val objects = listOf(
            Polyanet(Position(0, 0)),
            Soloon(Position(1, 1), SoloonColor.BLUE),
            Cometh(Position(2, 2), ComethDirection.UP)
        )

        var capturedObjects: List<AstralObject>? = null
        mockRepository.onCreateObjects = { objs ->
            capturedObjects = objs
            flowOf(CreationProgress.Completed(objs.size, 0))
        }

        // Act
        useCase(objects).collect { /* consume */ }

        // Assert
        assertNotNull(capturedObjects)
        assertEquals(objects, capturedObjects)
    }

    @Test
    fun `should log operation with correct object count`() = runTest {
        // Arrange
        val objects = listOf(
            Polyanet(Position(0, 0)),
            Polyanet(Position(1, 1))
        )

        // Act
        useCase(objects).collect { /* consume */ }

        // Assert
        assertTrue(mockLogger.operations.isNotEmpty())
        assertTrue(mockLogger.operations[0].contains("Creating 2 astral objects"))
    }

    @Test
    fun `should emit all progress events from repository`() = runTest {
        // Arrange
        val objects = listOf(Polyanet(Position(0, 0)))
        val expectedProgress = listOf(
            CreationProgress.InProgress(0, 1),
            CreationProgress.ObjectCreated(objects[0]),
            CreationProgress.InProgress(1, 1),
            CreationProgress.Completed(1, 0)
        )

        mockRepository.setCreateObjectsFlow(flowOf(*expectedProgress.toTypedArray()))

        // Act & Assert
        useCase(objects).test {
            expectedProgress.forEach { expected ->
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
            awaitComplete()
        }
    }

    @Test
    fun `should handle empty object list`() = runTest {
        // Arrange
        val emptyList = emptyList<AstralObject>()

        // Act & Assert
        useCase(emptyList).test {
            val initial = awaitItem()
            assertTrue(initial is CreationProgress.InProgress)
            assertEquals(0, initial.total)

            val completed = awaitItem()
            assertTrue(completed is CreationProgress.Completed)
            assertEquals(0, completed.successful)
            awaitComplete()
        }

        // Should still log the operation
        assertTrue(mockLogger.operations.any { it.contains("Creating 0 astral objects") })
    }

    @Test
    fun `should handle large batch of objects`() = runTest {
        // Arrange
        val objects = (0 until 100).map { Polyanet(Position(it, it)) }

        // Act
        useCase(objects).collect { /* consume */ }

        // Assert
        assertTrue(mockLogger.operations.any { it.contains("Creating 100 astral objects") })
    }

    @Test
    fun `should propagate errors from repository`() = runTest {
        // Arrange
        val objects = listOf(Polyanet(Position(0, 0)))
        val error = MegaverseException.ApiError(500, "Server error")

        mockRepository.setCreateObjectsFlow(
            flowOf(
                CreationProgress.InProgress(0, 1),
                CreationProgress.ObjectFailed(objects[0], error),
                CreationProgress.Completed(0, 1)
            )
        )

        // Act & Assert
        useCase(objects).test {
            awaitItem() // InProgress
            val failedItem = awaitItem()
            assertTrue(failedItem is CreationProgress.ObjectFailed)
            assertEquals(error, failedItem.error)

            val completedItem = awaitItem()
            assertTrue(completedItem is CreationProgress.Completed)
            assertEquals(0, completedItem.successful)
            assertEquals(1, completedItem.failed)

            awaitComplete()
        }
    }

    // Mock implementations

    private class MockMegaverseRepository : MegaverseRepository {
        private var createObjectsFlow: Flow<CreationProgress>? = null
        var onCreateObjects: ((List<AstralObject>) -> Flow<CreationProgress>)? = null

        fun setCreateObjectsFlow(flow: Flow<CreationProgress>) {
            createObjectsFlow = flow
        }

        override fun createAstralObjects(
            objects: List<AstralObject>
        ): Flow<CreationProgress> {
            return onCreateObjects?.invoke(objects) ?: createObjectsFlow ?: flowOf(
                CreationProgress.InProgress(0, objects.size),
                CreationProgress.Completed(objects.size, 0)
            )
        }

        override suspend fun getGoalMap(): MegaverseResult<MegaverseMap> =
            MegaverseResult.Success(MegaverseMap(emptyList()))

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

        override fun clearPositions(positions: List<Position>): Flow<DeletionProgress> =
            flowOf()
    }
}
