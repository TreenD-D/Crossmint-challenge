package com.achulkov.challenge.network

import com.achulkov.challenge.domain.MegaverseException
import com.achulkov.challenge.domain.MegaverseResult
import com.achulkov.challenge.domain.Position
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MegaverseApiTest {

    private fun createMockClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): HttpClient {
        val engine = MockEngine { request ->
            handler(request)
        }
        return HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Test
    fun `createPolyanet returns success when API responds with 200`() = runTest {
        val client = createMockClient { request ->
            assertEquals("/api/polyanets", request.url.encodedPath)
            respond(
                content = ByteReadChannel("{}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val api = MegaverseApiImpl(baseUrl = "https://challenge.crossmint.io/api", httpClient = client)

        val result = api.createPolyanet(Position(0, 0), "test-candidate")

        assertTrue(result is MegaverseResult.Success)
    }

    @Test
    fun `createPolyanet returns error when API responds with error`() = runTest {
        val client = createMockClient {
            respond(
                content = ByteReadChannel("Bad Request"),
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val api = MegaverseApiImpl(httpClient = client)

        val result = api.createPolyanet(Position(0, 0), "test-candidate")

        assertTrue(result is MegaverseResult.Error)
        assertTrue(result.exception is MegaverseException.ApiError)
        assertEquals(400, (result.exception as MegaverseException.ApiError).statusCode)
    }

    @Test
    fun `getGoalMap parses response correctly`() = runTest {
        val jsonResponse = """
            {
                "goal": [
                    ["POLYANET", "SPACE"],
                    ["SPACE", "POLYANET"]
                ]
            }
        """.trimIndent()

        val client = createMockClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/goal"))
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val api = MegaverseApiImpl(httpClient = client)

        val result = api.getGoalMap("test-candidate")

        assertTrue(result is MegaverseResult.Success)
        val map = result.data
        assertEquals(2, map.dimensions.first)
        assertEquals(2, map.dimensions.second)
        assertEquals("POLYANET", map.goal[0][0])
        assertEquals("SPACE", map.goal[0][1])
    }
}
