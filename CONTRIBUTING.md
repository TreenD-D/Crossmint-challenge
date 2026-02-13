# Contributing to Crossmint Megaverse SDK

First off, thank you for considering contributing to the Crossmint Megaverse SDK! It's people like you that make this library better for everyone.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Guidelines](#coding-guidelines)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Documentation](#documentation)

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [achulkov@example.com](mailto:achulkov@example.com).

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the [existing issues](https://github.com/achulkov/crossmint-challenge/issues) to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples** (code snippets, screenshots, etc.)
- **Describe the behavior you observed** and what you expected
- **Include details about your environment** (OS, Kotlin version, platform, etc.)

#### Bug Report Template

```markdown
**Description:**
A clear and concise description of the bug.

**Steps to Reproduce:**
1. Initialize SDK with '...'
2. Call method '...'
3. Observe error '...'

**Expected Behavior:**
What you expected to happen.

**Actual Behavior:**
What actually happened.

**Environment:**
- OS: [e.g., macOS 14, Windows 11, Ubuntu 22.04]
- Kotlin version: [e.g., 2.3.10]
- SDK version: [e.g., 1.0.0]
- Platform: [e.g., Android, iOS, JVM]

**Additional Context:**
Any other context, logs, or screenshots.
```

### Suggesting Enhancements

Enhancement suggestions are tracked as [GitHub issues](https://github.com/achulkov/crossmint-challenge/issues). Before creating an enhancement suggestion, please check existing issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description** of the suggested enhancement
- **Explain why this enhancement would be useful**
- **Include code examples** if applicable
- **Describe the current behavior** and explain **what behavior you'd like to see instead**

#### Enhancement Request Template

```markdown
**Feature Description:**
A clear description of the feature you'd like to see.

**Use Case:**
Explain the use case and why this feature would be beneficial.

**Proposed Solution:**
Describe how you envision this feature working.

**Alternative Solutions:**
Describe any alternative solutions or features you've considered.

**Additional Context:**
Any other context, mockups, or examples.
```

### Pull Requests

We actively welcome your pull requests! Here's the process:

1. **Fork the repo** and create your branch from `develop`
2. **Make your changes** following our [coding guidelines](#coding-guidelines)
3. **Add tests** for any new functionality
4. **Ensure all tests pass** (`./gradlew test`)
5. **Update documentation** if needed
6. **Format your code** with the project's code style
7. **Create a pull request** with a clear description

#### Pull Request Checklist

- [ ] Code follows the project's style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated (README, KDoc, etc.)
- [ ] Tests added/updated and passing
- [ ] No new warnings introduced
- [ ] Commit messages follow guidelines
- [ ] Changes are backwards compatible (or documented)

## Development Setup

### Prerequisites

- **JDK 17** or higher
- **Gradle 8.13** or higher (wrapper included)
- **Android Studio** (for Android development)
- **Xcode** (for iOS development on macOS)
- **Git**

### Initial Setup

1. **Clone the repository:**

```bash
git clone https://github.com/achulkov/crossmint-challenge.git
cd crossmint-challenge
```

2. **Checkout develop branch:**

```bash
git checkout develop
```

3. **Build the project:**

```bash
./gradlew build
```

4. **Run tests:**

```bash
./gradlew test
```

5. **Configure environment variables:**

Create a `local.properties` file (not committed to git):

```properties
MEGAVERSE_CANDIDATE_ID=your-candidate-id
```

### IDE Setup

#### Android Studio / IntelliJ IDEA

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Install recommended plugins:
   - Kotlin Multiplatform Mobile
   - Detekt
4. Enable code style from `.editorconfig`

#### Visual Studio Code

1. Install extensions:
   - Kotlin Language
   - Gradle for Java
2. Open the project folder

## Project Structure

```
crossmint-challenge/
├── .github/                    # GitHub workflows and templates
│   ├── workflows/             # CI/CD workflows
│   └── dependabot.yml         # Dependabot configuration
├── crossmint-challenge-lib/   # Main library module
│   └── src/
│       ├── commonMain/        # Common Kotlin code
│       ├── commonTest/        # Common tests
│       ├── androidMain/       # Android-specific code
│       ├── iosMain/           # iOS-specific code
│       ├── jvmMain/           # JVM-specific code
│       └── macosMain/         # macOS-specific code
├── sample/                    # Sample application
│   └── composeApp/           # Compose Multiplatform sample
├── gradle/                    # Gradle wrapper and configs
├── docs/                      # Documentation files
├── build.gradle.kts          # Root build script
├── settings.gradle.kts       # Gradle settings
├── detekt.yml               # Detekt configuration
└── README.md                # Project README
```

### Key Architectural Layers

- **Domain Layer** (`domain/`) - Business logic, use cases, domain models
- **Data Layer** (`data/`, `repository/`, `network/`) - Data sources, API implementations
- **DI Layer** (`di/`) - Dependency injection modules
- **Config** (`config/`) - Configuration and environment management
- **Utils** (`utils/`, `extensions/`) - Utility functions and extensions

## Coding Guidelines

### Kotlin Style Guide

We follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with a few additions:

#### General Principles

- **Write clear, self-documenting code**
- **Prefer composition over inheritance**
- **Follow SOLID principles**
- **Keep functions small and focused** (single responsibility)
- **Use meaningful names** for variables, functions, and classes

#### Naming Conventions

- **Classes**: PascalCase (`MegaverseSdk`, `AstralObject`)
- **Functions**: camelCase (`createPolyanet`, `getGoalMap`)
- **Properties**: camelCase (`candidateId`, `baseUrl`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRIES`, `BASE_URL`)
- **Interfaces**: Start with `I` prefix for clarity (`IConfiguration`, `ILogger`)

#### Code Organization

```kotlin
// 1. Package declaration
package com.achulkov.challenge.domain

// 2. Imports (sorted alphabetically)
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

// 3. File-level documentation
/**
 * Represents an astral object in the Megaverse.
 *
 * @property position The position of the object
 */

// 4. Class declaration
sealed class AstralObject {
    abstract val position: Position
    
    data class Polyanet(override val position: Position) : AstralObject()
}
```

#### Documentation (KDoc)

All public APIs must have KDoc documentation:

```kotlin
/**
 * Creates a Polyanet at the specified position.
 *
 * This method sends a request to the Megaverse API to create a Polyanet
 * at the given row and column coordinates.
 *
 * @param row The row position (0-indexed)
 * @param column The column position (0-indexed)
 * @return A [Result] containing true if successful, or an error
 * @throws IllegalArgumentException if row or column is negative
 *
 * Example usage:
 * ```kotlin
 * val result = sdk.createPolyanet(row = 5, column = 5)
 * result.onSuccess { println("Polyanet created!") }
 * ```
 */
suspend fun createPolyanet(row: Int, column: Int): Result<Boolean>
```

#### Nullability

- **Prefer non-null types** - use nullable types sparingly
- **Use safe calls** (`?.`) and Elvis operator (`?:`) appropriately
- **Avoid `!!`** - use safe alternatives or explicit null checks

```kotlin
// Good
val config = configuration.getCandidateId() ?: throw IllegalStateException("Candidate ID not set")

// Avoid
val config = configuration.getCandidateId()!!
```

#### Error Handling

- **Use `Result<T>`** for operations that can fail
- **Use sealed classes** for complex error hierarchies
- **Provide meaningful error messages**

```kotlin
sealed class MegaverseError {
    data class NetworkError(val message: String) : MegaverseError()
    data class ApiError(val code: Int, val message: String) : MegaverseError()
    data class ValidationError(val field: String) : MegaverseError()
}
```

#### Coroutines and Flow

- **Use `suspend` functions** for single async operations
- **Use `Flow`** for streaming data or progress updates
- **Handle cancellation** properly
- **Use structured concurrency**

```kotlin
suspend fun fetchData(): Result<Data> = withContext(Dispatchers.IO) {
    // Implementation
}

fun observeProgress(): Flow<Progress> = flow {
    // Emit progress updates
    emit(Progress.Loading)
    emit(Progress.Success(data))
}
```

### Code Quality Tools

#### Detekt

We use Detekt for static code analysis:

```bash
# Run Detekt
./gradlew detekt

# Auto-fix issues where possible
./gradlew detekt --auto-correct
```

#### Formatting

Code is formatted using the Kotlin plugin formatter:

```bash
# Format all code
./gradlew ktlintFormat
```

## Testing Guidelines

### Test Structure

We follow the **Arrange-Act-Assert (AAA)** pattern:

```kotlin
@Test
fun `createPolyanet should return success when API call succeeds`() = runBlocking {
    // Arrange
    val mockRepository = mockk<MegaverseRepository>()
    coEvery { mockRepository.createPolyanet(any(), any()) } returns Result.success(true)
    
    val sdk = MegaverseSdk(mockRepository, /* ... */)
    
    // Act
    val result = sdk.createPolyanet(row = 5, column = 5)
    
    // Assert
    assertTrue(result.isSuccess)
    coVerify { mockRepository.createPolyanet(5, 5) }
}
```

### Test Coverage

- **Aim for 80%+ code coverage** for common code
- **Write unit tests** for all business logic
- **Write integration tests** for API interactions
- **Include edge cases** and error scenarios

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific platform
./gradlew jvmTest
./gradlew iosSimulatorArm64Test

# Generate coverage report
./gradlew koverXmlReport
```

### Mocking

We use MockK for mocking:

```kotlin
val mockLogger = mockk<ILogger>(relaxed = true)
val mockConfig = mockk<IConfiguration> {
    every { getCandidateId() } returns "test-id"
}
```

## Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, no logic change)
- **refactor**: Code refactoring (no feature change or bug fix)
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **chore**: Maintenance tasks, dependency updates
- **ci**: CI/CD changes

### Examples

```
feat(sdk): add support for Cometh creation

Add new createCometh method to SDK with direction parameter.
Includes unit tests and documentation.

Closes #42
```

```
fix(retry): correct exponential backoff calculation

The retry delay was not properly doubling on each attempt.
Now correctly implements exponential backoff with max delay cap.

Fixes #108
```

```
docs(readme): update installation instructions

Add Maven Central badge and improve setup examples.
```

### Best Practices

- **Use imperative mood** ("add" not "added")
- **Keep subject line under 72 characters**
- **Capitalize the subject line**
- **Don't end subject with a period**
- **Reference issues** in footer when applicable

## Documentation

### Types of Documentation

1. **Code Documentation (KDoc)** - Document all public APIs
2. **README** - Keep README.md up to date with examples
3. **Migration Guides** - Document breaking changes
4. **Architecture Docs** - Explain design decisions

### Updating Documentation

When adding features or making changes:

1. **Update KDoc** for affected classes/functions
2. **Update README** if public API changes
3. **Update MIGRATION.md** for breaking changes
4. **Add examples** in the sample app if applicable

### Generating API Docs

```bash
# Generate Dokka documentation
./gradlew dokkaHtml

# View generated docs
open crossmint-challenge-lib/build/dokka/html/index.html
```

## Release Process

1. **Update version** in `gradle.properties` and `build.gradle.kts`
2. **Update CHANGELOG.md** with release notes
3. **Create a release branch** from `develop`
4. **Run all tests** and ensure CI passes
5. **Create a tag** (e.g., `v1.0.0`)
6. **Push tag** to trigger release workflow
7. **Create GitHub Release** with release notes
8. **Merge release branch** to `main` and `develop`

## Questions?

If you have questions or need help, feel free to:

- **Open an issue** with the `question` label
- **Start a discussion** in GitHub Discussions
- **Contact maintainers**

Thank you for contributing! 🎉
