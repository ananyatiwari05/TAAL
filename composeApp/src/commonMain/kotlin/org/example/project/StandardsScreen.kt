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

val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF333333)
val IconColor = Color(0xFF121212)
val IndicatorGray = Color(0xFF444444) // Color for "empty" pills

@Composable
fun StandardsScreen(onNavigateToProjects: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        val isLandscape = maxWidth > maxHeight

        // Centrally aligned vertically
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
                    StandardButton("Beginner", 1, onClick = onNavigateToProjects)
                    StandardButton("Intermediate", 2, onClick = onNavigateToProjects)
                    StandardButton("Advanced", 3, onClick = onNavigateToProjects)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    StandardButton("Beginner", 1, onClick = onNavigateToProjects)
                    StandardButton("Intermediate", 2, onClick = onNavigateToProjects)
                    StandardButton("Advanced", 3, onClick = onNavigateToProjects)
                }
            }
        }
    }
}

@Composable
fun StandardButton(label: String, level: Int, onClick: () -> Unit) {
    // Added Modifier.clickable here so the entire item registers the tap
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
            // Pass the level (1, 2, or 3) directly to the icon
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

        // Helper to decide if a part is "filled" or "hollow"
        fun getFillColor(index: Int) = if (index <= fillCount) IconColor else Color.Transparent
        fun getOutlineColor(index: Int) = if (index <= fillCount) IconColor else Color.Black

        // --- LEFT SHAPE (Index 1) ---
        val leftPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset(0f, topOffset), Size(itemWidth, itemHeight)),
                    topLeft = CornerRadius(cornerRadius),
                    bottomLeft = CornerRadius(cornerRadius)
                )
            )
        }
        // Draw Fill
        drawPath(path = leftPath, color = getFillColor(1))
        // Draw Outline
        drawPath(path = leftPath, color = getOutlineColor(1), style = Stroke(strokeWidth))

        // --- MIDDLE SQUARE (Index 2) ---
        val middleRect = Rect(Offset(itemWidth + spacing, topOffset), Size(itemWidth, itemHeight))
        // Draw Fill
        drawRect(color = getFillColor(2), topLeft = middleRect.topLeft, size = middleRect.size)
        // Draw Outline
        drawRect(color = getOutlineColor(2), topLeft = middleRect.topLeft, size = middleRect.size, style = Stroke(strokeWidth))

        // --- RIGHT SHAPE (Index 3) ---
        val rightPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset((itemWidth + spacing) * 2, topOffset), Size(itemWidth, itemHeight)),
                    topRight = CornerRadius(cornerRadius),
                    bottomRight = CornerRadius(cornerRadius)
                )
            )
        }
        // Draw Fill
        drawPath(path = rightPath, color = getFillColor(3))
        // Draw Outline
        drawPath(path = rightPath, color = getOutlineColor(3), style = Stroke(strokeWidth))
    }
}

@Preview
@Composable
fun StandardsPreview() {
    // Passed an empty lambda so the preview builds successfully
    StandardsScreen(onNavigateToProjects = {})
}