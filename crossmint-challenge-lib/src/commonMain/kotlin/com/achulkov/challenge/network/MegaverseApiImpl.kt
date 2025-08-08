package com.achulkov.challenge.network

import com.achulkov.challenge.domain.*
import com.achulkov.challenge.utils.MegaverseLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Ktor-based implementation of the Megaverse API.
 *
 * @param baseUrl The base URL of the Megaverse API
 * @param httpClient Optional custom HTTP client, if null a default will be created
 */
class MegaverseApiImpl(
    private val baseUrl: String = "https://challenge.crossmint.io/api",
    httpClient: HttpClient? = null
) : MegaverseApi {

    private val client = httpClient ?: HttpClient {
        install(HttpRedirect) {
            checkHttpMethod = false
        }
        followRedirects = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    MegaverseLogger.logOperation("NetworkRequest:", "$message")
                }
            }
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        install(HttpCallValidator) {
            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ResponseException
                if (clientException?.response?.status?.value == 429) {
                    MegaverseLogger.warn(
                        "Network",
                        "Rate limit (429) hit, waiting 2 seconds before retry..."
                    )
                    delay(2000)
                    throw exception // This will trigger a retry
                }
            }
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            retryOnException(maxRetries = 3, retryOnTimeout = false)
            retryIf { request, response ->
                response.status.value == 429
            }
            delayMillis { retry ->
                if (retry == 0) 2000L else retry * 2000L // 2s, 4s, 6s...
            }
        }
    }

    init {
        MegaverseLogger.info("MegaverseApi", "Initialized with base URL: $baseUrl")
    }

    override suspend fun createPolyanet(
        position: Position,
        candidateId: String
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "createPolyanet",
            "Creating polyanet at (${position.row}, ${position.column}) with candidateId"
        )

        return runCatching {
            val response = client.submitForm(
                url = "$baseUrl/polyanets",
                formParameters = Parameters.build {
                    append("candidateId", candidateId)
                    append("row", position.row.toString())
                    append("column", position.column.toString())
                }
            )

            MegaverseLogger.debug("Network", "Polyanet creation response: ${response.status}")
        }.fold(
            onSuccess = { MegaverseResult.Success(Unit) },
            onFailure = { e ->
                MegaverseLogger.error("Network", "Failed to create polyanet", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    override suspend fun deletePolyanet(
        position: Position,
        candidateId: String
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "deletePolyanet",
            "Deleting polyanet at (${position.row}, ${position.column}) with candidateId"
        )

        return runCatching {
            val response = client.submitForm(
                url = "$baseUrl/polyanets",
                formParameters = Parameters.build {
                    append("candidateId", candidateId)
                    append("row", position.row.toString())
                    append("column", position.column.toString())
                },
                encodeInQuery = false
            ) {
                method = HttpMethod.Delete
            }

            MegaverseLogger.debug("Network", "Polyanet deletion response: ${response.status}")
        }.fold(
            onSuccess = { MegaverseResult.Success(Unit) },
            onFailure = { e ->
                MegaverseLogger.error("Network", "Failed to delete polyanet", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    override suspend fun createSoloon(
        position: Position,
        color: SoloonColor,
        candidateId: String
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "createSoloon",
            "Creating ${color.value} soloon at (${position.row}, ${position.column}) with candidateId"
        )

        return runCatching {
            val response = client.submitForm(
                url = "$baseUrl/soloons",
                formParameters = Parameters.build {
                    append("candidateId", candidateId)
                    append("row", position.row.toString())
                    append("column", position.column.toString())
                    append("color", color.value)
                }
            )

            MegaverseLogger.debug("Network", "Soloon creation response: ${response.status}")
        }.fold(
            onSuccess = { MegaverseResult.Success(Unit) },
            onFailure = { e ->
                MegaverseLogger.error("Network", "Failed to create soloon", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    override suspend fun deleteSoloon(
        position: Position,
        candidateId: String
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "deleteSoloon",
            "Deleting soloon at (${position.row}, ${position.column}) with candidateId"
        )

        return runCatching {
            val response = client.submitForm(
                url = "$baseUrl/soloons",
                formParameters = Parameters.build {
                    append("candidateId", candidateId)
                    append("row", position.row.toString())
                    append("column", position.column.toString())
                },
                encodeInQuery = false
            ) {
                method = HttpMethod.Delete
            }

            MegaverseLogger.debug("Network", "Soloon deletion response: ${response.status}")
        }.fold(
            onSuccess = { MegaverseResult.Success(Unit) },
            onFailure = { e ->
                MegaverseLogger.error("Network", "Failed to delete soloon", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    override suspend fun createCometh(
        position: Position,
        direction: ComethDirection,
        candidateId: String
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "createCometh",
            "Creating ${direction.value}-facing cometh at (${position.row}, ${position.column}) with candidateId"
        )

        return runCatching {
            val response = client.submitForm(
                url = "$baseUrl/comeths",
                formParameters = Parameters.build {
                    append("candidateId", candidateId)
                    append("row", position.row.toString())
                    append("column", position.column.toString())
                    append("direction", direction.value)
                }
            )

            MegaverseLogger.debug("Network", "Cometh creation response: ${response.status}")
        }.fold(
            onSuccess = { MegaverseResult.Success(Unit) },
            onFailure = { e ->
                MegaverseLogger.error("Network", "Failed to create cometh", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    override suspend fun deleteCometh(
        position: Position,
        candidateId: String
    ): MegaverseResult<Unit> {
        MegaverseLogger.logOperation(
            "deleteCometh",
            "Deleting cometh at (${position.row}, ${position.column}) with candidateId"
        )

        return runCatching {
            val response = client.submitForm(
                url = "$baseUrl/comeths",
                formParameters = Parameters.build {
                    append("candidateId", candidateId)
                    append("row", position.row.toString())
                    append("column", position.column.toString())
                },
                encodeInQuery = false
            ) {
                method = HttpMethod.Delete
            }

            MegaverseLogger.debug("Network", "Cometh deletion response: ${response.status}")
        }.fold(
            onSuccess = { MegaverseResult.Success(Unit) },
            onFailure = { e ->
                MegaverseLogger.error("Network", "Failed to delete cometh", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    override suspend fun getGoalMap(candidateId: String): MegaverseResult<MegaverseMap> {
        MegaverseLogger.logOperation("getGoalMap", "Fetching goal map for candidate")

        return runCatching {
            val url = "$baseUrl/map/$candidateId/goal"
            MegaverseLogger.debug("Network", "Fetching goal map from: $url")

            val map: MegaverseMap = client.get(url).body()

            MegaverseLogger.logOperation(
                "getGoalMap",
                "Successfully retrieved goal map with dimensions ${map.dimensions}"
            )
            map
        }.fold(
            onSuccess = { MegaverseResult.Success(it) },
            onFailure = { e ->
                MegaverseLogger.error("getGoalMap", "Request failed", e)
                MegaverseResult.Error(mapException(e))
            }
        )
    }

    /**
     * Maps generic exceptions to specific MegaverseException types.
     */
    private fun mapException(exception: Throwable): MegaverseException {
        return when (exception) {
            is ResponseException -> {
                MegaverseLogger.error("Network", "Response exception: ${exception.response.status}")
                MegaverseException.ApiError(
                    exception.response.status.value,
                    exception.response.status.description
                )
            }

            is HttpRequestTimeoutException -> {
                MegaverseLogger.error("Network", "Request timeout")
                MegaverseException.NetworkError("Request timeout", exception)
            }

            is kotlinx.serialization.SerializationException -> {
                MegaverseLogger.error("Network", "Serialization error", exception)
                MegaverseException.SerializationError(
                    "Failed to serialize/deserialize data",
                    exception
                )
            }

            else -> {
                MegaverseLogger.error("Network", "Unknown error: ${exception.message}", exception)
                MegaverseException.Unknown(exception.message ?: "Unknown error occurred", exception)
            }
        }
    }
}