package com.example.coordsapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationDao {

    @Insert
    suspend fun insert(savedLocation: SavedLocation)

    @Update
    suspend fun update(savedLocation: SavedLocation)

    @Delete
    suspend fun delete(savedLocation: SavedLocation)

    @Query("SELECT * FROM saved_location_data_table")
    fun getAllLocations(): LiveData<List<SavedLocation>>

}

