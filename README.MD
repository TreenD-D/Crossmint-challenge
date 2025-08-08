# Crossmint Megaverse SDK

A robust, scalable Kotlin Multiplatform library for interacting with the Crossmint Megaverse API.
This SDK provides a clean, type-safe interface for creating and managing astral objects in the
megaverse challenge.

## 🌟 Features

- **Type-safe API**: Strongly typed domain models with validation
- **Kotlin Coroutines & Flow**: Asynchronous operations with progress tracking
- **Robust Error Handling**: Comprehensive error types and result wrappers
- **Rate Limiting**: Built-in delay mechanisms to prevent API rate limiting
- **Pattern Generation**: Built-in functions for common patterns (X-shape, borders, plus)
- **Extensible Architecture**: Repository pattern with clean abstractions
- **Multiplatform**: Works on Android, iOS, JVM, and more
- **Comprehensive Testing**: Full unit test coverage

## 🏗️ Architecture

The SDK follows clean architecture principles with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MegaverseSdk  │───▶│ MegaverseRepo   │───▶│ MegaverseApi    │
│   (Public API)  │    │  (Business      │    │   (Network      │
│                 │    │   Logic)        │    │    Layer)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Extensions    │    │   Domain        │    │   Config        │
│   (Utilities)   │    │   Models        │    │ (Settings)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📦 Installation

Add the library to your project:

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.achulkov.challenge:crossmint-challenge-lib:1.0.0")
}
```

## 🚀 Quick Start

### Basic Setup

```kotlin
import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.domain.*

// Initialize the SDK
val sdk = MegaverseSdk()

// Configure your candidate ID (required)
sdk.setCandidateId("your-candidate-id-here")
```

### Phase 1: X-Pattern Challenge

```kotlin
// Create the X-pattern for Phase 1 (11x11 grid)
sdk.createXPatternChallenge(11).collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> 
            println("Progress: ${progress.completed}/${progress.total}")
        is CreationProgress.ObjectCreated -> 
            println("✅ Created polyanet at ${progress.astralObject.position}")
        is CreationProgress.ObjectFailed -> 
            println("❌ Failed: ${progress.error.message}")
        is CreationProgress.Completed -> 
            println("🎉 Complete! ${progress.successful} successful, ${progress.failed} failed")
    }
}
```

### Individual Object Creation

```kotlin
// Create a single polyanet
val result = sdk.createPolyanet(Position(5, 5))
if (result.isSuccess) {
    println("Polyanet created successfully!")
} else {
    println("Error: ${result.getOrNull()}")
}

// Create a soloon with color
sdk.createSoloon(Position(3, 3), SoloonColor.BLUE)

// Create a cometh with direction  
sdk.createCometh(Position(7, 2), ComethDirection.UP)
```

### Advanced Usage: Solve Complete Challenge

```kotlin
// Automatically solve the entire challenge based on goal map
sdk.solveChallenge().collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> {
            println("Creating objects: ${progress.completed}/${progress.total}")
        }
        is CreationProgress.ObjectCreated -> {
            val obj = progress.astralObject
            val type = obj::class.simpleName
            println("✅ Created $type at (${obj.position.row}, ${obj.position.column})")
        }
        is CreationProgress.Completed -> {
            println("🚀 Challenge solved! Success: ${progress.successful}, Failed: ${progress.failed}")
        }
    }
}
```

## 🎯 Core Concepts

### Domain Models

```kotlin
// Position in the megaverse (0-based coordinates)
val position = Position(row = 2, column = 5)

// Astral Objects
val polyanet = Polyanet(position)
val soloon = Soloon(position, SoloonColor.PURPLE)
val cometh = Cometh(position, ComethDirection.LEFT)

// Colors and Directions
enum class SoloonColor { BLUE, RED, PURPLE, WHITE }
enum class ComethDirection { UP, DOWN, LEFT, RIGHT }
```

### Result Handling

```kotlin
// All operations return MegaverseResult<T>
when (val result = sdk.createPolyanet(position)) {
    is MegaverseResult.Success -> {
        // Operation succeeded
        println("Success!")
    }
    is MegaverseResult.Error -> {
        // Handle specific error types
        when (result.exception) {
            is MegaverseException.NetworkError -> println("Network issue")
            is MegaverseException.ApiError -> println("API error: ${result.exception.statusCode}")
            is MegaverseException.InvalidCandidateId -> println("Invalid candidate ID")
            else -> println("Unknown error: ${result.exception.message}")
        }
    }
}
```

### Progress Tracking

```kotlin
// Bulk operations provide real-time progress
sdk.createAstralObjects(listOfObjects).collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> updateProgressBar(progress.completed, progress.total)
        is CreationProgress.ObjectCreated -> logSuccess(progress.astralObject)
        is CreationProgress.ObjectFailed -> logError(progress.astralObject, progress.error)
        is CreationProgress.Completed -> showSummary(progress.successful, progress.failed)
    }
}
```

## 🛠️ Advanced Features

### Pattern Generation

```kotlin
// Generate common patterns
val xPattern = createXPattern(11)                    // X-shape
val borderPattern = createBorderPattern(10, 10)     // Border outline
val plusPattern = createPlusPattern(7, 7)           // Plus/cross shape

