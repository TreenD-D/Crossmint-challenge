plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }

    // JVM target for desktop/server use
    jvm("jvm")

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Additional targets for broader multiplatform support
    macosX64()
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.multiplatformSettings)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.turbine)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
        }

        androidUnitTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        // iOS dependencies
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest.get())
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        // macOS dependencies
        val macosX64Main by getting
        val macosArm64Main by getting
        val macosMain by creating {
            dependsOn(commonMain.get())
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val macosX64Test by getting
        val macosArm64Test by getting
        val macosTest by creating {
            dependsOn(commonTest.get())
            macosX64Test.dependsOn(this)
            macosArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.achulkov.challenge"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    showExceptions = true
                    showCauses = true
                    showStackTraces = true
                }
            }
        }
    }

    lint {
        disable += "UnsafeOptInUsageError"
    }
}

// Detekt configuration
detekt {
    config.from(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

// Kover code coverage configuration
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.databinding.*",
                    "*.BuildConfig*",
                    "androidx.*",
                    "com.achulkov.challenge.di.*"
                )
                packages("di", "databinding")
            }
        }
    }
}

// Publishing configuration
val libraryVersion = project.findProperty("VERSION") as String? ?: "1.0.0"

mavenPublishing {
    publishToMavenCentral()
    coordinates("com.achulkov.challenge", "crossmint-challenge-lib", libraryVersion)

    pom {
        name.set("Crossmint Megaverse SDK")
        description.set("A robust, scalable Kotlin Multiplatform library for interacting with the Crossmint Megaverse API")
        url.set("https://github.com/achulkov/crossmint-challenge")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("achulkov")
                name.set("Andrei Chulkov")
                email.set("achulkov@example.com")
            }
        }

        scm {
            url.set("https://github.com/achulkov/crossmint-challenge")
            connection.set("scm:git:git://github.com/achulkov/crossmint-challenge.git")
            developerConnection.set("scm:git:ssh://git@github.com/achulkov/crossmint-challenge.git")
        }
    }

    if (project.hasProperty("signing.keyId")) signAllPublications()
}

// GitHub Packages publishing
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/achulkov/crossmint-challenge")
            credentials {
                username = project.findProperty("githubPackagesUsername") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("githubPackagesPassword") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
