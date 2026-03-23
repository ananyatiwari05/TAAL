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
    audioPlayer: AudioPlayer,
    state: GuitarEditorState,
    onSave: (GuitarEditorState) -> Unit,
    onClose: () -> Unit
) {

    var editorState by remember { mutableStateOf(state) }

    LaunchedEffect(state) {
        editorState = state
    }

    val playing = remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }

    LaunchedEffect(playing.value) {

        if (!playing.value) return@LaunchedEffect

        val bpm = 60
        val stepDuration = 60000L / (bpm * 4)

        while (playing.value) {

            val activeRows = editorState.grid.mapIndexedNotNull { row, cols ->
                if (cols[currentStep]) row else null
            }

            activeRows.forEach { row ->
                try {
                    val note = audioPlayer.getGuitarNoteByIndex(row)
                    audioPlayer.playSound(note)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            delay(stepDuration)
            currentStep = (currentStep + 1) % editorState.cols
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.8f)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2B2B2B))
                .padding(12.dp)
        ) {

            TopBar(
                onPlay = { playing.value = !playing.value },
                onClose = { onClose() },
                onSave = { onSave(editorState) },
                onDelete = {
                    editorState.grid.forEach { row ->
                        for (i in row.indices) row[i] = false
                    }
                },
                onAdd = {
                    editorState.grid.forEach { row ->
                        for (i in row.indices) row[i] = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Column(modifier = Modifier.width(75.dp)) {
                    LeftPanel(rows = editorState.rows)
                }

                Column(modifier = Modifier.fillMaxWidth()) {

                    PatternGrid(
                        state = editorState,
                        currentStep = currentStep,
                        onToggle = { row, col ->

                            editorState.grid[row][col] =
                                !editorState.grid[row][col]

                            if (editorState.grid[row][col]) {
                                val note = audioPlayer.getGuitarNoteByIndex(row)
                                audioPlayer.playSound(note)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(
    onPlay: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }

        Row {
            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
            }

            IconButton(onClick = onSave) {
                Icon(Icons.Default.Save, null, tint = Color.White)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.White)
            }

            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun LeftPanel(rows: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF555555))
            )
        }
    }
}

@Composable
fun PatternGrid(
    state: GuitarEditorState,
    currentStep: Int,
    onToggle: (Int, Int) -> Unit
) {

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.grid.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEachIndexed { colIndex, isActive ->

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    colIndex == currentStep -> Color.Yellow
                                    isActive -> Color(0xFFB55454)
                                    else -> Color(0xFF444444)
                                }
                            )
                            .clickable {
                                onToggle(rowIndex, colIndex)
                            }
                    )
                }
            }
        }
    }
}