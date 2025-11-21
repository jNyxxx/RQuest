package com.example.ridequestcarrentalapp.data.repository

import com.example.ridequestcarrentalapp.data.models.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")

    // ========== CUSTOMER OPERATIONS ==========

    suspend fun createBooking(booking: Booking): Result<String> = try {
        val docRef = bookingsCollection.add(booking).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getBookingById(bookingId: String): Booking? = try {
        val doc = bookingsCollection.document(bookingId).get().await()
        doc.toObject(Booking::class.java)?.copy(id = doc.id)
    } catch (e: Exception) {
        null
    }

    suspend fun getCustomerBookings(customerId: String): List<Booking> = try {
        bookingsCollection
            .whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject(Booking::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    fun observeCustomerBookings(customerId: String): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull {
                    it.toObject(Booking::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> = try {
        bookingsCollection.document(bookingId).update(
            mapOf(
                "status" to "Cancelled",
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ========== ADMIN OPERATIONS ==========

    suspend fun getAllBookings(): List<Booking> = try {
        bookingsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject(Booking::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getBookingsByStatus(status: String): List<Booking> = try {
        bookingsCollection
            .whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject(Booking::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getPendingBookings(): List<Booking> = getBookingsByStatus("Pending")

    fun observeAllBookings(): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull {
                    it.toObject(Booking::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = try {
        bookingsCollection.document(bookingId).update(
            mapOf(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun confirmBooking(bookingId: String): Result<Unit> =
        updateBookingStatus(bookingId, "Confirmed")

    suspend fun startRental(bookingId: String): Result<Unit> =
        updateBookingStatus(bookingId, "Ongoing")

    suspend fun completeBooking(bookingId: String): Result<Unit> =
        updateBookingStatus(bookingId, "Completed")

    suspend fun updatePaymentStatus(bookingId: String, paymentStatus: String): Result<Unit> = try {
        bookingsCollection.document(bookingId).update(
            mapOf(
                "paymentStatus" to paymentStatus,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addAdminNotes(bookingId: String, notes: String): Result<Unit> = try {
        bookingsCollection.document(bookingId).update(
            mapOf(
                "adminNotes" to notes,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ========== STATISTICS ==========

    suspend fun getBookingStats(): BookingStats {
        val allBookings = getAllBookings()
        return BookingStats(
            total = allBookings.size,
            pending = allBookings.count { it.status == "Pending" },
            confirmed = allBookings.count { it.status == "Confirmed" },
            ongoing = allBookings.count { it.status == "Ongoing" },
            completed = allBookings.count { it.status == "Completed" },
            cancelled = allBookings.count { it.status == "Cancelled" },
            totalRevenue = allBookings
                .filter { it.paymentStatus == "Paid" }
                .sumOf { it.totalAmount }
        )
    }
}

data class BookingStats(
    val total: Int = 0,
    val pending: Int = 0,
    val confirmed: Int = 0,
    val ongoing: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0,
    val totalRevenue: Double = 0.0
)