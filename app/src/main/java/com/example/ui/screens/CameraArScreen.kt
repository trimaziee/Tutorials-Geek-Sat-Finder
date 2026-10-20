package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AlignmentCalculation
import com.example.data.model.Satellite
import com.example.sensor.SensorOrientationData
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.GoldSat
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CameraArScreen(
    satellite: Satellite,
    alignment: AlignmentCalculation,
    sensorData: SensorOrientationData,
    isFullyLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .testTag("camera_ar_screen")
    ) {
        if (hasCameraPermission) {
            // Live Camera Viewfinder
            CameraPreviewView(modifier = Modifier.fillMaxSize())

            // AR Overlay Canvas (Horizon, Clarke Belt Arc, Target Reticle, Obstruction Guide)
            ArSkyOverlay(
                satellite = satellite,
                alignment = alignment,
                sensorData = sensorData,
                isFullyLocked = isFullyLocked,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Camera Permission Needed State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Augmented Reality view overlays satellite position directly on your sky view to check for line-of-sight obstructions (trees, walls, or roofs).",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = SpaceBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Information Bar
        Surface(
            color = SpaceBlack.copy(alpha = 0.8f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(satellite.name, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(${satellite.orbitalPositionFormatted})", color = GoldSat, fontSize = 13.sp)
                    }
                    Text(
                        "Target: Az ${String.format("%.1f°", alignment.trueAzimuth)} • El ${String.format("%.1f°", alignment.elevation)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    color = if (isFullyLocked) EmeraldLock else AmberWarning.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isFullyLocked) "LOCKED" else "AIM PHONE",
                        color = if (isFullyLocked) SpaceBlack else AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Line-Of-Sight Obstruction Helper Banner
        Surface(
            color = CardSurface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = GoldSat,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Line-Of-Sight Clearance Check", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "Look through the reticle. Ensure no tree branches, tall buildings, or walls are inside the green clearance beam.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@Composable
fun ArSkyOverlay(
    satellite: Satellite,
    alignment: AlignmentCalculation,
    sensorData: SensorOrientationData,
    isFullyLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f

        // Camera Field of View approximation (Standard phone back camera ~65° horizontal, ~50° vertical)
        val fovH = 65f
        val fovV = 50f

        val pxPerDegH = w / fovH
        val pxPerDegV = h / fovV

        // Azimuth difference: positive means target is to the right of camera center
        var deltaAz = alignment.trueAzimuth.toFloat() - sensorData.azimuthDegrees
        if (deltaAz > 180f) deltaAz -= 360f
        if (deltaAz < -180f) deltaAz += 360f

        // Elevation difference: positive means target is higher than current camera pitch
        val deltaEl = alignment.elevation.toFloat() - sensorData.pitchDegrees

        // Target coordinates in screen pixels
        val satScreenX = centerX + (deltaAz * pxPerDegH)
        val satScreenY = centerY - (deltaEl * pxPerDegV)

        // 1. Draw Center Crosshair of camera
        val crosshairColor = Color.White.copy(alpha = 0.5f)
        drawLine(
            color = crosshairColor,
            start = Offset(centerX - 24.dp.toPx(), centerY),
            end = Offset(centerX + 24.dp.toPx(), centerY),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = crosshairColor,
            start = Offset(centerX, centerY - 24.dp.toPx()),
            end = Offset(centerX, centerY + 24.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        drawCircle(
            color = crosshairColor,
            radius = 16.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )

        // 2. Draw Simulated Geostationary Clarke Belt Arc across sky
        val arcPath = Path()
        var started = false
        for (azOffset in -40..40 step 4) {
            val az = sensorData.azimuthDegrees + azOffset
            // Geostationary arc elevation peaks at South (180°) and slopes down East/West
            val simulatedEl = alignment.elevation.toFloat() - (abs(az - alignment.trueAzimuth.toFloat()) * 0.25f)
            val arcElDiff = simulatedEl - sensorData.pitchDegrees
            val arcX = centerX + (azOffset * pxPerDegH)
            val arcY = centerY - (arcElDiff * pxPerDegV)

            if (!started) {
                arcPath.moveTo(arcX, arcY)
                started = true
            } else {
                arcPath.lineTo(arcX, arcY)
            }
        }
        drawPath(
            path = arcPath,
            color = CyanAccent.copy(alpha = 0.4f),
            style = Stroke(width = 2.dp.toPx())
        )

        // 3. Draw Target Satellite Reticle if in screen bounds
        val isInsideScreen = satScreenX in 30f..(w - 30f) && satScreenY in 30f..(h - 30f)

        if (isInsideScreen) {
            val targetColor = if (isFullyLocked) EmeraldLock else GoldSat

            // Target Outer Reticle Ring
            drawCircle(
                color = targetColor,
                radius = 32.dp.toPx(),
                center = Offset(satScreenX, satScreenY),
                style = Stroke(width = 2.5.dp.toPx())
            )
            // Inner pulsing dot
            drawCircle(
                color = targetColor,
                radius = 7.dp.toPx(),
                center = Offset(satScreenX, satScreenY)
            )

            // Clearance Tunnel (Beam cone representing dish beam width ~2.5°)
            drawCircle(
                color = EmeraldLock.copy(alpha = 0.2f),
                radius = 60.dp.toPx(),
                center = Offset(satScreenX, satScreenY)
            )
        } else {
            // Draw off-screen pointer indicator towards target
            val clampedX = satScreenX.coerceIn(50f, w - 50f)
            val clampedY = satScreenY.coerceIn(80f, h - 140f)

            drawCircle(
                color = AmberWarning,
                radius = 18.dp.toPx(),
                center = Offset(clampedX, clampedY)
            )
        }
    }
}
