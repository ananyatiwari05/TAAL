package org.example.project

import java.io.File
import javax.sound.sampled.*
import kotlin.math.abs
import dev.atsushieno.ktmidi.*
import org.example.project.getExportPath

val guitarNotes = listOf(
    "guitar_a2.wav","guitar_a3.wav","guitar_a4.wav",
    "guitar_b2.wav","guitar_b3.wav","guitar_b4.wav",
    "guitar_c3.wav","guitar_c4.wav","guitar_c5.wav",
    "guitar_d2.wav","guitar_d3.wav","guitar_d4.wav",
    "guitar_e2.wav","guitar_e3.wav","guitar_e4.wav",
    "guitar_f2.wav","guitar_f3.wav","guitar_f4.wav",
    "guitar_g2.wav","guitar_g3.wav","guitar_g4.wav"
)

actual class AudioExporter {

    actual fun exportBeat(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String,
        durationSeconds: Int
    ) {

        val sampleRate = 48000
        val stepDurationSec = 60.0 / (bpm * 4)
        val patternSteps = state.grid[0].size
        val totalSteps = ((durationSeconds) / stepDurationSec).toInt()
        val tailSeconds = 8
        val totalSamples = (sampleRate * (stepDurationSec * totalSteps + tailSeconds)).toInt()
        val mixBuffer = FloatArray(totalSamples)

        for (instrumentIndex in categories.indices) {
            val category = categories[instrumentIndex]

            for (step in 0 until totalSteps) {

                println("STEP: $step | instrument: $instrumentIndex")

                val actualStep = step % patternSteps
                val tileId = state.grid[instrumentIndex][actualStep] ?: continue
                val tile = category.tiles.find { it.id == tileId } ?: continue
                val beat = tile.beat ?: continue

                val startSample = (step * stepDurationSec * sampleRate).toInt()
                val velocity = state.velocityGrid[instrumentIndex][actualStep]

                when {
                    beat.drumPattern != null -> {
                        println("🥁 Drum triggered")
                        mixDrumPattern(beat.drumPattern, startSample, mixBuffer, sampleRate, velocity)
                    }

                    beat.pianoPattern != null -> {
                        println("🎹 Piano triggered")
                        mixPianoPattern(beat.pianoPattern, startSample, mixBuffer, sampleRate, velocity)
                    }

                    beat.guitarPattern != null -> {
                        println("🎸 Guitar pattern triggered")
                        mixGuitarPattern(beat.guitarPattern, startSample, mixBuffer, sampleRate, velocity)
                    }

                    beat.fileName != null -> {
                        println("🎵 File triggered: ${beat.fileName}")
                        val audioData = loadWav(beat.fileName) ?: continue
                        val maxLength = (stepDurationSec * sampleRate).toInt()

                        for (i in 0 until minOf(audioData.size, maxLength)) {
                            val index = startSample + i
                            if (index >= mixBuffer.size) break
                            mixBuffer[index] = (mixBuffer[index] + audioData[i] * velocity)
                                .coerceIn(-1f, 1f)
                        }
                    }
                }
            }
        }

        val max = mixBuffer.maxOfOrNull { abs(it) } ?: 0f
        println("🔥 FINAL BUFFER MAX = $max")

        val gain = 2.5f

        if (max > 0f) {
            for (i in mixBuffer.indices) {
                mixBuffer[i] = (mixBuffer[i] / max * gain)
                    .coerceIn(-1f, 1f)
            }
        }

        val safePath = getExportPath(File(outputPath).name)
        writeWav(mixBuffer, sampleRate, safePath)
        println("✅ WAV EXPORTED: $safePath")
    }

    actual fun exportMidi(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String
    ) {

        val music = MidiMusic()
        val track = MidiTrack()

        val ticksPerQuarter = 480
        music.deltaTimeSpec = ticksPerQuarter

        val stepTicks = ticksPerQuarter / 4
        val totalSteps = 32

        val messages = mutableListOf<Pair<Int, ByteArray>>()

        categories.forEachIndexed { instrumentIndex, category ->

            val tileMap = category.tiles.associateBy { it.id }

            for (step in 0 until totalSteps) {

                println("MIDI STEP: $step | instrument: $instrumentIndex")

                val tileId = state.grid[instrumentIndex][step] ?: continue
                val tile = tileMap[tileId] ?: continue
                val beat = tile.beat ?: continue

                val velocityFloat = state.velocityGrid[instrumentIndex][step]
                val velocity = (velocityFloat * 127).toInt().coerceIn(1, 127)

                val tick = step * stepTicks

                when {
                    beat.drumPattern != null -> {
                        println("🥁 MIDI Drum")
                        collectDrumMidi(messages, beat.drumPattern, tick, stepTicks, velocity)
                    }

                    beat.pianoPattern != null -> {
                        println("🎹 MIDI Piano")
                        collectPianoMidi(messages, beat.pianoPattern, tick, stepTicks, velocity)
                    }

                    beat.guitarPattern != null -> {
                        println("🎸 MIDI Guitar")
                        collectGuitarMidi(messages, beat.guitarPattern, tick, stepTicks, velocity)
                    }

                    beat.fileName != null -> {
                        println("🎵 MIDI File trigger")
                        val note = (60 + instrumentIndex).coerceIn(0, 127)
                        messages.add(tick to byteArrayOf(0x90.toByte(), note.toByte(), velocity.toByte()))
                        messages.add((tick + stepTicks / 2) to byteArrayOf(0x80.toByte(), note.toByte(), 0))
                    }
                }
            }
        }

        messages.sortBy { it.first }

        var lastTime = 0

        messages.forEach { (time, msg) ->

            val delta = time - lastTime
            lastTime = time

            val status = msg[0].toInt() and 0xFF
            val data1 = msg[1].toInt() and 0xFF
            val data2 = msg[2].toInt() and 0xFF

            track.messages.add(
                MidiMessage(
                    delta,
                    MidiEvent(status, data1, data2)
                )
            )
        }

        music.tracks.add(track)

        val bytes = mutableListOf<Byte>()
        music.write(bytes)

        val safePath = getExportPath(File(outputPath).name)
        File(safePath).writeBytes(bytes.toByteArray())
    }

    private fun loadWav(path: String): FloatArray? {
        return try {

            val file = if (path.startsWith("/") || path.startsWith("content://")) {
                File(path)
            } else {
                val resource = javaClass.classLoader.getResource(path) ?: return null
                File(resource.toURI())
            }

            val audioInput = AudioSystem.getAudioInputStream(file)
            val baseFormat = audioInput.format

            val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate,
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
            )

            val stream = AudioSystem.getAudioInputStream(decodedFormat, audioInput)
            val bytes = stream.readBytes()

            val channels = decodedFormat.channels
            val totalSamples = bytes.size / (2 * channels)

            val samples = FloatArray(totalSamples)
            var sampleIndex = 0

            for (i in 0 until bytes.size step 2 * channels) {
                var sum = 0f

                for (ch in 0 until channels) {
                    val low = bytes[i + ch * 2].toInt() and 0xff
                    val high = bytes[i + ch * 2 + 1].toInt()
                    val sample = (high shl 8) or low
                    sum += sample / 32768f
                }

                samples[sampleIndex++] = sum / channels
            }

            val maxVal = samples.maxOfOrNull { abs(it) } ?: 0f
            println("📊 WAV LOADED: $path | max amplitude = $maxVal")

            samples

        } catch (e: Exception) {
            println("❌ FAILED TO LOAD: $path")
            e.printStackTrace()
            null
        }
    }

    private fun mixGuitarPattern(
        pattern: GuitarEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
        val stepDuration = sampleRate / 4

        pattern.grid.forEachIndexed { row, steps ->

            val fileName = guitarNotes.getOrNull(row) ?: return@forEachIndexed
            val sound = loadWav(fileName) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                println("🎸 Guitar triggered row=$row col=$col file=$fileName")

                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val index = offset + i
                    if (index >= buffer.size) break

                    buffer[index] = (buffer[index] + sound[i] * velocity)
                        .coerceIn(-1f, 1f)
                }
            }
        }
    }
    private fun writeWav(data: FloatArray, sampleRate: Int, path: String) {

        val byteData = ByteArray(data.size * 4)

        var i = 0
        data.forEach { sample ->
            val clamped = (sample.coerceIn(-1f, 1f) * 32767).toInt()

            val low = (clamped and 0xff).toByte()
            val high = ((clamped shr 8) and 0xff).toByte()

            byteData[i++] = low
            byteData[i++] = high
            byteData[i++] = low
            byteData[i++] = high
        }

        val format = AudioFormat(sampleRate.toFloat(), 16, 2, true, false)
        val byteStream = byteData.inputStream()
        val audioStream = AudioInputStream(byteStream, format, data.size.toLong())

        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, File(path))
    }

    private fun mixDrumPattern(
        pattern: DrumEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
        val stepDuration = sampleRate / 4

        val drumFiles = listOf(
            "kick.wav","snare.wav","closedhat.wav",
            "openhat.wav","tom.wav","crash.wav",
            "ride.wav","clap.wav"
        )

        pattern.grid.forEachIndexed { row, steps ->
            val file = drumFiles.getOrNull(row) ?: return@forEachIndexed
            val sound = loadWav(file) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                println("🥁 Drum row=$row col=$col file=$file")

                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val index = offset + i
                    if (index >= buffer.size) break
                    buffer[index] = (buffer[index] + sound[i] * velocity)
                        .coerceIn(-1f, 1f)
                }
            }
        }
    }

    private fun mixPianoPattern(
        pattern: PianoEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
        val stepDuration = sampleRate / 4

        pattern.grid.forEachIndexed { row, steps ->
            val file = pianoNotes.getOrNull(row) ?: return@forEachIndexed
            val sound = loadWav(file) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                println("🎹 Piano row=$row col=$col file=$file")

                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val index = offset + i
                    if (index >= buffer.size) break
                    buffer[index] = (buffer[index] + sound[i] * velocity)
                        .coerceIn(-1f, 1f)
                }
            }
        }
    }

    private fun collectPianoMidi(
        messages: MutableList<Pair<Int, ByteArray>>,
        pattern: PianoEditorState,
        startTick: Int,
        stepTicks: Int,
        velocity: Int
    ) {
        pattern.grid.forEachIndexed { row, steps ->

            val note = (60 + row).coerceIn(0, 127)

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                val tick = startTick + col * stepTicks

                messages.add(tick to byteArrayOf(0x90.toByte(), note.toByte(), velocity.toByte()))
                messages.add((tick + stepTicks) to byteArrayOf(0x80.toByte(), note.toByte(), 0))
            }
        }
    }

    private fun collectGuitarMidi(
        messages: MutableList<Pair<Int, ByteArray>>,
        pattern: GuitarEditorState,
        startTick: Int,
        stepTicks: Int,
        velocity: Int
    ) {

        pattern.grid.forEachIndexed { row, steps ->

            if (row >= guitarNotes.size) return@forEachIndexed

            val note = (60 + row).coerceIn(0, 127)

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                val tick = startTick + col * stepTicks

                messages.add(tick to byteArrayOf(0x90.toByte(), note.toByte(), velocity.toByte()))
                messages.add((tick + stepTicks) to byteArrayOf(0x80.toByte(), note.toByte(), 0))
            }
        }
    }

    private fun collectDrumMidi(
        messages: MutableList<Pair<Int, ByteArray>>,
        pattern: DrumEditorState,
        startTick: Int,
        stepTicks: Int,
        velocity: Int
    ) {
        val drumMap = listOf(36, 38, 42, 46, 45, 49, 51, 39)

        pattern.grid.forEachIndexed { row, steps ->
            val note = drumMap.getOrNull(row) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                val tick = startTick + col * stepTicks

                messages.add(tick to byteArrayOf(0x99.toByte(), note.toByte(), velocity.toByte()))
                messages.add((tick + stepTicks / 2) to byteArrayOf(0x89.toByte(), note.toByte(), 0))
            }
        }
    }

}