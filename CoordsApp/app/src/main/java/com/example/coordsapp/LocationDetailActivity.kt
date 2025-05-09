package com.example.coordsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.coordsapp.db.LocationDatabase
import com.example.coordsapp.db.LocationRepository
import com.example.coordsapp.db.LocationViewModel
import com.example.coordsapp.db.LocationViewModelFactory
import com.example.coordsapp.db.SavedLocation

class LocationDetailActivity : AppCompatActivity(){
    private lateinit var viewModel: LocationViewModel


    // UI elements
    private lateinit var tvLocationName: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var editTextName: EditText
    private lateinit var btnStartNavigation: Button

    private var savedLocation: SavedLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location_detail)

        val locationId = intent.getIntExtra("LOCATION_ID", -1)

        // Initialize UI elements
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        editTextName = findViewById(R.id.editTextName)
        tvLocationName = findViewById(R.id.tvLocationName)
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        val dao = LocationDatabase.getDatabase(application).LocationDao()
        val repository = LocationRepository(dao)
        val factory = LocationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LocationViewModel::class.java]

        // Get the location details by ID
        viewModel.getLocationById(locationId).observe(this) { location ->
            if (location != null) {
                savedLocation = location
                tvLocationName.text = location.name
                tvLatitude.text = "Latitude: ${location.latitude}"
                tvLongitude.text = "Longitude: ${location.longitude}"

                // Handle Edit button click
                btnEdit.setOnClickListener {
                    val newName = editTextName.text.toString()
                    if (newName.isNotBlank()) {
                        val updated = location.copy(name = newName)
                        viewModel.update(updated)
                        Toast.makeText(this, "Location name updated", Toast.LENGTH_SHORT).show()
                        tvLocationName.text = newName
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }

                btnStartNavigation.setOnClickListener {
                    savedLocation?.let {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("TARGET_LAT", it.latitude)
                            putExtra("TARGET_LON", it.longitude)
                            putExtra("TARGET_NAME", it.name)
                        }
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(this, "Error: Location not found", Toast.LENGTH_SHORT).show()
                    }
                }

                // Handle Delete button click
                btnDelete.setOnClickListener {
                    // Delete the location
                    viewModel.delete(location)
                    Toast.makeText(this, "Location deleted", Toast.LENGTH_SHORT).show()
                    finish()  // Close the activity after deletion
                }
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to show the delete confirmation dialog
    private fun showDeleteConfirmationDialog(location: SavedLocation) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Location")
            .setMessage("Are you sure you want to delete the location '${location.name}'?")
            .setPositiveButton("Yes") { _, _ ->
                // If confirmed, delete the location from the database
                viewModel.delete(location)
                Toast.makeText(this, "Location deleted", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity after deletion
            }
            .setNegativeButton("No") { dialog, _ ->
                // If canceled, dismiss the dialog
                dialog.dismiss()
            }
            .create()

        dialog.show()  // Show the dialog
    }
}