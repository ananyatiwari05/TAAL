package org.example.project


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "app_settings")

object AppSettingsDataStore {

    private val MODE_KEY = stringPreferencesKey("user_mode")

    suspend fun saveMode(context: Context, mode: UserMode) {
        context.dataStore.edit { prefs ->
            prefs[MODE_KEY] = mode.name
        }
    }

    suspend fun loadMode(context: Context): UserMode {
        val prefs = context.dataStore.data.first()
        val saved = prefs[MODE_KEY] ?: UserMode.BEGINNER.name
        return UserMode.valueOf(saved)
    }
}