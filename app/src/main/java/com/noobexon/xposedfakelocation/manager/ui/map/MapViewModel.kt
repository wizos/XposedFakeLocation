package com.noobexon.xposedfakelocation.manager.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noobexon.xposedfakelocation.data.model.FavoriteLocation
import com.noobexon.xposedfakelocation.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

/**
 * ViewModel for the Map screen that manages map-related state and operations.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)

    /**
     * Represents field input state with value and validation error message
     */
    data class InputFieldState(val value: String = "", val errorMessage: String? = null)

    /**
     * Represents the UI state for the favorites input dialog
     */
    data class FavoritesInputState(
        val name: InputFieldState = InputFieldState(),
        val latitude: InputFieldState = InputFieldState(),
        val longitude: InputFieldState = InputFieldState()
    )

    /**
     * Represents the complete UI state for the Map screen
     */
    data class MapUiState(
        val isPlaying: Boolean = false,
        val lastClickedLocation: GeoPoint? = null,
        val userLocation: GeoPoint? = null,
        val isLoading: Boolean = true,
        val mapZoom: Double? = null,
        val showGoToPointDialog: Boolean = false,
        val showAddToFavoritesDialog: Boolean = false,
        val goToPointState: Pair<InputFieldState, InputFieldState> = InputFieldState() to InputFieldState(),
        val addToFavoritesState: FavoritesInputState = FavoritesInputState()
    ) {
        val isFabClickable: Boolean
            get() = lastClickedLocation != null
    }

    // Private mutable state
    private val _uiState = MutableStateFlow(MapUiState())
    
    // Public immutable state
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // Events
    private val _goToPointEvent = MutableSharedFlow<GeoPoint>()
    val goToPointEvent: SharedFlow<GeoPoint> = _goToPointEvent.asSharedFlow()

    private val _centerMapEvent = MutableSharedFlow<Unit>()
    val centerMapEvent: SharedFlow<Unit> = _centerMapEvent.asSharedFlow()

    fun togglePlaying() {
        val currentIsPlaying = !_uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = currentIsPlaying) }
        
        if (!currentIsPlaying) {
            updateClickedLocation(null)
        }
        preferencesRepository.saveIsPlaying(currentIsPlaying)
    }

    fun updateUserLocation(location: GeoPoint) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun updateClickedLocation(geoPoint: GeoPoint?) {
        _uiState.update { it.copy(lastClickedLocation = geoPoint) }
        
        geoPoint?.let {
            preferencesRepository.saveLastClickedLocation(
                it.latitude,
                it.longitude
            )
        } ?: preferencesRepository.clearLastClickedLocation()
    }

    fun addFavoriteLocation(favoriteLocation: FavoriteLocation) {
        preferencesRepository.addFavorite(favoriteLocation)
    }

    // Update specific fields in the FavoritesInputState
    fun updateAddToFavoritesField(fieldName: String, newValue: String) {
        val currentState = _uiState.value.addToFavoritesState
        val errorMessage = when (fieldName) {
            "name" -> if (newValue.isBlank()) "Please provide a name" else null
            "latitude" -> validateInput(newValue, -90.0..90.0, "Latitude must be between -90 and 90")
            "longitude" -> validateInput(newValue, -180.0..180.0, "Longitude must be between -180 and 180")
            else -> null
        }

        val updatedState = when (fieldName) {
            "name" -> currentState.copy(name = currentState.name.copy(value = newValue, errorMessage = errorMessage))
            "latitude" -> currentState.copy(latitude = currentState.latitude.copy(value = newValue, errorMessage = errorMessage))
            "longitude" -> currentState.copy(longitude = currentState.longitude.copy(value = newValue, errorMessage = errorMessage))
            else -> currentState
        }
        
        _uiState.update { it.copy(addToFavoritesState = updatedState) }
    }

    // Go to point logic
    fun goToPoint(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _goToPointEvent.emit(GeoPoint(latitude, longitude))
        }
    }

    // Update specific fields in the GoToPointDialog state
    fun updateGoToPointField(fieldName: String, newValue: String) {
        val (latitudeField, longitudeField) = _uiState.value.goToPointState
        val updatedGoToPointState = when (fieldName) {
            "latitude" -> latitudeField.copy(value = newValue) to longitudeField
            "longitude" -> latitudeField to longitudeField.copy(value = newValue)
            else -> latitudeField to longitudeField
        }
        
        _uiState.update { it.copy(goToPointState = updatedGoToPointState) }
    }

    // Center map
    fun triggerCenterMapEvent() {
        viewModelScope.launch {
            _centerMapEvent.emit(Unit)
        }
    }

    fun setLoadingStarted() {
        _uiState.update { it.copy(isLoading = true) }
    }

    // Set loading finished
    fun setLoadingFinished() {
        val isPlaying = preferencesRepository.getIsPlaying()
        val lastClickedLocation = preferencesRepository.getLastClickedLocation()?.let {
            GeoPoint(it.latitude, it.longitude)
        }
        
        _uiState.update { 
            it.copy(
                isLoading = false,
                isPlaying = isPlaying,
                lastClickedLocation = lastClickedLocation
            )
        }
    }

    // Dialog show/hide logic
    fun showGoToPointDialog() { 
        _uiState.update { it.copy(showGoToPointDialog = true) }
    }
    
    fun hideGoToPointDialog() {
        _uiState.update { it.copy(showGoToPointDialog = false) }
        clearGoToPointInputs()
    }

    fun showAddToFavoritesDialog() { 
        _uiState.update { it.copy(showAddToFavoritesDialog = true) }
    }
    
    fun hideAddToFavoritesDialog() {
        _uiState.update { it.copy(showAddToFavoritesDialog = false) }
        clearAddToFavoritesInputs()
    }

    // Helper for input validation
    private fun validateInput(
        input: String, range: ClosedRange<Double>, errorMessage: String
    ): String? {
        val value = input.toDoubleOrNull()
        return if (value == null || value !in range) errorMessage else null
    }

    // Validate GoToPoint inputs
    fun validateAndGo(onSuccess: (latitude: Double, longitude: Double) -> Unit) {
        val (latField, lonField) = _uiState.value.goToPointState
        val latitudeError = validateInput(latField.value, -90.0..90.0, "Latitude must be between -90 and 90")
        val longitudeError = validateInput(lonField.value, -180.0..180.0, "Longitude must be between -180 and 180")

        val updatedGoToPointState = latField.copy(errorMessage = latitudeError) to lonField.copy(errorMessage = longitudeError)
        _uiState.update { it.copy(goToPointState = updatedGoToPointState) }

        if (latitudeError == null && longitudeError == null) {
            onSuccess(latField.value.toDouble(), lonField.value.toDouble())
        }
    }

    // Clear GoToPoint inputs
    fun clearGoToPointInputs() {
        _uiState.update { it.copy(goToPointState = InputFieldState() to InputFieldState()) }
    }

    // Prefill AddToFavorites latitude/longitude with marker values (if available)
    fun prefillCoordinatesFromMarker(latitude: Double?, longitude: Double?) {
        val currentState = _uiState.value.addToFavoritesState
        val updatedState = currentState.copy(
            latitude = currentState.latitude.copy(value = latitude?.toString() ?: ""),
            longitude = currentState.longitude.copy(value = longitude?.toString() ?: "")
        )
        
        _uiState.update { it.copy(addToFavoritesState = updatedState) }
    }

    // Validate and add favorite location
    fun validateAndAddFavorite(onSuccess: (name: String, latitude: Double, longitude: Double) -> Unit) {
        val currentState = _uiState.value.addToFavoritesState

        val latitudeError = validateInput(currentState.latitude.value, -90.0..90.0, "Latitude must be between -90 and 90")
        val longitudeError = validateInput(currentState.longitude.value, -180.0..180.0, "Longitude must be between -180 and 180")
        val nameError = if (currentState.name.value.isBlank()) "Please provide a name" else null

        val updatedState = currentState.copy(
            name = currentState.name.copy(errorMessage = nameError),
            latitude = currentState.latitude.copy(errorMessage = latitudeError),
            longitude = currentState.longitude.copy(errorMessage = longitudeError)
        )
        
        _uiState.update { it.copy(addToFavoritesState = updatedState) }

        if (nameError == null && latitudeError == null && longitudeError == null) {
            onSuccess(currentState.name.value, currentState.latitude.value.toDouble(), currentState.longitude.value.toDouble())
        }
    }

    // Clear AddToFavorites inputs
    fun clearAddToFavoritesInputs() {
        _uiState.update { it.copy(addToFavoritesState = FavoritesInputState()) }
    }
    
    // Update map zoom level
    fun updateMapZoom(zoom: Double) {
        _uiState.update { it.copy(mapZoom = zoom) }
    }
}
