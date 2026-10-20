package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppSettings
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var beeperEnabled by remember { mutableStateOf(settings.audioBeeperEnabled) }
    var voiceEnabled by remember { mutableStateOf(settings.voiceGuidanceEnabled) }
    var hapticsEnabled by remember { mutableStateOf(settings.hapticFeedbackEnabled) }
    var useMagnetic by remember { mutableStateOf(settings.useMagneticNorth) }
    var offsetAngle by remember { mutableFloatStateOf(settings.dishOffsetAngle.toFloat()) }
    var tolerance by remember { mutableFloatStateOf(settings.alignmentToleranceDegrees.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SpaceBlack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings & Calibration",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = BorderCyan, modifier = Modifier.padding(vertical = 12.dp))

                // Audio Beeper
                SettingToggleRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Audio Sat Finder Beeper",
                    subtitle = "Modulates beep rate as dish approaches satellite",
                    checked = beeperEnabled,
                    onCheckedChange = {
                        beeperEnabled = it
                        onUpdateSettings(settings.copy(audioBeeperEnabled = it))
                    }
                )

                // Voice Guidance
                SettingToggleRow(
                    icon = Icons.Default.RecordVoiceOver,
                    title = "Voice Guidance (TTS)",
                    subtitle = "Speaks direction cues (e.g. Turn right 20°)",
                    checked = voiceEnabled,
                    onCheckedChange = {
                        voiceEnabled = it
                        onUpdateSettings(settings.copy(voiceGuidanceEnabled = it))
                    }
                )

                // Haptic Feedback
                SettingToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Vibration Lock",
                    subtitle = "Vibrates when dish reaches target orientation",
                    checked = hapticsEnabled,
                    onCheckedChange = {
                        hapticsEnabled = it
                        onUpdateSettings(settings.copy(hapticFeedbackEnabled = it))
                    }
                )

                // Magnetic vs True North
                SettingToggleRow(
                    icon = Icons.Default.Explore,
                    title = "Magnetic Compass Azimuth",
                    subtitle = if (useMagnetic) "Adjusted for local magnetic declination" else "Using True Geographic North",
                    checked = useMagnetic,
                    onCheckedChange = {
                        useMagnetic = it
                        onUpdateSettings(settings.copy(useMagneticNorth = it))
                    }
                )

                HorizontalDivider(color = BorderCyan, modifier = Modifier.padding(vertical = 8.dp))

                // Dish Offset Angle Slider
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dish Offset Angle", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format("%.1f°", offsetAngle),
                            color = CyanAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Set to 0° for Prime Focus C-Band dish; 22°–25° for typical Ku-Band offset dishes.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = offsetAngle,
                        onValueChange = {
                            offsetAngle = it
                            onUpdateSettings(settings.copy(dishOffsetAngle = it.toDouble()))
                        },
                        valueRange = 0f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = CardSurfaceVariant
                        )
                    )
                }

                // Tolerance Slider
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alignment Sensitivity", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format("±%.1f°", tolerance),
                            color = EmeraldLock,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Tighter values require more precise alignment before indicating LOCK.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = tolerance,
                        onValueChange = {
                            tolerance = it
                            onUpdateSettings(settings.copy(alignmentToleranceDegrees = it.toDouble()))
                        },
                        valueRange = 1.0f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldLock,
                            activeTrackColor = EmeraldLock,
                            inactiveTrackColor = CardSurfaceVariant
                        )
                    )
                }

                HorizontalDivider(color = BorderCyan, modifier = Modifier.padding(vertical = 8.dp))

                // Tutorials Geek Branding Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.padding(end = 8.dp))
                            Text("Tutorials Geek — Smart Sat Finder", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tagline: Point • Align • Lock\nDesigned for dish installers, technicians, and satellite enthusiasts in Pakistan & worldwide.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SpaceBlack,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardSurfaceVariant
            )
        )
    }
}
