package org.example.project



import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


private val Context.dataStore by preferencesDataStore(name = "app_settings")

actual class SettingsManager(private val context: Context) {

    private val MODE_KEY = stringPreferencesKey("mode")

    actual suspend fun saveMode(mode: UserMode) {
        context.dataStore.edit {
            it[MODE_KEY] = mode.name
        }
    }

    actual suspend fun loadMode(): UserMode {
        val prefs = context.dataStore.data.first()
        val value = prefs[MODE_KEY] ?: UserMode.BEGINNER.name
        return UserMode.valueOf(value)
    }
}