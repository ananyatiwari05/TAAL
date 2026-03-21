package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF333333)
val IconColor = Color(0xFF121212)
val IndicatorGray = Color(0xFF444444) // Color for "empty" pills

@Composable
fun StandardsScreen(
    onNavigateToProjects: () -> Unit,
    settingsManager: SettingsManager
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        val isLandscape = maxWidth > maxHeight


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Standards",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardButton("Beginner", 1, onClick = {
                        scope.launch {
                            AppSettings.userMode = UserMode.BEGINNER
                            settingsManager.saveMode(UserMode.BEGINNER)
                        }
                        onNavigateToProjects()
                    })
                    StandardButton("Intermediate", 2, onClick = {
                        scope.launch {
                            AppSettings.userMode = UserMode.INTERMEDIATE
                            settingsManager.saveMode(UserMode.INTERMEDIATE)
                        }
                        onNavigateToProjects()
                    })
                    StandardButton("Advanced", 3, onClick = {
                        scope.launch {
                            AppSettings.userMode = UserMode.ADVANCED
                            settingsManager.saveMode(UserMode.ADVANCED)
                        }
                        onNavigateToProjects()
                    })
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    StandardButton("Beginner", 1, onClick = {
                        scope.launch {
                            AppSettings.userMode = UserMode.BEGINNER
                            settingsManager.saveMode(UserMode.BEGINNER)
                        }
                        onNavigateToProjects()
                    })
                    StandardButton("Intermediate", 2, onClick = {
                        scope.launch {
                            AppSettings.userMode = UserMode.INTERMEDIATE
                            settingsManager.saveMode(UserMode.INTERMEDIATE)
                        }
                        onNavigateToProjects()
                    })
                    StandardButton("Advanced", 3, onClick = {
                        scope.launch {
                            AppSettings.userMode = UserMode.ADVANCED
                            settingsManager.saveMode(UserMode.ADVANCED)
                        }
                        onNavigateToProjects()
                    })
                }
            }
        }
    }
}

@Composable
fun StandardButton(label: String, level: Int, onClick: () -> Unit) {

    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 90.dp)
                .background(CardBackground, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {

            StandardIcon(fillCount = level)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
fun StandardIcon(fillCount: Int) {
    Canvas(modifier = Modifier.size(width = 100.dp, height = 50.dp)) {
        val spacing = 6.dp.toPx()
        val itemWidth = (size.width - (spacing * 2)) / 3
        val itemHeight = size.height * 0.7f
        val topOffset = (size.height - itemHeight) / 2
        val cornerRadius = 15.dp.toPx()
        val strokeWidth = 2.dp.toPx()


        fun getFillColor(index: Int) = if (index <= fillCount) IconColor else Color.Transparent
        fun getOutlineColor(index: Int) = if (index <= fillCount) IconColor else Color.Black


        val leftPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset(0f, topOffset), Size(itemWidth, itemHeight)),
                    topLeft = CornerRadius(cornerRadius),
                    bottomLeft = CornerRadius(cornerRadius)
                )
            )
        }

        drawPath(path = leftPath, color = getFillColor(1))

        drawPath(path = leftPath, color = getOutlineColor(1), style = Stroke(strokeWidth))


        val middleRect = Rect(Offset(itemWidth + spacing, topOffset), Size(itemWidth, itemHeight))

        drawRect(color = getFillColor(2), topLeft = middleRect.topLeft, size = middleRect.size)

        drawRect(color = getOutlineColor(2), topLeft = middleRect.topLeft, size = middleRect.size, style = Stroke(strokeWidth))


        val rightPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset((itemWidth + spacing) * 2, topOffset), Size(itemWidth, itemHeight)),
                    topRight = CornerRadius(cornerRadius),
                    bottomRight = CornerRadius(cornerRadius)
                )
            )
        }

        drawPath(path = rightPath, color = getFillColor(3))

        drawPath(path = rightPath, color = getOutlineColor(3), style = Stroke(strokeWidth))
    }
}

//@Preview
//@Composable
//fun StandardsPreview() {
//
//    StandardsScreen(onNavigateToProjects = {})
//}