// Convert positions to objects
val polyanets = xPattern.toPolyanets()
val blueSoloons = borderPattern.toSoloons(SoloonColor.BLUE)
val upComeths = plusPattern.toComeths(ComethDirection.UP)
```

### Custom Patterns

```kotlin
// Create custom patterns with function builders
sdk.createBorderPattern(15, 15) { position ->
    // Create different objects based on position
    when {
        position.row == 0 || position.row == 14 -> Polyanet(position)
        position.column == 0 || position.column == 14 -> 
            Soloon(position, SoloonColor.RED)
        else -> Cometh(position, ComethDirection.UP)
    }
}.collect { progress -> /* handle progress */ }
```

### Goal Map Integration

```kotlin
// Fetch and analyze the goal map
val goalMap = sdk.getGoalMap().getOrThrow()

// Get specific object positions
val polyanetPositions = goalMap.getPolyanetPositions()
val soloonsByColor = goalMap.getSoloonPositions()
val comethsByDirection = goalMap.getComethPositions()

// Check map properties
println("Map dimensions: ${goalMap.dimensions}")
println("Goal at (5,5): ${goalMap.getGoalAt(Position(5, 5))}")
```

### Cleanup Operations

```kotlin
// Clear specific positions
sdk.clearPositions(listOf(Position(1, 1), Position(2, 2))).collect { progress ->
    when (progress) {
        is DeletionProgress.PositionCleared -> println("Cleared ${progress.position}")
        is DeletionProgress.PositionFailed -> println("Failed to clear ${progress.position}")
        is DeletionProgress.Completed -> println("Cleanup complete")
    }
}

// Clear entire megaverse
sdk.clearEntireMegaverse(width = 11, height = 11)
```

## 🧪 Testing

Run the comprehensive test suite:

```bash
./gradlew test
```

The SDK includes extensive unit tests covering:

- Domain model validation
- Pattern generation algorithms
- Extension function utilities
- Error handling scenarios
- Configuration management

## 🔧 Configuration

### Debug Logging

The SDK includes comprehensive logging that can be enabled for debugging and development:

```kotlin
// Enable debug logging during SDK initialization
val sdk = MegaverseSdk(enableDebugLogging = true)

// Or enable/disable at runtime
sdk.setDebugLogging(true)  // Enable for debugging
sdk.setDebugLogging(false) // Disable for production
```

Debug logs include:

- **Network Requests & Responses**: Full HTTP request/response details with timing
- **API Operations**: Individual object creation/deletion attempts
- **Bulk Operations**: Progress tracking for batch operations
- **Configuration Changes**: Candidate ID and settings updates
- **Error Details**: Detailed error information with stack traces

⚠️ **Important**: Debug logging should only be enabled in debug builds to avoid performance impact
and log noise in production.

### Custom HTTP Client

```kotlin
// Provide custom HTTP client for testing or configuration
val customClient = HttpClient {
    install(Logging) { level = LogLevel.ALL }
    // ... other configurations
}

val sdk = MegaverseSdk(httpClient = customClient)
```

### Custom Settings Storage

```kotlin
// Use custom settings for configuration persistence
val customSettings = Settings() // Your multiplatform settings instance
val config = MegaverseConfig(customSettings)
val sdk = MegaverseSdk(config = config)
```

### Rate Limiting Configuration

```kotlin
// Customize delay between requests (default: 500ms)
val repository = MegaverseRepositoryImpl(api, config, delayBetweenRequests = 1000L)
```

## 🏛️ Design Principles

### Clean Architecture

- **Separation of Concerns**: Clear boundaries between layers
- **Dependency Inversion**: Abstractions don't depend on details
- **Repository Pattern**: Clean data access abstraction
- **Result Pattern**: Explicit error handling without exceptions

### Scalability

- **Extension Functions**: Easy to add new patterns and utilities
- **Sealed Classes**: Type-safe error handling and progress tracking
- **Flow-based Operations**: Reactive, cancellable operations
- **Modular Design**: Easy to extend with new object types

### Reliability

- **Rate Limiting**: Built-in protection against API limits
- **Robust Error Handling**: Comprehensive error types and recovery
- **Input Validation**: Fail-fast validation with clear error messages
- **Progress Tracking**: Real-time feedback for long-running operations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
