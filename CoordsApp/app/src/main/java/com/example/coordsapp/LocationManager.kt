package com.example.coordsapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location as AndroidLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

// Source for FusedLocationProviderClient
// https://developer.android.com/develop/sensors-and-location/location/retrieve-current

// LocationKManager handles all location-related logic for the app
class LocationManager(private val activity: Activity) {
    // FusedLocationProviderClient is Google's recommended way to get location.
    // It uses GPS, Wi-Fi, and cell data together to give the best location with less battery use.
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    // Gets the device's last known location.
    // The onLocationReceived callback will run when the location is ready.
    fun getCurrentLocation(onLocationReceived: (AndroidLocation?) -> Unit) {
        // First, check if the permission has been granted
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(activity, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            onLocationReceived(null)
            return
        }

        // Request the last known location from the fused location provider
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if(location != null) {
                    onLocationReceived(location)
                } else{
                    // Handle case where no last known location is possible
                    Toast.makeText(activity, "No location found.", Toast.LENGTH_SHORT).show()
                    onLocationReceived(null)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while getting the location
                Toast.makeText(activity, "Error getting location: $exception", Toast.LENGTH_SHORT).show()
                onLocationReceived(null)
            }
    }
}