package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DrumBeatEditor(
    state: DrumEditorState,
    audioPlayer: AudioPlayer,
    onSave: () -> Unit,
    onClose: () -> Unit
) {

    var playing by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val drumFiles = listOf(
        "kick.wav",
        "snare.wav",
        "closedhat.wav",
        "openhat.wav",
        "tom.wav",
        "crash.wav",
        "ride.wav",
        "clap.wav"
    )

    val drumNames = listOf(
        "Kick",
        "Snare",
        "Closed Hat",
        "Open Hat",
        "Tom",
        "Crash",
        "Ride",
        "Clap"
    )

    LaunchedEffect(playing) {

        if (!playing) return@LaunchedEffect

        val bpm = 60
        val stepDuration = 60000L / (bpm * 4)

        while (playing) {

            state.grid.forEachIndexed { row, steps ->

                if (steps[state.playhead]) {
                    audioPlayer.playSound(drumFiles[row])
                }
            }

            delay(stepDuration)

            state.playhead = (state.playhead + 1) % state.cols
        }
    }

    Column(
        modifier = Modifier
            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                playing = false
                onClose()
            }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Row {
                IconButton(onClick = { playing = !playing }) {
                    Icon(
                        imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playing) "Stop" else "Play",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    playing = false
                    onSave()
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                }
                IconButton(onClick = { state.clear() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
                IconButton(onClick = {  }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        state.grid.forEachIndexed { rowIndex, row ->

            Row(
                modifier = Modifier.padding(start = 20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {

                Text(
                    drumNames[rowIndex],
                    color = Color.White,
                    modifier = Modifier
                        .width(90.dp)
                        .padding(end = 6.dp)
                )

                Row(
                    modifier = Modifier.horizontalScroll(scrollState)
                ) {

                    row.forEachIndexed { colIndex, active ->

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(2.dp)
                                .background(
                                    when {
                                        colIndex == state.playhead -> Color.Yellow
                                        active -> Color.Green
                                        else -> Color.DarkGray
                                    }
                                )
                                .clickable {
                                    state.toggle(rowIndex, colIndex)
                                }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}