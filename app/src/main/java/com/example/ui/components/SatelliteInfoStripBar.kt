package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlignmentCalculation
import com.example.data.model.AppSettings
import com.example.data.model.CityLocation
import com.example.data.model.Satellite
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldSat
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SatelliteInfoStripBar(
    satellite: Satellite,
    location: CityLocation,
    alignment: AlignmentCalculation,
    settings: AppSettings,
    azimuthDelta: Float,
    elevationDelta: Float,
    isFullyLocked: Boolean,
    onOpenSatelliteSelector: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleBeeper: () -> Unit,
    onToggleVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate live signal quality percentage based on azimuth and elevation proximity (0% to 100%)
    val azCloseness = (1f - (abs(azimuthDelta) / 30f)).coerceIn(0f, 1f)
    val elCloseness = (1f - (abs(elevationDelta) / 20f)).coerceIn(0f, 1f)
    val rawQuality = if (isFullyLocked) 1.0f else (azCloseness * 0.7f + elCloseness * 0.3f)
    val signalPercent = (rawQuality * 100f).roundToInt()

    val animatedProgress by animateFloatAsState(
        targetValue = rawQuality,
        animationSpec = tween(300),
        label = "signalProgress"
    )

    val signalColor by animateColorAsState(
        targetValue = when {
            isFullyLocked -> EmeraldLock
            signalPercent >= 70 -> CyanAccent
            signalPercent >= 40 -> AmberWarning
            else -> ErrorRed
        },
        label = "signalColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("satellite_info_strip_bar"),
        color = DeepSpace,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Row 1: Brand & Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tutorials Geek Brand with glowing satellite icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Satellite,
                            contentDescription = "Tutorials Geek",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "TUTORIALS GEEK",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Smart Sat Finder",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Action Controls (Beeper, Voice, Settings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Audio Beeper Icon
                    IconButton(
                        onClick = onToggleBeeper,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.audioBeeperEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Beeper",
                            tint = if (settings.audioBeeperEnabled) CyanAccent else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Voice Guidance Icon
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.voiceGuidanceEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Voice",
                            tint = if (settings.voiceGuidanceEnabled) EmeraldLock else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Settings Dialog Icon
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Satellite & Location Interactive Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Satellite Chip (Clickable to switch satellite from any screen!)
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenSatelliteSelector() }
                        .border(1.dp, BorderCyan, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = satellite.name,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${satellite.orbitalPositionFormatted} • ${satellite.band}",
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Location Chip (Clickable to switch city or detect GPS from any screen!)
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenLocationPicker() }
                        .border(1.dp, BorderCyan, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldLock, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = location.name,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (location.isGps) "GPS Active" else location.country,
                                color = EmeraldLock,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: 4-Metric Strip Ticker (Azimuth, Elevation, Skew, Strong TP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Azimuth
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AZIMUTH", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.1f°", if (settings.useMagneticNorth) alignment.magneticAzimuth else alignment.trueAzimuth),
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (abs(azimuthDelta) <= 2.5f) "LOCK" else "${if (azimuthDelta > 0) "R" else "L"} ${abs(azimuthDelta).roundToInt()}°",
                        color = if (abs(azimuthDelta) <= 2.5f) EmeraldLock else AmberWarning,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Elevation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ELEVATION", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.1f°", alignment.elevation),
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Tilt: ${String.format("%.0f°", alignment.dishPhysicalTilt)}",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }

                // LNB Skew
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LNB SKEW", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%+.1f°", alignment.lnbSkew),
                        color = GoldSat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = alignment.skewClockText.substringBefore(" o'clock"),
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }

                // Strong TP
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STRONG TP", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    val tpParts = satellite.strongTransponder.trim().split(" ")
                    val tpFreq = tpParts.firstOrNull() ?: "-"
                    val tpRest = if (tpParts.size > 1) tpParts.drop(1).joinToString(" ") else ""
                    Text(
                        text = tpFreq,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = tpRest.ifEmpty { "TP" },
                        color = GoldSat,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 4: Signal Alignment Quality Meter Strip Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFullyLocked) "ALIGNMENT: LOCKED 100%" else "ALIGNMENT QUALITY: $signalPercent%",
                    color = signalColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                if (isFullyLocked) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Locked",
                        tint = EmeraldLock,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Multi-segment style Meter Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = signalColor,
                trackColor = CardSurfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
