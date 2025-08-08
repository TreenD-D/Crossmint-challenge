package com.achulkov.challenge.domain

import kotlinx.serialization.Serializable

/**
 * Represents a position in the megaverse map.
 *
 * @param row The row coordinate (0-based)
 * @param column The column coordinate (0-based)
 */
@Serializable
data class Position(
    val row: Int,
    val column: Int
) {
    init {
        require(row >= 0) { "Row must be non-negative" }
        require(column >= 0) { "Column must be non-negative" }
    }
}