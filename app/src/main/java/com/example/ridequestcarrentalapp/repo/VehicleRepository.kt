package com.example.ridequestcarrentalapp.data.repository

import com.example.ridequestcarrentalapp.data.models.Vehicle
import com.example.ridequestcarrentalapp.data.models.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class VehicleRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val vehiclesCollection = firestore.collection("vehicles")
    private val locationsCollection = firestore.collection("locations")

    // ========== VEHICLE OPERATIONS ==========

    suspend fun getAllVehicles(): List<Vehicle> = try {
        vehiclesCollection.orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject(Vehicle::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getAvailableVehicles(): List<Vehicle> = try {
        vehiclesCollection.whereEqualTo("status", "Available")
            .get().await()
            .documents.mapNotNull { it.toObject(Vehicle::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getVehicleById(id: String): Vehicle? = try {
        val doc = vehiclesCollection.document(id).get().await()
        doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
    } catch (e: Exception) {
        null
    }

    suspend fun searchVehicles(
        query: String = "",
        category: String? = null,
        brand: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        transmission: String? = null,
        fuelType: String? = null,
        minSeats: Int? = null
    ): List<Vehicle> = try {
        var vehicles = getAvailableVehicles()

        // Apply filters
        if (category != null && category.isNotEmpty()) {
            vehicles = vehicles.filter { it.category == category }
        }
        if (brand != null && brand.isNotEmpty() && brand != "All Brands") {
            vehicles = vehicles.filter { it.brand == brand }
        }
        if (minPrice != null) {
            vehicles = vehicles.filter { it.pricePerDay >= minPrice }
        }
        if (maxPrice != null) {
            vehicles = vehicles.filter { it.pricePerDay <= maxPrice }
        }
        if (transmission != null && transmission.isNotEmpty()) {
            vehicles = vehicles.filter { it.transmission == transmission }
        }
        if (fuelType != null && fuelType.isNotEmpty()) {
            vehicles = vehicles.filter { it.fuelType == fuelType }
        }
        if (minSeats != null) {
            vehicles = vehicles.filter { it.seats >= minSeats }
        }
        if (query.isNotEmpty()) {
            vehicles = vehicles.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.brand.contains(query, ignoreCase = true) ||
                        it.model.contains(query, ignoreCase = true)
            }
        }

        vehicles
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getFeaturedVehicles(): List<Vehicle> = try {
        vehiclesCollection
            .whereEqualTo("status", "Available")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(10)
            .get().await()
            .documents.mapNotNull { it.toObject(Vehicle::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        getAvailableVehicles().take(10)
    }

    suspend fun getBrands(): List<String> = try {
        val vehicles = getAllVehicles()
        listOf("All Brands") + vehicles.map { it.brand }.distinct().sorted()
    } catch (e: Exception) {
        listOf("All Brands")
    }

    fun observeVehicles(): Flow<List<Vehicle>> = callbackFlow {
        val listener = vehiclesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val vehicles = snapshot?.documents?.mapNotNull {
                    it.toObject(Vehicle::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(vehicles)
            }
        awaitClose { listener.remove() }
    }

    // ========== ADMIN OPERATIONS ==========

    suspend fun addVehicle(vehicle: Vehicle): Result<String> = try {
        val docRef = vehiclesCollection.add(vehicle).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> = try {
        vehiclesCollection.document(vehicle.id).set(
            vehicle.copy(updatedAt = System.currentTimeMillis())
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteVehicle(vehicleId: String): Result<Unit> = try {
        vehiclesCollection.document(vehicleId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateVehicleStatus(vehicleId: String, status: String): Result<Unit> = try {
        vehiclesCollection.document(vehicleId).update(
            mapOf(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ========== LOCATION OPERATIONS ==========

    suspend fun getAllLocations(): List<Location> = try {
        locationsCollection.whereEqualTo("isActive", true)
            .get().await()
            .documents.mapNotNull { it.toObject(Location::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        // Return default locations if none exist
        listOf(
            Location(id = "1", name = "Cebu City Center", address = "Osmena Boulevard, Cebu City"),
            Location(id = "2", name = "Mactan Airport", address = "Lapu-Lapu City"),
            Location(id = "3", name = "IT Park", address = "Lahug, Cebu City"),
            Location(id = "4", name = "SM City Cebu", address = "North Reclamation Area"),
            Location(id = "5", name = "Ayala Center Cebu", address = "Cebu Business Park")
        )
    }

    suspend fun addLocation(location: Location): Result<String> = try {
        val docRef = locationsCollection.add(location).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }
}