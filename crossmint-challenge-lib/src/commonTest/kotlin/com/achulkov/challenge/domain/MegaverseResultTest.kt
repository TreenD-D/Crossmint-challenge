package com.achulkov.challenge.domain

import kotlin.test.*

/**
 * Tests for MegaverseResult and MegaverseException.
 */
class MegaverseResultTest {

    @Test
    fun testSuccessResult() {
        val data = "test data"
        val result = MegaverseResult.Success(data)

        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals(data, result.getOrNull())
        assertEquals(data, result.getOrThrow())
    }

    @Test
    fun testSuccessWithNullData() {
        val result = MegaverseResult.Success<Unit>(Unit)

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun testErrorResult() {
        val exception = MegaverseException.NetworkError("Network error")
        val result = MegaverseResult.Error(exception)

        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertNull(result.getOrNull())
    }

    @Test
    fun testErrorResultThrowsOnGetOrThrow() {
        val exception = MegaverseException.NetworkError("Network error")
        val result = MegaverseResult.Error(exception)

        assertFailsWith<MegaverseException.NetworkError> {
            result.getOrThrow()
        }
    }

    @Test
    fun testErrorResultPreservesException() {
        val cause = RuntimeException("Root cause")
        val exception = MegaverseException.NetworkError("Network error", cause)
        val result = MegaverseResult.Error(exception)

        assertEquals(cause, (result.exception as MegaverseException.NetworkError).cause)
    }

    @Test
    fun testNetworkErrorException() {
        val exception = MegaverseException.NetworkError("Connection failed")

        assertEquals("Connection failed", exception.message)
        assertTrue(exception is MegaverseException)
    }

    @Test
    fun testNetworkErrorWithCause() {
        val cause = RuntimeException("Socket timeout")
        val exception = MegaverseException.NetworkError("Connection failed", cause)

        assertEquals("Connection failed", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun testApiErrorException() {
        val exception = MegaverseException.ApiError(429, "Rate Limited")

        assertEquals(429, exception.statusCode)
        assertEquals("API Error (429): Rate Limited", exception.message)
    }

    @Test
    fun testInvalidCandidateIdException() {
        val exception = MegaverseException.InvalidCandidateId("invalid-id")

        assertEquals("Invalid candidate ID: invalid-id", exception.message)
    }

    @Test
    fun testInvalidPositionException() {
        val position = Position(5, 10)
        val exception = MegaverseException.InvalidPosition(position)

        assertEquals("Invalid position: $position", exception.message)
    }

    @Test
    fun testSerializationErrorException() {
        val exception = MegaverseException.SerializationError("JSON parse error")

        assertEquals("JSON parse error", exception.message)
    }

    @Test
    fun testSerializationErrorWithCause() {
        val cause = IllegalStateException("Invalid JSON")
        val exception = MegaverseException.SerializationError("JSON parse error", cause)

        assertEquals("JSON parse error", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun testUnknownException() {
        val exception = MegaverseException.Unknown("Something went wrong")

        assertEquals("Something went wrong", exception.message)
    }

    @Test
    fun testUnknownExceptionWithCause() {
        val cause = RuntimeException("Unexpected error")
        val exception = MegaverseException.Unknown("Something went wrong", cause)

        assertEquals("Something went wrong", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun testSealedExceptionHierarchy() {
        val exceptions: List<MegaverseException> = listOf(
            MegaverseException.NetworkError("test"),
            MegaverseException.ApiError(500, "test"),
            MegaverseException.InvalidCandidateId("test"),
            MegaverseException.InvalidPosition(Position(0, 0)),
            MegaverseException.SerializationError("test"),
            MegaverseException.Unknown("test")
        )

        // Test exhaustiveness
        exceptions.forEach { exception ->
            when (exception) {
                is MegaverseException.NetworkError -> assertNotNull(exception.message)
                is MegaverseException.ApiError -> assertTrue(exception.statusCode > 0)
                is MegaverseException.InvalidCandidateId -> assertNotNull(exception.message)
                is MegaverseException.InvalidPosition -> assertNotNull(exception.message)
                is MegaverseException.SerializationError -> assertNotNull(exception.message)
                is MegaverseException.Unknown -> assertNotNull(exception.message)
            }
        }
    }

    @Test
    fun testResultEquality() {
        val result1 = MegaverseResult.Success("data")
        val result2 = MegaverseResult.Success("data")
        val result3 = MegaverseResult.Success("different")
        val error1 = MegaverseResult.Error(MegaverseException.NetworkError("error"))
        val error2 = MegaverseResult.Error(MegaverseException.NetworkError("error"))

        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
        // Error results with same exception type and message should be equal
        assertEquals(error1::class, error2::class)
    }

    @Test
    fun testSuccessResultTypes() {
        val stringResult = MegaverseResult.Success("string")
        val intResult = MegaverseResult.Success(42)
        val unitResult = MegaverseResult.Success(Unit)
        val mapResult = MegaverseResult.Success(MegaverseMap(emptyList()))

        assertEquals("string", stringResult.getOrNull())
        assertEquals(42, intResult.getOrNull())
        assertEquals(Unit, unitResult.getOrNull())
        assertNotNull(mapResult.getOrNull())
    }
}
