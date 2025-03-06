package com.example.coordsapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//    import androidx.core.view.ViewCompat
//    import androidx.core.view.WindowInsetsCompat
//    import androidx.room.Room
//    import com.example.coordsapp.db.LocationDatabase


// You should see this message if you updated/pulled correctly
class MainActivity : AppCompatActivity() {
    //val db = Room.databaseBuilder(applicationContext, LocationDatabase::class.java, "coords_database").build()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val goalCoords = findViewById<TextView>(R.id.tvGoalCoords)
        val currentPosition = findViewById<TextView>(R.id.tvCurrentCoords)
        goalCoords.text = "-75.355130620324° N\n-125.591606645161° W"
        currentPosition.text = "-84.294988648942° N\n-122.669134327106° W"
    }
}
