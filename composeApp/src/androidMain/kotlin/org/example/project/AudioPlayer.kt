package org.example.project

import android.content.Context
import android.media.MediaPlayer

actual class AudioPlayer actual constructor() {

    actual fun playSound(name: String) {
        val context = AppContextHolder.context ?: return

        val resId = when (name) {
            "drum" -> R.raw.drum
            "guitar" -> R.raw.guitar
            "sax" -> R.raw.sax
            else -> return
        }

        MediaPlayer.create(context, resId).start()
    }
}