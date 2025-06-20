package com.example.bloodlink.model


data class Place(
    val name: String,
    val geometry: Geometry,
    var distanceFromUser: String = ""
)

data class Geometry(
    val location: LocationLatLng
)

data class LocationLatLng(
    val lat: Double,
    val lng: Double
)