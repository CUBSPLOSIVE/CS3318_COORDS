package com.example.coordsapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Location1 : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var latField: EditText
    private lateinit var lonField: EditText
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location1)

        // Bind UI elements
        nameField = findViewById(R.id.tvName1)
        latField = findViewById(R.id.tvLatitude1)
        lonField = findViewById(R.id.tvLongitude1)
        updateButton = findViewById(R.id.btnUpdate1)

        // Load saved location if it exists
        val prefs = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        nameField.setText(prefs.getString("name", ""))
        latField.setText(prefs.getFloat("latitude", 0.0f).toString())
        lonField.setText(prefs.getFloat("longitude", 0.0f).toString())

        // Handle Update button click
        updateButton.setOnClickListener {
            val name = nameField.text.toString()
            val lat = latField.text.toString().toFloatOrNull()
            val lon = lonField.text.toString().toFloatOrNull()

            if (name.isNotBlank() && lat != null && lon != null) {
                // Save to SharedPreferences
                prefs.edit()
                    .putString("name", name)
                    .putFloat("latitude", lat)
                    .putFloat("longitude", lon)
                    .apply()

                Toast.makeText(this, "Location updated!", Toast.LENGTH_SHORT).show()

                // Navigate back to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "No values can be left blank!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
