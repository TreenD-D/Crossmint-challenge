package sample.app

import androidx.compose.runtime.*
import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.domain.MegaverseMap
import com.achulkov.challenge.domain.Position
import com.achulkov.challenge.repository.CreationProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ViewModel for the main sample app screen.
 * Handles all business logic and state management following MVVM pattern.
 */
class MainViewModel(
    private val coroutineScope: CoroutineScope
) {

    private val sdk = MegaverseSdk(enableDebugLogging = true)

    // View State
    var candidateId by mutableStateOf("")
        private set

    var isConfigured by mutableStateOf(false)
        private set

    var progressMessages by mutableStateOf(listOf<String>())
        private set

    var goalMap by mutableStateOf<MegaverseMap?>(null)
        private set

    var showGoalMap by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Actions

    fun updateCandidateId(newId: String) {
        candidateId = newId
    }

    fun configureSDK() {
        if (candidateId.isNotBlank()) {
            sdk.setCandidateId(candidateId)
            isConfigured = true
            addProgressMessage("SDK configured with candidate ID: $candidateId")
        }
    }

    fun createSinglePolyanet() {
        if (!isConfigured) return

        isLoading = true
        addProgressMessage("Creating single polyanet...")

        coroutineScope.launch {
            try {
                val result = sdk.createPolyanet(Position(3, 3))
                val message = when {
                    result.isSuccess -> "✅ Polyanet created at (3, 3)"
                    else -> "❌ Failed to create polyanet: ${result.getOrNull()}"
                }
                addProgressMessage(message)
            } catch (e: Exception) {
                addProgressMessage("❌ Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun createXPattern() {
        if (!isConfigured) return

        isLoading = true
        addProgressMessage("Starting X-pattern challenge...")

        coroutineScope.launch {
            try {
                sdk.createXPatternChallenge(11).collect { progress ->
                    when (progress) {
                        is CreationProgress.InProgress -> {
                            addProgressMessage("Progress: ${progress.completed}/${progress.total}")
                        }

                        is CreationProgress.ObjectCreated -> {
                            val pos = progress.astralObject.position
                            addProgressMessage("✅ Created at (${pos.row}, ${pos.column})")
                        }

                        is CreationProgress.ObjectFailed -> {
                            val pos = progress.astralObject.position
                            addProgressMessage("❌ Failed at (${pos.row}, ${pos.column}): ${progress.error.message}")
                        }

                        is CreationProgress.Completed -> {
                            addProgressMessage("🎉 X-Pattern complete! ${progress.successful} successful, ${progress.failed} failed")
                            isLoading = false
                        }
                    }
                }
            } catch (e: Exception) {
                addProgressMessage("❌ X-Pattern error: ${e.message}")
                isLoading = false
            }
        }
    }

    fun fetchGoalMap() {
        if (!isConfigured) return

        isLoading = true
        addProgressMessage("Fetching goal map...")

        coroutineScope.launch {
            try {
                val result = sdk.getGoalMap()
                val message = when (result) {
                    is com.achulkov.challenge.domain.MegaverseResult.Success -> {
                        val map = result.data
                        goalMap = map
                        showGoalMap = true
                        "✅ Goal map retrieved! Dimensions: ${map.dimensions}"
                    }

                    is com.achulkov.challenge.domain.MegaverseResult.Error -> {
                        "❌ Failed to get goal map: ${result.exception.message}"
                    }
                }
                addProgressMessage(message)
            } catch (e: Exception) {
                addProgressMessage("❌ Goal map error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun solveChallenge() {
        if (!isConfigured) return

        isLoading = true
        addProgressMessage("Solving complete challenge...")

        coroutineScope.launch {
            try {
                sdk.solveChallenge().collect { progress ->
                    when (progress) {
                        is CreationProgress.InProgress -> {
                            addProgressMessage("Progress: ${progress.completed}/${progress.total}")
                        }

                        is CreationProgress.ObjectCreated -> {
                            val pos = progress.astralObject.position
                            val type = progress.astralObject::class.simpleName
                            addProgressMessage("✅ $type at (${pos.row}, ${pos.column})")
                        }

                        is CreationProgress.ObjectFailed -> {
                            val pos = progress.astralObject.position
                            addProgressMessage("❌ Failed at (${pos.row}, ${pos.column}): ${progress.error.message}")
                        }

                        is CreationProgress.Completed -> {
                            addProgressMessage("🚀 Challenge complete! ${progress.successful} successful, ${progress.failed} failed")
                            isLoading = false
                        }
                    }
                }
            } catch (e: Exception) {
                addProgressMessage("❌ Challenge error: ${e.message}")
                isLoading = false
            }
        }
    }

    fun clearMessages() {
        progressMessages = listOf()
    }

    fun toggleGoalMapVisibility() {
        showGoalMap = !showGoalMap
    }

    private fun addProgressMessage(message: String) {
        progressMessages = progressMessages + message
    }

    // Computed properties
    val canConfigureSDK: Boolean get() = candidateId.isNotBlank()
    val hasGoalMap: Boolean get() = goalMap != null
    val currentCandidateId: String? get() = if (isConfigured) sdk.getCandidateId() else null
}