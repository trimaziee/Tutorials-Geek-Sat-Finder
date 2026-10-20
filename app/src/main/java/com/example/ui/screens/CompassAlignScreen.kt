package com.example.ui.screens

import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlignmentCalculation
import com.example.data.model.AppSettings
import com.example.data.model.CityLocation
import com.example.data.model.Satellite
import com.example.sensor.SensorOrientationData
import com.example.ui.components.LocationPickerSheet
import com.example.ui.components.SatelliteSelectorBar
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldSat
import com.example.ui.theme.PrimaryBlueVariant
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CompassAlignScreen(
    satellite: Satellite,
    allSatellites: List<Satellite>,
    currentLocation: CityLocation,
    isGpsLocating: Boolean,
    alignment: AlignmentCalculation,
    sensorData: SensorOrientationData,
    settings: AppSettings,
    azimuthDelta: Float,
    elevationDelta: Float,
    isAzimuthLocked: Boolean,
    isElevationLocked: Boolean,
    isFullyLocked: Boolean,
    onSelectSatellite: (Satellite) -> Unit,
    onToggleFavorite: (Satellite) -> Unit,
    onLocationSelected: (CityLocation) -> Unit,
    onRequestGps: () -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onToggleBeeper: () -> Unit,
    onToggleVoice: () -> Unit,
    onSimulateHeading: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showLocationSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val targetAzimuth = if (settings.useMagneticNorth) alignment.magneticAzimuth else alignment.trueAzimuth

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Satellite Selection Header
        SatelliteSelectorBar(
            currentSatellite = satellite,
            allSatellites = allSatellites,
            onSelectSatellite = onSelectSatellite,
            onToggleFavorite = onToggleFavorite
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Location & Quick Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showLocationSheet = true }
                    .testTag("location_picker_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = EmeraldLock,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentLocation.name} (${if (currentLocation.isGps) "GPS" else "City"})",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Beeper Toggle
                IconButton(
                    onClick = onToggleBeeper,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (settings.audioBeeperEnabled) CyanAccent.copy(alpha = 0.2f) else CardSurface)
                ) {
                    Icon(
                        imageVector = if (settings.audioBeeperEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Beeper",
                        tint = if (settings.audioBeeperEnabled) CyanAccent else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Voice Toggle
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (settings.voiceGuidanceEnabled) EmeraldLock.copy(alpha = 0.2f) else CardSurface)
                ) {
                    Icon(
                        imageVector = if (settings.voiceGuidanceEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Voice",
                        tint = if (settings.voiceGuidanceEnabled) EmeraldLock else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Settings
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direction Guidance Banner
        AlignmentGuidanceBanner(
            azimuthDelta = azimuthDelta,
            isLocked = isFullyLocked,
            isAzimuthLocked = isAzimuthLocked,
            satelliteName = satellite.name,
            targetAzimuth = targetAzimuth
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 360° Radar Compass Rose
        SatelliteRadarCompass(
            currentHeading = sensorData.azimuthDegrees,
            targetAzimuth = targetAzimuth.toFloat(),
            isLocked = isFullyLocked,
            satelliteName = satellite.name,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Alignment Telemetry Cards
        AlignmentTelemetryRow(
            targetAzimuth = targetAzimuth,
            currentHeading = sensorData.azimuthDegrees.toDouble(),
            targetElevation = alignment.elevation,
            currentPitch = sensorData.pitchDegrees.toDouble(),
            dishPhysicalTilt = alignment.dishPhysicalTilt,
            lnbSkew = alignment.lnbSkew,
            skewClockText = alignment.skewClockText,
            distanceKm = alignment.distanceKm,
            isVisible = alignment.isVisible
        )

        // Calibration warning if needed
        if (sensorData.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
            sensorData.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = AmberWarning.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compass accuracy is low. Move phone in a figure-8 motion away from metal dish to calibrate.",
                        color = AmberWarning,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Emulator / Manual Compass Testing Slider
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .testTag("emulator_compass_slider"),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Emulator Compass Slider", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${sensorData.azimuthDegrees.roundToInt()}°",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Slide to test compass rotation & lock tone on emulator:",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Slider(
                    value = sensorData.azimuthDegrees,
                    onValueChange = { onSimulateHeading(it) },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = CardSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showLocationSheet) {
        LocationPickerSheet(
            currentLocation = currentLocation,
            isGpsLocating = isGpsLocating,
            onLocationSelected = onLocationSelected,
            onRequestGps = onRequestGps,
            onDismiss = { showLocationSheet = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onUpdateSettings = onUpdateSettings,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun AlignmentGuidanceBanner(
    azimuthDelta: Float,
    isLocked: Boolean,
    isAzimuthLocked: Boolean,
    satelliteName: String,
    targetAzimuth: Double
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val bannerColor by animateColorAsState(
        targetValue = when {
            isLocked -> EmeraldLock
            isAzimuthLocked -> CyanAccent
            abs(azimuthDelta) <= 10f -> AmberWarning
            else -> DeepSpace
        },
        label = "bannerColor"
    )

    Surface(
        color = if (isLocked) EmeraldLock.copy(alpha = 0.18f * pulseAlpha) else bannerColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isLocked) EmeraldLock else bannerColor.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isLocked) EmeraldLock else CardSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Locked", tint = SpaceBlack, modifier = Modifier.size(28.dp))
                } else if (azimuthDelta > 0) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Turn Right", tint = CyanAccent, modifier = Modifier.size(28.dp))
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Turn Left", tint = CyanAccent, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isLocked) {
                    Text(
                        text = "TARGET LOCKED! 🎯",
                        color = EmeraldLock,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Dish is aligned with $satelliteName. Hold position & tighten bolts!",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                } else {
                    val deg = abs(azimuthDelta).roundToInt()
                    val turnDirection = if (azimuthDelta > 0) "TURN RIGHT" else "TURN LEFT"
                    Text(
                        text = "$turnDirection $deg°",
                        color = if (deg <= 5) AmberWarning else CyanAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Target Azimuth: ${String.format("%.1f°", targetAzimuth)} (Error: ${String.format("%+.1f°", azimuthDelta)})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SatelliteRadarCompass(
    currentHeading: Float,
    targetAzimuth: Float,
    isLocked: Boolean,
    satelliteName: String,
    modifier: Modifier = Modifier
) {
    val relativeAngle = (targetAzimuth - currentHeading + 360f) % 360f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f * 0.92f

            // Outer dark background circle with subtle gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CardSurface, SpaceBlack),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Outer decorative ring
            drawCircle(
                color = BorderCyan,
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner distance range rings
            drawCircle(
                color = BorderCyan.copy(alpha = 0.4f),
                radius = radius * 0.65f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = BorderCyan.copy(alpha = 0.3f),
                radius = radius * 0.35f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Compass degree tick marks (rotating with heading)
            rotate(degrees = -currentHeading, pivot = center) {
                for (deg in 0 until 360 step 10) {
                    val angleRad = Math.toRadians(deg.toDouble())
                    val isMajor = deg % 30 == 0
                    val isCardinal = deg % 90 == 0

                    val tickLength = if (isCardinal) 16.dp.toPx() else if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                    val tickColor = if (isCardinal) CyanAccent else if (isMajor) TextSecondary else TextMuted.copy(alpha = 0.4f)
                    val strokeWidth = if (isCardinal) 2.5.dp.toPx() else 1.dp.toPx()

                    val startX = center.x + (radius - tickLength) * sin(angleRad).toFloat()
                    val startY = center.y - (radius - tickLength) * cos(angleRad).toFloat()
                    val endX = center.x + radius * sin(angleRad).toFloat()
                    val endY = center.y - radius * cos(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeWidth
                    )
                }

                // Target Satellite Marker on perimeter
                val targetRad = Math.toRadians(targetAzimuth.toDouble())
                val satMarkerX = center.x + (radius * 0.88f) * sin(targetRad).toFloat()
                val satMarkerY = center.y - (radius * 0.88f) * cos(targetRad).toFloat()

                drawCircle(
                    color = if (isLocked) EmeraldLock else GoldSat,
                    radius = 12.dp.toPx(),
                    center = Offset(satMarkerX, satMarkerY)
                )
                drawCircle(
                    color = SpaceBlack,
                    radius = 5.dp.toPx(),
                    center = Offset(satMarkerX, satMarkerY)
                )
            }

            // Target pointer needle (pointing from center towards relative angle)
            rotate(degrees = relativeAngle, pivot = center) {
                val needlePath = Path().apply {
                    moveTo(center.x, center.y - radius * 0.78f) // Tip
                    lineTo(center.x + 12.dp.toPx(), center.y - radius * 0.2f)
                    lineTo(center.x + 4.dp.toPx(), center.y)
                    lineTo(center.x - 4.dp.toPx(), center.y)
                    lineTo(center.x - 12.dp.toPx(), center.y - radius * 0.2f)
                    close()
                }

                drawPath(
                    path = needlePath,
                    brush = Brush.verticalGradient(
                        colors = if (isLocked) listOf(EmeraldLock, CyanAccent) else listOf(CyanAccent, PrimaryBlueVariant),
                        startY = center.y - radius * 0.78f,
                        endY = center.y
                    )
                )
            }

            // Fixed Top Device Heading Reticle (12 o'clock)
            val topIndicatorPath = Path().apply {
                moveTo(center.x, center.y - radius - 2.dp.toPx())
                lineTo(center.x - 8.dp.toPx(), center.y - radius + 12.dp.toPx())
                lineTo(center.x + 8.dp.toPx(), center.y - radius + 12.dp.toPx())
                close()
            }
            drawPath(
                path = topIndicatorPath,
                color = if (isLocked) EmeraldLock else AmberWarning
            )

            // Center hub
            drawCircle(
                color = if (isLocked) EmeraldLock else CyanAccent,
                radius = 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = SpaceBlack,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Digital Heading Readout in Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 70.dp)
        ) {
            Text(
                text = "${currentHeading.roundToInt()}°",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "HEADING",
                color = TextSecondary,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun AlignmentTelemetryRow(
    targetAzimuth: Double,
    currentHeading: Double,
    targetElevation: Double,
    currentPitch: Double,
    dishPhysicalTilt: Double,
    lnbSkew: Double,
    skewClockText: String,
    distanceKm: Double,
    isVisible: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Azimuth & Elevation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Azimuth Card
            TelemetryCard(
                modifier = Modifier.weight(1f),
                title = "AZIMUTH",
                targetValue = String.format("%.1f°", targetAzimuth),
                targetSubtext = AlignmentCalculation.getCardinal(targetAzimuth),
                liveValue = String.format("%.1f°", currentHeading),
                liveSubtext = "Phone Heading",
                isMatched = abs(targetAzimuth - currentHeading) <= 2.5
            )

            // Elevation Card
            TelemetryCard(
                modifier = Modifier.weight(1f),
                title = "ELEVATION",
                targetValue = String.format("%.1f°", targetElevation),
                targetSubtext = if (isVisible) "Line-of-Sight OK" else "Below Horizon!",
                liveValue = String.format("%.1f°", currentPitch),
                liveSubtext = "Phone Tilt",
                isMatched = abs(targetElevation - currentPitch) <= 3.0
            )
        }

        // Row 2: LNB Skew & Dish Physical Face Tilt
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LNB Skew Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("LNB POLARIZATION / SKEW", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%+.1f°", lnbSkew),
                        color = GoldSat,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = skewClockText,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Offset Dish Face Tilt
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("OFFSET DISH FACE TILT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f°", dishPhysicalTilt),
                        color = CyanAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Face tilt = El - 22.5°",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryCard(
    modifier: Modifier = Modifier,
    title: String,
    targetValue: String,
    targetSubtext: String,
    liveValue: String,
    liveSubtext: String,
    isMatched: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp),
        border = if (isMatched) androidx.compose.foundation.BorderStroke(1.dp, EmeraldLock) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("TARGET", color = TextMuted, fontSize = 9.sp)
                    Text(targetValue, color = if (isMatched) EmeraldLock else CyanAccent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(targetSubtext, color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CURRENT", color = TextMuted, fontSize = 9.sp)
                    Text(liveValue, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(liveSubtext, color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
