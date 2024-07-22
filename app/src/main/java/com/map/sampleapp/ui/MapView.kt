package com.map.sampleapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.map.sampleapp.MapPin
import com.map.sampleapp.R
import com.map.sampleapp.bitmapDescriptorFromVector
import kotlinx.coroutines.delay

@Composable
fun CustomMapView(modifier: Modifier, list: List<MapPin>) {
    val context = LocalContext.current
    val boundsBuilder = LatLngBounds.builder()

    for (coordinate in list.map { it.coordinate }) {
        boundsBuilder.include(coordinate)
    }
    val bounds = try {
        boundsBuilder.build()
    } catch (e: Exception) {
        null
    }
    val cameraPositionState = rememberCameraPositionState {}

    var markerWithInfoWindow = remember<Marker?> { null }
    LaunchedEffect(key1 = list) {
        delay(500)
        if (bounds != null) {
            if (markerWithInfoWindow?.isInfoWindowShown == true)
                markerWithInfoWindow?.hideInfoWindow()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 100),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = if (ContextCompat.checkSelfPermission(
                LocalContext.current, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            MapProperties(isMyLocationEnabled = true)
        } else {
            MapProperties()
        }
    ) {
        list.forEach { mapPin ->
            MarkerInfoWindow(
                state = MarkerState(position = mapPin.coordinate),
                icon = context.bitmapDescriptorFromVector(R.drawable.ico_pin),
                onInfoWindowClick = {
                    //onClickWorker(worker.id)
                }
            ) {
                if (it.isInfoWindowShown)
                    markerWithInfoWindow = it
//                                    WorkerWidget(
//                                        modifier = Modifier
//                                            .defaultMinSize(minWidth = 200.dp)
//                                            .background(Color.White, RoundedCornerShape(10)),
//                                        worker = worker,
//                                        showLocationUpdated = true
//                                    ) {}
            }
        }
    }
}