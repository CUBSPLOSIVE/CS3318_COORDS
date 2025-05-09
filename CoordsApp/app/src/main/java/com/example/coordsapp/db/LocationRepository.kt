package com.example.coordsapp.db

import androidx.lifecycle.LiveData

class LocationRepository(private val locationDao: LocationDao) {

    val allLocations = locationDao.getAllLocations()

    suspend fun insert(location: SavedLocation) {
        locationDao.insert(location)
    }

    suspend fun update(location: SavedLocation) {
        locationDao.update(location)
    }

    suspend fun delete(location: SavedLocation) {
        locationDao.delete(location)
    }

    fun getLocationById(id: Int): LiveData<SavedLocation> {
        return locationDao.getLocationById(id)
    }

}