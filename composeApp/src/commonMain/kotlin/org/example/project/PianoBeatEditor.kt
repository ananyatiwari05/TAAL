package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun PianoBeatsEditor(
    audioPlayer: AudioPlayer,
    state: PianoEditorState,
    onSave: (PianoEditorState) -> Unit,
    onClose: () -> Unit
) {
    var editorState by remember { mutableStateOf(state.deepCopy()) }

    var playing by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }

    // ✅ Playback loop
    LaunchedEffect(playing) {

        if (!playing) {
            currentStep = 0
            return@LaunchedEffect
        }

        val bpm = 60
        val stepDuration = 60000L / (bpm * 4)

        while (playing) {

            val activeRows = editorState.grid.mapIndexedNotNull { row, cols ->
                if (cols[currentStep]) row else null
            }

            activeRows.forEach { row ->
                val note = audioPlayer.getPianoNoteByIndex(row)
                audioPlayer.playSound(note)
            }

            // ✅ sync playhead with UI
            editorState.playhead = currentStep

            delay(stepDuration)
            currentStep = (currentStep + 1) % editorState.cols
        }
    }

    // ✅ Pass control to UI
    PianoRollEditor(
        state = editorState,
        audioPlayer = audioPlayer,
        onSave = { onSave(editorState) },
        onClose = onClose,
        onPlayToggle = { playing = !playing }
    )
}