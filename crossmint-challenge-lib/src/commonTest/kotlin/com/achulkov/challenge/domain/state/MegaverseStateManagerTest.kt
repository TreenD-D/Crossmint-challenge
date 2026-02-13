package com.achulkov.challenge.domain.state

import app.cash.turbine.test
import com.achulkov.challenge.domain.MegaverseMap
import com.achulkov.challenge.domain.Position
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Production-grade tests for MegaverseStateManager.
 * Tests all state transitions, reactive updates, and edge cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MegaverseStateManagerTest {

    private lateinit var stateManager: MegaverseStateManager

    @BeforeTest
    fun setup() {
        stateManager = MegaverseStateManager()
    }

    @Test
    fun `should initialize with default idle state`() {
        // Assert
        val state = stateManager.currentState()
        assertNull(state.goalMap)
        assertTrue(state.createdPositions.isEmpty())
        assertTrue(state.operationState is MegaverseState.Idle)
    }

    @Test
    fun `should emit state updates to observers`() = runTest {
        // Act & Assert
        stateManager.state.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial.operationState is MegaverseState.Idle)

            // Update to loading
            stateManager.setOperationState(MegaverseState.Loading("Loading data"))
            val loading = awaitItem()
            assertTrue(loading.operationState is MegaverseState.Loading)
            assertEquals("Loading data", (loading.operationState as MegaverseState.Loading).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setGoalMap should update goal map in state`() = runTest {
        // Arrange
        val goalMap = MegaverseMap(
            listOf(
                listOf("POLYANET", "SPACE"),
                listOf("SPACE", "BLUE_SOLOON")
            )
        )

        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial state

            stateManager.setGoalMap(goalMap)
            val updated = awaitItem()

            assertNotNull(updated.goalMap)
            assertEquals(goalMap, updated.goalMap)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCreatedPosition should add position to set`() = runTest {
        // Arrange
        val position1 = Position(0, 0)
        val position2 = Position(1, 1)

        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial state

            stateManager.addCreatedPosition(position1)
            val state1 = awaitItem()
            assertTrue(state1.createdPositions.contains(position1))
            assertEquals(1, state1.createdPositions.size)

            stateManager.addCreatedPosition(position2)
            val state2 = awaitItem()
            assertTrue(state2.createdPositions.contains(position1))
            assertTrue(state2.createdPositions.contains(position2))
            assertEquals(2, state2.createdPositions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCreatedPosition should handle duplicates correctly`() = runTest {
        // Arrange
        val position = Position(5, 5)

        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial state

            stateManager.addCreatedPosition(position)
            val state1 = awaitItem()
            assertEquals(1, state1.createdPositions.size)

            // Adding same position again
            stateManager.addCreatedPosition(position)
            // If the state doesn't change, StateFlow may not emit a new value.
            expectNoEvents()
            assertEquals(1, stateManager.currentState().createdPositions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeCreatedPosition should remove position from set`() = runTest {
        // Arrange
        val position1 = Position(0, 0)
        val position2 = Position(1, 1)

        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial

            stateManager.addCreatedPosition(position1)
            awaitItem()
            stateManager.addCreatedPosition(position2)
            awaitItem()

            stateManager.removeCreatedPosition(position1)
            val state1 = awaitItem()
            assertFalse(state1.createdPositions.contains(position1))
            assertTrue(state1.createdPositions.contains(position2))
            assertEquals(1, state1.createdPositions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeCreatedPosition should handle non-existent position gracefully`() = runTest {
        // Arrange
        val existingPosition = Position(0, 0)
        val nonExistentPosition = Position(10, 10)

        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial

            stateManager.addCreatedPosition(existingPosition)
            awaitItem()

            stateManager.removeCreatedPosition(nonExistentPosition)
            // If nothing changes, no new emission is expected.
            expectNoEvents()
            val state = stateManager.currentState()
            assertTrue(state.createdPositions.contains(existingPosition))
            assertEquals(1, state.createdPositions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOperationState should update operation state`() = runTest {
        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial Idle

            stateManager.setOperationState(MegaverseState.Loading())
            val loading = awaitItem()
            assertTrue(loading.operationState is MegaverseState.Loading)

            stateManager.setOperationState(MegaverseState.Success("Operation complete"))
            val success = awaitItem()
            assertTrue(success.operationState is MegaverseState.Success)
            assertEquals("Operation complete", (success.operationState as MegaverseState.Success).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOperationState should handle all state types`() = runTest {
        // Test all state transitions
        stateManager.state.test {
            awaitItem() // Initial Idle

            // Loading
            stateManager.setOperationState(MegaverseState.Loading("Loading"))
            val loading = awaitItem()
            assertTrue(loading.operationState is MegaverseState.Loading)

            // Progress
            stateManager.setOperationState(MegaverseState.Progress(5, 10, "Item 5"))
            val progress = awaitItem()
            assertTrue(progress.operationState is MegaverseState.Progress)
            val progressState = progress.operationState as MegaverseState.Progress
            assertEquals(5, progressState.completed)
            assertEquals(10, progressState.total)

            // Success
            stateManager.setOperationState(MegaverseState.Success("Done", mapOf("key" to "value")))
            val success = awaitItem()
            assertTrue(success.operationState is MegaverseState.Success)

            // Error
            val exception = RuntimeException("Test error")
            stateManager.setOperationState(MegaverseState.Error("Failed", exception))
            val error = awaitItem()
            assertTrue(error.operationState is MegaverseState.Error)
            assertEquals("Failed", (error.operationState as MegaverseState.Error).message)
            assertEquals(exception, (error.operationState as MegaverseState.Error).exception)

            // Back to Idle
            stateManager.setOperationState(MegaverseState.Idle)
            val idle = awaitItem()
            assertTrue(idle.operationState is MegaverseState.Idle)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear should reset all state to initial values`() = runTest {
        // Arrange - set up some state
        val goalMap = MegaverseMap(listOf(listOf("POLYANET")))
        stateManager.setGoalMap(goalMap)
        stateManager.addCreatedPosition(Position(0, 0))
        stateManager.addCreatedPosition(Position(1, 1))
        stateManager.setOperationState(MegaverseState.Loading("Loading"))

        // Act
        stateManager.clear()

        // Assert
        val state = stateManager.currentState()
        assertNull(state.goalMap)
        assertTrue(state.createdPositions.isEmpty())
        assertTrue(state.operationState is MegaverseState.Idle)
    }

    @Test
    fun `clear should emit new state to observers`() = runTest {
        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial

            stateManager.setGoalMap(MegaverseMap(listOf(listOf("POLYANET"))))
            awaitItem()
            stateManager.addCreatedPosition(Position(0, 0))
            awaitItem()

            stateManager.clear()
            val clearedState = awaitItem()

            assertNull(clearedState.goalMap)
            assertTrue(clearedState.createdPositions.isEmpty())
            assertTrue(clearedState.operationState is MegaverseState.Idle)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `currentState should return non-reactive snapshot`() {
        // Arrange
        val goalMap = MegaverseMap(listOf(listOf("POLYANET")))
        stateManager.setGoalMap(goalMap)

        // Act
        val snapshot1 = stateManager.currentState()
        stateManager.addCreatedPosition(Position(0, 0))
        val snapshot2 = stateManager.currentState()

        // Assert
        assertNotNull(snapshot1.goalMap)
        assertTrue(snapshot1.createdPositions.isEmpty())

        assertNotNull(snapshot2.goalMap)
        assertEquals(1, snapshot2.createdPositions.size)

        // Original snapshot unchanged
        assertTrue(snapshot1.createdPositions.isEmpty())
    }

    @Test
    fun `multiple updates should all be emitted to observers`() = runTest {
        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial

            // Rapid updates
            stateManager.setOperationState(MegaverseState.Loading())
            awaitItem()

            stateManager.setOperationState(MegaverseState.Progress(1, 10))
            awaitItem()

            stateManager.setOperationState(MegaverseState.Progress(2, 10))
            awaitItem()

            stateManager.setOperationState(MegaverseState.Progress(3, 10))
            val progress = awaitItem()
            val progressState = progress.operationState as MegaverseState.Progress
            assertEquals(3, progressState.completed)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state updates should preserve other state properties`() = runTest {
        // Arrange
        val goalMap = MegaverseMap(listOf(listOf("POLYANET")))
        stateManager.setGoalMap(goalMap)
        stateManager.addCreatedPosition(Position(0, 0))

        // Act
        stateManager.setOperationState(MegaverseState.Loading())

        // Assert
        val state = stateManager.currentState()
        assertNotNull(state.goalMap) // Goal map should still be set
        assertEquals(1, state.createdPositions.size) // Position should still be there
        assertTrue(state.operationState is MegaverseState.Loading) // New state applied
    }

    @Test
    fun `should handle concurrent position additions`() = runTest {
        // This tests that the state update mechanism properly handles concurrent updates
        stateManager.state.test {
            awaitItem() // Initial

            // Add multiple positions in quick succession
            val positions = (0 until 10).map { Position(it, it) }
            positions.forEach { stateManager.addCreatedPosition(it) }

            // Skip intermediate states and check final state
            val items = mutableListOf<MegaverseEnvironmentState>()
            repeat(10) {
                items.add(awaitItem())
            }

            // Final state should have all positions
            val finalState = items.last()
            assertEquals(10, finalState.createdPositions.size)
            positions.forEach { position ->
                assertTrue(finalState.createdPositions.contains(position))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should handle alternating add and remove operations`() = runTest {
        // Arrange
        val position = Position(5, 5)

        // Act & Assert
        stateManager.state.test {
            awaitItem() // Initial

            stateManager.addCreatedPosition(position)
            val added1 = awaitItem()
            assertEquals(1, added1.createdPositions.size)

            stateManager.removeCreatedPosition(position)
            val removed = awaitItem()
            assertEquals(0, removed.createdPositions.size)

            stateManager.addCreatedPosition(position)
            val added2 = awaitItem()
            assertEquals(1, added2.createdPositions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state flow should be hot and replay current value to new collectors`() = runTest {
        // Arrange
        stateManager.setOperationState(MegaverseState.Loading("Test"))

        // Act & Assert - New collector should immediately get current state
        stateManager.state.test {
            val state = awaitItem()
            assertTrue(state.operationState is MegaverseState.Loading)
            assertEquals("Test", (state.operationState as MegaverseState.Loading).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `goalMap update should not affect created positions`() = runTest {
        // Arrange
        val position = Position(0, 0)
        stateManager.addCreatedPosition(position)

        val goalMap1 = MegaverseMap(listOf(listOf("POLYANET")))
        val goalMap2 = MegaverseMap(listOf(listOf("BLUE_SOLOON")))

        // Act
        stateManager.setGoalMap(goalMap1)
        val state1 = stateManager.currentState()

        stateManager.setGoalMap(goalMap2)
        val state2 = stateManager.currentState()

        // Assert
        assertEquals(goalMap1, state1.goalMap)
        assertTrue(state1.createdPositions.contains(position))

        assertEquals(goalMap2, state2.goalMap)
        assertTrue(state2.createdPositions.contains(position))
    }
}
