package com.achulkov.challenge.repository

import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.domain.MegaverseException
import com.achulkov.challenge.domain.MegaverseResult
import com.achulkov.challenge.utils.MegaverseLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * A resilient API executor that implements:
 * - Exponential backoff retry with jitter
 * - Circuit breaker pattern for fault tolerance
 * - Rate limiting with token bucket algorithm
 * - Request throttling for concurrent operations
 *
 * This class wraps API calls to provide resilience against transient failures,
 * rate limiting, and cascading failures.
 *
 * @param config The configuration for retry, backoff, and rate limiting settings
 */
class ResilientApiExecutor(
    private val config: MegaverseConfig
) {
    // Circuit breaker state
    private var circuitState: CircuitState = CircuitState.CLOSED
    private var failureCount = 0
    private var lastFailureTime: Long = 0
    private val circuitMutex = Mutex()

    // Rate limiting state (token bucket)
    private var tokens: Double
    private val maxTokens: Double
    private val tokenRate: Double
    private var lastTokenUpdate: Long = Clock.System.now().toEpochMilliseconds()
    private val rateMutex = Mutex()

    companion object {
        // Circuit breaker settings
        private const val FAILURE_THRESHOLD = 5
        private const val CIRCUIT_OPEN_DURATION_MS = 30_000L // 30 seconds
        private const val HALF_OPEN_REQUESTS = 3

        // Rate limiting settings
        private const val MIN_REQUEST_DELAY_MS = 100L

        // Exponential backoff settings
        private const val MAX_RETRY_DELAY_MS = 30_000L // 30 seconds
        private const val JITTER_FACTOR = 0.1 // 10% jitter
    }

    init {
        val rateLimitPerSecond = config.getRateLimitPerSecond()
        maxTokens = rateLimitPerSecond.toDouble()
        tokens = maxTokens
        tokenRate = rateLimitPerSecond.toDouble()

        MegaverseLogger.info(
            "ResilientApiExecutor",
            "Initialized with rate limit: $rateLimitPerSecond req/s, " +
                "max retries: ${config.getMaxRetries()}, " +
                "base delay: ${config.getRetryBaseDelayMs()}ms"
        )
    }

    /**
     * Executes an API call with full resilience: rate limiting, circuit breaker, and retry.
     *
     * @param operationName Name of the operation for logging
     * @param apiCall The API call to execute
     * @return The result of the API call
     */
    suspend fun <T> executeWithResilience(
        operationName: String,
        apiCall: suspend () -> MegaverseResult<T>
    ): MegaverseResult<T> {
        // Check circuit breaker
        if (!canExecute()) {
            MegaverseLogger.warn(
                "ResilientApiExecutor",
                "Circuit breaker OPEN: rejecting $operationName"
            )
            return MegaverseResult.Error(
                MegaverseException.NetworkError(
                    "Service temporarily unavailable. Circuit breaker is OPEN."
                )
            )
        }

        // Apply rate limiting
        applyRateLimit()

        // Execute with retry
        return executeWithRetry(operationName, apiCall)
    }

    /**
     * Executes an API call with exponential backoff retry.
     *
     * @param operationName Name of the operation for logging
     * @param apiCall The API call to execute
     * @return The result of the API call
     */
    private suspend fun <T> executeWithRetry(
        operationName: String,
        apiCall: suspend () -> MegaverseResult<T>
    ): MegaverseResult<T> {
        val maxRetries = config.getMaxRetries()
        val baseDelay = config.getRetryBaseDelayMs()

        var lastException: Throwable? = null

        for (attempt in 0..maxRetries) {
            try {
                MegaverseLogger.debug(
                    "ResilientApiExecutor",
                    "Executing $operationName (attempt ${attempt + 1}/${maxRetries + 1})"
                )

                val result = apiCall()

                when (result) {
                    is MegaverseResult.Success -> {
                        onSuccess()
                        MegaverseLogger.debug(
                            "ResilientApiExecutor",
                            "$operationName succeeded on attempt ${attempt + 1}"
                        )
                        return result
                    }

                    is MegaverseResult.Error -> {
                        lastException = result.exception

                        // Check if error is retryable
                        if (!isRetryableError(result.exception)) {
                            MegaverseLogger.warn(
                                "ResilientApiExecutor",
                                "$operationName failed with non-retryable error: ${result.exception.message}"
                            )
                            onFailure()
                            return result
                        }

                        if (attempt < maxRetries) {
                            val delayMs = calculateBackoffDelay(attempt, baseDelay)
                            MegaverseLogger.warn(
                                "ResilientApiExecutor",
                                "$operationName failed (attempt ${attempt + 1}), " +
                                    "retrying in ${delayMs}ms: ${result.exception.message}"
                            )
                            delay(delayMs)
                        } else {
                            MegaverseLogger.error(
                                "ResilientApiExecutor",
                                "$operationName failed after ${maxRetries + 1} attempts"
                            )
                            onFailure()
                        }
                    }
                }
            } catch (e: Exception) {
                lastException = e

                if (attempt < maxRetries) {
                    val delayMs = calculateBackoffDelay(attempt, baseDelay)
                    MegaverseLogger.error(
                        "ResilientApiExecutor",
                        "$operationName threw exception (attempt ${attempt + 1}), " +
                            "retrying in ${delayMs}ms: ${e.message}"
                    )
                    delay(delayMs)
                } else {
                    MegaverseLogger.error(
                        "ResilientApiExecutor",
                        "$operationName threw exception after ${maxRetries + 1} attempts: ${e.message}"
                    )
                    onFailure()
                }
            }
        }

        return MegaverseResult.Error(
            MegaverseException.NetworkError(
                "Failed after $maxRetries retries",
                lastException
            )
        )
    }

    /**
     * Calculates the backoff delay with exponential backoff and jitter.
     *
     * Formula: min(baseDelay * 2^attempt + jitter, maxDelay)
     *
     * @param attempt The current retry attempt (0-indexed)
     * @param baseDelay The base delay in milliseconds
     * @return The calculated delay in milliseconds
     */
    private fun calculateBackoffDelay(attempt: Int, baseDelay: Long): Long {
        // Exponential backoff: baseDelay * 2^attempt
        val exponentialDelay = baseDelay * (2.0.pow(attempt.toDouble())).toLong()

        // Add jitter to prevent thundering herd
        val jitter = (exponentialDelay * JITTER_FACTOR * Random.nextDouble()).toLong()

        // Cap at maximum delay
        return min(exponentialDelay + jitter, MAX_RETRY_DELAY_MS)
    }

    /**
     * Checks if an error is retryable.
     *
     * Non-retryable errors include client errors (4xx except 429) and deserialization errors.
     *
     * @param exception The exception to check
     * @return true if the error is retryable
     */
    private fun isRetryableError(exception: MegaverseException): Boolean {
        return when (exception) {
            is MegaverseException.ApiError -> {
                // Retry on rate limiting (429) and server errors (5xx)
                // Don't retry on client errors (4xx except 429)
                exception.statusCode == 429 || exception.statusCode >= 500
            }

            is MegaverseException.NetworkError -> true
            is MegaverseException.SerializationError -> false
            is MegaverseException.InvalidCandidateId -> false
            is MegaverseException.InvalidPosition -> false
            is MegaverseException.Unknown -> true
        }
    }

    /**
     * Applies rate limiting using token bucket algorithm.
     */
    private suspend fun applyRateLimit() {
        rateMutex.withLock {
            replenishTokens()

            if (tokens < 1.0) {
                // Calculate wait time for one token
                val waitTimeMs = ((1.0 - tokens) / tokenRate * 1000).toLong()
                val delayMs = maxOf(waitTimeMs, MIN_REQUEST_DELAY_MS)

                MegaverseLogger.debug(
                    "ResilientApiExecutor",
                    "Rate limit reached, waiting ${delayMs}ms"
                )

                rateMutex.unlock()
                delay(delayMs)
                rateMutex.lock()

                replenishTokens()
            }

            tokens -= 1.0
        }
    }

    /**
     * Replenishes tokens based on elapsed time.
     */
    private fun replenishTokens() {
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsedSeconds = (now - lastTokenUpdate) / 1000.0
        val tokensToAdd = elapsedSeconds * tokenRate

        tokens = min(tokens + tokensToAdd, maxTokens)
        lastTokenUpdate = now
    }

    /**
     * Checks if the circuit breaker allows execution.
     */
    private suspend fun canExecute(): Boolean {
        return circuitMutex.withLock {
            when (circuitState) {
                CircuitState.CLOSED -> true
                CircuitState.OPEN -> {
                    val now = Clock.System.now().toEpochMilliseconds()
                    if (now - lastFailureTime >= CIRCUIT_OPEN_DURATION_MS) {
                        // Transition to half-open
                        circuitState = CircuitState.HALF_OPEN
                        failureCount = 0
                        MegaverseLogger.info(
                            "ResilientApiExecutor",
                            "Circuit breaker transitioning from OPEN to HALF_OPEN"
                        )
                        true
                    } else {
                        false
                    }
                }

                CircuitState.HALF_OPEN -> failureCount < HALF_OPEN_REQUESTS
            }
        }
    }

    /**
     * Called when an operation succeeds.
     */
    private suspend fun onSuccess() {
        circuitMutex.withLock {
            if (circuitState == CircuitState.HALF_OPEN) {
                // Close the circuit
                circuitState = CircuitState.CLOSED
                failureCount = 0
                MegaverseLogger.info(
                    "ResilientApiExecutor",
                    "Circuit breaker transitioning from HALF_OPEN to CLOSED"
                )
            }
        }
    }

    /**
     * Called when an operation fails.
     */
    private suspend fun onFailure() {
        circuitMutex.withLock {
            failureCount++
            lastFailureTime = Clock.System.now().toEpochMilliseconds()

            when (circuitState) {
                CircuitState.CLOSED -> {
                    if (failureCount >= FAILURE_THRESHOLD) {
                        circuitState = CircuitState.OPEN
                        MegaverseLogger.error(
                            "ResilientApiExecutor",
                            "Circuit breaker OPEN after $failureCount failures"
                        )
                    }
                }

                CircuitState.HALF_OPEN -> {
                    circuitState = CircuitState.OPEN
                    MegaverseLogger.error(
                        "ResilientApiExecutor",
                        "Circuit breaker returning to OPEN after failure in HALF_OPEN"
                    )
                }

                CircuitState.OPEN -> {
                    // Already open, do nothing
                }
            }
        }
    }

    /**
     * Gets the current circuit breaker state for monitoring.
     */
    suspend fun getCircuitState(): CircuitState {
        return circuitMutex.withLock { circuitState }
    }

    /**
     * Resets the circuit breaker to CLOSED state.
     * Use with caution, typically only for testing or manual recovery.
     */
    suspend fun resetCircuitBreaker() {
        circuitMutex.withLock {
            circuitState = CircuitState.CLOSED
            failureCount = 0
            MegaverseLogger.warn(
                "ResilientApiExecutor",
                "Circuit breaker manually reset to CLOSED"
            )
        }
    }

    /**
     * Circuit breaker states.
     */
    enum class CircuitState {
        /** Normal operation, requests pass through. */
        CLOSED,

        /** Failure threshold reached, requests are blocked. */
        OPEN,

        /** Testing if service has recovered, limited requests allowed. */
        HALF_OPEN
    }
}

