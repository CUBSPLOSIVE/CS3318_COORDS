package com.example.coordsapp.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(version = 1, entities = [SavedLocation::class], exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao() : LocationDao
}