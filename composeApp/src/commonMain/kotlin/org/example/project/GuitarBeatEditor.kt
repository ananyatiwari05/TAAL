package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun guitarBeatsEditor() {
    Box(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f)) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2B2B2B))
                .padding(12.dp)
        ) {

            TopBar()
            Spacer(modifier = Modifier.width(10.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2B2B))
                    .padding(12.dp)
            ){
                Column (
                    modifier = Modifier
                        .width(75.dp)
                )
                {
                    LeftPanel()
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    PatternGrid()
                }
            }
        }
    }

}

@Composable
fun LeftPanel() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {  }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Row {
            IconButton(onClick = {  }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
            }
            IconButton(onClick = {  }) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
            }
            IconButton(onClick = {  }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
            IconButton(onClick = {  }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    }
}

@Composable
fun GridCell(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                if (isActive) Color(0xFFB55454)
                else Color(0xFF444444)
            )
    )
}

@Composable
fun PatternGrid() {
    val rows = 16
    val cols = 8

    val grid = List(rows) { row ->
        List(cols) { col ->
            col < 2
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        grid.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { isActive ->
                    GridCell(isActive)
                }
            }
        }
    }
}

/*LaunchedEffect(playing) {

    if (!playing) return@LaunchedEffect

    val bpm = 60
    val stepDuration = 60000L / (bpm * 4)

    var playheadRow = 0   // 👈 moves top → bottom

    while (playing) {

        // iterate through columns of current row
        state.grid[playheadRow].forEachIndexed { col, isActive ->

            if (isActive) {
                audioPlayer.playSound(drumFiles[col])
                // 👆 now column decides sound
            }
        }

        delay(stepDuration)

        playheadRow = (playheadRow + 1) % state.rows
    }
}
fun GridCell(isActive: Boolean, isPlaying: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                when {
                    isPlaying -> Color.Yellow   // 👈 highlight current row
                    isActive -> Color(0xFFB55454)
                    else -> Color(0xFF444444)
                }
            )
    )
}
*/




