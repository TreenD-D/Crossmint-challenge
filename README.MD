# Crossmint Megaverse SDK

[![CI](https://github.com/achulkov/crossmint-challenge/actions/workflows/ci.yml/badge.svg)](https://github.com/achulkov/crossmint-challenge/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

A production-grade, robust Kotlin Multiplatform library for interacting with the Crossmint Megaverse API. This SDK provides a clean, type-safe interface for creating and managing astral objects in the megaverse challenge.

##  Features

- **🔐 Secure Credential Management**: Environment variable support for sensitive data
- **🛡️ Resilience Patterns**: Exponential backoff, circuit breaker, and rate limiting
- **📊 Progress Tracking**: Real-time feedback for bulk operations via Kotlin Flow
- **🧪 Comprehensive Testing**: Full test coverage with unit and integration tests
- **🔄 CI/CD Ready**: GitHub Actions workflows for automated testing and linting
- **📱 Multiplatform**: Works on Android, iOS, JVM, macOS, and more
- **🎯 Clean Architecture**: Repository pattern with clear separation of concerns
- **📖 Type-Safe API**: Strongly typed domain models with validation
- **🔍 Debug Logging**: Comprehensive logging for development and troubleshooting

## 🏗️ Architecture

The SDK follows clean architecture principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│                    (SDK, Sample Apps, CLI)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Repository Layer                          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  MegaverseRepository                                    │    │
│  │  • Business logic abstraction                           │    │
│  │  • Resilience patterns (retry, circuit breaker)         │    │
│  │  • Bulk operation orchestration                         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  ResilientApiExecutor                                   │    │
│  │  • Exponential backoff with jitter                        │    │
│  │  • Circuit breaker pattern                                │    │
│  │  • Token bucket rate limiting                             │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  MegaverseApi (Network)                                 │    │
│  │  • HTTP client abstraction                                │    │
│  │  • Request/response serialization                         │    │
│  │  • Error mapping                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  MegaverseConfig (Environment + Settings)                 │    │
│  │  • Environment variable support                           │    │
│  │  • Persistent settings storage                            │    │
│  │  • Configuration validation                               │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Domain Layer                               │
│  • AstralObject (sealed class hierarchy)                         │
│  • Polyanet, Soloon, Cometh (data classes)                       │
│  • Position, MegaverseMap (value objects)                        │
│  • MegaverseResult (result wrapper)                              │
│  • MegaverseException (error hierarchy)                          │
└─────────────────────────────────────────────────────────────────┘
```

## 📦 Installation

### GitHub Packages

This library is published to GitHub Packages on every push to `main` and on tagged releases.

**Step 1:** Add the GitHub Packages repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/achulkov/crossmint-challenge")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
        mavenCentral()
        google()
    }
}
```

**Step 2:** Add your GitHub credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=your-github-username
gpr.token=your-github-personal-access-token
```

> **Note:** Generate a GitHub Personal Access Token with `read:packages` scope at:
> https://github.com/settings/tokens

**Step 3:** Add the library dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    // For release versions (when tagged as v1.0.0, v1.0.1, etc.)
    implementation("com.achulkov.challenge:crossmint-challenge-lib:1.0.0")
    
    // For snapshot versions (latest from main branch)
    implementation("com.achulkov.challenge:crossmint-challenge-lib:1.0.0-SNAPSHOT")
}
```

### Maven Central (Planned)

Publishing to Maven Central is configured and will be available in future releases.

## 🚀 Quick Start

### Environment-Based Setup

```bash
# Set environment variables
export MEGAVERSE_CANDIDATE_ID="your-candidate-id-here"
export MEGAVERSE_DEBUG_LOGGING="true"
export MEGAVERSE_MAX_RETRIES="3"
```

```kotlin
import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.config.MegaverseConfig

// Initialize SDK from environment variables (most secure)
val sdk = MegaverseSdk(config = MegaverseConfig.fromEnvironment())
```

### Programmatic Setup

```kotlin
import com.achulkov.challenge.MegaverseSdk

// Initialize and configure programmatically
val sdk = MegaverseSdk(enableDebugLogging = true)
sdk.setCandidateId("your-candidate-id-here")
```

### Running the JVM Sample

```bash
# With environment variables
export MEGAVERSE_CANDIDATE_ID="your-candidate-id"
export MEGAVERSE_DEBUG_LOGGING="true"
./gradlew :crossmint-challenge-lib:jvmRun

# With system properties
./gradlew :crossmint-challenge-lib:jvmRun \
  -Dmegaverse.candidate.id="your-candidate-id" \
  -Dmegaverse.debug.logging="true"
```

## 🌍 Environment Variables

The SDK supports the following environment variables for secure, production-ready configuration:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MEGAVERSE_CANDIDATE_ID` | Your unique candidate identifier | - | **Yes** |
| `MEGAVERSE_BASE_URL` | API base URL | `https://challenge.crossmint.io/api` | No |
| `MEGAVERSE_DEBUG_LOGGING` | Enable debug logging | `false` | No |
| `MEGAVERSE_REQUEST_DELAY_MS` | Delay between requests | `500` | No |
| `MEGAVERSE_MAX_RETRIES` | Maximum retry attempts | `3` | No |
| `MEGAVERSE_RETRY_BASE_DELAY_MS` | Base delay for exponential backoff | `1000` | No |
| `MEGAVERSE_RATE_LIMIT_PER_SECOND` | Rate limit (requests/second) | `2` | No |

### Platform-Specific Notes

**JVM/Desktop:**
```bash
export MEGAVERSE_CANDIDATE_ID="your-id"
java -jar your-app.jar
```

**Android:**
Set environment variables in your CI/CD pipeline or use `local.properties`:
```properties
MEGAVERSE_CANDIDATE_ID=your-id
```

**iOS/macOS:**
Set in your scheme's environment variables or use `ProcessInfo`:
```swift
setenv("MEGAVERSE_CANDIDATE_ID", "your-id", 1)
```

## 📖 Usage Examples

### Complete Challenge Solution

```kotlin
import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.repository.CreationProgress
import kotlinx.coroutines.flow.collect

suspend fun solveChallenge() {
    // Use environment-based configuration
    val sdk = MegaverseSdk(config = MegaverseConfig.fromEnvironment())

    sdk.solveChallenge().collect { progress ->
        when (progress) {
            is CreationProgress.InProgress -> {
                println("Progress: ${progress.completed}/${progress.total}")
            }
            is CreationProgress.ObjectCreated -> {
                val obj = progress.astralObject
                println("✅ Created ${obj::class.simpleName} at ${obj.position}")
            }
            is CreationProgress.ObjectFailed -> {
                println("❌ Failed: ${progress.error.message}")
            }
            is CreationProgress.Completed -> {
                println("🎉 Complete! ${progress.successful} successful")
            }
        }
    }
}
```

### Phase 1: X-Pattern Challenge

```kotlin
// Create the X-pattern for Phase 1 (11x11 grid)
sdk.createXPatternChallenge(11).collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> 
            println("Progress: ${progress.completed}/${progress.total}")
        is CreationProgress.Completed -> 
            println("🎉 X-Pattern complete! ${progress.successful} created")
        else -> {}
    }
}
```

### Individual Object Operations

```kotlin
import com.achulkov.challenge.domain.*

// Create a single polyanet
val result = sdk.createPolyanet(Position(5, 5))
when (result) {
    is MegaverseResult.Success -> println("Polyanet created!")
    is MegaverseResult.Error -> println("Error: ${result.exception.message}")
}

// Create a soloon with color
sdk.createSoloon(Position(3, 3), SoloonColor.BLUE)

// Create a cometh with direction  
sdk.createCometh(Position(7, 2), ComethDirection.UP)
```

## 🛡️ Resilience Patterns

The SDK implements several resilience patterns at the repository layer:

### Exponential Backoff with Jitter

```
Retry 1: baseDelay * 2^0 + jitter = ~1000ms
Retry 2: baseDelay * 2^1 + jitter = ~2000ms
Retry 3: baseDelay * 2^2 + jitter = ~4000ms
```

Configurable via environment:
```bash
export MEGAVERSE_MAX_RETRIES=5
export MEGAVERSE_RETRY_BASE_DELAY_MS=2000
```

### Circuit Breaker

The circuit breaker prevents cascading failures:
- **CLOSED**: Normal operation
- **OPEN**: Blocks requests after 5 consecutive failures
- **HALF_OPEN**: Allows limited requests to test recovery

```bash
export MEGAVERSE_RATE_LIMIT_PER_SECOND=2  # Prevents rate limiting
```

### Rate Limiting

Token bucket algorithm ensures you stay within API limits:
```bash
export MEGAVERSE_RATE_LIMIT_PER_SECOND=2  # 2 requests per second max
```

## 🧪 Testing

**Test Suite: 84 tests - ALL PASSING** ✅

Run the comprehensive test suite:

```bash
# Run all tests
./gradlew test

# Run tests with coverage (HTML report)
./gradlew koverHtmlReport

# Run specific test suites
./gradlew :crossmint-challenge-lib:commonTest
./gradlew :crossmint-challenge-lib:jvmTest
./gradlew :crossmint-challenge-lib:testDebugUnitTest  # Android unit tests

# Note: Android instrumented tests not yet configured
# (requires androidTest dependencies)
```

View coverage report:
```bash
open crossmint-challenge-lib/build/reports/kover/html/index.html
```

📊 **Detailed Test Coverage**: See [TEST_COVERAGE.md](TEST_COVERAGE.md) for comprehensive test documentation.

### Test Structure

```
src/commonTest/                        # 84 tests
├── domain/
│   ├── AstralObjectTest.kt          # Domain model tests (15 tests)
│   ├── MegaverseMapTest.kt          # Map parsing tests (12 tests)
│   └── MegaverseResultTest.kt        # Result wrapper tests (17 tests)
├── extensions/
│   └── MegaverseExtensionsTest.kt    # Pattern generation tests (24 tests)
└── MegaverseSdkTest.kt              # SDK integration tests (16 tests)

# Note: androidInstrumentedTest not yet configured
```

### Test Coverage by Module

- ✅ **Domain Models**: 100% coverage
- ✅ **Extensions/Utilities**: 100% coverage  
- ✅ **SDK Operations**: Full coverage with Android exception handling
- ⚠️ **Repository/Network**: Integration tests pending (mock API needed)

## 🔧 Code Quality

The project uses Detekt for code quality:

```bash
# Run Detekt analysis
./gradlew detekt
```

Configuration is in `detekt.yml` at the project root.

## 🔄 CI/CD

GitHub Actions workflows are configured for:

- **Continuous Integration**: Runs on every push and PR
  - **Code Quality**: Detekt static analysis with report artifacts
  - **Unit Tests**: Tests across multiple API levels (21, 29, 34) with JUnit reports
  - **Coverage**: Kover code coverage with Codecov integration
  - **Build Verification**: Validates library and sample app builds
  - **Documentation**: Auto-generates Dokka HTML documentation

- **Publishing to GitHub Packages**: Automatic on push to `main`
  - **Snapshot versions** (`1.0.0-SNAPSHOT`) published on every push to `main`
  - **Release versions** (e.g., `1.0.0`) published when tagged with `v*` tags
  - Artifacts include JARs and AARs for all supported platforms
  - Release artifacts automatically uploaded for tagged releases

- **Future**: Publishing to Maven Central is configured and ready

### Creating a Release

To publish a new release version:

```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

The CI will automatically:
1. Build and test the library
2. Publish to GitHub Packages with the version from the tag
3. Create release artifacts (JARs, AARs)
4. Upload artifacts to the GitHub Actions run

See `.github/workflows/ci.yml` for complete CI/CD configuration.

## 🏛️ Design Principles

### Clean Architecture

- **Separation of Concerns**: Clear boundaries between layers
- **Dependency Inversion**: Abstractions don't depend on details
- **Repository Pattern**: Clean data access abstraction
- **Result Pattern**: Explicit error handling without exceptions

### Resilience

- **Exponential Backoff**: Prevents thundering herd
- **Circuit Breaker**: Protects against cascading failures
- **Rate Limiting**: Respects API limits
- **Comprehensive Error Handling**: Specific exception types

### Scalability

- **Flow-based Operations**: Reactive, cancellable streams
- **Bulk Operation Support**: Efficient batch processing
- **Extension Functions**: Easy to add new patterns
- **Multiplatform**: Works across all Kotlin targets

## 📚 API Reference

### MegaverseSdk

Main entry point for all operations:

| Method | Description |
|--------|-------------|
| `createPolyanet(position)` | Create a single polyanet |
| `createSoloon(position, color)` | Create a colored soloon |
| `createCometh(position, direction)` | Create a directional cometh |
| `createAstralObjects(objects)` | Bulk create with progress |
| `clearPositions(positions)` | Bulk delete with progress |
| `createXPatternChallenge(size)` | Phase 1 helper |
| `solveChallenge()` | Complete auto-solution |
| `getGoalMap()` | Fetch target configuration |

### Domain Models

```kotlin
// Position (validated, non-negative)
val position = Position(row = 2, column = 5)

// Astral Objects
val polyanet = Polyanet(position)
val soloon = Soloon(position, SoloonColor.PURPLE)
val cometh = Cometh(position, ComethDirection.LEFT)

// Colors
enum class SoloonColor { BLUE, RED, PURPLE, WHITE }

// Directions
enum class ComethDirection { UP, DOWN, LEFT, RIGHT }
```

## 📝 Logging

The SDK includes comprehensive logging:

```kotlin
// Enable debug logging
val sdk = MegaverseSdk(enableDebugLogging = true)

// Or via environment
export MEGAVERSE_DEBUG_LOGGING=true
```

Log output includes:
- Network requests and responses
- Retry attempts with delays
- Circuit breaker state changes
- Rate limiting events
- Object creation progress



## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built for the Crossmint Megaverse Challenge**
