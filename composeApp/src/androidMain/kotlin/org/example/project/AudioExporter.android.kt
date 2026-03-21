package org.example.project

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import dev.atsushieno.ktmidi.*

actual class AudioExporter(private val context: Context) {

    actual fun exportBeat(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String
    ) {

        val sampleRate = 48000
        val stepDurationSec = 60.0 / (bpm * 4)
        val totalSteps = 32

        val totalSamples = (sampleRate * stepDurationSec * totalSteps).toInt()
        val mixBuffer = FloatArray(totalSamples)

        for (instrumentIndex in categories.indices) {

            val category = categories[instrumentIndex]

            for (step in 0 until totalSteps) {

                val tileId = state.grid[instrumentIndex][step] ?: continue
                val tile = category.tiles.find { it.id == tileId } ?: continue
                val beat = tile.beat ?: continue

                val startSample = (step * stepDurationSec * sampleRate).toInt()
                val velocity = state.velocityGrid[instrumentIndex][step]

                when {
                    beat.drumPattern != null -> {
                        mixDrumPattern(beat.drumPattern, startSample, mixBuffer, sampleRate, velocity)
                    }

                    beat.pianoPattern != null -> {
                        mixPianoPattern(beat.pianoPattern, startSample, mixBuffer, sampleRate, velocity)
                    }

                    beat.fileName != null -> {
                        val audioData = loadWav(beat.fileName) ?: continue
                        val maxLength = (stepDurationSec * sampleRate).toInt()

                        for (i in 0 until minOf(audioData.size, maxLength)) {
                            val index = startSample + i
                            if (index >= mixBuffer.size) break
                            mixBuffer[index] += audioData[i] * velocity
                        }
                    }
                }
            }
        }

        val max = mixBuffer.maxOfOrNull { abs(it) } ?: 0f
        if (max > 0f) {
            for (i in mixBuffer.indices) {
                mixBuffer[i] /= max
            }
        }

        writeWav(mixBuffer, sampleRate, outputPath)
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

                val tileId = state.grid[instrumentIndex][step] ?: continue
                val tile = tileMap[tileId] ?: continue
                val beat = tile.beat ?: continue

                val velocityFloat = state.velocityGrid[instrumentIndex][step]
                val velocity = (velocityFloat * 127).toInt().coerceIn(1, 127)

                val tick = step * stepTicks

                when {
                    beat.drumPattern != null -> {
                        collectDrumMidi(messages, beat.drumPattern, tick, stepTicks, velocity)
                    }

                    beat.pianoPattern != null -> {
                        collectPianoMidi(messages, beat.pianoPattern, tick, stepTicks, velocity)
                    }

                    beat.fileName != null -> {
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
                MidiMessage(delta, MidiEvent(status, data1, data2))
            )
        }

        music.tracks.add(track)

        val bytes = mutableListOf<Byte>()
        music.write(bytes)

        File(outputPath).writeBytes(bytes.toByteArray())
    }

    actual fun exportFromMidi(
        midiPath: String,
        bpm: Int,
        outputPath: String
    ) {
        val sampleRate = 48000
        val durationSec = 10
        val buffer = FloatArray(sampleRate * durationSec)

        loadMidiAndPlay(midiPath, buffer, sampleRate, bpm)

        val file = File(outputPath)
        file.parentFile?.mkdirs()

        writeWav(buffer, sampleRate, file.absolutePath)
    }

    private fun loadWav(path: String): FloatArray? {
        return try {
            val bytes = File(path).readBytes()
            val headerSize = 44
            val samples = FloatArray((bytes.size - headerSize) / 2)

            var idx = 0
            var i = headerSize

            while (i < bytes.size - 1) {
                val sample = (bytes[i + 1].toInt() shl 8) or (bytes[i].toInt() and 0xff)
                samples[idx++] = sample / 32768f
                i += 2
            }

            samples

        } catch (e: Exception) {
            null
        }
    }

    private fun writeWav(data: FloatArray, sampleRate: Int, path: String) {

        val file = File(path)
        file.parentFile?.mkdirs()

        val byteData = ByteArray(data.size * 2)

        var i = 0
        data.forEach {
            val v = (it.coerceIn(-1f, 1f) * 32767).toInt()
            byteData[i++] = (v and 0xff).toByte()
            byteData[i++] = ((v shr 8) and 0xff).toByte()
        }

        val out = FileOutputStream(file)
        out.write(createWavHeader(byteData.size, sampleRate))
        out.write(byteData)
        out.close()
    }

    private fun createWavHeader(dataSize: Int, sampleRate: Int): ByteArray {
        val header = ByteArray(44)
        val byteRate = sampleRate * 2

        fun writeInt(offset: Int, value: Int) {
            header[offset] = (value and 0xff).toByte()
            header[offset + 1] = (value shr 8).toByte()
            header[offset + 2] = (value shr 16).toByte()
            header[offset + 3] = (value shr 24).toByte()
        }

        fun writeShort(offset: Int, value: Int) {
            header[offset] = (value and 0xff).toByte()
            header[offset + 1] = (value shr 8).toByte()
        }

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        writeInt(4, dataSize + 36)

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeInt(16, 16)
        writeShort(20, 1)
        writeShort(22, 1)
        writeInt(24, sampleRate)
        writeInt(28, byteRate)
        writeShort(32, 2)
        writeShort(34, 16)

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        writeInt(40, dataSize)

        return header
    }

    fun mixDrumPattern(
        pattern: DrumEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
        val stepDuration = sampleRate / 4
        val drumFiles = listOf("kick.wav","snare.wav","closedhat.wav","openhat.wav","tom.wav","crash.wav","ride.wav","clap.wav")

        pattern.grid.forEachIndexed { row, steps ->
            val sound = loadWav(drumFiles[row]) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed
                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val idx = offset + i
                    if (idx >= buffer.size) break
                    buffer[idx] += sound[i] * velocity
                }
            }
        }
    }

    fun mixPianoPattern(
        pattern: PianoEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
        val stepDuration = sampleRate / 4

        pattern.grid.forEachIndexed { row, steps ->
            val sound = loadWav(pianoNotes[row]) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed
                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val idx = offset + i
                    if (idx >= buffer.size) break
                    buffer[idx] += sound[i] * velocity
                }
            }
        }
    }

    fun loadMidiAndPlay(
        filePath: String,
        buffer: FloatArray,
        sampleRate: Int,
        bpm: Int
    ) {

        val music = MidiMusic()
        val bytes = File(filePath).readBytes().toList()
        music.read(bytes)

        val secondsPerTick = 60.0 / (bpm * music.deltaTimeSpec)

        music.tracks.forEach { track ->

            var currentTick = 0

            track.messages.forEach { msg ->

                currentTick += msg.deltaTime

                val event = msg.event
                val status = event.statusByte.toInt() and 0xFF
                val note = event.msb.toInt() and 0xFF
                val velocity = (event.lsb.toInt() and 0xFF) / 127f

                if ((status and 0xF0) == 0x90 && velocity > 0f) {

                    val sampleIndex = (currentTick * secondsPerTick * sampleRate).toInt()

                    val sound = when (note) {
                        36 -> loadWav("kick.wav")
                        38 -> loadWav("snare.wav")
                        42 -> loadWav("closedhat.wav")
                        else -> loadWav(pianoNotes.getOrNull(note - 60) ?: return@forEach)
                    } ?: return@forEach

                    for (i in sound.indices) {
                        val idx = sampleIndex + i
                        if (idx >= buffer.size) break
                        buffer[idx] += sound[i] * velocity
                    }
                }
            }
        }
    }

    fun collectDrumMidi(
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

                messages.add(
                    tick to byteArrayOf(0x99.toByte(), note.toByte(), velocity.toByte())
                )

                messages.add(
                    (tick + stepTicks / 2) to byteArrayOf(0x89.toByte(), note.toByte(), 0)
                )
            }
        }
    }

    fun collectPianoMidi(
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

                messages.add(
                    tick to byteArrayOf(0x90.toByte(), note.toByte(), velocity.toByte())
                )

                messages.add(
                    (tick + stepTicks) to byteArrayOf(0x80.toByte(), note.toByte(), 0)
                )
            }
        }
    }
}