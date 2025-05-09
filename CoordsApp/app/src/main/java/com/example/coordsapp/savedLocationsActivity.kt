package com.example.coordsapp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.coordsapp.db.LocationDatabase
import com.example.coordsapp.db.LocationRepository
import com.example.coordsapp.db.LocationViewModel
import com.example.coordsapp.db.LocationViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class savedLocationsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var btnSaveLocation: Button
    private lateinit var viewModel: LocationViewModel

    // Button references
    private lateinit var locationButtons: List<Button>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved_locations)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Connect buttons
        locationButtons = listOf(
            findViewById(R.id.btnLocationOne),
            findViewById(R.id.btnLocationTwo),
            findViewById(R.id.btnLocationThree),
            findViewById(R.id.btnLocationFour),
            findViewById(R.id.btnLocationFive)
        )

        // Set up ViewModel
        val dao = LocationDatabase.getDatabase(application).LocationDao()
        val repository = LocationRepository(dao)
        val factory = LocationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LocationViewModel::class.java]

        // Observe saved locations and bind to buttons
        viewModel.allLocations.observe(this) { locations ->
            // Fill up to 5 buttons
            for (i in 0 until 5) {
                val button = locationButtons[i]
                if (i < locations.size) {
                    val location = locations[i]
                    button.text = location.name ?: "Location ${i + 1}"

                    button.setOnClickListener {
                        val intent = Intent(this, LocationDetailActivity::class.java)
                        intent.putExtra("LOCATION_ID", location.id)  // Pass the location ID
                        startActivity(intent)
                    }
                } else {
                    button.text = "Empty Slot"
                    button.setOnClickListener {
                        // Optionally let user add a location here
                        Toast.makeText(this, "No saved location here", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}