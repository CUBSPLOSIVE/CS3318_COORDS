package com.example.coordsapp.db

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    val allLocations: LiveData<List<SavedLocation>> = repository.allLocations

    fun insert(location: SavedLocation) = viewModelScope.launch {
        repository.insert(location)
    }

    fun update(location: SavedLocation) = viewModelScope.launch {
        repository.update(location)
    }

    fun delete(location: SavedLocation) = viewModelScope.launch {
        repository.delete(location)
    }

    fun getLocationById(id: Int): LiveData<SavedLocation> {
        return repository.getLocationById(id)
    }

    // Add this function to get the last saved location
    fun getLastSavedLocation(): SavedLocation? {
        val locations = allLocations.value
        return locations?.lastOrNull() // Returns the most recent location or null if no locations are saved
    }

}

// Factory to pass repository into ViewModel
class LocationViewModelFactory(private val repository: LocationRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}