package com.example.coordsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.coordsapp.db.LocationDatabase

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometerData = FloatArray(3) // Reads 3 for 3D space (X,Y,Z)
    private var magnetometerData = FloatArray(3)

    private lateinit var navArrow: ImageView
    private var currentDegree = 0f //current rotation angle of the arrow

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

    // Rotate the arrow according to the degree passed in
    private fun rotateArrow(degree: Float) {
        val rotateAnimation = RotateAnimation(
            currentDegree,
            degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 210
        rotateAnimation.fillAfter = true
        navArrow.startAnimation(rotateAnimation)
        currentDegree = degree
    }

    // Moves Arrow based on changes in data read by the accelerometer and the magnetometer
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            when (event?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> accelerometerData = event.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> magnetometerData = event.values.clone()
            }

            val rotationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData)

            if (success) {
                val orientationAngles = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                rotateArrow(-azimuth)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


    @RequiresApi(Build.VERSION_CODES.M) // Requires API level 23 (Marshmallow)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        navArrow = findViewById(R.id.navArrow)

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

        sensorManager.unregisterListener(sensorListener)

    }

    override fun onResume(){
        super.onResume()
        // Start updating location when the activity is resumed
        startLocationUpdates()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer -> sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI) }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField -> sensorManager.registerListener(sensorListener, magneticField, SensorManager.SENSOR_DELAY_UI) }

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
