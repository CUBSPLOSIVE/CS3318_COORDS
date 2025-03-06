package com.example.coordsapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_location_data_table")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    val id: Int = 0,

    @ColumnInfo(name = "location_name")
    val name: String,

    @ColumnInfo(name = "location_latitude")
    val latitude: Double,

    @ColumnInfo(name = "location_longitude")
    val longitude: Double
)
