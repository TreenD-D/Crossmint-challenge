package com.achulkov.challenge.domain.usecases

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
 * Production-grade tests for GetGoalMapUseCase.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetGoalMapUseCaseTest {

    private lateinit var mockRepository: MockMegaverseRepository
    private lateinit var mockLogger: MockLogger
    private lateinit var useCase: GetGoalMapUseCase

    @BeforeTest
    fun setup() {
        mockRepository = MockMegaverseRepository()
        mockLogger = MockLogger()
        useCase = GetGoalMapUseCase(mockRepository, mockLogger)
    }

    @Test
    fun `should return success result when repository returns success`() = runTest {
        // Arrange
        val expectedMap = MegaverseMap(
            listOf(
                listOf("POLYANET", "SPACE"),
                listOf("SPACE", "BLUE_SOLOON")
            )
        )
        mockRepository.setGoalMapResult(MegaverseResult.Success(expectedMap))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(expectedMap, result.data)
    }

    @Test
    fun `should return error result when repository returns error`() = runTest {
        // Arrange
        val exception = MegaverseException.NetworkError("Connection failed")
        mockRepository.setGoalMapResult(MegaverseResult.Error(exception))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is MegaverseResult.Error)
        assertEquals(exception, result.exception)
    }

    @Test
    fun `should log operation before fetching goal map`() = runTest {
        // Arrange
        mockRepository.setGoalMapResult(
            MegaverseResult.Success(MegaverseMap(emptyList()))
        )

        // Act
        useCase()

        // Assert
        assertTrue(mockLogger.operations.isNotEmpty())
        assertTrue(mockLogger.operations[0].contains("Fetching goal map"))
    }

    @Test
    fun `should handle empty goal map successfully`() = runTest {
        // Arrange
        val emptyMap = MegaverseMap(emptyList())
        mockRepository.setGoalMapResult(MegaverseResult.Success(emptyMap))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(emptyMap, result.data)
        assertEquals(0 to 0, result.data.dimensions)
    }

    @Test
    fun `should handle large goal map`() = runTest {
        // Arrange
        val largeMap = MegaverseMap(
            (0 until 50).map { row ->
                (0 until 50).map { col ->
                    if ((row + col) % 2 == 0) "POLYANET" else "SPACE"
                }
            }
        )
        mockRepository.setGoalMapResult(MegaverseResult.Success(largeMap))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(50 to 50, result.data.dimensions)
    }

    @Test
    fun `should handle API error correctly`() = runTest {
        // Arrange
        val apiError = MegaverseException.ApiError(404, "Not found")
        mockRepository.setGoalMapResult(MegaverseResult.Error(apiError))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is MegaverseResult.Error)
        assertTrue(result.exception is MegaverseException.ApiError)
        val exception = result.exception as MegaverseException.ApiError
        assertEquals(404, exception.statusCode)
        assertEquals("API Error (404): Not found", exception.message)
    }

    @Test
    fun `should handle invalid candidate id error correctly`() = runTest {
        // Arrange
        val invalidCandidateId = MegaverseException.InvalidCandidateId("bad-id")
        mockRepository.setGoalMapResult(MegaverseResult.Error(invalidCandidateId))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is MegaverseResult.Error)
        assertTrue(result.exception is MegaverseException.InvalidCandidateId)
    }

    @Test
    fun `should delegate directly to repository without transformation`() = runTest {
        // Arrange
        val goalMap = MegaverseMap(listOf(listOf("POLYANET")))
        val expectedResult = MegaverseResult.Success(goalMap)
        mockRepository.setGoalMapResult(expectedResult)

        // Act
        val result = useCase()

        // Assert
        assertEquals(expectedResult, result)
        assertEquals(1, mockRepository.getGoalMapCallCount)
    }

    @Test
    fun `should invoke repository exactly once per call`() = runTest {
        // Arrange
        mockRepository.setGoalMapResult(
            MegaverseResult.Success(MegaverseMap(emptyList()))
        )

        // Act
        useCase()
        useCase()
        useCase()

        // Assert
        assertEquals(3, mockRepository.getGoalMapCallCount)
    }

    // Mock implementations

    private class MockMegaverseRepository : MegaverseRepository {
        private var goalMapResult: MegaverseResult<MegaverseMap>? = null
        var getGoalMapCallCount = 0

        fun setGoalMapResult(result: MegaverseResult<MegaverseMap>) {
            goalMapResult = result
        }

        override suspend fun getGoalMap(): MegaverseResult<MegaverseMap> {
            getGoalMapCallCount++
            return goalMapResult ?: MegaverseResult.Error(
                MegaverseException.NetworkError("Not configured")
            )
        }

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

        override fun clearPositions(positions: List<Position>): Flow<DeletionProgress> =
            flowOf()
    }
}
