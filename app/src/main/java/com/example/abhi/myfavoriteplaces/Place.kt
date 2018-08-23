package com.example.abhi.myfavoriteplaces

/**
 * Place of saved location stores name, latitude, and longitude values of a place
 */
data class Place (
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0

)