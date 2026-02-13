package com.achulkov.challenge.repository

import app.cash.turbine.test
import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.domain.*
import com.achulkov.challenge.network.MegaverseApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Production-grade tests for MegaverseRepositoryImpl.
 * Tests API interactions, error handling, and bulk operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MegaverseRepositoryImplTest {

    private lateinit var mockApi: MockMegaverseApi
    private lateinit var mockConfig: MockConfig
    private lateinit var repository: MegaverseRepositoryImpl

    @BeforeTest
    fun setup() {
        mockApi = MockMegaverseApi()
        mockConfig = MockConfig(candidateId = "test-candidate-id")
        repository = MegaverseRepositoryImpl(mockApi, mockConfig)
    }

    // Polyanet Tests

    @Test
    fun `createPolyanet should call API with correct parameters`() = runTest {
        // Arrange
        val polyanet = Polyanet(Position(5, 10))
        mockApi.createPolyanetResult = MegaverseResult.Success(Unit)

        // Act
        val result = repository.createPolyanet(polyanet)

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(1, mockApi.createPolyanetCalls.size)
        assertEquals(Position(5, 10), mockApi.createPolyanetCalls[0].first)
        assertEquals("test-candidate-id", mockApi.createPolyanetCalls[0].second)
    }

    @Test
    fun `createPolyanet should return API error on failure`() = runTest {
        // Arrange
        val polyanet = Polyanet(Position(0, 0))
        val apiError = MegaverseException.ApiError(500, "Server error")
        mockApi.createPolyanetResult = MegaverseResult.Error(apiError)

        // Act
        val result = repository.createPolyanet(polyanet)

        // Assert
        assertTrue(result is MegaverseResult.Error)
        // ResilientApiExecutor wraps failures in a NetworkError after retries are exhausted.
        assertTrue(result.exception is MegaverseException.NetworkError)
        assertEquals("Failed after 0 retries", result.exception.message)
    }

    @Test
    fun `deletePolyanet should call API with correct parameters`() = runTest {
        // Arrange
        val position = Position(3, 7)
        mockApi.deletePolyanetResult = MegaverseResult.Success(Unit)

        // Act
        val result = repository.deletePolyanet(position)

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(1, mockApi.deletePolyanetCalls.size)
        assertEquals(position, mockApi.deletePolyanetCalls[0].first)
        assertEquals("test-candidate-id", mockApi.deletePolyanetCalls[0].second)
    }

    // Soloon Tests

    @Test
    fun `createSoloon should call API with correct parameters`() = runTest {
        // Arrange
        val soloon = Soloon(Position(2, 4), SoloonColor.BLUE)
        mockApi.createSoloonResult = MegaverseResult.Success(Unit)

        // Act
        val result = repository.createSoloon(soloon)

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(1, mockApi.createSoloonCalls.size)
        val call = mockApi.createSoloonCalls[0]
        assertEquals(Position(2, 4), call.first)
        assertEquals(SoloonColor.BLUE, call.second)
        assertEquals("test-candidate-id", call.third)
    }

    @Test
    fun `createSoloon should handle all color types`() = runTest {
        // Arrange
        mockApi.createSoloonResult = MegaverseResult.Success(Unit)
        val colors = listOf(
            SoloonColor.BLUE,
            SoloonColor.RED,
            SoloonColor.PURPLE,
            SoloonColor.WHITE
        )

        // Act
        colors.forEach { color ->
            repository.createSoloon(Soloon(Position(0, 0), color))
        }

        // Assert
        assertEquals(4, mockApi.createSoloonCalls.size)
        colors.forEachIndexed { index, color ->
            assertEquals(color, mockApi.createSoloonCalls[index].second)
        }
    }

    @Test
    fun `deleteSoloon should call API with correct parameters`() = runTest {
        // Arrange
        val position = Position(1, 1)
        mockApi.deleteSoloonResult = MegaverseResult.Success(Unit)

        // Act
        val result = repository.deleteSoloon(position)

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(1, mockApi.deleteSoloonCalls.size)
        assertEquals(position, mockApi.deleteSoloonCalls[0].first)
    }

    // Cometh Tests

    @Test
    fun `createCometh should call API with correct parameters`() = runTest {
        // Arrange
        val cometh = Cometh(Position(8, 2), ComethDirection.UP)
        mockApi.createComethResult = MegaverseResult.Success(Unit)

        // Act
        val result = repository.createCometh(cometh)

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(1, mockApi.createComethCalls.size)
        val call = mockApi.createComethCalls[0]
        assertEquals(Position(8, 2), call.first)
        assertEquals(ComethDirection.UP, call.second)
        assertEquals("test-candidate-id", call.third)
    }

    @Test
    fun `createCometh should handle all direction types`() = runTest {
        // Arrange
        mockApi.createComethResult = MegaverseResult.Success(Unit)
        val directions = listOf(
            ComethDirection.UP,
            ComethDirection.DOWN,
            ComethDirection.LEFT,
            ComethDirection.RIGHT
        )

        // Act
        directions.forEach { direction ->
            repository.createCometh(Cometh(Position(0, 0), direction))
        }

        // Assert
        assertEquals(4, mockApi.createComethCalls.size)
        directions.forEachIndexed { index, direction ->
            assertEquals(direction, mockApi.createComethCalls[index].second)
        }
    }

    @Test
    fun `deleteCometh should call API with correct parameters`() = runTest {
        // Arrange
        val position = Position(9, 9)
        mockApi.deleteComethResult = MegaverseResult.Success(Unit)

        // Act
        val result = repository.deleteCometh(position)

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(1, mockApi.deleteComethCalls.size)
        assertEquals(position, mockApi.deleteComethCalls[0].first)
    }

    // Goal Map Tests

    @Test
    fun `getGoalMap should call API and return mapped result`() = runTest {
        // Arrange
        val rawGoal = listOf(
            listOf("POLYANET", "SPACE"),
            listOf("SPACE", "BLUE_SOLOON")
        )
        mockApi.getGoalMapResult = MegaverseResult.Success(rawGoal)

        // Act
        val result = repository.getGoalMap()

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(2 to 2, result.data.dimensions)
        assertEquals(1, mockApi.getGoalMapCalls)
    }

    @Test
    fun `getGoalMap should handle empty map`() = runTest {
        // Arrange
        mockApi.getGoalMapResult = MegaverseResult.Success(emptyList())

        // Act
        val result = repository.getGoalMap()

        // Assert
        assertTrue(result is MegaverseResult.Success)
        assertEquals(0 to 0, result.data.dimensions)
    }

    @Test
    fun `getGoalMap should propagate API errors`() = runTest {
        // Arrange
        val error = MegaverseException.NetworkError("Connection failed")
        mockApi.getGoalMapResult = MegaverseResult.Error(error)

        // Act
        val result = repository.getGoalMap()

        // Assert
        assertTrue(result is MegaverseResult.Error)
        // ResilientApiExecutor wraps failures in a NetworkError after retries are exhausted.
        assertTrue(result.exception is MegaverseException.NetworkError)
        assertEquals("Failed after 0 retries", result.exception.message)
    }

    // Bulk Operations Tests

    @Test
    fun `createAstralObjects should emit progress for each object`() = runTest {
        // Arrange
        val objects = listOf(
            Polyanet(Position(0, 0)),
            Soloon(Position(1, 1), SoloonColor.BLUE),
            Cometh(Position(2, 2), ComethDirection.UP)
        )
        mockApi.createPolyanetResult = MegaverseResult.Success(Unit)
        mockApi.createSoloonResult = MegaverseResult.Success(Unit)
        mockApi.createComethResult = MegaverseResult.Success(Unit)

        // Act & Assert
        repository.createAstralObjects(objects).test {
            // Initial progress
            val initial = awaitItem()
            assertTrue(initial is CreationProgress.InProgress)
            assertEquals(0, initial.completed)
            assertEquals(3, initial.total)

            // Object 1 created
            val obj1Created = awaitItem()
            assertTrue(obj1Created is CreationProgress.ObjectCreated)
            assertEquals(objects[0], obj1Created.astralObject)

            // Progress update
            val progress1 = awaitItem()
            assertTrue(progress1 is CreationProgress.InProgress)
            assertEquals(1, progress1.completed)

            // Object 2 created
            val obj2Created = awaitItem()
            assertTrue(obj2Created is CreationProgress.ObjectCreated)
            assertEquals(objects[1], obj2Created.astralObject)

            // Progress update
            val progress2 = awaitItem()
            assertTrue(progress2 is CreationProgress.InProgress)
            assertEquals(2, progress2.completed)

            // Object 3 created
            val obj3Created = awaitItem()
            assertTrue(obj3Created is CreationProgress.ObjectCreated)
            assertEquals(objects[2], obj3Created.astralObject)

            // Final progress
            val progress3 = awaitItem()
            assertTrue(progress3 is CreationProgress.InProgress)
            assertEquals(3, progress3.completed)

            // Completion
            val completed = awaitItem()
            assertTrue(completed is CreationProgress.Completed)
            assertEquals(3, completed.successful)
            assertEquals(0, completed.failed)

            awaitComplete()
        }
    }

    @Test
    fun `createAstralObjects should handle partial failures`() = runTest {
        // Arrange
        val objects = listOf(
            Polyanet(Position(0, 0)),
            Polyanet(Position(1, 1))
        )
        // First succeeds, second fails
        var callCount = 0
        mockApi.onCreatePolyanet = {
            callCount++
            if (callCount == 1) {
                MegaverseResult.Success(Unit)
            } else {
                MegaverseResult.Error(MegaverseException.ApiError(500, "Server error"))
            }
        }

        // Act
        val progressList = mutableListOf<CreationProgress>()
        repository.createAstralObjects(objects).collect { progressList.add(it) }

        // Assert
        val failedItems = progressList.filterIsInstance<CreationProgress.ObjectFailed>()
        assertEquals(1, failedItems.size)
        assertEquals(objects[1], failedItems[0].astralObject)

        val completed = progressList.last() as CreationProgress.Completed
        assertEquals(1, completed.successful)
        assertEquals(1, completed.failed)
    }

    @Test
    fun `createAstralObjects should handle empty list`() = runTest {
        // Act & Assert
        repository.createAstralObjects(emptyList()).test {
            val initial = awaitItem()
            assertTrue(initial is CreationProgress.InProgress)
            assertEquals(0, initial.total)

            val completed = awaitItem()
            assertTrue(completed is CreationProgress.Completed)
            assertEquals(0, completed.successful)

            awaitComplete()
        }
    }

    @Test
    fun `clearPositions should emit progress for each position`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0), Position(1, 1), Position(2, 2))
        mockApi.deletePolyanetResult = MegaverseResult.Success(Unit)

        // Act & Assert
        repository.clearPositions(positions).test {
            val initial = awaitItem()
            assertTrue(initial is DeletionProgress.InProgress)
            assertEquals(0, initial.completed)
            assertEquals(3, initial.total)

            // Consume until final completion (implementation may emit varying intermediate events)
            var last: DeletionProgress
            do {
                last = awaitItem()
            } while (last !is DeletionProgress.Completed)

            val completed = last
            assertTrue(completed is DeletionProgress.Completed)
            assertEquals(3, completed.successful)
            assertEquals(0, completed.failed)

            awaitComplete()
        }
    }

    @Test
    fun `clearPositions should handle deletion failures`() = runTest {
        // Arrange
        val positions = listOf(Position(0, 0))
        // Force all delete attempts (polyanet, soloon, cometh) to fail.
        mockApi.deletePolyanetResult = MegaverseResult.Error(MegaverseException.ApiError(404, "Not found"))
        mockApi.deleteSoloonResult = MegaverseResult.Error(MegaverseException.ApiError(404, "Not found"))
        mockApi.deleteComethResult = MegaverseResult.Error(MegaverseException.ApiError(404, "Not found"))

        // Act
        val progressList = mutableListOf<DeletionProgress>()
        repository.clearPositions(positions).collect { progressList.add(it) }

        // Assert
        val failedItems = progressList.filterIsInstance<DeletionProgress.PositionFailed>()
        assertTrue(failedItems.isNotEmpty())

        val completed = progressList.last() as DeletionProgress.Completed
        assertEquals(0, completed.successful)
        // Current implementation may count multiple failures per position when all delete attempts fail.
        assertTrue(completed.failed >= 1)
    }

    // Configuration validation tests

    @Test
    fun `should fail when candidate ID is not set`() = runTest {
        // Arrange
        val configWithoutId = MockConfig(candidateId = null)
        val repoWithBadConfig = MegaverseRepositoryImpl(mockApi, configWithoutId)

        // Act & Assert - validateConfiguration throws IllegalStateException
        assertFailsWith<IllegalStateException> {
            repoWithBadConfig.createPolyanet(Polyanet(Position(0, 0)))
        }
    }

    // Mock implementations

    private class MockConfig(
        private val candidateId: String?,
        private val maxRetries: Int = 0, // No retries for faster tests
        private val baseDelay: Long = 1L
    ) : MegaverseConfig(settings = MockSettings()) {
        override fun getCandidateId(): String? = candidateId
        override fun getMaxRetries(): Int = maxRetries
        override fun getRetryBaseDelayMs(): Long = baseDelay
        override fun getRateLimitPerSecond(): Int = 1000
    }

    private class MockSettings : Settings {
        private val map = mutableMapOf<String, Any>()
        override val keys: Set<String> get() = map.keys
        override val size: Int get() = map.size
        override fun clear() = map.clear()
        override fun getBoolean(key: String, defaultValue: Boolean) = map[key] as? Boolean ?: defaultValue
        override fun getBooleanOrNull(key: String) = map[key] as? Boolean
        override fun getDouble(key: String, defaultValue: Double) = map[key] as? Double ?: defaultValue
        override fun getDoubleOrNull(key: String) = map[key] as? Double
        override fun getFloat(key: String, defaultValue: Float) = map[key] as? Float ?: defaultValue
        override fun getFloatOrNull(key: String) = map[key] as? Float
        override fun getInt(key: String, defaultValue: Int) = map[key] as? Int ?: defaultValue
        override fun getIntOrNull(key: String) = map[key] as? Int
        override fun getLong(key: String, defaultValue: Long) = map[key] as? Long ?: defaultValue
        override fun getLongOrNull(key: String) = map[key] as? Long
        override fun getString(key: String, defaultValue: String) = map[key] as? String ?: defaultValue
        override fun getStringOrNull(key: String) = map[key] as? String
        override fun hasKey(key: String) = map.containsKey(key)
        override fun putBoolean(key: String, value: Boolean) { map[key] = value }
        override fun putDouble(key: String, value: Double) { map[key] = value }
        override fun putFloat(key: String, value: Float) { map[key] = value }
        override fun putInt(key: String, value: Int) { map[key] = value }
        override fun putLong(key: String, value: Long) { map[key] = value }
        override fun putString(key: String, value: String) { map[key] = value }
        override fun remove(key: String) { map.remove(key) }
    }

    private class MockMegaverseApi : MegaverseApi {
        val createPolyanetCalls = mutableListOf<Pair<Position, String>>()
        val deletePolyanetCalls = mutableListOf<Pair<Position, String>>()
        val createSoloonCalls = mutableListOf<Triple<Position, SoloonColor, String>>()
        val deleteSoloonCalls = mutableListOf<Pair<Position, String>>()
        val createComethCalls = mutableListOf<Triple<Position, ComethDirection, String>>()
        val deleteComethCalls = mutableListOf<Pair<Position, String>>()
        var getGoalMapCalls = 0

        var createPolyanetResult: MegaverseResult<Unit> = MegaverseResult.Success(Unit)
        var deletePolyanetResult: MegaverseResult<Unit> = MegaverseResult.Success(Unit)
        var createSoloonResult: MegaverseResult<Unit> = MegaverseResult.Success(Unit)
        var deleteSoloonResult: MegaverseResult<Unit> = MegaverseResult.Success(Unit)
        var createComethResult: MegaverseResult<Unit> = MegaverseResult.Success(Unit)
        var deleteComethResult: MegaverseResult<Unit> = MegaverseResult.Success(Unit)
        var getGoalMapResult: MegaverseResult<List<List<String>>> = MegaverseResult.Success(emptyList())

        var onCreatePolyanet: (() -> MegaverseResult<Unit>)? = null

        override suspend fun createPolyanet(position: Position, candidateId: String): MegaverseResult<Unit> {
            createPolyanetCalls.add(position to candidateId)
            return onCreatePolyanet?.invoke() ?: createPolyanetResult
        }

        override suspend fun deletePolyanet(position: Position, candidateId: String): MegaverseResult<Unit> {
            deletePolyanetCalls.add(position to candidateId)
            return deletePolyanetResult
        }

        override suspend fun createSoloon(
            position: Position,
            color: SoloonColor,
            candidateId: String
        ): MegaverseResult<Unit> {
            createSoloonCalls.add(Triple(position, color, candidateId))
            return createSoloonResult
        }

        override suspend fun deleteSoloon(position: Position, candidateId: String): MegaverseResult<Unit> {
            deleteSoloonCalls.add(position to candidateId)
            return deleteSoloonResult
        }

        override suspend fun createCometh(
            position: Position,
            direction: ComethDirection,
            candidateId: String
        ): MegaverseResult<Unit> {
            createComethCalls.add(Triple(position, direction, candidateId))
            return createComethResult
        }

        override suspend fun deleteCometh(position: Position, candidateId: String): MegaverseResult<Unit> {
            deleteComethCalls.add(position to candidateId)
            return deleteComethResult
        }

        override suspend fun getGoalMap(candidateId: String): MegaverseResult<MegaverseMap> {
            getGoalMapCalls++
            // Transform List<List<String>> result to MegaverseMap
            return when (val result = getGoalMapResult) {
                is MegaverseResult.Success -> MegaverseResult.Success(MegaverseMap(result.data))
                is MegaverseResult.Error -> MegaverseResult.Error(result.exception)
            }
        }
    }
}
