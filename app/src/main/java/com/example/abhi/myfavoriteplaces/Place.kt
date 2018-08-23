package com.example.abhi.myfavoriteplaces

import com.google.android.gms.maps.model.LatLng

data class Place (
    val locationName: String = "",
    val latLng : LatLng = LatLng(0.0, 0.0)
)