package com.achulkov.challenge.domain

/**
 * Sealed class representing the result of a megaverse operation.
 */
sealed class MegaverseResult<out T> {

    /**
     * Represents a successful operation.
     */
    data class Success<T>(val data: T) : MegaverseResult<T>()

    /**
     * Represents a failed operation.
     */
    data class Error(val exception: MegaverseException) : MegaverseResult<Nothing>()

    /**
     * Returns true if the result is successful.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if the result is an error.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns the data if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the data if successful, throws the exception otherwise.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
}

/**
 * Sealed class for megaverse-specific exceptions.
 */
sealed class MegaverseException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    /**
     * Network-related errors.
     */
    class NetworkError(message: String, cause: Throwable? = null) :
        MegaverseException(message, cause)

    /**
     * API-related errors (4xx, 5xx responses).
     */
    class ApiError(val statusCode: Int, message: String) :
        MegaverseException("API Error ($statusCode): $message")

    /**
     * Invalid candidate ID.
     */
    class InvalidCandidateId(candidateId: String) :
        MegaverseException("Invalid candidate ID: $candidateId")

    /**
     * Invalid position coordinates.
     */
    class InvalidPosition(position: Position) : MegaverseException("Invalid position: $position")

    /**
     * Serialization/deserialization errors.
     */
    class SerializationError(message: String, cause: Throwable? = null) :
        MegaverseException(message, cause)

    /**
     * Unknown errors.
     */
    class Unknown(message: String, cause: Throwable? = null) : MegaverseException(message, cause)
}