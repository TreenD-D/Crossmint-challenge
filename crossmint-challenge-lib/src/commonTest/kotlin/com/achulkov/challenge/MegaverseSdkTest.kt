package com.achulkov.challenge

import com.achulkov.challenge.domain.*
import com.achulkov.challenge.extensions.*
import kotlin.test.*

class MegaverseSdkTest {

    @Test
    fun testPositionCreation() {
        val position = Position(5, 3)
        assertEquals(5, position.row)
        assertEquals(3, position.column)
    }

    @Test
    fun testPositionValidation() {
        assertFailsWith<IllegalArgumentException> {
            Position(-1, 5)
        }
        assertFailsWith<IllegalArgumentException> {
            Position(5, -1)
        }
    }

    @Test
    fun testPolyanetCreation() {
        val position = Position(2, 3)
        val polyanet = Polyanet(position)
        assertEquals(position, polyanet.position)
    }

    @Test
    fun testSoloonCreation() {
        val position = Position(1, 4)
        val soloon = Soloon(position, SoloonColor.BLUE)
        assertEquals(position, soloon.position)
        assertEquals(SoloonColor.BLUE, soloon.color)
        assertEquals("blue", soloon.color.value)
    }

    @Test
    fun testComethCreation() {
        val position = Position(0, 0)
        val cometh = Cometh(position, ComethDirection.UP)
        assertEquals(position, cometh.position)
        assertEquals(ComethDirection.UP, cometh.direction)
        assertEquals("up", cometh.direction.value)
    }

    @Test
    fun testXPatternGeneration() {
        val pattern = createXPattern(5)
        val expected = listOf(
            Position(0, 0), Position(0, 4),
            Position(1, 1), Position(1, 3),
            Position(2, 2),
            Position(3, 1), Position(3, 3),
            Position(4, 0), Position(4, 4)
        )
        assertEquals(expected.size, pattern.size)
        assertTrue(pattern.containsAll(expected))
    }

    @Test
    fun testXPatternSize11() {
        val pattern = createXPattern(11)
        assertEquals(21, pattern.size) // 11 + 11 - 1 (center overlap)

        // Check that diagonal positions are included
        assertTrue(pattern.contains(Position(0, 0)))
        assertTrue(pattern.contains(Position(10, 10)))
        assertTrue(pattern.contains(Position(0, 10)))
        assertTrue(pattern.contains(Position(10, 0)))
        assertTrue(pattern.contains(Position(5, 5))) // Center
    }

    @Test
    fun testBorderPatternGeneration() {
        val pattern = createBorderPattern(5, 3)
        // Top row: (0,0), (0,1), (0,2), (0,3), (0,4)
        // Bottom row: (2,0), (2,1), (2,2), (2,3), (2,4) 
        // Left side: (1,0)
        // Right side: (1,4)
        assertEquals(12, pattern.size)

        // Check corners
        assertTrue(pattern.contains(Position(0, 0)))
        assertTrue(pattern.contains(Position(0, 4)))
        assertTrue(pattern.contains(Position(2, 0)))
        assertTrue(pattern.contains(Position(2, 4)))
    }

    @Test
    fun testPlusPatternGeneration() {
        val pattern = createPlusPattern(5, 5)
        assertEquals(9, pattern.size) // 5 + 5 - 1 (center overlap)

        // Check center and arms
        assertTrue(pattern.contains(Position(2, 2))) // Center
        assertTrue(pattern.contains(Position(0, 2))) // Top
        assertTrue(pattern.contains(Position(4, 2))) // Bottom
        assertTrue(pattern.contains(Position(2, 0))) // Left
        assertTrue(pattern.contains(Position(2, 4))) // Right
    }

    @Test
    fun testPositionExtensions() {
        val pos1 = Position(0, 0)
        val pos2 = Position(3, 4)

        assertEquals(7, pos1.manhattanDistanceTo(pos2))
        assertTrue(pos1.isWithinBounds(5, 5))
        assertFalse(Position(5, 5).isWithinBounds(5, 5))
    }

