# Module: Crossmint Megaverse SDK

A modern, production-ready Kotlin Multiplatform library for interacting with the Crossmint Megaverse API.

## Overview

This module provides a complete SDK for building applications that interact with the Crossmint Megaverse API. It follows Clean Architecture principles with clear separation of concerns across multiple layers.

## Main Components

### Public API

- **[MegaverseSdk](com.achulkov.challenge/-megaverse-sdk/index.md)** - Main SDK facade providing high-level operations

### Domain Layer

- **Use Cases** - Encapsulated business logic
  - [SolveChallengeUseCase](com.achulkov.challenge.domain.usecases/-solve-challenge-use-case/index.md)
  - [CreateAstralObjectsUseCase](com.achulkov.challenge.domain.usecases/-create-astral-objects-use-case/index.md)
  - [GetGoalMapUseCase](com.achulkov.challenge.domain.usecases/-get-goal-map-use-case/index.md)
  - [ClearPositionsUseCase](com.achulkov.challenge.domain.usecases/-clear-positions-use-case/index.md)

- **Domain Models**
  - [AstralObject](com.achulkov.challenge.domain/-astral-object/index.md) - Sealed class hierarchy for different object types
  - [Position](com.achulkov.challenge.domain/-position/index.md) - Coordinate representation
  - [MegaverseMap](com.achulkov.challenge.domain/-megaverse-map/index.md) - Map representation
  - [MegaverseResult](com.achulkov.challenge.domain/-megaverse-result/index.md) - Operation results

- **State Management**
  - [MegaverseStateManager](com.achulkov.challenge.domain.state/-megaverse-state-manager/index.md)
  - [MegaverseState](com.achulkov.challenge.domain.state/-megaverse-state/index.md)

### Data Layer

- **Repository**
  - [MegaverseRepository](com.achulkov.challenge.repository/-megaverse-repository/index.md) - Repository interface
  - [MegaverseRepositoryImpl](com.achulkov.challenge.repository/-megaverse-repository-impl/index.md) - Implementation
  - [ResilientApiExecutor](com.achulkov.challenge.repository/-resilient-api-executor/index.md) - Handles retries and rate limiting

- **Network**
  - [MegaverseApi](com.achulkov.challenge.network/-megaverse-api/index.md) - API interface
  - [MegaverseApiImpl](com.achulkov.challenge.network/-megaverse-api-impl/index.md) - Ktor-based implementation

- **Data Sources**
  - [RemoteMegaverseDataSource](com.achulkov.challenge.data.datasource/-remote-megaverse-data-source/index.md)
  - [HttpClientFactory](com.achulkov.challenge.data.datasource/-http-client-factory/index.md)

### Configuration

- **[MegaverseConfig](com.achulkov.challenge.config/-megaverse-config/index.md)** - Main configuration class
- **[EnvironmentConfig](com.achulkov.challenge.config/-environment-config/index.md)** - Environment-based configuration

### Dependency Injection

- **[KoinInitializer](com.achulkov.challenge.di/-koin-initializer/index.md)** - DI initialization
- **[MegaverseModule](com.achulkov.challenge.di/-megaverse-module/index.md)** - Koin module definitions

## Platform Support

This library supports the following platforms:

- **Android** (API 23+)
- **iOS** (iOS 14.0+)
- **JVM** (Java 17+)
- **macOS** (macOS 11.0+)

## Key Features

### Reactive Programming

The SDK extensively uses Kotlin Coroutines and Flow for asynchronous operations:

```kotlin
// Progress tracking with Flow
sdk.solveChallenge().collect { progress ->
    when (progress) {
        is CreationProgress.InProgress -> updateUI(progress)
        is CreationProgress.Completed -> showSuccess()
    }
}
```

### Error Handling

Comprehensive error handling using Kotlin's `Result` type:

```kotlin
val result = sdk.createPolyanet(5, 5)
result.onSuccess { println("Created!") }
result.onFailure { error -> handleError(error) }
```

### Resilient Operations

Built-in retry logic with exponential backoff and rate limiting:

- Automatic retries on transient failures
- Configurable retry delays and maximum attempts
- Intelligent rate limiting to respect API constraints

### State Management

Reactive state management with StateFlow:

```kotlin
stateManager.state.collect { state ->
    when (state) {
        is MegaverseState.Loading -> showLoading()
        is MegaverseState.Success -> showData(state.data)
        is MegaverseState.Error -> showError(state.message)
    }
}
```

## Getting Started

See the [main README](../README.md) for setup instructions and usage examples.

## API Documentation

For detailed API documentation, see the package-level documentation for each module.
