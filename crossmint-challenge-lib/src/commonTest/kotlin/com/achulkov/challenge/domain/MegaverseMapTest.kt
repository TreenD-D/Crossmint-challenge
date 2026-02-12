package com.achulkov.challenge.domain

import kotlin.test.*

/**
 * Tests for MegaverseMap domain model.
 */
class MegaverseMapTest {

    @Test
    fun testMapCreationAndDimensions() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET", "SPACE"),
            listOf("BLUE_SOLOON", "SPACE", "UP_COMETH"),
            listOf("SPACE", "SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)

        assertEquals(3 to 3, map.dimensions)
    }

    @Test
    fun testEmptyMapDimensions() {
        val emptyMap = MegaverseMap(emptyList())
        assertEquals(0 to 0, emptyMap.dimensions)

        val singleRowMap = MegaverseMap(listOf(emptyList()))
        assertEquals(1 to 0, singleRowMap.dimensions)
    }

    @Test
    fun testGetGoalAt() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET", "SPACE"),
            listOf("BLUE_SOLOON", "SPACE", "UP_COMETH"),
            listOf("SPACE", "SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)

        // Valid positions
        assertEquals("POLYANET", map.getGoalAt(Position(0, 1)))
        assertEquals("BLUE_SOLOON", map.getGoalAt(Position(1, 0)))
        assertEquals("UP_COMETH", map.getGoalAt(Position(1, 2)))
        assertEquals("SPACE", map.getGoalAt(Position(0, 0)))

        // Invalid positions return null
        assertNull(map.getGoalAt(Position(3, 0)))
        assertNull(map.getGoalAt(Position(0, 3)))
        assertNull(map.getGoalAt(Position(100, 100)))
    }

    @Test
    fun testIsValidPosition() {
        val goalMap = listOf(
            listOf("SPACE", "SPACE"),
            listOf("SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)

        assertTrue(map.isValidPosition(Position(0, 0)))
        assertTrue(map.isValidPosition(Position(1, 1)))
        assertFalse(map.isValidPosition(Position(2, 0)))
        assertFalse(map.isValidPosition(Position(0, 2)))
    }

    @Test
    fun testGetPositionsForGoal() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET", "SPACE"),
            listOf("POLYANET", "SPACE", "POLYANET"),
            listOf("SPACE", "SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)

        val polyanetPositions = map.getPositionsForGoal("POLYANET")

        assertEquals(3, polyanetPositions.size)
        assertTrue(polyanetPositions.contains(Position(0, 1)))
        assertTrue(polyanetPositions.contains(Position(1, 0)))
        assertTrue(polyanetPositions.contains(Position(1, 2)))
    }

    @Test
    fun testGetPolyanetPositions() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET", "SPACE"),
            listOf("BLUE_SOLOON", "SPACE", "UP_COMETH"),
            listOf("SPACE", "POLYANET", "SPACE")
        )

        val map = MegaverseMap(goalMap)
        val positions = map.getPolyanetPositions()

        assertEquals(2, positions.size)
        assertTrue(positions.contains(Position(0, 1)))
        assertTrue(positions.contains(Position(2, 1)))
    }

    @Test
    fun testGetSoloonPositions() {
        val goalMap = listOf(
            listOf("BLUE_SOLOON", "RED_SOLOON", "WHITE_SOLOON"),
            listOf("PURPLE_SOLOON", "SPACE", "SPACE"),
            listOf("SPACE", "SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)
        val soloonsByColor = map.getSoloonPositions()

        assertEquals(4, soloonsByColor.size)

        // Blue soloons
        assertEquals(1, soloonsByColor[SoloonColor.BLUE]?.size)
        assertTrue(soloonsByColor[SoloonColor.BLUE]?.contains(Position(0, 0)) ?: false)

        // Red soloons
        assertEquals(1, soloonsByColor[SoloonColor.RED]?.size)
        assertTrue(soloonsByColor[SoloonColor.RED]?.contains(Position(0, 1)) ?: false)

        // White soloons
        assertEquals(1, soloonsByColor[SoloonColor.WHITE]?.size)
        assertTrue(soloonsByColor[SoloonColor.WHITE]?.contains(Position(0, 2)) ?: false)

        // Purple soloons
        assertEquals(1, soloonsByColor[SoloonColor.PURPLE]?.size)
        assertTrue(soloonsByColor[SoloonColor.PURPLE]?.contains(Position(1, 0)) ?: false)
    }

    @Test
    fun testGetSoloonPositionsEmpty() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET"),
            listOf("POLYANET", "SPACE")
        )

        val map = MegaverseMap(goalMap)
        val soloonsByColor = map.getSoloonPositions()

        assertTrue(soloonsByColor.isEmpty())
    }

