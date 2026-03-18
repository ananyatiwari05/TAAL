package org.example.project

expect class AudioImporter() {
    fun pickAudio(onAudioPicked: (String) -> Unit)
}