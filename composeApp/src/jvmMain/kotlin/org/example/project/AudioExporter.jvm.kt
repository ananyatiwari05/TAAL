package org.example.project

import pianoNotes
import java.io.File
import javax.sound.sampled.*
import kotlin.math.abs

actual class AudioExporter {

    actual fun exportBeat(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String
    ) {

        println("===== EXPORT START =====")

        val sampleRate = 48000
        val stepDurationSec = 60.0 / (bpm * 4)
        val totalSteps = 32

        println("BPM: $bpm")
        println("Step duration: $stepDurationSec sec")

        val totalSamples = (sampleRate * stepDurationSec * totalSteps).toInt()
        val mixBuffer = FloatArray(totalSamples)

        for (instrumentIndex in categories.indices) {

            val category = categories[instrumentIndex]
            println("\n--- Instrument $instrumentIndex (${category.title}) ---")

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
        println("\nMax sample before normalize: $max")

        if (max > 0f) {
            for (i in mixBuffer.indices) {
                mixBuffer[i] /= max
            }
            println("✅ Normalization applied")
        } else {
            println("❌ MixBuffer is SILENT")
        }

        writeWav(mixBuffer, sampleRate, outputPath)

        println("EXPORT DONE: $outputPath")
        println("===== EXPORT END =====")
    }

    private fun loadWav(path: String): FloatArray? {
        return try {

            println("→ loadWav called with: $path")

            val audioInput = if (path.startsWith("/") || path.startsWith("content://")) {
                println("Reading from FILE system")
                AudioSystem.getAudioInputStream(File(path))
            } else {
                println("Reading from RESOURCES")

                val resource = javaClass.classLoader.getResource(path)
                    ?: run {
                        println("❌ Resource not found: $path")
                        return null
                    }

                AudioSystem.getAudioInputStream(resource)
            }

            val format = audioInput.format
            println("Format: $format")

            val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.sampleRate,
                16,
                format.channels,
                format.channels * 2,
                format.sampleRate,
                false
            )

            val stream = AudioSystem.getAudioInputStream(decodedFormat, audioInput)

            val bytes = stream.readBytes()
            println("Bytes read: ${bytes.size}")

            val samples = FloatArray(bytes.size / 2)

            var sampleIndex = 0

            for (i in bytes.indices step 2) {
                val sample = (bytes[i + 1].toInt() shl 8) or (bytes[i].toInt() and 0xff)
                samples[sampleIndex++] = sample / 32768f
            }

            println("Converted samples: ${samples.size}")

            samples

        } catch (e: Exception) {
            println("❌ Exception in loadWav")
            e.printStackTrace()
            null
        }
    }

    private fun writeWav(data: FloatArray, sampleRate: Int, path: String) {

        println("Writing WAV file...")

        val byteData = ByteArray(data.size * 2)

        var i = 0
        data.forEach { sample ->
            val clamped = (sample.coerceIn(-1f, 1f) * 32767).toInt()
            byteData[i++] = (clamped and 0xff).toByte()
            byteData[i++] = ((clamped shr 8) and 0xff).toByte()
        }

        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val byteStream = byteData.inputStream()
        val audioStream = AudioInputStream(byteStream, format, data.size.toLong())

        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, File(path))

        println("✅ WAV written successfully")
    }

    fun mixDrumPattern(
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
            val sound = loadWav(drumFiles[row]) ?: return@forEachIndexed

            steps.forEachIndexed { col, active ->
                if (!active) return@forEachIndexed

                val offset = startSample + col * stepDuration

                for (i in sound.indices) {
                    val index = offset + i
                    if (index >= buffer.size) break

                    buffer[index] += sound[i] * velocity
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
                    val index = offset + i
                    if (index >= buffer.size) break

                    buffer[index] += sound[i] * velocity
                }
            }
        }
    }
}