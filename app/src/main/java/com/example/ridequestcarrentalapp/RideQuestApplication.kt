package com.example.ridequestcarrentalapp

import android.app.Application
import com.example.ridequestcarrentalapp.data.repository.AuthRepository
import com.example.ridequestcarrentalapp.data.repository.BookingRepository
import com.example.ridequestcarrentalapp.data.repository.VehicleRepository
import com.google.firebase.FirebaseApp

class RideQuestApplication : Application() {

    // Singleton repositories
    lateinit var authRepository: AuthRepository
        private set

    lateinit var vehicleRepository: VehicleRepository
        private set

    lateinit var bookingRepository: BookingRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize repositories
        authRepository = AuthRepository()
        vehicleRepository = VehicleRepository()
        bookingRepository = BookingRepository()
    }

    companion object {
        private lateinit var instance: RideQuestApplication

        fun getInstance(): RideQuestApplication = instance
    }

    init {
        instance = this
    }
}