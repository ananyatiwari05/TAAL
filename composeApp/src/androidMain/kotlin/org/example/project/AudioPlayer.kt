package org.example.project

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

actual class AudioPlayer {

    private lateinit var soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()

    fun init(context: Context) {

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attributes)
            .build()

        sounds["kick.wav"] = soundPool.load(context, R.raw.kick, 1)
        sounds["snare.wav"] = soundPool.load(context, R.raw.snare, 1)
        sounds["closedhat.wav"] = soundPool.load(context, R.raw.closedhat, 1)
        sounds["openhat.wav"] = soundPool.load(context, R.raw.openhat, 1)
        sounds["tom.wav"] = soundPool.load(context, R.raw.tom, 1)
        sounds["crash.wav"] = soundPool.load(context, R.raw.crash, 1)
        sounds["ride.wav"] = soundPool.load(context, R.raw.ride, 1)
        sounds["clap.wav"] = soundPool.load(context, R.raw.clap, 1)
    }

    actual fun playSound(name: String) {

        val id = sounds[name] ?: return

        soundPool.play(
            id,
            1f,
            1f,
            1,
            0,
            1f
        )
    }
}