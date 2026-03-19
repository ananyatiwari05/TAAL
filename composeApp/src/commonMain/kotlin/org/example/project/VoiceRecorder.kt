package org.example.project

expect class VoiceRecorder(){

    fun startRecording()

    fun stopRecording(): String

    fun playRecording(path: String)

    fun stopPlayback()

}