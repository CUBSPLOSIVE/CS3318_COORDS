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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.coordsapp.db.LocationDatabase
import com.example.coordsapp.db.LocationRepository
import com.example.coordsapp.db.LocationViewModel
import com.example.coordsapp.db.LocationViewModelFactory
import com.example.coordsapp.db.SavedLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

//    import androidx.core.view.ViewCompat
//    import androidx.core.view.WindowInsetsCompat
//    import androidx.room.Room
//    import com.example.coordsapp.db.LocationDatabase

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
    observe(owner, object : Observer<T> {
        override fun onChanged(t: T) {
            removeObserver(this)
            observer.onChanged(t)
        }
    })
}

class MainActivity : AppCompatActivity() {

    // Declare variables for target location
    private var targetLat: Double? = null
    private var targetLon: Double? = null
    private var targetName: String? = null

    private lateinit var locationManager: LocationManager
    private lateinit var tvCurrentCoords: TextView
    private lateinit var goalCoords: TextView

    private lateinit var tvDistance: TextView
    private lateinit var currentPosition: TextView
    private lateinit var locationViewModel: LocationViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var savedLocation: SavedLocation? = null

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

        // Initialize TextView
        tvCurrentCoords = findViewById<TextView>(R.id.tvCurrentCoords)
        goalCoords = findViewById<TextView>(R.id.tvGoalCoords)
        currentPosition = findViewById<TextView>(R.id.tvCurrentCoords)
        tvDistance = findViewById(R.id.tvDistance)

        // Initialize the LocationManager
        locationManager = LocationManager(this)


        // Initialize ViewModel with factory
        val dao = LocationDatabase.getDatabase(applicationContext).LocationDao()
        val repository = LocationRepository(dao)
        val factory = LocationViewModelFactory(repository)
        locationViewModel = ViewModelProvider(this, factory).get(LocationViewModel::class.java)
        val btnSavedActivity = findViewById<Button>(R.id.btnSaved)
        val btnSaveLocation = findViewById<Button>(R.id.btnSaveLocation)

        val targetLat = intent.getDoubleExtra("TARGET_LAT", 0.0)
        val targetLon = intent.getDoubleExtra("TARGET_LON", 0.0)
        val targetName = intent.getStringExtra("TARGET_NAME") ?: "Unknown Location"

        // If the target location exists, update the UI
        if (!targetLat.isNaN() && !targetLon.isNaN() && targetName != null) {
            goalCoords.text = "Goal Location: $targetName\nLat: $targetLat, Lon: $targetLon"
        }

        btnSavedActivity.setOnClickListener{
            val intent = Intent(this,savedLocationsActivity::class.java)
            startActivity(intent)
        }


        // Check and request location permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Handle save location button click
        btnSaveLocation.setOnClickListener {
            locationViewModel.allLocations.observeOnce(this) { locations ->
                if (locations.size >= 5) {
                    Toast.makeText(this, "You can only save up to 5 locations.", Toast.LENGTH_SHORT).show()
                } else {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            val locationName = "Saved Location ${System.currentTimeMillis()}"

                            val savedLocation = SavedLocation(
                                name = locationName,
                                latitude = latitude,
                                longitude = longitude
                            )

                            locationViewModel.insert(savedLocation)
                            Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show()

                            // Optionally navigate to saved locations
                            val intent = Intent(this, savedLocationsActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
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

            savedLocation = locationViewModel.getLastSavedLocation()

            // Check if a saved location exists
            savedLocation?.let {
                // Calculate the distance and direction to the destination
                val currentLatLng = location?.let { it1 -> LatLng(it1.latitude, location.longitude) }
                val distance =
                    currentLatLng?.let { it1 -> calculateDistance(it1, LatLng(it.latitude, it.longitude)) }
                // Update the UI with distance and direction
                goalCoords.text = "Goal Location: ${it.name}\nLat: ${it.latitude}, Lon: ${it.longitude}"
                currentPosition.text = "Current Location: Lat: $latitude, Lon: $longitude"
                tvDistance.text = "Distance: ${String.format("%.2f", distance)} meters"
            }
        }
    }

    // Calculate the distance between two LatLng points using Haversine formula
    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val radius = 6371000.0 // meters
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radius * c
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
