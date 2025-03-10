package com.example.coordsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class savedLocationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved_locations)

        val btnLocationOne = findViewById<Button>(R.id.btnLocationOne)

        btnLocationOne.setOnClickListener{
            val intent = Intent(this,Location1::class.java)
            startActivity(intent)
        }

//        val btnSavedActivity = findViewById<Button>(R.id.btnSaved)
//
//        btnSavedActivity.setOnClickListener{
//            val intent = Intent(this,savedLocationsActivity::class.java)
//            startActivity(intent)
//        }

    }
}