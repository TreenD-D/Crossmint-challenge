package com.achulkov.challenge.extensions

import com.achulkov.challenge.domain.*
import kotlin.test.*

/**
 * Tests for Megaverse extension functions.
 */
class MegaverseExtensionsTest {

    @Test
    fun testCreateXPattern() {
        val pattern = createXPattern(5)

        // Should have 9 positions (5 + 5 - 1 for center overlap)
        assertEquals(9, pattern.size)

        // Check diagonal positions
        assertTrue(pattern.contains(Position(0, 0))) // Top-left
        assertTrue(pattern.contains(Position(1, 1)))
        assertTrue(pattern.contains(Position(2, 2))) // Center
        assertTrue(pattern.contains(Position(3, 3)))
        assertTrue(pattern.contains(Position(4, 4))) // Bottom-right

        // Check anti-diagonal positions
        assertTrue(pattern.contains(Position(0, 4))) // Top-right
        assertTrue(pattern.contains(Position(1, 3)))
        assertTrue(pattern.contains(Position(3, 1)))
        assertTrue(pattern.contains(Position(4, 0))) // Bottom-left
    }

    @Test
    fun testCreateXPatternWithOddSize() {
        val pattern = createXPattern(11)

        // Should have 21 positions (11 + 11 - 1 for center overlap)
        assertEquals(21, pattern.size)

        // Check corners
        assertTrue(pattern.contains(Position(0, 0)))
        assertTrue(pattern.contains(Position(10, 10)))
        assertTrue(pattern.contains(Position(0, 10)))
        assertTrue(pattern.contains(Position(10, 0)))

        // Check center
        assertTrue(pattern.contains(Position(5, 5)))
    }

    @Test
    fun testCreateXPatternWithEvenSize() {
        val pattern = createXPattern(4)

        // Should have 8 positions (4 + 4, no overlap since center is between positions)
        assertEquals(8, pattern.size)
    }

    @Test
    fun testCreateXPatternRequiresPositiveSize() {
        assertFailsWith<IllegalArgumentException> {
            createXPattern(0)
        }
        assertFailsWith<IllegalArgumentException> {
            createXPattern(-1)
        }
    }

    @Test
    fun testCreateXPatternPolyanets() {
        val polyanets = createXPatternPolyanets(5)

        assertEquals(9, polyanets.size)
        assertTrue(polyanets.all { it is Polyanet })

        // Check positions
        val positions = polyanets.map { it.position }
        assertTrue(positions.contains(Position(0, 0)))
        assertTrue(positions.contains(Position(2, 2)))
    }

    @Test
    fun testCreateBorderPattern() {
        val pattern = createBorderPattern(5, 3)

        // Should have 12 positions:
        // Top row: (0,0), (0,1), (0,2), (0,3), (0,4) = 5
        // Bottom row: (2,0), (2,1), (2,2), (2,3), (2,4) = 5
        // Left edge (excluding corners): (1,0) = 1
        // Right edge (excluding corners): (1,4) = 1
        // Total: 5 + 5 + 1 + 1 = 12
        assertEquals(12, pattern.size)

        // Check corners
        assertTrue(pattern.contains(Position(0, 0)))
        assertTrue(pattern.contains(Position(0, 4)))
        assertTrue(pattern.contains(Position(2, 0)))
        assertTrue(pattern.contains(Position(2, 4)))

        // Check edges
        assertTrue(pattern.contains(Position(0, 2)))
        assertTrue(pattern.contains(Position(2, 2)))
        assertTrue(pattern.contains(Position(1, 0)))
        assertTrue(pattern.contains(Position(1, 4)))

        // Check interior is not included
        assertFalse(pattern.contains(Position(1, 1)))
        assertFalse(pattern.contains(Position(1, 2)))
        assertFalse(pattern.contains(Position(1, 3)))
    }

    @Test
    fun testCreateBorderPatternWithSingleCell() {
        val pattern = createBorderPattern(1, 1)

        // Single cell border is just that cell
        assertEquals(1, pattern.size)
        assertTrue(pattern.contains(Position(0, 0)))
    }

