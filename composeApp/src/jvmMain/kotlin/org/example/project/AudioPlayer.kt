package org.example.project

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

actual class AudioPlayer {

    actual fun playSound(name: String) {

        try {

            val resource = javaClass.classLoader.getResource(name)
                ?: return

            val audioInputStream = AudioSystem.getAudioInputStream(resource)

            val clip: Clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}