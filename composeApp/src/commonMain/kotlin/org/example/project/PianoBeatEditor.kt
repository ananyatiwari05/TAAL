package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun PianoBeatEditor(
    state: PianoEditorState,
    audioPlayer: AudioPlayer,
    onSave: () -> Unit,
    onClose: () -> Unit
) {

    var playing by remember { mutableStateOf(false) }

    LaunchedEffect(playing) {

        if (!playing) return@LaunchedEffect

        val bpm = 60
        val stepDuration = 60000L / (bpm * 4)

        while (playing) {

            state.grid.forEachIndexed { row, steps ->

                if (steps[state.playhead]) {
                    audioPlayer.playSound(pianoNotes[row])
                }
            }

            delay(stepDuration)

            state.playhead = (state.playhead + 1) % state.cols
        }
    }

    PianoRollEditor(
        state = state,
        audioPlayer = audioPlayer,
        onSave = onSave,
        onClose = onClose,
        onPlayToggle = { playing = !playing },
        isPlaying = playing
    )
}