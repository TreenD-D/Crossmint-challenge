package com.achulkov.challenge.domain

import kotlinx.serialization.Serializable

/**
 * Represents a megaverse map with its dimensions and objects.
 *
 * @param goal The 2D array representing the goal state of the map
 */
@Serializable
data class MegaverseMap(
    val goal: List<List<String>>
) {
    /**
     * The dimensions of the map.
     */
    val dimensions: Pair<Int, Int> = goal.size to (goal.firstOrNull()?.size ?: 0)

    /**
     * Gets the goal value at the specified position.
     *
     * @param position The position to check
     * @return The goal value at the position, or null if position is invalid
     */
    fun getGoalAt(position: Position): String? {
        return if (isValidPosition(position)) {
            goal[position.row][position.column]
        } else null
    }

    /**
     * Checks if the given position is valid within the map bounds.
     *
     * @param position The position to validate
     * @return True if the position is within bounds, false otherwise
     */
    fun isValidPosition(position: Position): Boolean {
        return position.row in goal.indices &&
                position.column in 0 until (goal.firstOrNull()?.size ?: 0)
    }

    /**
     * Returns all positions that should contain the specified goal type.
     *
     * @param goalType The type of goal to find (e.g., "POLYANET", "BLUE_SOLOON", etc.)
     * @return List of positions where this goal type should be placed
     */
    fun getPositionsForGoal(goalType: String): List<Position> {
        val positions = mutableListOf<Position>()
        goal.forEachIndexed { row, columns ->
            columns.forEachIndexed { column, value ->
                if (value == goalType) {
                    positions.add(Position(row, column))
                }
            }
        }
        return positions
    }

    /**
     * Returns all polyanet positions in the goal map.
     */
    fun getPolyanetPositions(): List<Position> = getPositionsForGoal("POLYANET")

    /**
     * Returns all soloon positions grouped by color.
     */
    fun getSoloonPositions(): Map<SoloonColor, List<Position>> {
        return SoloonColor.values().associateWith { color ->
            getPositionsForGoal("${color.name}_SOLOON")
        }.filterValues { it.isNotEmpty() }
    }

    /**
     * Returns all cometh positions grouped by direction.
     */
    fun getComethPositions(): Map<ComethDirection, List<Position>> {
        return ComethDirection.values().associateWith { direction ->
            getPositionsForGoal("${direction.name}_COMETH")
        }.filterValues { it.isNotEmpty() }
    }
}