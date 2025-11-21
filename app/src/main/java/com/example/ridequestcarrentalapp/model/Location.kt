package com.example.ridequestcarrentalapp.data.models

data class Location(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val contactNumber: String = "",
    val operatingHours: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)