    @Test
    fun testGetComethPositions() {
        val goalMap = listOf(
            listOf("UP_COMETH", "DOWN_COMETH", "SPACE"),
            listOf("LEFT_COMETH", "SPACE", "RIGHT_COMETH"),
            listOf("SPACE", "SPACE", "SPACE")
        )

        val map = MegaverseMap(goalMap)
        val comethsByDirection = map.getComethPositions()

        assertEquals(4, comethsByDirection.size)

        // Up comeths
        assertEquals(1, comethsByDirection[ComethDirection.UP]?.size)
        assertTrue(comethsByDirection[ComethDirection.UP]?.contains(Position(0, 0)) ?: false)

        // Down comeths
        assertEquals(1, comethsByDirection[ComethDirection.DOWN]?.size)
        assertTrue(comethsByDirection[ComethDirection.DOWN]?.contains(Position(0, 1)) ?: false)

        // Left comeths
        assertEquals(1, comethsByDirection[ComethDirection.LEFT]?.size)
        assertTrue(comethsByDirection[ComethDirection.LEFT]?.contains(Position(1, 0)) ?: false)

        // Right comeths
        assertEquals(1, comethsByDirection[ComethDirection.RIGHT]?.size)
        assertTrue(comethsByDirection[ComethDirection.RIGHT]?.contains(Position(1, 2)) ?: false)
    }

    @Test
    fun testGetComethPositionsEmpty() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET"),
            listOf("POLYANET", "SPACE")
        )

        val map = MegaverseMap(goalMap)
        val comethsByDirection = map.getComethPositions()

        assertTrue(comethsByDirection.isEmpty())
    }

    @Test
    fun testComplexMap() {
        val goalMap = listOf(
            listOf("SPACE", "POLYANET", "SPACE", "POLYANET"),
            listOf("BLUE_SOLOON", "SPACE", "RED_SOLOON", "SPACE"),
            listOf("SPACE", "UP_COMETH", "SPACE", "DOWN_COMETH"),
            listOf("POLYANET", "SPACE", "POLYANET", "SPACE")
        )

        val map = MegaverseMap(goalMap)

        // Check dimensions
        assertEquals(4 to 4, map.dimensions)

        // Check polyanets
        val polyanets = map.getPolyanetPositions()
        assertEquals(4, polyanets.size)

        // Check soloons
        val soloons = map.getSoloonPositions()
        assertEquals(2, soloons.size)
        assertEquals(1, soloons[SoloonColor.BLUE]?.size)
        assertEquals(1, soloons[SoloonColor.RED]?.size)

        // Check comeths
        val comeths = map.getComethPositions()
        assertEquals(2, comeths.size)
        assertEquals(1, comeths[ComethDirection.UP]?.size)
        assertEquals(1, comeths[ComethDirection.DOWN]?.size)
    }

    @Test
    fun testMapWithDifferentRowLengths() {
        val goalMap = listOf(
            listOf("POLYANET", "SPACE"),
            listOf("SPACE", "POLYANET", "BLUE_SOLOON"),
            listOf("UP_COMETH")
        )

        val map = MegaverseMap(goalMap)

        // Dimensions should be based on first row
        assertEquals(3 to 2, map.dimensions)

        // Valid position
        assertEquals("POLYANET", map.getGoalAt(Position(0, 0)))

        // Position out of bounds for first row but in bounds for second
        assertNull(map.getGoalAt(Position(1, 2)))
    }
}