    @Test
    fun testPositionConversions() {
        val positions = listOf(Position(1, 2), Position(3, 4))

        val polyanets = positions.toPolyanets()
        assertEquals(2, polyanets.size)
        assertEquals(Position(1, 2), polyanets[0].position)

        val soloons = positions.toSoloons(SoloonColor.RED)
        assertEquals(2, soloons.size)
        assertEquals(SoloonColor.RED, soloons[0].color)

        val comeths = positions.toComeths(ComethDirection.LEFT)
        assertEquals(2, comeths.size)
        assertEquals(ComethDirection.LEFT, comeths[0].direction)
    }

    @Test
    fun testFilterWithinBounds() {
        val positions = listOf(
            Position(0, 0),   // Valid
            Position(1, 1),   // Valid
            Position(5, 5),   // Invalid (out of bounds)
            Position(-1, 2),  // Invalid (negative)
            Position(2, -1)   // Invalid (negative)
        )

        val validPositions = positions.filterWithinBounds(5, 5)
        assertEquals(2, validPositions.size)
        assertTrue(validPositions.contains(Position(0, 0)))
        assertTrue(validPositions.contains(Position(1, 1)))
    }

    @Test
    fun testAdjacentPositions() {
        val center = Position(2, 2)
        val adjacent = center.getAdjacentPositions(5, 5)

        assertEquals(4, adjacent.size)
        assertTrue(adjacent.contains(Position(1, 2))) // Up
        assertTrue(adjacent.contains(Position(3, 2))) // Down
        assertTrue(adjacent.contains(Position(2, 1))) // Left
        assertTrue(adjacent.contains(Position(2, 3))) // Right

        // Test corner case (should have fewer adjacent positions)
        val corner = Position(0, 0)
        val cornerAdjacent = corner.getAdjacentPositions(5, 5)
        assertEquals(2, cornerAdjacent.size)
    }

    @Test
    fun testMegaverseMapOperations() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET", "SPACE"),
            listOf("BLUE_SOLOON", "SPACE", "UP_COMETH"),
            listOf("SPACE", "SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)

        // Test dimensions
        assertEquals(3 to 3, map.dimensions)

        // Test getGoalAt
        assertEquals("POLYANET", map.getGoalAt(Position(0, 1)))
        assertEquals("BLUE_SOLOON", map.getGoalAt(Position(1, 0)))
        assertNull(map.getGoalAt(Position(5, 5))) // Out of bounds

        // Test position validation
        assertTrue(map.isValidPosition(Position(0, 0)))
        assertFalse(map.isValidPosition(Position(3, 3)))

        // Test getPositionsForGoal
        val polyanetPositions = map.getPositionsForGoal("POLYANET")
        assertEquals(1, polyanetPositions.size)
        assertEquals(Position(0, 1), polyanetPositions[0])
    }

    @Test
    fun testMegaverseResult() {
        val successResult = MegaverseResult.Success("test data")
        assertTrue(successResult.isSuccess)
        assertFalse(successResult.isError)
        assertEquals("test data", successResult.getOrNull())
        assertEquals("test data", successResult.getOrThrow())

        val errorResult = MegaverseResult.Error(MegaverseException.NetworkError("Network error"))
        assertFalse(errorResult.isSuccess)
        assertTrue(errorResult.isError)
        assertNull(errorResult.getOrNull())

        assertFailsWith<MegaverseException.NetworkError> {
            errorResult.getOrThrow()
        }
    }

    @Test
    fun testSdkConfiguration() {
        val sdk = MegaverseSdk()

        // Initially no candidate ID
        assertNull(sdk.getCandidateId())

        // Set candidate ID
        sdk.setCandidateId("test-candidate-123")
        assertEquals("test-candidate-123", sdk.getCandidateId())

        // Set custom base URL
        sdk.setBaseUrl("https://test-api.example.com")

        // Configuration should be valid now
        assertNotNull(sdk.getCandidateId())
    }
}