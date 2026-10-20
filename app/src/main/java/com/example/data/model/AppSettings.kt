package com.example.data.model

data class AppSettings(
    val audioBeeperEnabled: Boolean = true,
    val voiceGuidanceEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val useMagneticNorth: Boolean = true,
    val dishOffsetAngle: Double = 22.5, // Standard offset dish angle in degrees
    val alignmentToleranceDegrees: Double = 2.5 // Within +/- this angle triggers lock
)
