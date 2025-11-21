package com.example.ridequestcarrentalapp.data.models

data class Customer(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val address: String = "",
    val licenseNumber: String = "",
    val profileImageUrl: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFullName() = "$firstName $lastName"
}