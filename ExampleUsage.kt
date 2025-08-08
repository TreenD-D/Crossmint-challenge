package com.achulkov.challenge.examples

import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.domain.Position
import com.achulkov.challenge.domain.SoloonColor
import com.achulkov.challenge.domain.ComethDirection
import com.achulkov.challenge.extensions.createXPattern
import com.achulkov.challenge.repository.CreationProgress
import kotlinx.coroutines.runBlocking

/**
 * Example usage of the Crossmint Megaverse SDK
 *
 * This demonstrates how to solve the challenge step by step:
 * 1. Configure the SDK with your candidate ID and enable debug logging
 * 2. Phase 1: Create X-pattern with polyanets
 * 3. Advanced: Solve the complete challenge automatically
 * 4. Error handling and debugging
 */
fun main() = runBlocking {

    // Step 1: Initialize SDK with debug logging enabled (for development/testing)
    val sdk = MegaverseSdk(enableDebugLogging = true)
    sdk.setCandidateId("YOUR_CANDIDATE_ID_HERE") // Replace with your actual candidate ID

    println("🚀 Crossmint Megaverse Challenge")
    println("🐛 Debug logging enabled - check console for detailed logs")
    println("Candidate ID: ${sdk.getCandidateId()}")

    // You can also enable/disable debug logging at runtime
    // sdk.setDebugLogging(false) // Disable for production

    // Step 2: Test single polyanet creation with error handling
    println("\n🪐 Testing single polyanet creation...")

    val singlePolyanetResult = sdk.createPolyanet(Position(1, 1))
    when (singlePolyanetResult) {
        is com.achulkov.challenge.domain.MegaverseResult.Success -> {
            println("✅ Single polyanet created successfully!")
        }

        is com.achulkov.challenge.domain.MegaverseResult.Error -> {
            println("❌ Failed to create polyanet: ${singlePolyanetResult.exception.message}")
            when (singlePolyanetResult.exception) {
                is com.achulkov.challenge.domain.MegaverseException.ApiError -> {
                    println("   API Error Code: ${singlePolyanetResult.exception.statusCode}")
                }

                is com.achulkov.challenge.domain.MegaverseException.NetworkError -> {
                    println("   Network issue - check connection")
                }

                is com.achulkov.challenge.domain.MegaverseException.InvalidCandidateId -> {
                    println("   Invalid candidate ID - please check your ID")
                }

                else -> {
                    println("   Unknown error occurred")
                }
            }
        }
    }

    // Step 3: Phase 1 - Create X-pattern (11x11 grid)
    println("\n📍 Phase 1: Creating X-pattern...")

    sdk.createXPatternChallenge(11).collect { progress ->
        when (progress) {
            is CreationProgress.InProgress -> {
                println("⏳ Progress: ${progress.completed}/${progress.total}")
            }

            is CreationProgress.ObjectCreated -> {
                val pos = progress.astralObject.position
                println("✅ Polyanet created at (${pos.row}, ${pos.column})")
            }

            is CreationProgress.ObjectFailed -> {
                val pos = progress.astralObject.position
                println("❌ Failed at (${pos.row}, ${pos.column}): ${progress.error.message}")
            }

            is CreationProgress.Completed -> {
                println("🎉 X-Pattern complete!")
                println("   Successful: ${progress.successful}")
                println("   Failed: ${progress.failed}")
            }
        }
    }

    // Step 4: Individual object creation examples
    println("\n🪐 Creating individual objects...")

    // Create a single polyanet
    val polyanetResult = sdk.createPolyanet(Position(1, 1))
    if (polyanetResult.isSuccess) {
        println("✅ Single polyanet created at (1, 1)")
    } else {
        println("❌ Failed to create polyanet: ${polyanetResult.getOrNull()}")
    }

    // Create a soloon with color
    val soloonResult = sdk.createSoloon(Position(2, 2), SoloonColor.BLUE)
    if (soloonResult.isSuccess) {
        println("✅ Blue soloon created at (2, 2)")
    }

    // Create a cometh with direction
    val comethResult = sdk.createCometh(Position(3, 3), ComethDirection.UP)
    if (comethResult.isSuccess) {
        println("✅ Up-facing cometh created at (3, 3)")
    }

    // Step 5: Fetch and analyze goal map
    println("\n🗺️  Fetching goal map...")

    val goalMapResult = sdk.getGoalMap()
    when (goalMapResult) {
        is com.achulkov.challenge.domain.MegaverseResult.Success -> {
            val map = goalMapResult.data
            println("✅ Goal map retrieved!")
            println("   Dimensions: ${map.dimensions}")
            println("   Polyanets needed: ${map.getPolyanetPositions().size}")
            println("   Soloons by color: ${map.getSoloonPositions().mapValues { it.value.size }}")
            println(
                "   Comeths by direction: ${
                    map.getComethPositions().mapValues { it.value.size }
                }"
            )
        }

        is com.achulkov.challenge.domain.MegaverseResult.Error -> {
            println("❌ Failed to get goal map: ${goalMapResult.exception.message}")
        }
    }

    // Step 6: Solve the complete challenge automatically
    println("\n🚀 Solving complete challenge...")

    sdk.solveChallenge().collect { progress ->
        when (progress) {
            is CreationProgress.InProgress -> {
                println("⏳ Creating objects: ${progress.completed}/${progress.total}")
            }

            is CreationProgress.ObjectCreated -> {
                val obj = progress.astralObject
                val type = obj::class.simpleName
                println("✅ $type created at (${obj.position.row}, ${obj.position.column})")
            }

            is CreationProgress.ObjectFailed -> {
                val pos = progress.astralObject.position
                println("❌ Failed at (${pos.row}, ${pos.column}): ${progress.error.message}")
            }

            is CreationProgress.Completed -> {
                println("🎊 Challenge completed!")
                println("   Total successful: ${progress.successful}")
                println("   Total failed: ${progress.failed}")
                println("   Success rate: ${(progress.successful * 100) / (progress.successful + progress.failed)}%")
            }
        }
    }

    println("\n✨ All done! Check the Crossmint website to see your megaverse!")
}

