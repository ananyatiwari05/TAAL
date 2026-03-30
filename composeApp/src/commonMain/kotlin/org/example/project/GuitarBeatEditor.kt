package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun GuitarBeatsEditor(
    audioPlayer: AudioPlayer,
    state: GuitarEditorState,
    onSave: (GuitarEditorState) -> Unit,
    onClose: () -> Unit
) {

    val guitarNotes = listOf(
        "guitar_a2.wav","guitar_b2.wav","guitar_c3.wav","guitar_d2.wav",
        "guitar_e2.wav","guitar_f2.wav","guitar_g2.wav",
        "guitar_a3.wav","guitar_b3.wav","guitar_c4.wav","guitar_d3.wav",
        "guitar_e3.wav","guitar_f3.wav","guitar_g3.wav",
        "guitar_a4.wav","guitar_b4.wav","guitar_c5.wav","guitar_d4.wav",
        "guitar_e4.wav","guitar_f4.wav","guitar_g4.wav"
    )

    val chordLibrary = listOf(

        Chord("C", listOf("guitar_c3.wav","guitar_e3.wav","guitar_g3.wav")),
        Chord("Cm", listOf("guitar_c3.wav","guitar_d3.wav","guitar_g3.wav")),
        Chord("Cmaj7", listOf("guitar_c3.wav","guitar_e3.wav","guitar_g3.wav","guitar_b3.wav")),
        Chord("C7", listOf("guitar_c3.wav","guitar_e3.wav","guitar_g3.wav","guitar_a3.wav")),

        Chord("D", listOf("guitar_d3.wav","guitar_f3.wav","guitar_a3.wav")),
        Chord("Dm", listOf("guitar_d3.wav","guitar_f3.wav","guitar_a3.wav")),
        Chord("D7", listOf("guitar_d3.wav","guitar_f3.wav","guitar_a3.wav","guitar_c4.wav")),

        Chord("E", listOf("guitar_e3.wav","guitar_g3.wav","guitar_b3.wav")),
        Chord("Em", listOf("guitar_e3.wav","guitar_g3.wav","guitar_b3.wav")),
        Chord("E7", listOf("guitar_e3.wav","guitar_g3.wav","guitar_b3.wav","guitar_d4.wav")),

        Chord("F", listOf("guitar_f3.wav","guitar_a3.wav","guitar_c4.wav")),
        Chord("Fm", listOf("guitar_f3.wav","guitar_g3.wav","guitar_c4.wav")),
        Chord("Fmaj7", listOf("guitar_f3.wav","guitar_a3.wav","guitar_c4.wav","guitar_e4.wav")),

        Chord("G", listOf("guitar_g3.wav","guitar_b3.wav","guitar_d4.wav")),
        Chord("Gm", listOf("guitar_g3.wav","guitar_a3.wav","guitar_d4.wav")),
        Chord("G7", listOf("guitar_g3.wav","guitar_b3.wav","guitar_d4.wav","guitar_f4.wav")),

        Chord("A", listOf("guitar_a3.wav","guitar_c4.wav","guitar_e4.wav")),
        Chord("Am", listOf("guitar_a3.wav","guitar_c4.wav","guitar_e4.wav")),
        Chord("A7", listOf("guitar_a3.wav","guitar_c4.wav","guitar_e4.wav","guitar_g4.wav")),

        Chord("B", listOf("guitar_b3.wav","guitar_d4.wav","guitar_f4.wav")),
        Chord("Bm", listOf("guitar_b3.wav","guitar_d4.wav","guitar_f4.wav")),
        Chord("B7", listOf("guitar_b3.wav","guitar_d4.wav","guitar_f4.wav","guitar_a4.wav")),

        Chord("Csus2", listOf("guitar_c3.wav","guitar_d3.wav","guitar_g3.wav")),
        Chord("Dsus2", listOf("guitar_d3.wav","guitar_e3.wav","guitar_a3.wav")),
        Chord("Gsus4", listOf("guitar_g3.wav","guitar_c4.wav","guitar_d4.wav")),
        Chord("Asus2", listOf("guitar_a3.wav","guitar_b3.wav","guitar_e4.wav"))
    )
    var selectedChord by remember { mutableStateOf<String?>(null) }

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
                    val note = guitarNotes[row]
                    audioPlayer.playSound(note)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            delay(stepDuration)
            currentStep = (currentStep + 1) % editorState.cols
        }
    }
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

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
                onClose = onClose,
                onSave = { onSave(editorState) },
                onDelete = {
                    editorState.clear()
                },
                onAdd = {
                    editorState.grid.forEach { row ->
                        for (i in row.indices) row[i] = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .fillMaxHeight()
                )  {

                    LeftPanel(
                        chordLibrary = chordLibrary,
                        selectedChord = selectedChord,
                        onChordClick = { chord ->

                            selectedChord =
                                if (selectedChord == chord.name) null else chord.name

                            val col = currentStep

                            editorState.grid = editorState.grid.mapIndexed { rowIndex, row ->
                                val newRow = row.toMutableList()
                                val note = guitarNotes.getOrNull(rowIndex)

                                if (note != null && chord.notes.contains(note)) {
                                    newRow[col] = true
                                }

                                newRow
                            }

                            chord.notes.forEach {
                                audioPlayer.playSound(it)
                            }
                        }
                    )
                }

                val horizontalScroll = rememberScrollState()
                val verticalScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(verticalScroll)
                        .horizontalScroll(horizontalScroll)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                horizontalScroll.dispatchRawDelta(-dragAmount.x)
                                verticalScroll.dispatchRawDelta(-dragAmount.y)
                            }
                        }
                ) {

                    PatternGrid(
                        state = editorState,
                        currentStep = currentStep,
                        onToggle = { row, col ->
                            editorState.toggle(row, col)

                            if (editorState.grid[row][col]) {
                                val note = guitarNotes[row]
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
fun LeftPanel(
    chordLibrary: List<Chord>,
    selectedChord: String?,
    onChordClick: (Chord) -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        items(chordLibrary) { chord ->

            val isSelected = chord.name == selectedChord

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Color(0xFFB55454)
                        else Color(0xFF555555)
                    )
                    .clickable {
                        onChordClick(chord)
                    }
            ) {
                Text(
                    text = chord.name,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
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

