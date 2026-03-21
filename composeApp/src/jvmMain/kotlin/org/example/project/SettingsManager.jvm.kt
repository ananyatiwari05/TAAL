package org.example.project

import java.io.File

actual class SettingsManager {

    private val file = File(System.getProperty("user.home"), ".taal_settings")

    actual suspend fun saveMode(mode: UserMode) {
        try {
            file.writeText(mode.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun loadMode(): UserMode {
        return try {
            if (!file.exists()) {
                UserMode.BEGINNER
            } else {
                val value = file.readText()
                UserMode.valueOf(value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UserMode.BEGINNER
        }
    }
}