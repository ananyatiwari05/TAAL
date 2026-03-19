package org.example.project

import javax.sound.sampled.*
import java.io.File

actual class AudioPlayer {

    private val clips = mutableMapOf<String, Clip>()
    private var currentVolume = 1f

    actual fun playSound(name: String) {
        playSample(name)
    }

    actual fun playImported(uri: String) {
        playFile(uri)
    }

    actual fun setVolume(newVolume: Float) {
        currentVolume = newVolume.coerceIn(0f, 1f)

        clips.values.forEach { clip ->
            try {
                val control = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl

                val min = control.minimum
                val max = control.maximum
                val gain = min + (max - min) * currentVolume

                control.value = gain
            } catch (_: Exception) {}
        }
    }


    private fun playSample(name: String) {

        try {

            val clip = clips.getOrPut(name) {

                val resource = Thread.currentThread()
                    .contextClassLoader
                    ?.getResource(name)
                    ?: throw IllegalArgumentException("Sound resource not found: $name")

                val originalStream = AudioSystem.getAudioInputStream(resource)

                val decodedStream = decodeStream(originalStream)

                val c = AudioSystem.getClip()
                c.open(decodedStream)
                applyVolume(c)
                c
            }

            restartClip(clip)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun playFile(path: String) {

        try {

            val clip = clips.getOrPut(path) {

                val file = File(path)

                val originalStream = AudioSystem.getAudioInputStream(file)

                val decodedStream = decodeStream(originalStream)

                val c = AudioSystem.getClip()
                c.open(decodedStream)
                applyVolume(c)
                c
            }

            restartClip(clip)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun decodeStream(originalStream: AudioInputStream): AudioInputStream {
        val baseFormat = originalStream.format

        val decodedFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.sampleRate,
            16,
            baseFormat.channels,
            baseFormat.channels * 2,
            baseFormat.sampleRate,
            false
        )

        return AudioSystem.getAudioInputStream(decodedFormat, originalStream)
    }

    private fun restartClip(clip: Clip) {
        if (clip.isRunning) {
            clip.stop()
        }
        clip.framePosition = 0
        clip.start()
    }

    private fun applyVolume(clip: Clip) {
        try {
            val control = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl

            val min = control.minimum
            val max = control.maximum
            val gain = min + (max - min) * currentVolume

            control.value = gain
        } catch (_: Exception) {}
    }
}