package com.achulkov.challenge

import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.di.KoinInitializer
import com.achulkov.challenge.repository.CreationProgress
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * JVM entry point for running the Crossmint Megaverse Challenge.
 *
 * This main function demonstrates how to use the SDK with environment-based configuration
 * for a production-ready setup.
 *
 * ## Running from Environment Variables
 *
 * ```bash
 * export MEGAVERSE_CANDIDATE_ID="your-candidate-id"
 * export MEGAVERSE_DEBUG_LOGGING="true"
 * ./gradlew :crossmint-challenge-lib:jvmRun
 * ```
 *
 * ## Running with System Properties
 *
 * ```bash
 * ./gradlew :crossmint-challenge-lib:jvmRun \
 *   -Dmegaverse.candidate.id="your-candidate-id" \
 *   -Dmegaverse.debug.logging="true"
 * ```
 */
fun main(args: Array<String>) = runBlocking {
    println("=".repeat(60))
    println("🚀 Crossmint Megaverse Challenge - JVM Runner")
    println("=".repeat(60))

    // Initialize configuration from environment
    val config = try {
        MegaverseConfig.fromEnvironment()
    } catch (e: IllegalStateException) {
        println("\n❌ Configuration Error: ${e.message}")
        println("\nPlease set the required environment variable:")
        println("  export MEGAVERSE_CANDIDATE_ID=\"your-candidate-id\"")
        println("\nOr use system properties:")
        println("  -Dmegaverse.candidate.id=\"your-candidate-id\"")
        println("\nOptional environment variables:")
        println("  MEGAVERSE_DEBUG_LOGGING=true")
        println("  MEGAVERSE_BASE_URL=https://custom-api.example.com")
        println("  MEGAVERSE_REQUEST_DELAY_MS=500")
        println("  MEGAVERSE_MAX_RETRIES=3")
        println("  MEGAVERSE_RETRY_BASE_DELAY_MS=1000")
        println("=".repeat(60))
        return@runBlocking
    }

    // Initialize Koin with the config from environment
    KoinInitializer.init(config)

    // Get SDK instance from Koin
    val sdkComponent = object : KoinComponent {}
    val sdk: MegaverseSdk by sdkComponent.inject()

    println("\n📋 Configuration:")
    println("  Candidate ID: ${sdk.getCandidateId()?.take(8)?.plus("****") ?: "Not set"}")

    println("\n🎯 Starting Challenge Solution...")
    println("-".repeat(60))

    var totalCreated = 0
    var totalFailed = 0

    try {
        sdk.solveChallenge().collect { progress ->
            when (progress) {
                is CreationProgress.InProgress -> {
                    if (progress.completed % 10 == 0 || progress.completed == progress.total) {
                        val percentage = (progress.completed * 100) / progress.total
                        print("\r  ⏳ Progress: ${progress.completed}/${progress.total} ($percentage%)")
                    }
                }

                is CreationProgress.ObjectCreated -> {
                    totalCreated++
                }

                is CreationProgress.ObjectFailed -> {
                    totalFailed++
                    val obj = progress.astralObject
                    val type = obj::class.simpleName
                    println("\n  ❌ Failed: $type at (${obj.position.row}, ${obj.position.column})")
                }

                is CreationProgress.Completed -> {
                    totalCreated = progress.successful
                    totalFailed = progress.failed
                    println("\n\n✅ Challenge Complete!")
                    println("  Total Successful: ${progress.successful}")
                    println("  Total Failed: ${progress.failed}")

                    val successRate = if (progress.successful + progress.failed > 0) {
                        (progress.successful * 100) / (progress.successful + progress.failed)
                    } else {
                        0
                    }
                    println("  Success Rate: $successRate%")
                }
            }
        }

        println("\n🎉 All done! Check the Crossmint website to see your megaverse!")

    } catch (e: Exception) {
        println("\n❌ Error during challenge execution: ${e.message}")
        e.printStackTrace()
    }

    println("=".repeat(60))
}

/**
 * Entry point for Phase 1 (X-Pattern) only.
 */
fun mainXPattern(args: Array<String>) = runBlocking {
    println("🚀 Crossmint Megaverse Challenge - Phase 1: X-Pattern")
    println("=".repeat(60))

    val config = try {
        MegaverseConfig.fromEnvironment()
    } catch (e: IllegalStateException) {
        println("\n❌ ${e.message}")
        println("\nSet MEGAVERSE_CANDIDATE_ID environment variable.")
        return@runBlocking
    }

    // Initialize Koin with the config from environment
    KoinInitializer.init(config)

    // Get SDK instance from Koin
    val sdkComponent = object : KoinComponent {}
    val sdk: MegaverseSdk by sdkComponent.inject()
    
    sdk.setDebugLogging(true)

    println("\n📍 Creating X-pattern (11x11 grid)...")

    sdk.createXPatternChallenge(11).collect { progress ->
        when (progress) {
            is CreationProgress.InProgress -> {
                val percentage = if (progress.total > 0) {
                    (progress.completed * 100) / progress.total
                } else 0
                print("\r  ⏳ Progress: ${progress.completed}/${progress.total} ($percentage%)")
            }

            is CreationProgress.ObjectFailed -> {
                println("\n  ❌ Failed at ${progress.astralObject.position}")
            }

            is CreationProgress.Completed -> {
                println("\n\n✅ X-Pattern Complete!")
                println("  Successful: ${progress.successful}")
                println("  Failed: ${progress.failed}")
            }

            else -> {}
        }
    }

    println("=".repeat(60))
}
