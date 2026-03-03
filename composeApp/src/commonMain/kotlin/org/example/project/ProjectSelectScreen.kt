package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook // This looks like an open book
import androidx.compose.material3.Icon

// Use these if you haven't moved them to Color.kt yet
private val BackgroundColor = Color(0xFF121212)
private val CardColor = Color(0xFF333333)
private val ContentIconColor = Color(0xFF121212)

@Composable
fun ProjectSelectionScreen() {
    // BoxWithConstraints allows us to check screen size for landscape/portrait
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize() // This makes the page fit the entire screen
            .background(BackgroundColor)
    ) {
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centers the content vertically
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProjectItemCard("New Project") { PlusGraphic() }
                    ProjectItemCard("Open Project") { BookGraphic() }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    ProjectItemCard("New Project") { PlusGraphic() }
                    ProjectItemCard("Open Project") { BookGraphic() }
                }
            }
        }
    }
}

@Composable
fun ProjectItemCard(label: String, icon: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(CardColor, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 20.sp
        )
    }
}

@Composable
fun PlusGraphic() {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "New Project",
        modifier = Modifier.size(60.dp),
        tint = ContentIconColor
    )
}

@Composable
fun BookGraphic() {
    Icon(
        imageVector = Icons.Default.MenuBook, // Built-in "Open Book" icon
        contentDescription = "Open Project",
        modifier = Modifier.size(60.dp),
        tint = ContentIconColor
    )
}

// --- THE NORMAL PREVIEW ---
@Preview
@Composable
fun ProjectSelectionPreview() {
    MaterialTheme {
        ProjectSelectionScreen()
    }
}