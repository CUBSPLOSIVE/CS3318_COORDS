package com.example.coordsapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
//    import androidx.core.view.ViewCompat
//    import androidx.core.view.WindowInsetsCompat
//    import androidx.room.Room
//    import com.example.coordsapp.db.LocationDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var db: LocationDatabase
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var tvCurrentCoords: TextView

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    //val db = Room.databaseBuilder(applicationContext, LocationDatabase::class.java, "coords_database").build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize TextView
        tvCurrentCoords = findViewById(R.id.tvCurrentCoords)

        // Initialize the database inside onCreate to ensure it happens when the activity is ready
        db = Room.databaseBuilder(applicationContext, LocationDatabase::class.java, "coords_database")
            .build()

        // Initialize the LocationManager
        locationManager = LocationManager(this)

        // Initialize Handler and Runnable
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                updateLocation() // Get the location and update the TextView
                handler.postDelayed(this, 1000) // Run every 5 seconds
            }
        }


        // Check and request permissions
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, start location updates
                handler.post(runnable)
            } else{
                // Permission is denied, handle accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Request permission on app startup
        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Method to update location
    private fun updateLocation() {
        locationManager.getCurrentLocation { location -> location?.let{
            val latitude = it.latitude
            val longitude = it.longitude
            val coordsText = "Latitude: $latitude\nLongitude: $longitude"
            tvCurrentCoords.text = coordsText
            }
        }
    }

    override fun onPause(){
        super.onPause()
        // Stop updating location when the activity is paused
        handler.removeCallbacks(runnable)
    }

    override fun onResume(){
        super.onResume()
        // Start updating location when the activity is resumed
        handler.post(runnable)
        val goalCoords = findViewById<TextView>(R.id.tvGoalCoords)
        val currentPosition = findViewById<TextView>(R.id.tvCurrentCoords)
        goalCoords.text = "-75.355130620324° N\n-125.591606645161° W"
        currentPosition.text = "-84.294988648942° N\n-122.669134327106° W"
    }
}
