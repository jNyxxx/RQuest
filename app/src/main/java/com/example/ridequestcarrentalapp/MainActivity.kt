package com.example.ridequestcarrentalapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ridequestcarrentalapp.data.models.Booking
import com.example.ridequestcarrentalapp.data.models.Customer
import com.example.ridequestcarrentalapp.data.models.Vehicle
import com.example.ridequestcarrentalapp.data.repository.AuthRepository
import com.example.ridequestcarrentalapp.data.repository.BookingRepository
import com.example.ridequestcarrentalapp.data.repository.VehicleRepository
import com.example.ridequestcarrentalapp.ui.booking.BookingFlowScreenFirebase
import com.example.ridequestcarrentalapp.ui.dashboard.MainDashboardFirebase
import com.example.ridequestcarrentalapp.ui.detail.CarDetailScreenFirebase
import com.example.ridequestcarrentalapp.ui.theme.Orange
import com.example.ridequestcarrentalapp.ui.theme.RideQuestCarRentalAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var bookingRepository: BookingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as RideQuestApplication
        authRepository = app.authRepository
        vehicleRepository = app.vehicleRepository
        bookingRepository = app.bookingRepository

        // Check if logged in
        if (!authRepository.isLoggedIn()) {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            RideQuestCarRentalAppTheme {
                MainActivityContent(
                    authRepository = authRepository,
                    vehicleRepository = vehicleRepository,
                    bookingRepository = bookingRepository,
                    onLogout = {
                        authRepository.signOut()
                        startActivity(Intent(this, SecondActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun MainActivityContent(
    authRepository: AuthRepository,
    vehicleRepository: VehicleRepository,
    bookingRepository: BookingRepository,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentCustomer by remember { mutableStateOf<Customer?>(null) }
    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load initial data
    LaunchedEffect(Unit) {
        currentCustomer = authRepository.getCurrentCustomer()
        vehicles = vehicleRepository.getAvailableVehicles()
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange)
        }
        return
    }

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            MainDashboardFirebase(
                vehicleRepository = vehicleRepository,
                onVehicleClick = { vehicle ->
                    navController.navigate("vehicle_detail/${vehicle.id}")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onNotificationClick = {
                    Toast.makeText(context, "Notifications coming soon", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable("vehicle_detail/{vehicleId}") { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            var vehicle by remember { mutableStateOf<Vehicle?>(null) }
            var loadingVehicle by remember { mutableStateOf(true) }

            LaunchedEffect(vehicleId) {
                vehicle = vehicleRepository.getVehicleById(vehicleId)
                loadingVehicle = false
            }

            if (loadingVehicle) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            } else if (vehicle != null) {
                CarDetailScreenFirebase(
                    vehicle = vehicle!!,
                    onBackClick = { navController.popBackStack() },
                    onBookNowClick = {
                        navController.navigate("booking/${vehicle!!.id}")
                    }
                )
            }
        }

        composable("booking/{vehicleId}") { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            var vehicle by remember { mutableStateOf<Vehicle?>(null) }
            var locations by remember { mutableStateOf<List<com.example.ridequestcarrentalapp.data.models.Location>>(emptyList()) }
            var loadingData by remember { mutableStateOf(true) }

            LaunchedEffect(vehicleId) {
                vehicle = vehicleRepository.getVehicleById(vehicleId)
                locations = vehicleRepository.getAllLocations()
                loadingData = false
            }

            if (loadingData) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            } else if (vehicle != null && currentCustomer != null) {
                BookingFlowScreenFirebase(
                    vehicle = vehicle!!,
                    customer = currentCustomer!!,
                    locations = locations,
                    onBackClick = { navController.popBackStack() },
                    onConfirmBooking = { booking ->
                        scope.launch {
                            bookingRepository.createBooking(booking)
                                .onSuccess {
                                    Toast.makeText(context, "Booking submitted! Waiting for admin confirmation.", Toast.LENGTH_LONG).show()
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                                .onFailure {
                                    Toast.makeText(context, "Booking failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                )
            }
        }

        composable("profile") {
            // Simple profile screen placeholder
            ProfileScreenSimple(
                customer = currentCustomer,
                bookingRepository = bookingRepository,
                onBackClick = { navController.popBackStack() },
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun ProfileScreenSimple(
    customer: Customer?,
    bookingRepository: BookingRepository,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }

    LaunchedEffect(customer) {
        customer?.let {
            bookings = bookingRepository.getCustomerBookings(it.id)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Name: ${customer?.getFullName() ?: "Guest"}")
        Text("Email: ${customer?.email ?: ""}")
        Text("Phone: ${customer?.phone ?: ""}")
        Spacer(modifier = Modifier.height(24.dp))
        Text("My Bookings: ${bookings.size}")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
            Text("Logout")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}