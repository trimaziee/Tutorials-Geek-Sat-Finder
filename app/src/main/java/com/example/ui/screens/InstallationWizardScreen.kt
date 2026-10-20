package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlignmentCalculation
import com.example.data.model.CityLocation
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
import com.example.util.SatMath

@Composable
fun InstallationWizardScreen(
    satellite: Satellite,
    allSatellites: List<Satellite>,
    currentLocation: CityLocation,
    alignment: AlignmentCalculation,
    onNavigateToAlign: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("Wizard") } // "Wizard", "Multi-LNB", "Receiver Guide"
    var currentStep by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Navigation Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Wizard", "Multi-LNB", "Receiver Guide").forEach { sec ->
                val isSelected = selectedSection == sec
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CyanAccent else CardSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedSection = sec }
                        .testTag("tab_${sec.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = sec,
                        color = if (isSelected) SpaceBlack else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSection) {
            "Wizard" -> {
                WizardContent(
                    satellite = satellite,
                    alignment = alignment,
                    currentStep = currentStep,
                    onNextStep = { if (currentStep < 5) currentStep++ },
                    onPrevStep = { if (currentStep > 1) currentStep-- },
                    onNavigateToAlign = onNavigateToAlign
                )
            }
            "Multi-LNB" -> {
                MultiLnbCalculatorContent(
                    primarySat = satellite,
                    allSatellites = allSatellites,
                    currentLocation = currentLocation
                )
            }
            "Receiver Guide" -> {
                ReceiverTroubleshootingContent(satellite = satellite)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun WizardContent(
    satellite: Satellite,
    alignment: AlignmentCalculation,
    currentStep: Int,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onNavigateToAlign: () -> Unit
) {
    // Stepper header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { step ->
            val isPassed = step < currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPassed -> EmeraldLock
                            isCurrent -> CyanAccent
                            else -> CardSurfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPassed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SpaceBlack, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "$step",
                        color = if (isCurrent) SpaceBlack else TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Wizard Step Details Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            when (currentStep) {
                1 -> {
                    Text("STEP 1: PLUMB THE MAST POLE", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Ensure Vertical 90° Alignment", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "The golden rule of dish installation: If the mounting pipe/mast is tilted even by 2°, your azimuth sweep will miss the satellite arc entirely!\n\n" +
                            "• Use the 'Dish Level' tab in this app.\n" +
                            "• Place your phone along the mast to confirm both axes show a centered green bubble (0.0° deviation).",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                2 -> {
                    Text("STEP 2: SET ELEVATION BRACKET", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Target Elevation: ${String.format("%.1f°", alignment.elevation)}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "• On the back of the dish bracket, loosen the elevation elevation scale bolts.\n" +
                            "• Align the indicator line on the bracket to exactly ${String.format("%.1f°", alignment.elevation)}.\n\n" +
                            "• For Offset Dishes: If using a clinometer on the dish face, the face tilt is ${String.format("%.1f°", alignment.dishPhysicalTilt)} (True El - 22.5° Offset).",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                3 -> {
                    Text("STEP 3: SET LNB SKEW ROTATION", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Target Skew: ${alignment.skewClockText}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "LNB skew aligns the probe inside the feedhorn with the incoming polarized satellite signal.\n\n" +
                            "• Stand in FRONT of the dish facing the LNB.\n" +
                            "• Rotate the LNB so the coaxial cable connector points towards ${alignment.skewClockText}.\n" +
                            "• Target Skew Angle: ${String.format("%+.1f°", alignment.lnbSkew)}.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                4 -> {
                    Text("STEP 4: SWEEP AZIMUTH & AUDIO LOCK", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Target Heading: ${String.format("%.1f°", alignment.trueAzimuth)}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "• Open the 'Align' compass view.\n" +
                            "• Turn the dish slowly towards ${String.format("%.1f°", alignment.trueAzimuth)} (${alignment.cardinalDirection}).\n" +
                            "• The audio beeper pulses faster as you approach the angle, turning into a solid continuous lock tone when centered!\n" +
                            "• Tighten azimuth clamping bolts alternately.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNavigateToAlign,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLock, contentColor = SpaceBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Live Compass Alignment", fontWeight = FontWeight.Bold)
                    }
                }
                5 -> {
                    Text("STEP 5: RECEIVER CONFIRMATION", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Lock Frequency: ${satellite.strongTransponder}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "1. Turn on your Satellite Receiver & TV.\n" +
                            "2. Go to Antenna Settings / Installation Menu.\n" +
                            "3. Set LNB Frequency: ${if (satellite.band.contains("C-Band")) "5150 MHz" else "Universal (9750 - 10600 MHz)"}.\n" +
                            "4. Enter TP: ${satellite.strongTransponder}.\n" +
                            "5. Confirm Signal Quality bar turns GREEN (>65%).\n" +
                            "6. Execute 'Blind Scan' or 'Network Search' to load all channels!",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = onPrevStep,
                        colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceVariant, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Previous Step")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStep < 5) {
                    Button(
                        onClick = onNextStep,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = SpaceBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next Step", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MultiLnbCalculatorContent(
    primarySat: Satellite,
    allSatellites: List<Satellite>,
    currentLocation: CityLocation
) {
    var secondarySat by remember {
        mutableStateOf(
            allSatellites.find { it.orbitalPosition == 93.5 }
                ?: allSatellites.find { it.id != primarySat.id }
                ?: primarySat
        )
    }

    val primaryAlign = SatMath.calculateAlignment(primarySat, currentLocation)
    val secondaryAlign = SatMath.calculateAlignment(secondarySat, currentLocation)

    val separationCm = SatMath.calculateMultiLnbSeparationCm(
        primaryAzimuth = primaryAlign.trueAzimuth,
        primaryElevation = primaryAlign.elevation,
        secondaryAzimuth = secondaryAlign.trueAzimuth,
        secondaryElevation = secondaryAlign.elevation,
        focalLengthCm = if (primarySat.band.contains("C-Band")) 68.0 else 42.0
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Multi-LNB Bracket Distance Calculator", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Calculate side-LNB bracket offset spacing to receive two satellites on one single dish reflector (e.g. AsiaSat 7 + DD FreeDish or PakSat).",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Satellite (Center Focus)
            Surface(color = CardSurfaceVariant, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CENTER LNB (Primary)", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(primarySat.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Az: ${String.format("%.1f°", primaryAlign.trueAzimuth)} • El: ${String.format("%.1f°", primaryAlign.elevation)}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Satellite selector
            Text("SIDE LNB (Secondary Satellite):", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allSatellites.take(4).forEach { sat ->
                    val isSel = sat.id == secondarySat.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) GoldSat else CardSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { secondarySat = sat }
                    ) {
                        Text(
                            text = sat.orbitalPositionFormatted,
                            color = if (isSel) SpaceBlack else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Card
            Surface(
                color = SpaceBlack,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSat),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CALCULATED BRACKET SPACING", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f cm", separationCm),
                        color = GoldSat,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Physical distance between LNB centers on multi-bracket",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiverTroubleshootingContent(satellite: Satellite) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Receiver Setup & Troubleshooting Guide", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            TroubleshootItem(
                question = "Q: Signal Strength is 80%, but Signal Quality is 0%?",
                answer = "Signal Strength only indicates that the coaxial cable between your receiver and LNB is connected! Signal Quality will remain 0% until the dish is precisely aligned to the satellite in azimuth, elevation, and LNB skew."
            )

            Spacer(modifier = Modifier.height(10.dp))

            TroubleshootItem(
                question = "Q: What LNB Frequency should I set in Receiver menu?",
                answer = "• C-Band dishes (big 6ft-8ft): Set LNB Type to '5150'.\n• Ku-Band dishes (small 2ft-3ft): Set LNB Type to 'Universal' (9750 - 10600). Do NOT use 5150 for Ku-band or 0 channels will be found!"
            )

            Spacer(modifier = Modifier.height(10.dp))

            TroubleshootItem(
                question = "Q: How do I configure DiSEqC 1.0 Switch?",
                answer = "If you have multiple LNBs connected into a 4-way DiSEqC switch:\n• Port 1 (LNB 1): e.g. PakSat 38E\n• Port 2 (LNB 2): e.g. AsiaSat 7 105.5E\n• Port 3 (LNB 3): e.g. DD FreeDish 93.5E\nIn your receiver installation menu, match the DiSEqC 1.0 port to the physical port on your roof switch."
            )
        }
    }
}

@Composable
private fun TroubleshootItem(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardSurfaceVariant)
            .padding(12.dp)
    ) {
        Text(question, color = GoldSat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(answer, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
    }
}
