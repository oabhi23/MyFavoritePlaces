package com.example.abhi.myfavoriteplaces

/**
 * User data class stores list of saved locations
 */
data class User (
        val list: MutableList<Place> = mutableListOf(),
        var uuid: String
)