package com.noobexon.xposedfakelocation

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import com.noobexon.xposedfakelocation.data.KEY_IS_PLAYING
import com.noobexon.xposedfakelocation.data.KEY_LATITUDE
import com.noobexon.xposedfakelocation.data.KEY_LONGITUDE
import com.noobexon.xposedfakelocation.data.SHARED_PREFS_FILE
import org.osmdroid.util.GeoPoint

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // App's shared prefrences
    private val sharedPrefs = (getApplication() as Context).getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

    // State variable for the play FAB
    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying

    // State variable for coordinates of the last clicked point in the map
    private val _lastClickedLocation = mutableStateOf<GeoPoint?>(null)
    val lastClickedLocation: State<GeoPoint?> get() = _lastClickedLocation

    // State variable for permission status
    private val _hasPermissions = mutableStateOf(false)
    val hasPermissions: State<Boolean> get() = _hasPermissions

    // State variable to track if permission check is done
    private val _isPermissionsCheckDone = mutableStateOf(false)
    val isPermissionsCheckDone: State<Boolean> get() = _isPermissionsCheckDone

    // State variable for tracking whether permissions are permanently denied
    private val _permanentlyDenied = mutableStateOf(false)
    val permanentlyDenied: State<Boolean> get() = _permanentlyDenied

    // Drawer state
    private val _drawerState = mutableStateOf(DrawerValue.Closed)
    val drawerState: State<DrawerValue> get() = _drawerState

    // Navigation state for showing settings screen
    private val _showSettings = mutableStateOf(false)
    val showSettings: State<Boolean> get() = _showSettings

    // Navigation state for showing about screen
    private val _showAbout = mutableStateOf(false)
    val showAbout: State<Boolean> get() = _showAbout

    // FAB clickability
    val isFabClickable: Boolean
        get() = lastClickedLocation.value != null

    // Toggle the play/stop status
    fun togglePlaying() {
        _isPlaying.value = !_isPlaying.value
        if (!_isPlaying.value) {
            updateClickedLocation(null)
        }
        sharedPrefs.edit().putBoolean(KEY_IS_PLAYING, _isPlaying.value).apply()
    }

    // Update the last clicked location
    fun updateClickedLocation(geoPoint: GeoPoint?) {
        _lastClickedLocation.value = geoPoint

        geoPoint?.let {
            sharedPrefs.edit()
                .putFloat(KEY_LATITUDE, it.latitude.toFloat())
                .putFloat(KEY_LONGITUDE, it.longitude.toFloat())
                .apply()
        } ?: run {
            sharedPrefs.edit()
                .remove(KEY_LATITUDE)
                .remove(KEY_LONGITUDE)
                .apply()
        }
    }

    // Update location permission status
    fun updatePermissionsStatus(granted: Boolean) {
        _hasPermissions.value = granted
    }

    // Update permanent denial status
    fun updatePermanentlyDenied(denied: Boolean) {
        _permanentlyDenied.value = denied
    }

    // Mark permission check as done
    fun markPermissionCheckDone() {
        _isPermissionsCheckDone.value = true
    }



    fun toggleDrawerState() {
        _drawerState.value = if (_drawerState.value == DrawerValue.Closed) {
            DrawerValue.Open
        } else {
            DrawerValue.Closed
        }
    }

    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }

    fun toggleAbout() {
        _showAbout.value = !_showAbout.value
    }

    fun closeDrawer() {
        _drawerState.value = DrawerValue.Closed
    }

    fun handleBackPress(activity: Activity?) {
        when {
            _drawerState.value == DrawerValue.Open -> closeDrawer()
            _showSettings.value -> _showSettings.value = false
            _showAbout.value -> _showAbout.value = false
            else -> activity?.finish()
        }
    }
}
