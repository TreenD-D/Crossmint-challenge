package com.achulkov.challenge.di

import com.achulkov.challenge.config.MegaverseConfig
import com.achulkov.challenge.domain.interfaces.IConfiguration
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Initializer for Koin dependency injection.
 * Provides convenient methods to start and stop the DI container.
 */
object KoinInitializer {
    /**
     * Initializes Koin with the Megaverse SDK modules.
     *
     * @param config Optional custom configuration. If null, a default configuration will be used.
     * @param appDeclaration Optional additional configuration for Koin
     */
    fun init(config: MegaverseConfig? = null, appDeclaration: KoinAppDeclaration? = null) {
        startKoin {
            appDeclaration?.invoke(this)
            
            // If a custom config is provided, override the default config module
            if (config != null) {
                modules(
                    module {
                        single<IConfiguration> { config }
                    },
                    *allModules.toTypedArray()
                )
            } else {
                modules(allModules)
            }
        }
    }

    /**
     * Stops the Koin DI container.
     * Useful for testing or when the SDK needs to be completely reset.
     */
    fun stop() {
        stopKoin()
    }
}
