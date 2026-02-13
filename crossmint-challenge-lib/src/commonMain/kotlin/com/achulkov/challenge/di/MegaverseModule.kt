package com.achulkov.challenge.di

import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.data.datasource.DefaultHttpClientFactory
import com.achulkov.challenge.data.datasource.IHttpClientFactory
import com.achulkov.challenge.domain.interfaces.IConfiguration
import com.achulkov.challenge.domain.interfaces.ILogger
import com.achulkov.challenge.domain.state.MegaverseStateManager
import com.achulkov.challenge.domain.usecases.ClearPositionsUseCase
import com.achulkov.challenge.domain.usecases.CreateAstralObjectsUseCase
import com.achulkov.challenge.domain.usecases.GetGoalMapUseCase
import com.achulkov.challenge.domain.usecases.SolveChallengeUseCase
import com.achulkov.challenge.network.MegaverseApi
import com.achulkov.challenge.network.MegaverseApiImpl
import com.achulkov.challenge.repository.MegaverseRepository
import com.achulkov.challenge.repository.MegaverseRepositoryImpl
import com.achulkov.challenge.utils.MegaverseLogger
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for Megaverse SDK dependency injection.
 * This module defines all the dependencies and their lifecycles.
 */
val megaverseModule = module {
    // Configuration
    single<IConfiguration> {
        MegaverseConfig()
    }

    // Logger - use singleton pattern
    single<ILogger> {
        MegaverseLogger
    }

    // HTTP Client Factory
    single<IHttpClientFactory> {
        DefaultHttpClientFactory()
    }

    // Network layer
    single<MegaverseApi> {
        MegaverseApiImpl(
            baseUrl = get<IConfiguration>().getBaseUrl(),
            httpClient = get<IHttpClientFactory>().createClient(
                enableLogging = get<IConfiguration>().isDebugLoggingEnabled()
            )
        )
    }

    // Repository layer
    single<MegaverseRepository> {
        MegaverseRepositoryImpl(
            api = get(),
            config = get()
        )
    }

    // State management
    single {
        MegaverseStateManager()
    }

    // Use cases
    factoryOf(::CreateAstralObjectsUseCase)
    factoryOf(::GetGoalMapUseCase)
    factoryOf(::ClearPositionsUseCase)
    factoryOf(::SolveChallengeUseCase)

    // SDK - factory with constructor injection
    factory {
        MegaverseSdk(
            configuration = get(),
            logger = get(),
            repository = get(),
            createAstralObjectsUseCase = get(),
            getGoalMapUseCase = get(),
            clearPositionsUseCase = get(),
            solveChallengeUseCase = get(),
            stateManager = getOrNull()
        )
    }
}

/**
 * All modules for the Megaverse SDK.
 */
val allModules = listOf(megaverseModule)
