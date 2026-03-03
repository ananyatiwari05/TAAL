package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook

// Use these if you haven't moved them to Color.kt yet
private val BackgroundColor = Color(0xFF121212)
private val CardColor = Color(0xFF333333)
private val ContentIconColor = Color(0xFF121212)

@Composable
fun ProjectSelectionScreen(
    onNavigateToMusic: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        val isLandscape = maxWidth > maxHeight

        Column(modifier = Modifier.fillMaxSize()) {

            // Added Top Back Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 40.dp, start = 40.dp, end = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pass the navigation to New Project
                        ProjectItemCard("New Project", onClick = onNavigateToMusic) { PlusGraphic() }
                        ProjectItemCard("Open Project", onClick = { /* TODO: Implement later */ }) { BookGraphic() }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        // Pass the navigation to New Project
                        ProjectItemCard("New Project", onClick = onNavigateToMusic) { PlusGraphic() }
                        ProjectItemCard("Open Project", onClick = { /* TODO: Implement later */ }) { BookGraphic() }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItemCard(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    // Added Modifier.clickable so the whole block registers the tap
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
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
        imageVector = Icons.Default.MenuBook,
        contentDescription = "Open Project",
        modifier = Modifier.size(60.dp),
        tint = ContentIconColor
    )
}

@Preview
@Composable
fun ProjectSelectionPreview() {
    MaterialTheme {
        // Pass empty lambdas so the preview renders properly
        ProjectSelectionScreen(
            onNavigateToMusic = {},
            onNavigateBack = {}
        )
    }
}