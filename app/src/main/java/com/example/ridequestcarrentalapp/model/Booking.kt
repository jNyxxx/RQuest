package com.example.ridequestcarrentalapp.data.models

data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val vehicleId: String = "",
    val vehicleName: String = "",
    val vehiclePlate: String = "",
    val vehicleImageUrl: String = "",
    val pickupDate: Long = 0L,
    val returnDate: Long = 0L,
    val pickupTime: String = "",
    val returnTime: String = "",
    val pickupLocationId: String = "",
    val pickupLocationName: String = "",
    val returnLocationId: String = "",
    val returnLocationName: String = "",
    val totalDays: Int = 0,
    val dailyRate: Double = 0.0,
    val subtotal: Double = 0.0,
    val insuranceCost: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "Pending", // "Pending", "Confirmed", "Ongoing", "Completed", "Cancelled"
    val paymentStatus: String = "Pending", // "Pending", "Paid", "Failed", "Refunded"
    val paymentMethod: String = "",
    val addInsurance: Boolean = false,
    val specialRequests: String = "",
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)