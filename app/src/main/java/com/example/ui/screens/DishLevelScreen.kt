package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlignmentCalculation
import com.example.data.model.AppSettings
import com.example.data.model.Satellite
import com.example.sensor.SensorOrientationData
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.GoldSat
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DishLevelScreen(
    satellite: Satellite,
    alignment: AlignmentCalculation,
    sensorData: SensorOrientationData,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    var isOffsetDishMode by remember { mutableStateOf(!satellite.band.contains("C-Band")) }

    val targetTiltAngle = if (isOffsetDishMode) alignment.dishPhysicalTilt else alignment.elevation
    val currentTiltAngle = abs(sensorData.pitchDegrees)
    val tiltDiff = abs(targetTiltAngle - currentTiltAngle)
    val isTiltMatched = tiltDiff <= 1.5

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: Offset Dish (Ku-Band) vs Prime Focus Dish (C-Band)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOffsetDishMode) CyanAccent else CardSurface,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isOffsetDishMode = true }
                    .testTag("mode_offset_dish")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Offset Dish (Ku-Band)",
                        color = if (isOffsetDishMode) SpaceBlack else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Uses -${settings.dishOffsetAngle}° Offset",
                        color = if (isOffsetDishMode) SpaceBlack.copy(alpha = 0.7f) else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (!isOffsetDishMode) CyanAccent else CardSurface,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isOffsetDishMode = false }
                    .testTag("mode_prime_focus")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Prime Focus (C-Band)",
                        color = if (!isOffsetDishMode) SpaceBlack else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "0° Offset (Mesh / 6ft-8ft)",
                        color = if (!isOffsetDishMode) SpaceBlack.copy(alpha = 0.7f) else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Match Banner
        Surface(
            color = if (isTiltMatched) EmeraldLock.copy(alpha = 0.2f) else CardSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isTiltMatched) EmeraldLock else BorderCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isTiltMatched) EmeraldLock else CardSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTiltMatched) Icons.Default.CheckCircle else Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = if (isTiltMatched) SpaceBlack else CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isTiltMatched) "ELEVATION TILT LOCKED!" else "Place phone edge on dish face rim",
                        color = if (isTiltMatched) EmeraldLock else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTiltMatched) "Dish face tilt is precisely set." else "Tilt dish up/down until target matches.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // High Precision Clinometer Dial
        ClinometerDial(
            currentAngle = currentTiltAngle,
            targetAngle = targetTiltAngle.toFloat(),
            isMatched = isTiltMatched,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Tilt Calculation Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Elevation Tilt Parameters",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("True Satellite Elevation:", color = TextSecondary, fontSize = 13.sp)
                    Text(String.format("%.1f°", alignment.elevation), color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (isOffsetDishMode) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dish Inherent Offset Angle:", color = TextSecondary, fontSize = 13.sp)
                        Text(String.format("-%.1f°", settings.dishOffsetAngle), color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Physical Face Tilt:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(String.format("%.1f°", targetTiltAngle), color = EmeraldLock, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isOffsetDishMode) {
                        "Note: Offset satellite dishes point at the satellite via an angled reflector. Placing a level on the front face of the dish measures (Elevation - Offset Angle)."
                    } else {
                        "Note: On prime-focus circular dishes, the physical dish face angle directly equals the true satellite elevation."
                    },
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vertical Pole Bubble Spirit Level
        DualAxisSpiritBubbleLevel(
            pitch = sensorData.pitchDegrees,
            roll = sensorData.rollDegrees,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ClinometerDial(
    currentAngle: Float,
    targetAngle: Float,
    isMatched: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f * 0.9f

            // Background circle
            drawCircle(
                color = CardSurface,
                radius = radius,
                center = center
            )
            drawCircle(
                color = BorderCyan,
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw Clinometer Arc (0° to 90°) from horizontal right (0°) to top (90°)
            // In Android Canvas: 0° is 3 o'clock (horizontal), -90° is 12 o'clock (vertical)
            drawArc(
                color = CardSurfaceVariant,
                startAngle = 0f,
                sweepAngle = -90f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius * 0.75f, center.y - radius * 0.75f),
                size = androidx.compose.ui.geometry.Size(radius * 1.5f, radius * 1.5f)
            )

            // Target Angle marker tick on arc
            val targetRad = Math.toRadians(targetAngle.toDouble())
            val targetMarkerX = center.x + (radius * 0.75f) * cos(targetRad).toFloat()
            val targetMarkerY = center.y - (radius * 0.75f) * sin(targetRad).toFloat()

            drawCircle(
                color = if (isMatched) EmeraldLock else GoldSat,
                radius = 8.dp.toPx(),
                center = Offset(targetMarkerX, targetMarkerY)
            )

            // Current Angle colored active arc
            val clampedCurrent = currentAngle.coerceIn(0f, 90f)
            drawArc(
                color = if (isMatched) EmeraldLock else CyanAccent,
                startAngle = 0f,
                sweepAngle = -clampedCurrent,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius * 0.75f, center.y - radius * 0.75f),
                size = androidx.compose.ui.geometry.Size(radius * 1.5f, radius * 1.5f)
            )
        }

        // Center Digital Readout
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f°", currentAngle),
                color = if (isMatched) EmeraldLock else TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "TARGET: ${String.format("%.1f°", targetAngle)}",
                color = if (isMatched) EmeraldLock else CyanAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isMatched) "PERFECT LOCK" else "PHONE TILT",
                color = TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DualAxisSpiritBubbleLevel(
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val isPlumb = abs(pitch) <= 1.5f && abs(roll) <= 1.5f

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pole Spirit Level (Plumb Check)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Surface(
                    color = if (isPlumb) EmeraldLock.copy(alpha = 0.2f) else CardSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isPlumb) "PLUMB LEVEL" else "OFF-LEVEL",
                        color = if (isPlumb) EmeraldLock else AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Dish mounting pole must be 100% vertically plumb. Hold phone flat against the pole to verify.",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    // Outer level boundary
                    drawCircle(color = SpaceBlack, radius = radius, center = center)
                    drawCircle(color = BorderCyan, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

                    // Center crosshair and deadband ring
                    drawCircle(
                        color = if (isPlumb) EmeraldLock else BorderCyan,
                        radius = 16.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawLine(
                        color = BorderCyan.copy(alpha = 0.5f),
                        start = Offset(center.x - radius, center.y),
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = BorderCyan.copy(alpha = 0.5f),
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y + radius),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Bubble position mapped from roll and pitch
                    val bubbleOffsetMax = radius * 0.75f
                    val bubbleX = center.x + (roll / 15f).coerceIn(-1f, 1f) * bubbleOffsetMax
                    val bubbleY = center.y + (pitch / 15f).coerceIn(-1f, 1f) * bubbleOffsetMax

                    drawCircle(
                        color = if (isPlumb) EmeraldLock else CyanAccent,
                        radius = 12.dp.toPx(),
                        center = Offset(bubbleX, bubbleY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 4.dp.toPx(),
                        center = Offset(bubbleX - 3.dp.toPx(), bubbleY - 3.dp.toPx())
                    )
                }
            }
        }
    }
}
