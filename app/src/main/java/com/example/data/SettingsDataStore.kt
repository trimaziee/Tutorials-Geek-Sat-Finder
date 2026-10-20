package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sat_finder_preferences")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val BEEPER_ENABLED = booleanPreferencesKey("beeper_enabled")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val USE_MAGNETIC = booleanPreferencesKey("use_magnetic")
        val OFFSET_ANGLE = doublePreferencesKey("offset_angle")
        val TOLERANCE = doublePreferencesKey("tolerance")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            audioBeeperEnabled = preferences[PreferencesKeys.BEEPER_ENABLED] ?: true,
            voiceGuidanceEnabled = preferences[PreferencesKeys.VOICE_ENABLED] ?: true,
            hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
            useMagneticNorth = preferences[PreferencesKeys.USE_MAGNETIC] ?: true,
            dishOffsetAngle = preferences[PreferencesKeys.OFFSET_ANGLE] ?: 22.5,
            alignmentToleranceDegrees = preferences[PreferencesKeys.TOLERANCE] ?: 2.5
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BEEPER_ENABLED] = settings.audioBeeperEnabled
            preferences[PreferencesKeys.VOICE_ENABLED] = settings.voiceGuidanceEnabled
            preferences[PreferencesKeys.HAPTICS_ENABLED] = settings.hapticFeedbackEnabled
            preferences[PreferencesKeys.USE_MAGNETIC] = settings.useMagneticNorth
            preferences[PreferencesKeys.OFFSET_ANGLE] = settings.dishOffsetAngle
            preferences[PreferencesKeys.TOLERANCE] = settings.alignmentToleranceDegrees
        }
    }
}
