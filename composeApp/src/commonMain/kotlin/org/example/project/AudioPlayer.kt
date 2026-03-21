package org.example.project

expect class AudioPlayer {
    fun playSound(name: String)
    fun playImported(uri: String)
    fun setVolume(newVolume: Float)
}