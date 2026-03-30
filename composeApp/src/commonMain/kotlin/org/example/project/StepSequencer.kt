package org.example.project

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class StepSequencer(
    private val metronome: MetronomeEngine,
    private val audioPlayer: AudioPlayer
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private var job: Job? = null

    private val activeDrumPatterns = mutableListOf<DrumEditorState>()
    private val activePianoPatterns = mutableListOf<PianoEditorState>()

    private val activeGuitarPatterns = mutableListOf<GuitarEditorState>()

    fun start() {

        val loopCount = 8

        if (job != null) return

        job = scope.launch {

            val bpm = metronome.bpm
            val stepDuration = 60000L / (bpm * 4)

            val maxSteps = listOf(
                activeDrumPatterns.firstOrNull()?.cols ?: 0,
                activePianoPatterns.firstOrNull()?.cols ?: 0,
                activeGuitarPatterns.firstOrNull()?.cols ?: 0
            ).maxOrNull() ?: 32

            var step = 0

            repeat(loopCount) {

                for (i in 0 until maxSteps) {

                    playDrums(step)
                    playPianos(step)
                    playGuitar(step)

                    delay(stepDuration)

                    step = (step + 1) % maxSteps
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun addDrumPattern(pattern: DrumEditorState) {
        activeDrumPatterns.add(pattern)
    }

    fun addPianoPattern(pattern: PianoEditorState) {
        activePianoPatterns.add(pattern)
    }

    private fun playDrums(step: Int) {

        val drumFiles = listOf(
            "kick.wav","snare.wav","closedhat.wav",
            "openhat.wav","tom.wav","crash.wav",
            "ride.wav","clap.wav"
        )

        val snapshot = activeDrumPatterns.toList()

        snapshot.forEach { pattern ->

            pattern.grid.forEachIndexed { row, steps ->
                val safeStep = step % steps.size

                if (steps[safeStep]) {
                    audioPlayer.playSound(drumFiles[row])
                }
            }

        }
    }

    private fun playPianos(step: Int) {

        activePianoPatterns.forEach { pattern ->

            pattern.playhead = step % pattern.cols

            pattern.grid.forEachIndexed { row, steps ->
                val safeStep = step % steps.size

                if (steps[safeStep]) {
                    audioPlayer.playSound(pianoNotes[row])
                }
            }
        }
    }
    private fun playGuitar(step: Int) {

        val snapshot = activeGuitarPatterns.toList()

        snapshot.forEach { pattern ->

            pattern.grid.forEachIndexed { row, steps ->

                val safeStep = step % steps.size

                if (steps[safeStep]) {
                    val note = audioPlayer.getGuitarNoteByIndex(row)
                    audioPlayer.playSound(note)
                }
            }
        }
    }
    fun addGuitarPattern(pattern: GuitarEditorState) {
        activeGuitarPatterns.add(pattern)
    }

    fun clearPatterns() {
        activeDrumPatterns.clear()
        activePianoPatterns.clear()
        activeGuitarPatterns.clear()
    }

}