    @Test
    fun testCreateBorderPatternRequiresPositiveDimensions() {
        assertFailsWith<IllegalArgumentException> {
            createBorderPattern(0, 5)
        }
        assertFailsWith<IllegalArgumentException> {
            createBorderPattern(5, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            createBorderPattern(-1, -1)
        }
    }

    @Test
    fun testCreatePlusPattern() {
        val pattern = createPlusPattern(5, 5)

        // Should have 9 positions (5 + 5 - 1 for center overlap)
        assertEquals(9, pattern.size)

        // Check center
        assertTrue(pattern.contains(Position(2, 2)))

        // Check horizontal line
        assertTrue(pattern.contains(Position(2, 0)))
        assertTrue(pattern.contains(Position(2, 1)))
        assertTrue(pattern.contains(Position(2, 3)))
        assertTrue(pattern.contains(Position(2, 4)))

        // Check vertical line
        assertTrue(pattern.contains(Position(0, 2)))
        assertTrue(pattern.contains(Position(1, 2)))
        assertTrue(pattern.contains(Position(3, 2)))
        assertTrue(pattern.contains(Position(4, 2)))
    }

    @Test
    fun testCreatePlusPatternRequiresPositiveDimensions() {
        assertFailsWith<IllegalArgumentException> {
            createPlusPattern(0, 5)
        }
        assertFailsWith<IllegalArgumentException> {
            createPlusPattern(5, 0)
        }
    }

    @Test
    fun testPositionIsWithinBounds() {
        val position = Position(2, 3)

        assertTrue(position.isWithinBounds(5, 5))
        assertTrue(position.isWithinBounds(4, 3))  // width=4, height=3 allows column 3 and row 2
        assertTrue(position.isWithinBounds(100, 100))

        assertFalse(position.isWithinBounds(2, 5)) // row 2 not < height 2 (boundary)
        assertFalse(position.isWithinBounds(3, 3)) // column 3 not < width 3 (boundary)
        assertFalse(position.isWithinBounds(1, 1)) // completely out
    }

    @Test
    fun testPositionIsWithinBoundsEdgeCases() {
        assertTrue(Position(0, 0).isWithinBounds(1, 1))
        assertTrue(Position(4, 4).isWithinBounds(5, 5))
        assertFalse(Position(5, 5).isWithinBounds(5, 5)) // boundary is exclusive
    }

    @Test
    fun testFilterWithinBounds() {
        val positions = listOf(
            Position(0, 0),   // Valid
            Position(1, 1),   // Valid
            Position(2, 2),   // Valid
            Position(5, 0),   // Invalid (row >= height)
            Position(0, 5)    // Invalid (column >= width)
        )

        val validPositions = positions.filterWithinBounds(5, 5)

        assertEquals(3, validPositions.size)
        assertTrue(validPositions.contains(Position(0, 0)))
        assertTrue(validPositions.contains(Position(1, 1)))
        assertTrue(validPositions.contains(Position(2, 2)))
    }

    @Test
    fun testFilterWithinBoundsEmptyList() {
        val emptyList = emptyList<Position>()
        assertTrue(emptyList.filterWithinBounds(5, 5).isEmpty())
    }

    @Test
    fun testPositionsToPolyanets() {
        val positions = listOf(
            Position(0, 0),
            Position(1, 1),
            Position(2, 2)
        )

        val polyanets = positions.toPolyanets()

        assertEquals(3, polyanets.size)
        polyanets.forEachIndexed { index, polyanet ->
            assertEquals(positions[index], polyanet.position)
            assertTrue(polyanet is Polyanet)
        }
    }

    @Test
    fun testPositionsToSoloons() {
        val positions = listOf(
            Position(0, 0),
            Position(1, 1)
        )

        val blueSoloons = positions.toSoloons(SoloonColor.BLUE)
        val redSoloons = positions.toSoloons(SoloonColor.RED)

        assertEquals(2, blueSoloons.size)
        blueSoloons.forEach { assertEquals(SoloonColor.BLUE, it.color) }

        assertEquals(2, redSoloons.size)
        redSoloons.forEach { assertEquals(SoloonColor.RED, it.color) }
    }

    @Test
    fun testPositionsToComeths() {
        val positions = listOf(
            Position(0, 0),
            Position(1, 1)
        )

        val upComeths = positions.toComeths(ComethDirection.UP)
        val downComeths = positions.toComeths(ComethDirection.DOWN)

        assertEquals(2, upComeths.size)
        upComeths.forEach { assertEquals(ComethDirection.UP, it.direction) }

        assertEquals(2, downComeths.size)
        downComeths.forEach { assertEquals(ComethDirection.DOWN, it.direction) }
    }

    @Test
    fun testManhattanDistanceTo() {
        val pos1 = Position(0, 0)
        val pos2 = Position(3, 4)

        assertEquals(7, pos1.manhattanDistanceTo(pos2))
        assertEquals(7, pos2.manhattanDistanceTo(pos1)) // Symmetric
        assertEquals(0, pos1.manhattanDistanceTo(pos1)) // Same position
    }

    @Test
    fun testManhattanDistanceExamples() {
        assertEquals(0, Position(5, 5).manhattanDistanceTo(Position(5, 5)))
        assertEquals(1, Position(0, 0).manhattanDistanceTo(Position(0, 1)))
        assertEquals(1, Position(0, 0).manhattanDistanceTo(Position(1, 0)))
        assertEquals(2, Position(0, 0).manhattanDistanceTo(Position(1, 1)))
    }

    @Test
    fun testGetAdjacentPositions() {
        val center = Position(2, 2)
        val adjacent = center.getAdjacentPositions(5, 5)

        assertEquals(4, adjacent.size)
        assertTrue(adjacent.contains(Position(1, 2))) // Up
        assertTrue(adjacent.contains(Position(3, 2))) // Down
        assertTrue(adjacent.contains(Position(2, 1))) // Left
        assertTrue(adjacent.contains(Position(2, 3))) // Right
    }

    @Test
    fun testGetAdjacentPositionsCorner() {
        val corner = Position(0, 0)
        val adjacent = corner.getAdjacentPositions(5, 5)

        // Corner should have only 2 adjacent positions within bounds
        assertEquals(2, adjacent.size)
        assertTrue(adjacent.contains(Position(1, 0))) // Down
        assertTrue(adjacent.contains(Position(0, 1))) // Right
    }

    @Test
    fun testGetAdjacentPositionsEdge() {
        val edge = Position(0, 2)
        val adjacent = edge.getAdjacentPositions(5, 5)

        // Edge should have 3 adjacent positions
        assertEquals(3, adjacent.size)
        assertTrue(adjacent.contains(Position(1, 2))) // Down
        assertTrue(adjacent.contains(Position(0, 1))) // Left
        assertTrue(adjacent.contains(Position(0, 3))) // Right
    }

    @Test
    fun testGetAdjacentPositionsSingleCellGrid() {
        val position = Position(0, 0)
        val adjacent = position.getAdjacentPositions(1, 1)

        // No adjacent positions in a 1x1 grid
        assertEquals(0, adjacent.size)
    }

    @Test
    fun testEmptyListConversions() {
        val emptyList = emptyList<Position>()

        assertTrue(emptyList.toPolyanets().isEmpty())
        assertTrue(emptyList.toSoloons(SoloonColor.BLUE).isEmpty())
        assertTrue(emptyList.toComeths(ComethDirection.UP).isEmpty())
    }
}
