package com.map.sampleapp

import com.google.android.gms.maps.model.LatLng

data class MapPin(
    val name: String,
    val coordinate: LatLng
)