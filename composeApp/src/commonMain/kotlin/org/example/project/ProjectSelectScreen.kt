package org.example.project

import TileViewModel
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow

private val BackgroundColor = Color(0xFF121212)
private val CardColor = Color(0xFF333333)
private val ContentIconColor = Color(0xFF121212)


object AppSettings {
    var userMode by mutableStateOf(UserMode.BEGINNER)
}

@Composable
fun ProjectSelectionScreen(
    tileViewModel: TileViewModel,
    audioPlayer: AudioPlayer?,
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

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 40.dp, start = 40.dp, end = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    text = "Select Mode",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))

                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProjectItemCard("New Project", onClick = onNavigateToMusic) { PlusGraphic() }
                        ProjectItemCard("Open Project", onClick = { }) { BookGraphic() }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        ProjectItemCard("New Project", onClick = onNavigateToMusic) { PlusGraphic() }
                        ProjectItemCard("Open Project", onClick = { }) { BookGraphic() }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (AppSettings.userMode != UserMode.BEGINNER &&
                    tileViewModel.recordedAudios.isNotEmpty()
                ) {

                    Text(
                        text = "Your Recordings",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        tileViewModel.recordedAudios.forEachIndexed { index, path ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardColor, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column(modifier = Modifier.weight(1f)) {

                                    Text(
                                        text = "Recording ${index + 1}",
                                        color = Color.White
                                    )

                                    Text(
                                        text = path.substringAfterLast("/"),
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }

                                Row {

                                    IconButton(onClick = {
                                        audioPlayer?.playImported(path)
                                    }) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                                    }

                                    IconButton(onClick = {
                                        tileViewModel.recordedAudios.remove(path)
                                    }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ProjectItemCard(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
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