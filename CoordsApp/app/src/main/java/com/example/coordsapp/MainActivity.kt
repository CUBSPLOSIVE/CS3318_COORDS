package com.example.coordsapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.coordsapp.db.LocationDatabase


// You should see this message if you updated/pulled correctly
class MainActivity : AppCompatActivity() {
    val db = Room.databaseBuilder(applicationContext, LocationDatabase::class.java, "coords_database").build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
    }
}
