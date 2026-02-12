package com.achulkov.challenge.domain

import kotlin.test.*

/**
 * Tests for AstralObject domain models.
 */
class AstralObjectTest {

    @Test
    fun testPolyanetCreation() {
        val position = Position(2, 3)
        val polyanet = Polyanet(position)

        assertEquals(position, polyanet.position)
        assertTrue(polyanet is AstralObject)
    }

    @Test
    fun testPolyanetEquality() {
        val position1 = Position(2, 3)
        val position2 = Position(2, 3)
        val position3 = Position(3, 2)

        val polyanet1 = Polyanet(position1)
        val polyanet2 = Polyanet(position2)
        val polyanet3 = Polyanet(position3)

        assertEquals(polyanet1, polyanet2)
        assertNotEquals(polyanet1, polyanet3)
        assertEquals(polyanet1.hashCode(), polyanet2.hashCode())
    }

    @Test
    fun testPolyanetCopy() {
        val polyanet = Polyanet(Position(1, 1))
        val copied = polyanet.copy(position = Position(2, 2))

        assertEquals(Position(1, 1), polyanet.position)
        assertEquals(Position(2, 2), copied.position)
    }

    @Test
    fun testSoloonCreation() {
        val position = Position(1, 4)
        val soloon = Soloon(position, SoloonColor.BLUE)

        assertEquals(position, soloon.position)
        assertEquals(SoloonColor.BLUE, soloon.color)
        assertEquals("blue", soloon.color.value)
        assertTrue(soloon is AstralObject)
    }

    @Test
    fun testAllSoloonColors() {
        val position = Position(0, 0)

        SoloonColor.values().forEach { color ->
            val soloon = Soloon(position, color)
            assertEquals(color, soloon.color)
            assertEquals(color.value, soloon.color.value)
        }
    }

    @Test
    fun testSoloonEquality() {
        val position = Position(1, 1)
        val soloon1 = Soloon(position, SoloonColor.RED)
        val soloon2 = Soloon(position, SoloonColor.RED)
        val soloon3 = Soloon(position, SoloonColor.BLUE)
        val soloon4 = Soloon(Position(2, 2), SoloonColor.RED)

        assertEquals(soloon1, soloon2)
        assertNotEquals(soloon1, soloon3) // Different color
        assertNotEquals(soloon1, soloon4) // Different position
    }

    @Test
    fun testComethCreation() {
        val position = Position(0, 0)
        val cometh = Cometh(position, ComethDirection.UP)

        assertEquals(position, cometh.position)
        assertEquals(ComethDirection.UP, cometh.direction)
        assertEquals("up", cometh.direction.value)
        assertTrue(cometh is AstralObject)
    }

    @Test
    fun testAllComethDirections() {
        val position = Position(0, 0)

        ComethDirection.values().forEach { direction ->
            val cometh = Cometh(position, direction)
            assertEquals(direction, cometh.direction)
            assertEquals(direction.value, cometh.direction.value)
        }
    }

    @Test
    fun testComethEquality() {
        val position = Position(1, 1)
        val cometh1 = Cometh(position, ComethDirection.LEFT)
        val cometh2 = Cometh(position, ComethDirection.LEFT)
        val cometh3 = Cometh(position, ComethDirection.RIGHT)
        val cometh4 = Cometh(Position(2, 2), ComethDirection.LEFT)

        assertEquals(cometh1, cometh2)
        assertNotEquals(cometh1, cometh3) // Different direction
        assertNotEquals(cometh1, cometh4) // Different position
    }

    @Test
    fun testAstralObjectSealedClassHierarchy() {
        val polyanet: AstralObject = Polyanet(Position(0, 0))
        val soloon: AstralObject = Soloon(Position(0, 0), SoloonColor.PURPLE)
        val cometh: AstralObject = Cometh(Position(0, 0), ComethDirection.DOWN)

        // Test exhaustiveness of when expression
        fun getTypeName(obj: AstralObject): String = when (obj) {
            is Polyanet -> "Polyanet"
            is Soloon -> "Soloon"
            is Cometh -> "Cometh"
        }

        assertEquals("Polyanet", getTypeName(polyanet))
        assertEquals("Soloon", getTypeName(soloon))
        assertEquals("Cometh", getTypeName(cometh))
    }

    @Test
    fun testPositionValidation() {
        // Valid positions
        assertEquals(5, Position(5, 3).row)
        assertEquals(3, Position(5, 3).column)
        assertEquals(0, Position(0, 0).row)
        assertEquals(0, Position(0, 0).column)

        // Invalid positions should throw
        assertFailsWith<IllegalArgumentException> { Position(-1, 5) }
        assertFailsWith<IllegalArgumentException> { Position(5, -1) }
        assertFailsWith<IllegalArgumentException> { Position(-1, -1) }
    }

    @Test
    fun testPositionEquality() {
        val pos1 = Position(1, 2)
        val pos2 = Position(1, 2)
        val pos3 = Position(2, 1)

        assertEquals(pos1, pos2)
        assertNotEquals(pos1, pos3)
        assertEquals(pos1.hashCode(), pos2.hashCode())
    }

    @Test
    fun testPositionToString() {
        val position = Position(5, 10)
        val str = position.toString()

        assertTrue(str.contains("5"))
        assertTrue(str.contains("10"))
    }

    @Test
    fun testPositionComponentFunctions() {
        val position = Position(3, 7)
        val (row, column) = position

        assertEquals(3, row)
        assertEquals(7, column)
    }

    @Test
    fun testPositionCopy() {
        val position = Position(1, 2)
        val copied = position.copy(row = 5)

        assertEquals(5, copied.row)
        assertEquals(2, copied.column)
    }
}
