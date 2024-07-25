package com.map.sampleapp

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.map.sampleapp.ui.CustomMapView
import com.map.sampleapp.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {

    val showRationaleDialog = MutableStateFlow(false)

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.getOrElse(Manifest.permission.ACCESS_FINE_LOCATION) { false }
        when {
            permissions.getOrElse(Manifest.permission.ACCESS_FINE_LOCATION) { false } -> {
                checkIfLocationEnabled()
            }

            permissions.getOrElse(Manifest.permission.ACCESS_COARSE_LOCATION) { false } -> {
                checkIfLocationEnabled()
            }

            else -> {
                showRationaleDialog.value = true
            }
        }
    }

    private val settingsResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkIfLocationEnabled()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationAccessResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            onLocationEnabled()
        } else {
            Toast.makeText(this, "User Refused to turn on location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val list = listOf(
            MapPin("The Empire State Building", LatLng(40.748817, -73.985428), 50.0),
            MapPin("central park", LatLng(40.785091, -73.968285), 500.0),
            MapPin("JFK Airport", LatLng(40.641766, -73.780968), 1000.0),
            MapPin("Statue of Liberty", LatLng(40.689247, -74.044502), 200.0)
        )

        setContent {
            MyApplicationTheme {
                val showDialog by showRationaleDialog.collectAsState()
                if (showDialog) {
                    RationaleDialog()
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Color.White)
                    ) {
                        CustomMapView(
                            modifier = Modifier
                                .weight(1f)
                                .padding(20.dp),
                            list = list
                        )
                        Button(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 20.dp),
                            onClick = {
                                askForLocationPermission()
                            }) {
                            Text(text = "Ask Location Permission/Start Location Tracking")
                        }

                        Button(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 20.dp),
                            onClick = {
                                stopService(Intent(this@MainActivity, LocationService::class.java))
                            }) {
                            Text(text = "Stop LocationTracking")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RationaleDialog() {
        AlertDialog(
            title = { Text(text = "Location Permission Required") },
            text = {
                Text(text = "Please enable location for this usage.")
            },
            onDismissRequest = {
                showRationaleDialog.update { false }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        settingsResult.launch(intent)
                        showRationaleDialog.update { false }
                    }
                ) {
                    Text(text = "Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog.update { false }
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )

    }

    private fun askForLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkIfLocationEnabled()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkIfLocationEnabled() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30 * 1000)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest.build())
            .setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                onLocationEnabled()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            locationAccessResult.launch(
                                IntentSenderRequest.Builder(resolvable.resolution).build()
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                    }
                }
            }
        }
    }

    private fun onLocationEnabled() {
        startService(Intent(this, LocationService::class.java))
    }
}