package com.example.coordsapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location as AndroidLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// Source for FusedLocationProviderClient
// https://developer.android.com/develop/sensors-and-location/location/retrieve-current

// LocationKManager handles all location-related logic for the app
class LocationManager(private val activity: Activity) {

    // FusedLocationProviderClient is Google's recommended way to get location.
    // It uses GPS, Wi-Fi, and cell data together to give the best location with less battery use.
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    // Hold LocationCallback to listen for location updates
    private var locationCallback: LocationCallback? = null

    // Start Continuous location updates
    fun startLocationUpdates(onLocationRecieved : (AndroidLocation?) -> Unit){

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // Permission not granted, do not start location updates
        }

        // Define LocationCallback to handle location updates
        locationCallback = object : LocationCallback() {
            // Trigger every time we get a new location
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.let{
                    for (location in it.locations) {
                        Log.d("LocationManager", "Received location: ${location.latitude} ,${location.longitude}")
                        onLocationRecieved(location)
                    }
                }
            }
        }

        // Using LocationRequest.Builder (new API)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500) // Fastest update interval
            .setMaxUpdateDelayMillis(1000)  // Max delay for location updates
            .build()

        // Start receiving location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper() // Make sure location updates happen on the main thread
        )


    }

    // Function to stop continuous location updates
    fun stopLocationUpdates() {
        // If locationCallback is not null remove location updates
        locationCallback?.let{
            fusedLocationClient.removeLocationUpdates(it)

        }
    }

    // Get current location once (without continuous updates)
    fun getCurrentLocation(onLocationUpdate: (AndroidLocation?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onLocationUpdate(location) // Call with the last known location
        }
    }

}