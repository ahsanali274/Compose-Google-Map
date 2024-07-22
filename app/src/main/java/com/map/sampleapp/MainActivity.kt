package com.map.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.map.sampleapp.ui.CustomMapView
import com.map.sampleapp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val list = listOf(
            MapPin("santiago", LatLng(-33.4489, -70.6693)),
            MapPin("bogota", LatLng(-4.7110, -74.0721)),
            MapPin("lima", LatLng(-12.0464, -77.0428)),
            MapPin("salvador", LatLng(-12.9777, -38.5016))
        )

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CustomMapView(Modifier.matchParentSize(), list)
                    }
                }
            }
        }
    }
}