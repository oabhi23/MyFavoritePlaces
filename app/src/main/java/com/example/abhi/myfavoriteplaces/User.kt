package com.example.abhi.myfavoriteplaces

data class User (
        val list: MutableList<Place> = mutableListOf(),
        var uuid: String
)