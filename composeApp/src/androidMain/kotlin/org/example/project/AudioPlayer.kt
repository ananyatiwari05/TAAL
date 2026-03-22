package org.example.project

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import android.net.Uri

val guitarNotes = listOf(
    "guitar_c2","guitar_d2","guitar_e2","guitar_f2","guitar_g2","guitar_a2","guitar_b2",
    "guitar_c3","guitar_d3","guitar_e3","guitar_f3","guitar_g3","guitar_a3","guitar_b3",
    "guitar_c4","guitar_d4","guitar_e4","guitar_f4","guitar_g4","guitar_a4","guitar_b4",
    "guitar_c5"
)

val drumSounds = listOf(
    "kick","snare","closedhat","openhat",
    "tom","crash","ride","clap"
)

actual class AudioPlayer(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()

    private var mediaPlayer: MediaPlayer? = null
    private var volume: Float = 1f

    init {

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(32)
            .setAudioAttributes(attrs)
            .build()

        // preload guitar
        guitarNotes.forEach {
            try { loadSound(it) } catch (_: Exception) {}
        }

        // preload drums (MP3 also works)
        drumSounds.forEach {
            try { loadSound(it) } catch (_: Exception) {}
        }
    }

    actual fun playSound(name: String) {

        val cleanName = name.substringBefore(".")

        val id = sounds[cleanName] ?: loadSound(cleanName)

        soundPool.play(
            id,
            volume,
            volume,
            1,
            0,
            1f
        )
    }

    actual fun playImported(uri: String) {

        stopImported()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(appContext, Uri.parse(uri))
            prepare()
            setVolume(volume, volume)
            start()
        }
    }

    fun stopImported() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun loadSound(name: String): Int {

        val resId = appContext.resources.getIdentifier(
            name,
            "raw",
            appContext.packageName
        )

        if (resId == 0) {
            throw IllegalArgumentException("Audio file not found in res/raw: $name")
        }

        val soundId = soundPool.load(appContext, resId, 1)
        sounds[name] = soundId

        return soundId
    }

    actual fun setVolume(newVolume: Float) {
        volume = newVolume
        mediaPlayer?.setVolume(volume, volume)
    }

    actual fun getGuitarNoteByIndex(index: Int): String {
        return guitarNotes.getOrElse(index) { "guitar_c2" }
    }

    fun getDrumSoundByIndex(index: Int): String {
        return drumSounds.getOrElse(index) { "kick" }
    }
}