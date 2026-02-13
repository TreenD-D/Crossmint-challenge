package com.achulkov.challenge.data.datasource

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory interface for creating HTTP clients.
 * Allows for platform-specific implementations and easier testing.
 */
interface IHttpClientFactory {
    /**
     * Creates a configured HTTP client.
     *
     * @param enableLogging Whether to enable HTTP logging
     * @return Configured HttpClient instance
     */
    fun createClient(enableLogging: Boolean = false): HttpClient
}

/**
 * Default implementation of HTTP client factory.
 * Creates a Ktor HTTP client with JSON serialization and logging support.
 */
class DefaultHttpClientFactory : IHttpClientFactory {
    override fun createClient(enableLogging: Boolean): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            // Default headers
            defaultRequest {
                headers.append("Accept", "application/json")
                headers.append("Content-Type", "application/json")
            }
        }
    }
}