/**
 * Extension function to execute a bulk operation with resilience for each item.
 *
 * @param items The items to process
 * @param operationName Name of the operation for logging
 * @param operation The operation to perform on each item
 * @return Flow emitting progress updates
 */
fun <T, R> ResilientApiExecutor.executeBulkWithResilience(
    items: List<T>,
    operationName: String,
    operation: suspend (T) -> MegaverseResult<R>
): Flow<BulkOperationProgress<T, R>> = flow {
    var successful = 0
    var failed = 0
    val total = items.size

    emit(BulkOperationProgress.InProgress(0, total))

    for ((index, item) in items.withIndex()) {
        val result = executeWithResilience("$operationName[$index]") {
            operation(item)
        }

        when (result) {
            is MegaverseResult.Success -> {
                successful++
                emit(BulkOperationProgress.ItemSuccess(item, result.data))
            }

            is MegaverseResult.Error -> {
                failed++
                emit(BulkOperationProgress.ItemFailed(item, result.exception))
            }
        }

        emit(BulkOperationProgress.InProgress(index + 1, total))
    }

    emit(BulkOperationProgress.Completed(successful, failed))
}

/**
 * Represents progress during bulk operations with resilience.
 */
sealed class BulkOperationProgress<out T, out R> {
    data class InProgress(val completed: Int, val total: Int) :
        BulkOperationProgress<Nothing, Nothing>()

    data class ItemSuccess<T, R>(val item: T, val result: R) :
        BulkOperationProgress<T, R>()

    data class ItemFailed<T>(val item: T, val error: MegaverseException) :
        BulkOperationProgress<T, Nothing>()

    data class Completed(val successful: Int, val failed: Int) :
        BulkOperationProgress<Nothing, Nothing>()
}
