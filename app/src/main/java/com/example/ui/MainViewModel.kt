package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsDataStore
import com.example.data.db.AppDatabase
import com.example.data.db.PresetSatellites
import com.example.data.db.SatelliteRepository
import com.example.data.model.AlignmentCalculation
import com.example.data.model.AppSettings
import com.example.data.model.CityLocation
import com.example.data.model.Satellite
import com.example.sensor.SensorFusionManager
import com.example.sensor.SensorOrientationData
import com.example.util.AudioBeeper
import com.example.util.HapticManager
import com.example.util.LocationManagerHelper
import com.example.util.SatMath
import com.example.util.VoiceGuidance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SatelliteRepository(database.satelliteDao())
    private val settingsDataStore = SettingsDataStore(application)
    private val locationManagerHelper = LocationManagerHelper(application)
    private val sensorManager = SensorFusionManager(application)

    val audioBeeper = AudioBeeper()
    val voiceGuidance = VoiceGuidance(application)
    val hapticManager = HapticManager(application)

    // All Satellites from Room
    val satellites: StateFlow<List<Satellite>> = repository.allSatellites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PresetSatellites.PRESET_LIST
        )

    // Selected Satellite
    private val _selectedSatellite = MutableStateFlow(PresetSatellites.PRESET_LIST.first())
    val selectedSatellite: StateFlow<Satellite> = _selectedSatellite.asStateFlow()

    // Current Location (Defaults to Islamabad, Pakistan)
    private val _currentLocation = MutableStateFlow(CityLocation.DEFAULT_LOCATION)
    val currentLocation: StateFlow<CityLocation> = _currentLocation.asStateFlow()

    // Settings
    val appSettings: StateFlow<AppSettings> = settingsDataStore.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    // Sensor Orientation Data
    val sensorData: StateFlow<SensorOrientationData> = sensorManager.orientationData

    // GPS Loading state
    private val _isGpsLocating = MutableStateFlow(false)
    val isGpsLocating: StateFlow<Boolean> = _isGpsLocating.asStateFlow()

    // Alignment calculation state (recalculated whenever satellite, location, or offset angle changes)
    private val _alignment = MutableStateFlow(
        SatMath.calculateAlignment(_selectedSatellite.value, _currentLocation.value)
    )
    val alignment: StateFlow<AlignmentCalculation> = _alignment.asStateFlow()

    // Live Deltas & Lock status
    private val _azimuthDelta = MutableStateFlow(0f)
    val azimuthDelta: StateFlow<Float> = _azimuthDelta.asStateFlow()

    private val _elevationDelta = MutableStateFlow(0f)
    val elevationDelta: StateFlow<Float> = _elevationDelta.asStateFlow()

    private val _isAzimuthLocked = MutableStateFlow(false)
    val isAzimuthLocked: StateFlow<Boolean> = _isAzimuthLocked.asStateFlow()

    private val _isElevationLocked = MutableStateFlow(false)
    val isElevationLocked: StateFlow<Boolean> = _isElevationLocked.asStateFlow()

    private val _isFullyLocked = MutableStateFlow(false)
    val isFullyLocked: StateFlow<Boolean> = _isFullyLocked.asStateFlow()

    init {
        // Initialize preset database
        viewModelScope.launch {
            repository.initializePresetsIfEmpty()
        }

        // Start listening to sensors
        sensorManager.startListening()

        // Sync settings to managers
        viewModelScope.launch {
            appSettings.collect { settings ->
                audioBeeper.isEnabled = settings.audioBeeperEnabled
                voiceGuidance.isEnabled = settings.voiceGuidanceEnabled
                hapticManager.isEnabled = settings.hapticFeedbackEnabled
                recalculate()
            }
        }

        // Live calculation and alignment evaluation on sensor changes
        viewModelScope.launch {
            combine(sensorData, alignment, appSettings) { sensor, align, settings ->
                val targetAzimuth = if (settings.useMagneticNorth) align.magneticAzimuth else align.trueAzimuth
                val diffAz = SatMath.calculateAngleDifference(sensor.azimuthDegrees, targetAzimuth)
                val diffEl = align.elevation.toFloat() - sensor.pitchDegrees

                val tolerance = settings.alignmentToleranceDegrees.toFloat()
                val azLocked = abs(diffAz) <= tolerance
                val elLocked = abs(diffEl) <= (tolerance * 1.5f)
                val fullyLocked = azLocked && elLocked

                DeltaUpdate(diffAz, diffEl, azLocked, elLocked, fullyLocked)
            }.collect { update ->
                _azimuthDelta.value = update.azimuthDiff
                _elevationDelta.value = update.elevationDiff
                _isAzimuthLocked.value = update.azimuthLocked
                _isElevationLocked.value = update.elevationLocked

                val wasFullyLocked = _isFullyLocked.value
                _isFullyLocked.value = update.fullyLocked

                // Audio beeper modulation
                audioBeeper.updateAlignmentStatus(abs(update.azimuthDiff), update.fullyLocked)

                // Voice guidance
                voiceGuidance.provideDirectionalGuidance(
                    update.azimuthDiff,
                    update.fullyLocked,
                    _selectedSatellite.value.name
                )

                // Haptic feedback
                if (update.fullyLocked && !wasFullyLocked) {
                    hapticManager.pulseLock()
                } else if (update.azimuthLocked) {
                    hapticManager.pulseTick()
                }
            }
        }
    }

    private data class DeltaUpdate(
        val azimuthDiff: Float,
        val elevationDiff: Float,
        val azimuthLocked: Boolean,
        val elevationLocked: Boolean,
        val fullyLocked: Boolean
    )

    private fun recalculate() {
        val sat = _selectedSatellite.value
        val loc = _currentLocation.value
        val offset = appSettings.value.dishOffsetAngle
        _alignment.value = SatMath.calculateAlignment(sat, loc, offset)
    }

    fun selectSatellite(satellite: Satellite) {
        _selectedSatellite.value = satellite
        recalculate()
        voiceGuidance.speak("Selected ${satellite.name} at ${satellite.orbitalPositionFormatted}")
    }

    fun setLocation(location: CityLocation) {
        _currentLocation.value = location
        recalculate()
    }

    fun requestGpsLocation() {
        viewModelScope.launch {
            _isGpsLocating.value = true
            val detected = locationManagerHelper.getCurrentGpsLocation()
            if (detected != null) {
                _currentLocation.value = detected
                recalculate()
                voiceGuidance.speak("GPS location acquired")
            }
            _isGpsLocating.value = false
        }
    }

    fun toggleFavorite(satellite: Satellite) {
        viewModelScope.launch {
            repository.toggleFavorite(satellite.id, !satellite.isFavorite)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsDataStore.updateSettings(newSettings)
        }
    }

    fun toggleBeeper() {
        val current = appSettings.value
        updateSettings(current.copy(audioBeeperEnabled = !current.audioBeeperEnabled))
    }

    fun toggleVoice() {
        val current = appSettings.value
        updateSettings(current.copy(voiceGuidanceEnabled = !current.voiceGuidanceEnabled))
    }

    fun saveCustomSatellite(
        name: String,
        orbitalPosition: Double,
        band: String,
        defaultLnbLo: Int,
        recommendedDishSize: String,
        strongTp: String,
        notes: String
    ) {
        viewModelScope.launch {
            val custom = Satellite(
                id = 0,
                name = name,
                orbitalPosition = orbitalPosition,
                band = band,
                defaultLnbLo = defaultLnbLo,
                recommendedDishSize = recommendedDishSize,
                popularChannels = "Custom user satellite",
                strongTransponder = strongTp,
                coverageNotes = notes,
                isFavorite = true,
                isCustom = true
            )
            repository.saveCustomSatellite(custom)
        }
    }

    fun setSimulatedHeading(heading: Float, pitch: Float = 45f) {
        sensorManager.setSimulatedOrientation(heading, pitch)
    }

    fun resetSimulatedMode() {
        sensorManager.resetSimulatedMode()
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
        audioBeeper.release()
        voiceGuidance.shutdown()
    }
}
