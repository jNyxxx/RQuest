package com.example.ridequestcarrentalapp.data.repository

import com.example.ridequestcarrentalapp.data.models.Customer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val customersCollection = firestore.collection("customers")

    // Admin credentials - hardcoded for simplicity
    companion object {
        const val ADMIN_EMAIL = "admin@ridequest.com"
        const val ADMIN_PASSWORD = "admin123"
    }

    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<Customer> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

        val customer = Customer(
            id = userId,
            email = email,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            isAdmin = false,
            createdAt = System.currentTimeMillis()
        )

        customersCollection.document(userId).set(customer).await()
        Result.success(customer)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signIn(email: String, password: String): Result<Customer> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to sign in")

        val doc = customersCollection.document(userId).get().await()
        val customer = doc.toObject(Customer::class.java)?.copy(id = doc.id)
            ?: throw Exception("Customer not found")

        Result.success(customer)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signInAdmin(email: String, password: String): Result<Customer> = try {
        // Check if it's the admin
        if (email != ADMIN_EMAIL) {
            throw Exception("Not an admin account")
        }

        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to sign in")

        // Check or create admin document
        val doc = customersCollection.document(userId).get().await()
        val customer = if (doc.exists()) {
            doc.toObject(Customer::class.java)?.copy(id = doc.id, isAdmin = true)
                ?: throw Exception("Admin not found")
        } else {
            // Create admin if doesn't exist
            val admin = Customer(
                id = userId,
                email = email,
                firstName = "Admin",
                lastName = "RideQuest",
                isAdmin = true
            )
            customersCollection.document(userId).set(admin).await()
            admin
        }

        if (!customer.isAdmin) {
            throw Exception("Not an admin account")
        }

        Result.success(customer)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun getCurrentCustomer(): Customer? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val doc = customersCollection.document(userId).get().await()
            doc.toObject(Customer::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
}