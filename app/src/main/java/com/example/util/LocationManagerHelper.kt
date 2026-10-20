package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.data.model.CityLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationManagerHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentGpsLocation(): CityLocation? {
        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()

            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val cityLocation = CityLocation(
                                name = "GPS Location",
                                country = "Detected",
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitudeMeters = location.altitude,
                                isGps = true
                            )
                            continuation.resume(cityLocation)
                        } else {
                            // Fallback to last known location
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    continuation.resume(
                                        CityLocation(
                                            name = "GPS Location (Cached)",
                                            country = "Detected",
                                            latitude = lastLoc.latitude,
                                            longitude = lastLoc.longitude,
                                            altitudeMeters = lastLoc.altitude,
                                            isGps = true
                                        )
                                    )
                                } else {
                                    continuation.resume(null)
                                }
                            }.addOnFailureListener {
                                continuation.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            } catch (_: SecurityException) {
                continuation.resume(null)
            }
        }
    }
}
