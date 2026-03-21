package org.example.project

import java.io.File
import javax.sound.sampled.*

actual class VoiceRecorder {

    private var line: TargetDataLine? = null
    private var recordingThread: Thread? = null
    private var outputFile: File? = null

    actual fun startRecording() {

        try {
            val format = AudioFormat(
                44100f,
                16,
                1,
                true,
                false
            )

            val info = DataLine.Info(TargetDataLine::class.java, format)

            line = AudioSystem.getLine(info) as TargetDataLine
            line!!.open(format)
            line!!.start()

            outputFile = File(
                "recording_${System.currentTimeMillis()}.wav"
            )

            recordingThread = Thread {

                AudioSystem.write(
                    AudioInputStream(line),
                    AudioFileFormat.Type.WAVE,
                    outputFile
                )

            }

            recordingThread!!.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stopRecording(): String {

        line?.stop()
        line?.close()
        line = null

        recordingThread = null

        return outputFile?.absolutePath ?: ""
    }

    actual fun playRecording(path: String) {

        try {
            val file = File(path)
            val audioStream = AudioSystem.getAudioInputStream(file)
            val clip = AudioSystem.getClip()

            clip.open(audioStream)
            clip.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stopPlayback() {
        // optional for JVM
    }
}