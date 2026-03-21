package org.example.project

expect class SettingsManager {
    suspend fun saveMode(mode: UserMode)
    suspend fun loadMode(): UserMode
}