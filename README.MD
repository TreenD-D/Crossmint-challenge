# Crossmint Megaverse SDK

[![Build Status](https://github.com/achulkov/crossmint-challenge/workflows/CI/badge.svg)](https://github.com/achulkov/crossmint-challenge/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.achulkov.challenge/crossmint-challenge-lib.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.achulkov.challenge%22%20AND%20a:%22crossmint-challenge-lib%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-1.10.0-blue.svg?logo=jetpack-compose)](https://www.jetbrains.com/lp/compose-multiplatform/)

> A modern, production-ready Kotlin Multiplatform (KMP) library for interacting with the Crossmint Megaverse API.

---

 [Features](#features) &bull;
 [Setup](#setup) &bull;
 [Usage](#usage) &bull;
 [Architecture](#architecture) &bull;
 [Documentation](#documentation) &bull;
 [Contributing](#contributing) &bull;
 [License](#license) 

---

## What's Included 🚀

- **🌐 Kotlin Multiplatform** - Supports Android, iOS, JVM, and macOS platforms
- **🏗️ Clean Architecture** - Domain, Data, and Presentation layers following SOLID principles
- **💉 Dependency Injection** - Koin DI for loose coupling and enhanced testability
- **⚡ Reactive State Management** - StateFlow for real-time reactive updates
- **🔒 Type-Safe API** - Strongly-typed models with comprehensive error handling
- **🔄 Coroutines & Flow** - Modern async programming with Kotlin Flow
- **🔁 Retry Logic** - Built-in exponential backoff and intelligent rate limiting
- **🧪 Comprehensive Testing** - Unit tests with mocking support and high coverage
- **📚 API Documentation** - Auto-generated Dokka documentation
- **🎨 Sample Apps** - Compose Multiplatform sample application included

## Features

✅ **Multi-platform Support** - Single codebase for Android, iOS, JVM, and macOS  
✅ **Goal Map Fetching** - Retrieve challenge goal maps from the API  
✅ **Astral Object Creation** - Create Polyanets, Soloons, and Comeths with various attributes  
✅ **Challenge Solver** - Automated challenge solving with progress tracking  
✅ **State Management** - Reactive state updates via Kotlin StateFlow  
✅ **Error Handling** - Robust error handling with detailed error types  
✅ **Rate Limiting** - Automatic rate limiting to respect API constraints  
✅ **Retry Mechanism** - Exponential backoff for failed requests  
✅ **Debug Logging** - Comprehensive logging for development and debugging  

## Setup

### Using Gradle

Choose the appropriate configuration based on your project type:

#### Multiplatform Projects

For Kotlin Multiplatform projects, add the dependency to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.achulkov.challenge:crossmint-challenge-lib:1.0.0")
        }
    }
}
```

#### Android-Only Projects

For Android-only projects:

```kotlin
dependencies {
    implementation("com.achulkov.challenge:crossmint-challenge-lib-android:1.0.0")
}
```

#### JVM-Only Projects

For JVM (Desktop/Server) projects:

```kotlin
dependencies {
    implementation("com.achulkov.challenge:crossmint-challenge-lib-jvm:1.0.0")
}
```

### Version Catalog

If using Gradle version catalogs, add to `gradle/libs.versions.toml`:

```toml
[versions]
crossmint-megaverse = "1.0.0"
koin = "4.1.1"

[libraries]
crossmint-megaverse = { module = "com.achulkov.challenge:crossmint-challenge-lib", version.ref = "crossmint-megaverse" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
```

Then in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.crossmint.megaverse)
    implementation(libs.koin.core)
}
```

## Usage

### Quick Start

#### 1. Initialize Koin

```kotlin
import com.achulkov.challenge.di.KoinInitializer
import com.achulkov.challenge.config.MegaverseConfig

// Option 1: With custom configuration
val config = MegaverseConfig().apply {
    setCandidateId("your-candidate-id")
    setDebugLogging(true)
}
KoinInitializer.init(config)

// Option 2: With environment variables
val config = MegaverseConfig.fromEnvironment()
KoinInitializer.init(config)

// Option 3: With default configuration
KoinInitializer.init()
```

#### 2. Get SDK Instance

```kotlin
import com.achulkov.challenge.MegaverseSdk
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AppComponent : KoinComponent {
    val sdk: MegaverseSdk by inject()
}

val sdk = AppComponent.sdk
```

#### 3. Use the SDK

```kotlin
import com.achulkov.challenge.repository.CreationProgress
import kotlinx.coroutines.flow.collect

// Configure candidate ID if not set during initialization
sdk.setCandidateId("your-candidate-id")

// Solve the challenge with progress tracking
sdk.solveChallenge().collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> {
            println("Progress: ${progress.completed}/${progress.total}")
        }
        is CreationProgress.ObjectCreated -> {
            println("✅ Created: ${progress.astralObject}")
        }
        is CreationProgress.ObjectFailed -> {
            println("❌ Failed: ${progress.astralObject}")
        }
        is CreationProgress.Completed -> {
            println("🎉 Challenge completed! Success: ${progress.successful}, Failed: ${progress.failed}")
        }
    }
}
```

### Advanced Usage

#### Creating Individual Astral Objects

```kotlin
import com.achulkov.challenge.domain.*

// Create a Polyanet
val result = sdk.createPolyanet(row = 5, column = 5)
result.onSuccess { 
    println("Polyanet created successfully")
}

// Create a Soloon with color
sdk.createSoloon(row = 5, column = 6, color = "blue")

// Create a Cometh with direction
sdk.createCometh(row = 5, column = 7, direction = "right")
```

#### Batch Operations with Progress Tracking

```kotlin
val objects = listOf(
    AstralObject.Polyanet(Position(5, 5)),
    AstralObject.Soloon(Position(5, 6), "blue"),
    AstralObject.Cometh(Position(5, 7), "right")
)

sdk.createAstralObjects(objects).collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> {
            val percentage = (progress.completed * 100) / progress.total
            println("Progress: $percentage%")
        }
        is CreationProgress.Completed -> {
            println("All operations completed!")
        }
    }
}
```

#### Reactive State Management

```kotlin
import com.achulkov.challenge.domain.state.MegaverseStateManager
import org.koin.core.component.inject

val stateManager: MegaverseStateManager by inject()

// Observe state changes
stateManager.state.collect { state ->
    when (state) {
        is MegaverseState.Idle -> println("Ready")
        is MegaverseState.Loading -> println("Loading...")
        is MegaverseState.Success -> println("Success: ${state.data}")
        is MegaverseState.Error -> println("Error: ${state.message}")
    }
}
```

#### Clearing the Megaverse

```kotlin
sdk.clearMegaverse(mapSize = 11).collect { progress ->
    when (progress) {
        is DeletionProgress.InProgress -> {
            println("Clearing: ${progress.completed}/${progress.total}")
        }
        is DeletionProgress.Completed -> {
            println("Megaverse cleared!")
        }
    }
}
```

## Platform-Specific Setup

### Android

```kotlin
// In your Application class
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = MegaverseConfig().apply {
            setCandidateId(BuildConfig.CANDIDATE_ID)
            setDebugLogging(BuildConfig.DEBUG)
        }
        KoinInitializer.init(config)
    }
}
```

### iOS

```swift
import crossmint_challenge_lib

// In your App or AppDelegate
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        let config = MegaverseConfig()
        config.setCandidateId(candidateId: "your-candidate-id")
        config.setDebugLogging(enabled: true)
        KoinInitializerKt.init(config: config, appDeclaration: nil)
        
        return true
    }
}
```

### JVM (Desktop/Server)

```kotlin
// In your main function
fun main() = runBlocking {
    // Initialize from environment variables
    val config = MegaverseConfig.fromEnvironment()
    KoinInitializer.init(config)
    
    val sdkComponent = object : KoinComponent {}
    val sdk: MegaverseSdk by sdkComponent.inject()
    
    // Solve the challenge
    sdk.solveChallenge().collect { progress ->
        // Handle progress updates
    }
}
```

### macOS

```kotlin
fun main() {
    val config = MegaverseConfig.fromEnvironment()
    KoinInitializer.init(config)
    
    // Same as JVM usage
}
```

## Architecture

The SDK follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│         Presentation Layer                  │
│  • MegaverseSdk (Public API Facade)        │
│  • Platform-specific integrations           │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Domain Layer                        │
│  • Use Cases (Business Logic)               │
│    - SolveChallengeUseCase                  │
│    - CreateAstralObjectsUseCase             │
│    - GetGoalMapUseCase                      │
│    - ClearPositionsUseCase                  │
│  • Domain Models                            │
│  • Repository Interfaces                    │
│  • State Management                         │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Data Layer                          │
│  • Repository Implementation                │
│  • Remote Data Sources                      │
│  • API Implementation (Ktor)                │
│  • Response/Request Models                  │
│  • Error Handling & Retry Logic             │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Infrastructure                      │
│  • Dependency Injection (Koin)              │
│  • Configuration Management                 │
│  • Logging & Monitoring                     │
│  • Platform-specific implementations        │
└─────────────────────────────────────────────┘
```

### Key Components

- **Use Cases**: Encapsulate business logic for specific operations
  - `SolveChallengeUseCase` - Orchestrates the complete challenge solution
  - `CreateAstralObjectsUseCase` - Handles batch creation with retries
  - `GetGoalMapUseCase` - Fetches and parses goal maps
  - `ClearPositionsUseCase` - Clears astral objects from positions

- **Repository**: `MegaverseRepository` - Abstracts data operations and provides a clean API
- **State Manager**: `MegaverseStateManager` - Manages reactive state with StateFlow
- **Configuration**: `MegaverseConfig` - Centralized configuration with platform-specific overrides
- **Resilient Executor**: `ResilientApiExecutor` - Handles retries, rate limiting, and error recovery

## API Reference

### MegaverseSdk

The main entry point for all SDK operations.

#### Configuration Methods

```kotlin
// Set candidate ID
sdk.setCandidateId("your-id")

// Get candidate ID
val id = sdk.getCandidateId(): String

// Enable/disable debug logging
sdk.setDebugLogging(true)
```

#### Challenge Methods

```kotlin
// Solve the full challenge (Phase 2) - fetches goal map and creates all objects
sdk.solveChallenge(): Flow<CreationProgress>

// Create X-pattern (Phase 1) - legacy method
sdk.createXPatternChallenge(gridSize: Int): Flow<CreationProgress>

// Get goal map from API
sdk.getGoalMap(): Result<GoalMap>

// Clear the entire megaverse
sdk.clearMegaverse(mapSize: Int): Flow<DeletionProgress>
```

#### Object Creation Methods

```kotlin
// Create a single Polyanet
sdk.createPolyanet(row: Int, column: Int): Result<Boolean>

// Create a single Soloon with color attribute
sdk.createSoloon(row: Int, column: Int, color: String): Result<Boolean>

// Create a single Cometh with direction attribute
sdk.createCometh(row: Int, column: Int, direction: String): Result<Boolean>

// Create multiple objects with progress tracking
sdk.createAstralObjects(objects: List<AstralObject>): Flow<CreationProgress>
```

#### Object Deletion Methods

```kotlin
// Delete a single Polyanet
sdk.deletePolyanet(row: Int, column: Int): Result<Boolean>

// Delete a single Soloon
sdk.deleteSoloon(row: Int, column: Int): Result<Boolean>

// Delete a single Cometh
sdk.deleteCometh(row: Int, column: Int): Result<Boolean>
```

### Progress Types

```kotlin
sealed class CreationProgress {
    data class InProgress(val completed: Int, val total: Int) : CreationProgress()
    data class ObjectCreated(val astralObject: AstralObject) : CreationProgress()
    data class ObjectFailed(val astralObject: AstralObject, val error: Throwable) : CreationProgress()
    data class Completed(val successful: Int, val failed: Int) : CreationProgress()
}

sealed class DeletionProgress {
    data class InProgress(val completed: Int, val total: Int) : DeletionProgress()
    data class Completed(val successful: Int, val failed: Int) : DeletionProgress()
}
```

## Configuration

### Environment Variables

The SDK supports configuration via environment variables:

```bash
# Required
export MEGAVERSE_CANDIDATE_ID="your-candidate-id"

# Optional - API Configuration
export MEGAVERSE_BASE_URL="https://challenge.crossmint.io/api"
export MEGAVERSE_DEBUG_LOGGING="true"

# Optional - Rate Limiting
export MEGAVERSE_REQUEST_DELAY_MS="500"
export MEGAVERSE_RATE_LIMIT_PER_SECOND="2"

# Optional - Retry Configuration
export MEGAVERSE_MAX_RETRIES="3"
export MEGAVERSE_RETRY_BASE_DELAY_MS="1000"
export MEGAVERSE_RETRY_MAX_DELAY_MS="10000"
```

### Programmatic Configuration

```kotlin
val config = MegaverseConfig().apply {
    setCandidateId("your-id")
    setDebugLogging(true)
    setBaseUrl("https://challenge.crossmint.io/api")
    setRequestDelayMs(500)
    setMaxRetries(3)
}
```

## Testing

### Unit Tests

The SDK includes comprehensive unit tests with mocking support:

```kotlin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.dsl.module
import io.mockk.mockk

class MyViewModelTest : KoinTest {
    @Test
    fun testSolveChallenge() = runBlocking {
        // Arrange
        val mockRepository = mockk<MegaverseRepository>(relaxed = true)
        
        startKoin {
            modules(module {
                single { mockRepository }
                single { mockk<IConfiguration>(relaxed = true) }
                single { mockk<ILogger>(relaxed = true) }
                // ... other dependencies
            })
        }
        
        val sdk: MegaverseSdk by inject()
        
        // Act & Assert
        // ... test implementation
    }
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific platform
./gradlew jvmTest
./gradlew iosX64Test

# Run tests with coverage report
./gradlew koverXmlReport
```

See [TESTING.md](./TESTING.md) for detailed testing strategies and examples.

## Documentation

- **[API Documentation](https://achulkov.github.io/crossmint-challenge/)** - Auto-generated Dokka API docs
- **[TESTING.md](./TESTING.md)** - Testing strategies and examples
- **[CONTRIBUTING.md](./CONTRIBUTING.md)** - Contribution guidelines

## Sample Application

A complete Compose Multiplatform sample application is included in the `sample/` directory:

```bash
# Run Android sample
./gradlew :sample:composeApp:installDebug

# Run Desktop sample
./gradlew :sample:composeApp:run

# Run iOS sample (requires Xcode)
cd sample/iosApp && xcodebuild -scheme iosApp -configuration Debug
```

## Dependencies

This library uses the following key dependencies:

- [Kotlin](https://kotlinlang.org/) - Kotlin language and standard library
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) - Asynchronous programming
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON serialization
- [Ktor Client](https://ktor.io/) - HTTP client for API communication
- [Koin](https://insert-koin.io/) - Dependency injection framework
- [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) - Platform-agnostic settings storage
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) - UI framework (for sample app)

## Requirements

- Kotlin 2.3.10 or higher
- Gradle 8.13 or higher
- JDK 17 or higher

### Platform-Specific Requirements

- **Android**: Min SDK 23 (Android 6.0)
- **iOS**: iOS 14.0+
- **JVM**: Java 17+
- **macOS**: macOS 11.0+

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for details on:

- How to submit issues and feature requests
- Development setup and workflow
- Code style guidelines
- Pull request process

## License

```
Copyright 2025 Andrei Chulkov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

See [LICENSE](./LICENSE) for the full license text.

---

**Questions or Issues?** Please file an issue on [GitHub](https://github.com/achulkov/crossmint-challenge/issues).
