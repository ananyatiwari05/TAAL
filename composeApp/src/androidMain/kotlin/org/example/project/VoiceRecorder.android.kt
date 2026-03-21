package org.example.project

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File

actual class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var outputFile: String? = null

    actual fun startRecording() {

        outputFile = File(
            context.filesDir,
            "recording_${System.currentTimeMillis()}.mp3"
        ).absolutePath

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            prepare()
            start()
        }
    }

    actual fun stopRecording(): String {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        recorder = null
        return outputFile ?: ""
    }

    actual fun playRecording(path: String) {
        stopPlayback()

        player = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
    }

    actual fun stopPlayback() {
        player?.release()
        player = null
    }

    actual constructor() : this(
        AppContextHolder.context
            ?: throw IllegalStateException("Context not initialized")
    )
}