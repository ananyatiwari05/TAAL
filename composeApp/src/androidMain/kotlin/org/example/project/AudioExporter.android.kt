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

        val totalSteps = state.grid[0].size
        val tailSeconds = 3

        val totalSamples = (sampleRate * (stepDurationSec * totalSteps + tailSeconds)).toInt()
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

                    beat.guitarPattern != null -> {
                        mixGuitarPattern(beat.guitarPattern, startSample, mixBuffer, sampleRate, velocity)
                    }

                    beat.fileName != null -> {
                        val audioData = loadWav(beat.fileName) ?: continue

                        for (i in audioData.indices) {
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
        val gain = 2.5f

        if (max > 0f) {
            for (i in mixBuffer.indices) {
                mixBuffer[i] = (mixBuffer[i] / max * gain)
                    .coerceIn(-1f, 1f)
            }
        }

        val safePath = getSafePath(File(outputPath).name)
        writeWav(mixBuffer, sampleRate, safePath)
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

            track.messages.add(MidiMessage(delta, MidiEvent(status, data1, data2)))
        }

        music.tracks.add(track)

        val bytes = mutableListOf<Byte>()
        music.write(bytes)

        val safePath = getSafePath(File(outputPath).name)
        File(safePath).writeBytes(bytes.toByteArray())
    }

    private fun loadWav(path: String): FloatArray? {
        return try {

            val name = path.substringBeforeLast(".")
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId == 0) return null

            val fileDescriptor = context.resources.openRawResourceFd(resId)

            val extractor = android.media.MediaExtractor()
            extractor.setDataSource(fileDescriptor.fileDescriptor, fileDescriptor.startOffset, fileDescriptor.length)

            var format: android.media.MediaFormat? = null
            var trackIndex = -1

            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(android.media.MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    format = f
                    trackIndex = i
                    break
                }
            }

            if (trackIndex == -1 || format == null) return null

            extractor.selectTrack(trackIndex)

            val codec = android.media.MediaCodec.createDecoderByType(format.getString(android.media.MediaFormat.KEY_MIME)!!)
            codec.configure(format, null, null, 0)
            codec.start()

            val outputBufferInfo = android.media.MediaCodec.BufferInfo()
            val samples = mutableListOf<Float>()

            var isEOS = false

            while (true) {

                if (!isEOS) {
                    val inputIndex = codec.dequeueInputBuffer(10000)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = codec.dequeueOutputBuffer(outputBufferInfo, 10000)

                if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)!!
                    val chunk = ByteArray(outputBufferInfo.size)
                    outputBuffer.get(chunk)
                    outputBuffer.clear()

                    var i = 0
                    while (i < chunk.size - 1) {
                        val sample = (chunk[i + 1].toInt() shl 8) or (chunk[i].toInt() and 0xff)
                        samples.add(sample / 32768f)
                        i += 2
                    }

                    codec.releaseOutputBuffer(outputIndex, false)

                    if (outputBufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        break
                    }
                }
            }

            codec.stop()
            codec.release()
            extractor.release()

            samples.toFloatArray()

        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun getSafePath(fileName: String): String {
        val dir = File(context.getExternalFilesDir(null), "taal_exports")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName).absolutePath
    }

    private val guitarNotes = listOf(
        "guitar_a2.wav","guitar_a3.wav","guitar_a4.wav",
        "guitar_b2.wav","guitar_b3.wav","guitar_b4.wav",
        "guitar_c3.wav","guitar_c4.wav","guitar_c5.wav",
        "guitar_d2.wav","guitar_d3.wav","guitar_d4.wav",
        "guitar_e2.wav","guitar_e3.wav","guitar_e4.wav",
        "guitar_f2.wav","guitar_f3.wav","guitar_f4.wav",
        "guitar_g2.wav","guitar_g3.wav","guitar_g4.wav"
    )

    fun mixGuitarPattern(
        pattern: GuitarEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
        val stepDuration = sampleRate / 4

        pattern.grid.forEachIndexed { row, steps ->

            val file = guitarNotes.getOrNull(row) ?: return@forEachIndexed
            val sound = loadWav(file) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val idx = offset + i
                    if (idx >= buffer.size) break

                    buffer[idx] = (buffer[idx] + sound[i] * velocity)
                        .coerceIn(-1f, 1f)
                }
            }
        }
    }

    fun mixDrumPattern(
        pattern: DrumEditorState,
        startSample: Int,
        buffer: FloatArray,
        sampleRate: Int,
        velocity: Float
    ) {
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

                val offset = startSample + col * (sampleRate / 4)

                for (i in sound.indices) {
                    val idx = offset + i
                    if (idx >= buffer.size) break

                    buffer[idx] = (buffer[idx] + sound[i] * velocity)
                        .coerceIn(-1f, 1f)
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
        pattern.grid.forEachIndexed { row, steps ->
            val file = pianoNotes.getOrNull(row) ?: return@forEachIndexed
            val sound = loadWav(file) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                val offset = startSample + col * (sampleRate / 4)

                for (i in sound.indices) {
                    val idx = offset + i
                    if (idx >= buffer.size) break

                    buffer[idx] = (buffer[idx] + sound[i] * velocity)
                        .coerceIn(-1f, 1f)
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

                messages.add(tick to byteArrayOf(0x99.toByte(), note.toByte(), velocity.toByte()))
                messages.add((tick + stepTicks / 2) to byteArrayOf(0x89.toByte(), note.toByte(), 0))
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

                messages.add(tick to byteArrayOf(0x90.toByte(), note.toByte(), velocity.toByte()))
                messages.add((tick + stepTicks) to byteArrayOf(0x80.toByte(), note.toByte(), 0))
            }
        }
    }
}