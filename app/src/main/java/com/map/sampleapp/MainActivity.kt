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
            MapPin("The Empire State Building", LatLng(40.748817, -73.985428), 50.0),
            MapPin("central park", LatLng(40.785091, -73.968285), 500.0),
            MapPin("JFK Airport", LatLng(40.641766, -73.780968), 1000.0),
            MapPin("Statue of Liberty", LatLng(40.689247, -74.044502), 200.0)
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