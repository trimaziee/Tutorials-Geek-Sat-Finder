package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlignmentCalculation
import com.example.data.model.Satellite
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
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LnbSkewScreen(
    satellite: Satellite,
    alignment: AlignmentCalculation,
    modifier: Modifier = Modifier
) {
    val skewDeg = alignment.lnbSkew
    val isClockwise = skewDeg < -0.5
    val rotationText = if (isClockwise) "Rotate Clockwise" else if (skewDeg > 0.5) "Rotate Counter-Clockwise" else "Keep Centered"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Overview Banner
        Surface(
            color = CardSurface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = null,
                        tint = GoldSat,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "LNB Skew for ${satellite.name}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Perspective: Standing in FRONT of dish looking into LNB",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual LNB Clock Dial
        LnbClockFace(
            skewDeg = skewDeg.toFloat(),
            skewClockText = alignment.skewClockText,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .testTag("lnb_clock_dial")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Key Values Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TARGET SKEW ANGLE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%+.1f°", skewDeg),
                            color = GoldSat,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CLOCK ORIENTATION", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = alignment.skewClockText,
                            color = CyanAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = CardSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Action: $rotationText until coaxial cable connector points towards ${alignment.skewClockText.substringBefore(" (")}.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Practical Installer Guidance for C-Band vs Ku-Band
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LNB Polarization Setup Instructions",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Ku-Band (Offset Dish):\n" +
                        "1. Loosen the LNB collar screws slightly.\n" +
                        "2. Stand in front of the dish facing the LNB feedhorn.\n" +
                        "3. Rotate the LNB so the cable points to ${alignment.skewClockText}.\n" +
                        "4. Watch the receiver signal quality bar on TP ${satellite.strongTransponder} and tighten gently.\n\n" +
                        "• C-Band (Prime Focus Dish):\n" +
                        "1. On typical 6ft-8ft dishes in Pakistan, align the '0' mark on the C-Band LNB scale horizontally (parallel to the ground).\n" +
                        "2. Set feedhorn focal depth scale (F/D ratio) between 38 and 42 for optimum gain.\n" +
                        "3. Fine rotate by 2°–3° while monitoring receiver signal quality.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LnbClockFace(
    skewDeg: Float,
    skewClockText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f * 0.9f

            // Background Clock Face
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

            // Hour tick marks (12, 1, 2 ... 11)
            for (hour in 1..12) {
                val hourRad = Math.toRadians((hour * 30).toDouble())
                val isQuarter = hour % 3 == 0
                val tickLength = if (isQuarter) 14.dp.toPx() else 8.dp.toPx()
                val tickColor = if (isQuarter) CyanAccent else TextMuted

                val startX = center.x + (radius - tickLength) * sin(hourRad).toFloat()
                val startY = center.y - (radius - tickLength) * cos(hourRad).toFloat()
                val endX = center.x + radius * sin(hourRad).toFloat()
                val endY = center.y - radius * cos(hourRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isQuarter) 2.5.dp.toPx() else 1.dp.toPx()
                )
            }

            // 0° reference at 6 o'clock (standard F-connector straight down position)
            drawLine(
                color = TextSecondary.copy(alpha = 0.4f),
                start = center,
                end = Offset(center.x, center.y + radius * 0.8f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Cable stem pointing towards target skew rotation
            // 0° skew is 6 o'clock (180° in standard canvas rotation)
            // Negative skew is clockwise rotation: angle = 180 - skewDeg
            val cableAngle = 180f - skewDeg

            rotate(degrees = cableAngle, pivot = center) {
                // LNB Cable Stem
                drawLine(
                    color = GoldSat,
                    start = center,
                    end = Offset(center.x, center.y + radius * 0.78f),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // F-Connector Hex Nut graphic
                drawCircle(
                    color = AmberWarning,
                    radius = 9.dp.toPx(),
                    center = Offset(center.x, center.y + radius * 0.74f)
                )

                // Cable wire exit
                drawLine(
                    color = SpaceBlack,
                    start = Offset(center.x, center.y + radius * 0.74f),
                    end = Offset(center.x, center.y + radius * 0.88f),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Center LNB Feedhorn Circular Cap
            drawCircle(
                color = CardSurfaceVariant,
                radius = radius * 0.38f,
                center = center
            )
            drawCircle(
                color = CyanAccent,
                radius = radius * 0.38f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Feedhorn rings (concentric grooves)
            drawCircle(
                color = BorderCyan,
                radius = radius * 0.28f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = BorderCyan,
                radius = radius * 0.18f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = CyanAccent,
                radius = 6.dp.toPx(),
                center = center
            )
        }

        // Center Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 110.dp)
        ) {
            Text(
                text = String.format("%+.1f°", skewDeg),
                color = GoldSat,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "LNB SKEW",
                color = TextSecondary,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
