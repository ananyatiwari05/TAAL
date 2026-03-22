package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun GuitarBeatsEditor(
    state: BeatEditorState,
    audioPlayer: AudioPlayer,
    onClose: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            state.currentStep = -1
            return@LaunchedEffect
        }

        val bpm = 120
        val stepDuration = 60000L / (bpm * 4)

        while (isPlaying) {
            state.currentStep = (state.currentStep + 1) % 16

            state.guitarGrid[state.currentStep].forEachIndexed { index, note ->
                if (note != null) {
                    audioPlayer.playSound("guitar_${index + 1}")
                }
            }
            delay(stepDuration)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.8f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2B2B2B))
                .padding(12.dp)
        ) {
            TopBar(
                isPlaying = isPlaying,
                onPlayToggle = { isPlaying = !isPlaying },
                onClose = onClose,
                onDelete = { state.clearGrid() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.width(75.dp)) {
                    LeftPanel()
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    PatternGrid(state)
                }
            }
        }
    }
}

@Composable
fun TopBar(
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Row {
            IconButton(onClick = onPlayToggle) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Toggle Play",
                    tint = if (isPlaying) Color.Yellow else Color.White
                )
            }

            IconButton(onClick = { /* save functionality daalini h */ }) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }

            IconButton(onClick = { /* functionality add karni h abhi */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    }
}

@Composable
fun PatternGrid(state: BeatEditorState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.guitarGrid.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEachIndexed { colIndex, cellValue ->
                    GridCell(
                        isActive = cellValue != null,
                        isCurrentStep = state.currentStep == rowIndex,
                        onClick = { state.toggleGuitarCell(rowIndex, colIndex) }
                    )
                }
            }
        }
    }
}

@Composable
fun GridCell(
    isActive: Boolean,
    isCurrentStep: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCurrentStep -> Color.Yellow
                    isActive -> Color(0xFFB55454)
                    else -> Color(0xFF444444)
                }
            )
            .clickable { onClick() }
    )
}

@Composable
fun LeftPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(7) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF555555))
            )
        }
    }
}