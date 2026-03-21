package org.example.project

actual class SettingsManager {
    actual suspend fun saveMode(mode: UserMode) {
    }

    actual suspend fun loadMode(): UserMode {
        TODO("Not yet implemented")
    }
}