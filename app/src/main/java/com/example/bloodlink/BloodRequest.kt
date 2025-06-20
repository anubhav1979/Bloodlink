package com.example.bloodlink
data class BloodRequest(
    val name: String = "",
    val type: String = "",
    val bloodGroup: String = "",
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)