package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.LocationPickerSheet
import com.example.ui.components.SatelliteInfoStripBar
import com.example.ui.components.SatelliteSelectionSheetContent
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CameraArScreen
import com.example.ui.screens.CompassAlignScreen
import com.example.ui.screens.DishLevelScreen
import com.example.ui.screens.InstallationWizardScreen
import com.example.ui.screens.LnbSkewScreen
import com.example.ui.screens.TranspondersScreen
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class AppNavTab(val title: String, val icon: ImageVector, val tag: String) {
    ALIGN("Align", Icons.Default.Explore, "nav_align"),
    AR_VIEW("AR View", Icons.Default.CameraAlt, "nav_ar_view"),
    DISH_LEVEL("Level", Icons.Default.Straighten, "nav_dish_level"),
    LNB_SKEW("LNB Skew", Icons.Default.RotateRight, "nav_lnb_skew"),
    TRANSPONDERS("Sat & TP", Icons.Default.Satellite, "nav_transponders"),
    GUIDES("Guides", Icons.Default.Build, "nav_guides")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartSatFinderApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSatFinderApp(viewModel: MainViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(AppNavTab.ALIGN) }
    var showSatSelectorSheet by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val selectedSatellite by viewModel.selectedSatellite.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val alignment by viewModel.alignment.collectAsStateWithLifecycle()
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val isGpsLocating by viewModel.isGpsLocating.collectAsStateWithLifecycle()

    val azimuthDelta by viewModel.azimuthDelta.collectAsStateWithLifecycle()
    val elevationDelta by viewModel.elevationDelta.collectAsStateWithLifecycle()
    val isAzimuthLocked by viewModel.isAzimuthLocked.collectAsStateWithLifecycle()
    val isElevationLocked by viewModel.isElevationLocked.collectAsStateWithLifecycle()
    val isFullyLocked by viewModel.isFullyLocked.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpaceBlack,
        topBar = {
            // Persistent Live Satellite Info Strip Bar (Live Telemetry & Alignment Status Ticker)
            SatelliteInfoStripBar(
                satellite = selectedSatellite,
                location = currentLocation,
                alignment = alignment,
                settings = appSettings,
                azimuthDelta = azimuthDelta,
                elevationDelta = elevationDelta,
                isFullyLocked = isFullyLocked,
                onOpenSatelliteSelector = { showSatSelectorSheet = true },
                onOpenLocationPicker = { showLocationSheet = true },
                onOpenSettings = { showSettingsDialog = true },
                onToggleBeeper = { viewModel.toggleBeeper() },
                onToggleVoice = { viewModel.toggleVoice() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyanAccent else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = if (isSelected) CyanAccent else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CardSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.ALIGN -> {
                    CompassAlignScreen(
                        satellite = selectedSatellite,
                        allSatellites = satellites,
                        currentLocation = currentLocation,
                        isGpsLocating = isGpsLocating,
                        alignment = alignment,
                        sensorData = sensorData,
                        settings = appSettings,
                        azimuthDelta = azimuthDelta,
                        elevationDelta = elevationDelta,
                        isAzimuthLocked = isAzimuthLocked,
                        isElevationLocked = isElevationLocked,
                        isFullyLocked = isFullyLocked,
                        onSelectSatellite = { viewModel.selectSatellite(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onLocationSelected = { viewModel.setLocation(it) },
                        onRequestGps = { viewModel.requestGpsLocation() },
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onToggleBeeper = { viewModel.toggleBeeper() },
                        onToggleVoice = { viewModel.toggleVoice() },
                        onSimulateHeading = { viewModel.setSimulatedHeading(it) }
                    )
                }
                AppNavTab.AR_VIEW -> {
                    CameraArScreen(
                        satellite = selectedSatellite,
                        alignment = alignment,
                        sensorData = sensorData,
                        isFullyLocked = isFullyLocked
                    )
                }
                AppNavTab.DISH_LEVEL -> {
                    DishLevelScreen(
                        satellite = selectedSatellite,
                        alignment = alignment,
                        sensorData = sensorData,
                        settings = appSettings
                    )
                }
                AppNavTab.LNB_SKEW -> {
                    LnbSkewScreen(
                        satellite = selectedSatellite,
                        alignment = alignment
                    )
                }
                AppNavTab.TRANSPONDERS -> {
                    TranspondersScreen(
                        satellites = satellites,
                        selectedSatellite = selectedSatellite,
                        onSelectSatellite = {
                            viewModel.selectSatellite(it)
                            currentTab = AppNavTab.ALIGN
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddCustomSatellite = { name, pos, band, lnb, dish, strongTp, notes ->
                            viewModel.saveCustomSatellite(name, pos, band, lnb, dish, strongTp, notes)
                        }
                    )
                }
                AppNavTab.GUIDES -> {
                    InstallationWizardScreen(
                        satellite = selectedSatellite,
                        allSatellites = satellites,
                        currentLocation = currentLocation,
                        alignment = alignment,
                        onNavigateToAlign = { currentTab = AppNavTab.ALIGN }
                    )
                }
            }
        }
    }

    // Global Satellite Selection Sheet (accessible from top strip bar on any screen)
    if (showSatSelectorSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSatSelectorSheet = false },
            sheetState = sheetState,
            containerColor = SpaceBlack,
            dragHandle = null
        ) {
            SatelliteSelectionSheetContent(
                satellites = satellites,
                currentSatellite = selectedSatellite,
                onSelect = {
                    viewModel.selectSatellite(it)
                    showSatSelectorSheet = false
                },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onClose = { showSatSelectorSheet = false }
            )
        }
    }

    // Global Location Picker Sheet (accessible from top strip bar on any screen)
    if (showLocationSheet) {
        LocationPickerSheet(
            currentLocation = currentLocation,
            isGpsLocating = isGpsLocating,
            onLocationSelected = {
                viewModel.setLocation(it)
                showLocationSheet = false
            },
            onRequestGps = { viewModel.requestGpsLocation() },
            onDismiss = { showLocationSheet = false }
        )
    }

    // Global Settings Dialog (accessible from top strip bar on any screen)
    if (showSettingsDialog) {
        SettingsDialog(
            settings = appSettings,
            onUpdateSettings = { viewModel.updateSettings(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