/**
 * Alternative approach: Manual step-by-step solution
 */
suspend fun manualSolution() {
    val sdk = MegaverseSdk()
    sdk.setCandidateId("YOUR_CANDIDATE_ID_HERE")

    // Get the goal map first
    val goalMap = sdk.getGoalMap().getOrThrow()

    // Create polyanets
    val polyanetPositions = goalMap.getPolyanetPositions()
    println("Creating ${polyanetPositions.size} polyanets...")

    for (position in polyanetPositions) {
        val result = sdk.createPolyanet(position)
        if (result.isSuccess) {
            println("✅ Polyanet at (${position.row}, ${position.column})")
        } else {
            println("❌ Failed polyanet at (${position.row}, ${position.column})")
        }
        // Add delay to avoid rate limiting
        kotlinx.coroutines.delay(500)
    }

    // Create soloons by color
    goalMap.getSoloonPositions().forEach { (color, positions) ->
        println("Creating ${positions.size} ${color.name.lowercase()} soloons...")

        for (position in positions) {
            val result = sdk.createSoloon(position, color)
            if (result.isSuccess) {
                println("✅ ${color.name} soloon at (${position.row}, ${position.column})")
            }
            kotlinx.coroutines.delay(500)
        }
    }

    // Create comeths by direction
    goalMap.getComethPositions().forEach { (direction, positions) ->
        println("Creating ${positions.size} ${direction.name.lowercase()}-facing comeths...")

        for (position in positions) {
            val result = sdk.createCometh(position, direction)
            if (result.isSuccess) {
                println("✅ ${direction.name} cometh at (${position.row}, ${position.column})")
            }
            kotlinx.coroutines.delay(500)
        }
    }

    println("Manual solution completed!")
}

/**
 * Utility: Generate X-pattern positions for reference
 */
fun printXPattern(size: Int = 11) {
    println("X-pattern positions for ${size}x${size} grid:")
    val positions = createXPattern(size)

    // Create a visual representation
    val grid = Array(size) { Array(size) { "🌌" } }
    positions.forEach { pos ->
        grid[pos.row][pos.column] = "🪐"
    }

    grid.forEach { row ->
        println(row.joinToString(""))
    }

    println("\nTotal positions: ${positions.size}")
    println("Positions: ${positions.joinToString(", ") { "(${it.row},${it.column})" }}")
}