package com.example.ridequestcarrentalapp.data.models

data class Vehicle(
    val id: String = "",
    val name: String = "",           // e.g., "Vios"
    val brand: String = "",          // e.g., "Toyota"
    val model: String = "",          // e.g., "Vios 1.3 J MT"
    val year: Int = 0,
    val plateNumber: String = "",
    val vin: String = "",
    val category: String = "",       // "Economy", "SUV", "Van", "Luxury", "Sedan"
    val transmission: String = "",   // "Automatic", "Manual"
    val fuelType: String = "",       // "Gasoline", "Diesel", "Electric", "Hybrid"
    val seats: Int = 5,
    val color: String = "",
    val pricePerDay: Double = 0.0,
    val pricePerHour: Double = 0.0,
    val status: String = "Available", // "Available", "Rented", "Maintenance"
    val currentMileage: Double = 0.0,
    val locationId: String = "",
    val locationName: String = "",
    val imageUrl: String = "",
    val features: List<String> = emptyList(),
    val description: String = "",
    val rating: Float = 0f,
    val totalRentals: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)