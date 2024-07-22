package com.map.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.map.sampleapp.ui.CustomMapView
import com.map.sampleapp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val list = listOf(
            MapPin("santiago", LatLng(-33.4489, -70.6693)),
            MapPin("bogota", LatLng(-4.7110, -74.0721)),
            MapPin("lima", LatLng(4-12.0464, -77.0428)),
            MapPin("salvador", LatLng(-12.9777, -38.5016))
        )

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Color.White)
                    ) {
                        CustomMapView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            list
                        )
                    }
                }
            }
        }
    }
}