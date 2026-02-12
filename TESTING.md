# Testing Guide

## Overview

**Total Tests: 92** ✅  
**Test Status: ALL PASSING** ✓

## Test Breakdown

### 1. Domain Logic Tests (44 tests)
- **AstralObjectTest.kt** (15 tests): Validates astral object types, properties, and constraints.
- **MegaverseMapTest.kt** (12 tests): Tests map structure, goal parsing, and validation.
- **MegaverseResultTest.kt** (17 tests): Tests result type handling, transformations, and error propagation.

### 2. Extension Function Tests (24 tests)
- **MegaverseExtensionsTest.kt** (24 tests): Tests pattern generation, position utilities, distance calculations, and boundary checks.

### 3. SDK Integration Tests (16 tests)
- **MegaverseSdkTest.kt** (16 tests): Tests SDK initialization, configuration, and high-level operations.

### 4. Network Layer Tests (8 tests) - **NEW**
- **MegaverseApiTest.kt** (3 tests): Tests API client with MockEngine, verifying request serialization and response parsing.
- **ResilientApiExecutorTest.kt** (5 tests): Tests resilience patterns including:
  - Retry logic with exponential backoff
  - Circuit breaker state transitions
  - Rate limiting behavior
  - Error handling for retryable vs non-retryable errors

## Running Tests

### All Platforms
```bash
./gradlew test
```

### JVM Tests
```bash
./gradlew :crossmint-challenge-lib:jvmTest
```

### Android Unit Tests
```bash
./gradlew :crossmint-challenge-lib:testDebugUnitTest
```

### With Coverage Report
```bash
./gradlew koverHtmlReport
# Report: crossmint-challenge-lib/build/reports/kover/html/index.html
```

## Test Quality Metrics

- ✅ **Fast**: All unit tests run in < 10 seconds
- ✅ **Isolated**: Network tests use `MockEngine` and `MockConfig`, no external dependencies
- ✅ **Deterministic**: No flaky tests
- ✅ **Comprehensive**: Covers domain, extensions, SDK, and network resilience logic

## Platform Support

| Platform | Unit Tests | Integration Tests | Status |
|----------|-----------|-------------------|--------|
| JVM | ✅ | ✅ Full coverage | **PASSING** |
| Android | ✅ | ⚠️ Unit tests only | **PASSING** |
| iOS | ✅ | ✅ Full coverage | **PASSING** |
| macOS | ✅ | ✅ Full coverage | **PASSING** |

**Note**: Android configuration is tested via unit tests with graceful exception handling for Context requirements. Instrumented tests can be added as required.

## CI/CD Integration

Tests are automatically run on GitHub Actions for every push and PR.
