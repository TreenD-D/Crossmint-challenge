package com.achulkov.challenge.extensions

import com.achulkov.challenge.domain.*

/**
 * Extension functions for common Megaverse patterns and utilities.
 */

/**
 * Creates positions for an X-shaped pattern within the given dimensions.
 *
 * @param size The size of the square grid (e.g., 11 for an 11x11 grid)
 * @return List of positions forming an X pattern
 */
fun createXPattern(size: Int): List<Position> {
    require(size > 0) { "Size must be positive" }

    val positions = mutableListOf<Position>()

    for (i in 0 until size) {
        // Main diagonal (top-left to bottom-right)
        positions.add(Position(i, i))

        // Anti-diagonal (top-right to bottom-left)
        positions.add(Position(i, size - 1 - i))
    }

    // Remove duplicates (center position if size is odd)
    return positions.distinct()
}

/**
 * Creates polyanets for an X-shaped pattern.
 *
 * @param size The size of the square grid
 * @return List of Polyanet objects forming an X pattern
 */
fun createXPatternPolyanets(size: Int): List<Polyanet> {
    return createXPattern(size).map { Polyanet(it) }
}

/**
 * Creates positions for a border pattern around the given dimensions.
 *
 * @param width The width of the grid
 * @param height The height of the grid
 * @return List of positions forming a border
 */
fun createBorderPattern(width: Int, height: Int): List<Position> {
    require(width > 0 && height > 0) { "Width and height must be positive" }

    val positions = mutableListOf<Position>()

    // Top and bottom edges
    for (col in 0 until width) {
        positions.add(Position(0, col))              // Top edge
        if (height > 1) {
            positions.add(Position(height - 1, col)) // Bottom edge
        }
    }

    // Left and right edges (excluding corners already added)
    for (row in 1 until height - 1) {
        positions.add(Position(row, 0))              // Left edge
        if (width > 1) {
            positions.add(Position(row, width - 1))  // Right edge
        }
    }

    return positions
}

/**
 * Creates positions for a plus (+) pattern centered in the given dimensions.
 *
 * @param width The width of the grid
 * @param height The height of the grid
 * @return List of positions forming a plus pattern
 */
fun createPlusPattern(width: Int, height: Int): List<Position> {
    require(width > 0 && height > 0) { "Width and height must be positive" }

    val positions = mutableListOf<Position>()
    val centerRow = height / 2
    val centerCol = width / 2

    // Horizontal line
    for (col in 0 until width) {
        positions.add(Position(centerRow, col))
    }

    // Vertical line
    for (row in 0 until height) {
        positions.add(Position(row, centerCol))
    }

    return positions.distinct()
}

/**
 * Validates that a position is within the given bounds.
 *
 * @param width The maximum width
 * @param height The maximum height
 * @return true if the position is within bounds
 */
fun Position.isWithinBounds(width: Int, height: Int): Boolean {
    return row >= 0 && row < height && column >= 0 && column < width
}

/**
 * Filters a list of positions to only include those within the given bounds.
 *
 * @param width The maximum width
 * @param height The maximum height
 * @return List of positions within bounds
 */
fun List<Position>.filterWithinBounds(width: Int, height: Int): List<Position> {
    return filter { it.isWithinBounds(width, height) }
}

/**
 * Converts a list of positions to polyanets.
 */
fun List<Position>.toPolyanets(): List<Polyanet> = map { Polyanet(it) }

/**
 * Converts a list of positions to soloons with the specified color.
 *
 * @param color The color for all soloons
 */
fun List<Position>.toSoloons(color: SoloonColor): List<Soloon> = map { Soloon(it, color) }

/**
 * Converts a list of positions to comeths with the specified direction.
 *
 * @param direction The direction for all comeths
 */
fun List<Position>.toComeths(direction: ComethDirection): List<Cometh> =
    map { Cometh(it, direction) }

/**
 * Gets the Manhattan distance between two positions.
 *
 * @param other The other position
 * @return The Manhattan distance
 */
fun Position.manhattanDistanceTo(other: Position): Int {
    return kotlin.math.abs(row - other.row) + kotlin.math.abs(column - other.column)
}

/**
 * Gets all adjacent positions (up, down, left, right) within the given bounds.
 *
 * @param width The maximum width
 * @param height The maximum height
 * @return List of adjacent positions within bounds
 */
fun Position.getAdjacentPositions(width: Int, height: Int): List<Position> {
    val adjacent = listOf(
        Position(row - 1, column), // Up
        Position(row + 1, column), // Down
        Position(row, column - 1), // Left
        Position(row, column + 1)  // Right
    )
    return adjacent.filterWithinBounds(width, height)
}