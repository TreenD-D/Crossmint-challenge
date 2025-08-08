package com.achulkov.challenge.domain

import kotlinx.serialization.Serializable

/**
 * Base sealed class for all astral objects in the megaverse.
 */
sealed class AstralObject {
    abstract val position: Position
}

/**
 * Represents a Polyanet in the megaverse.
 *
 * @param position The position of the polyanet
 */
@Serializable
data class Polyanet(
    override val position: Position
) : AstralObject()

/**
 * Available colors for Soloons.
 */
enum class SoloonColor(val value: String) {
    BLUE("blue"),
    RED("red"),
    PURPLE("purple"),
    WHITE("white")
}

/**
 * Represents a Soloon in the megaverse.
 *
 * @param position The position of the soloon
 * @param color The color of the soloon
 */
@Serializable
data class Soloon(
    override val position: Position,
    val color: SoloonColor
) : AstralObject()

/**
 * Available directions for Comeths.
 */
enum class ComethDirection(val value: String) {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right")
}

/**
 * Represents a Cometh in the megaverse.
 *
 * @param position The position of the cometh
 * @param direction The direction of the cometh
 */
@Serializable
data class Cometh(
    override val position: Position,
    val direction: ComethDirection
) : AstralObject()