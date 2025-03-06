package com.example.coordsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.coordsapp.db.LocationDatabase

//    import androidx.core.view.ViewCompat
//    import androidx.core.view.WindowInsetsCompat
//    import androidx.room.Room
//    import com.example.coordsapp.db.LocationDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var db: LocationDatabase
    private lateinit var tvCurrentCoords: TextView
    private lateinit var goalCoords: TextView
    private lateinit var currentPosition: TextView

    // Registering for location permission request result
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // If permission is granted, start receiving location updates
                startLocationUpdates()
            } else {
                // If permission is denied, show a Toast message.
                Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            }
        }


    @RequiresApi(Build.VERSION_CODES.M) // Requires API level 23 (Marshmallow)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnSavedActivity = findViewById<Button>(R.id.btnSaved)

        btnSavedActivity.setOnClickListener{
            val intent = Intent(this,savedLocationsActivity::class.java)
            startActivity(intent)
        }

        // Initialize TextView
        tvCurrentCoords = findViewById<TextView>(R.id.tvCurrentCoords)
        goalCoords = findViewById<TextView>(R.id.tvGoalCoords)
        currentPosition = findViewById<TextView>(R.id.tvCurrentCoords)

        // Initialize the database inside onCreate to ensure it happens when the activity is ready
        db = Room.databaseBuilder(applicationContext, LocationDatabase::class.java, "coords_database")
            .build()

        // Initialize the LocationManager
        locationManager = LocationManager(this)

        // Check and request location permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Starts receiving location updates (every second)
    private fun startLocationUpdates() {
        locationManager.startLocationUpdates { location ->
            // Callback will be called every time location is updated
            val latitude = location?.latitude
            val longitude = location?.longitude
            val coordsText = "Latitude: $latitude\nLongitude: $longitude"
            Log.d("MainActivity", "Received location: ${tvCurrentCoords.text}")
            tvCurrentCoords.text = coordsText // Update the UI with new coordinates
        }
    }

    override fun onPause(){
        super.onPause()
        // Stop updating location when the activity is paused
        locationManager.stopLocationUpdates()
    }

    override fun onResume(){
        super.onResume()
        // Start updating location when the activity is resumed
        startLocationUpdates()


    }

    // Handle onDestroy to reset any data when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        // Clear coordinates and stop location updates when the activity is destroyed
        goalCoords.text = "-75.355130620324° N\n-125.591606645161° W"
        currentPosition.text = "-84.294988648942° N\n-122.669134327106° W"
        locationManager.stopLocationUpdates() // Ensure no location updates are running
    }
}
