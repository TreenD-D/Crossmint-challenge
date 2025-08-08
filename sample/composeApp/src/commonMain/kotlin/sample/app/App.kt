package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achulkov.challenge.MegaverseSdk
import com.achulkov.challenge.domain.MegaverseMap
import com.achulkov.challenge.domain.Position
import com.achulkov.challenge.repository.CreationProgress
import kotlinx.coroutines.launch

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { MainViewModel(coroutineScope) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(
            text = "Crossmint Megaverse SDK Demo",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        // Debug mode indicator
        BasicText(
            text = "🐛 DEBUG MODE ACTIVE - Detailed logging enabled",
            style = TextStyle(
                fontSize = 10.sp,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!viewModel.isConfigured) {
            ConfigurationSection(viewModel)
        } else {
            ConfiguredSection(viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goal Map Display
        if (viewModel.showGoalMap && viewModel.hasGoalMap) {
            GoalMapDisplay(viewModel.goalMap!!)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Progress messages
        ProgressSection(viewModel.progressMessages)
    }
}

@Composable
private fun ConfigurationSection(viewModel: MainViewModel) {
    Column {
        BasicText(
            text = "Enter your Candidate ID:",
            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
        )
        BasicTextField(
            value = viewModel.candidateId,
            onValueChange = viewModel::updateCandidateId,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(8.dp),
            textStyle = TextStyle(color = Color.Black)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Configure SDK button
    Box(
        modifier = Modifier
            .background(
                if (viewModel.canConfigureSDK) Color(0xFF2196F3) else Color.Gray,
                RoundedCornerShape(4.dp)
            )
            .clickable(enabled = true/*viewModel.canConfigureSDK*/) {
                viewModel.configureSDK()
            }
            .padding(16.dp, 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = "Configure SDK",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun ConfiguredSection(viewModel: MainViewModel) {
    BasicText(
        text = "SDK configured for candidate: ${viewModel.currentCandidateId}",
        style = TextStyle(fontSize = 14.sp, color = Color.Black)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            text = "Create Polyanet",
            enabled = !viewModel.isLoading,
            onClick = viewModel::createSinglePolyanet
        )

        ActionButton(
            text = "X-Pattern",
            enabled = !viewModel.isLoading,
            onClick = viewModel::createXPattern
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            text = "Get Goal Map",
            enabled = !viewModel.isLoading,
            onClick = viewModel::fetchGoalMap
        )

        ActionButton(
            text = "Solve Challenge",
            enabled = !viewModel.isLoading,
            onClick = viewModel::solveChallenge
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            text = "Clear Messages",
            enabled = !viewModel.isLoading,
            onClick = viewModel::clearMessages
        )

        if (viewModel.hasGoalMap) {
            ActionButton(
                text = if (viewModel.showGoalMap) "Hide Map" else "Show Map",
                enabled = true,
                onClick = viewModel::toggleGoalMapVisibility
            )
        }
    }

    // Loading indicator
    if (viewModel.isLoading) {
        Spacer(modifier = Modifier.height(8.dp))
        BasicText(
            text = "⏳ Loading...",
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (enabled) Color(0xFF2196F3) else Color.Gray,
                RoundedCornerShape(4.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp, 6.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ProgressSection(messages: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(messages.reversed()) { message ->
            BasicText(
                text = message,
                style = TextStyle(fontSize = 12.sp, color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun GoalMapDisplay(map: MegaverseMap) {
    Column {
        BasicText(
            text = "Goal Map (${map.dimensions.first}x${map.dimensions.second})",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Map grid
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            itemsIndexed(map.goal) { rowIndex, row ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    itemsIndexed(row) { colIndex, cell ->
                        MapCell(
                            content = cell,
                            position = Position(rowIndex, colIndex)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        MapLegend()
    }
}

@Composable
private fun MapCell(
    content: String,
    position: Position
) {
    val (emoji, backgroundColor) = when {
        content == "SPACE" -> "🌌" to Color(0xFF1A1A2E)
        content == "POLYANET" -> "🪐" to Color(0xFF4CAF50)
        content.endsWith("_SOLOON") -> {
            val color = content.substringBefore("_SOLOON")
            when (color) {
                "BLUE" -> "🔵" to Color(0xFF2196F3)
                "RED" -> "🔴" to Color(0xFFF44336)
                "PURPLE" -> "🟣" to Color(0xFF9C27B0)
                "WHITE" -> "⚪" to Color(0xFF9E9E9E)
                else -> "🔮" to Color(0xFF607D8B)
            }
        }

        content.endsWith("_COMETH") -> {
            val direction = content.substringBefore("_COMETH")
            when (direction) {
                "UP" -> "⬆️" to Color(0xFFFF9800)
                "DOWN" -> "⬇️" to Color(0xFFFF9800)
                "LEFT" -> "⬅️" to Color(0xFFFF9800)
                "RIGHT" -> "➡️" to Color(0xFFFF9800)
                else -> "☄️" to Color(0xFFFF9800)
            }
        }

        else -> "❓" to Color(0xFF757575)
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .background(backgroundColor, RoundedCornerShape(2.dp))
            .border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = emoji,
            style = TextStyle(fontSize = 10.sp)
        )
    }
}

@Composable
private fun MapLegend() {
    Column {
        BasicText(
            text = "Legend:",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        val legendItems = listOf(
            "🌌 SPACE" to Color(0xFF1A1A2E),
            "🪐 POLYANET" to Color(0xFF4CAF50),
            "🔵 BLUE_SOLOON" to Color(0xFF2196F3),
            "🔴 RED_SOLOON" to Color(0xFFF44336),
            "🟣 PURPLE_SOLOON" to Color(0xFF9C27B0),
            "⚪ WHITE_SOLOON" to Color(0xFF9E9E9E),
            "⬆️ UP_COMETH" to Color(0xFFFF9800),
            "⬇️ DOWN_COMETH" to Color(0xFFFF9800),
            "⬅️ LEFT_COMETH" to Color(0xFFFF9800),
            "➡️ RIGHT_COMETH" to Color(0xFFFF9800)
        )

        LazyColumn(
            modifier = Modifier.height(120.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(legendItems) { (text, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, RoundedCornerShape(2.dp))
                            .border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicText(
                        text = text,
                        style = TextStyle(fontSize = 10.sp, color = Color.Black)
                    )
                }
            }
        }
    }
}