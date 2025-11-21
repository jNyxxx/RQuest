package com.example.ridequestcarrentalapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridequestcarrentalapp.ui.theme.Orange
import com.example.ridequestcarrentalapp.ui.theme.RideQuestCarRentalAppTheme
import kotlinx.coroutines.launch

// Assuming these data classes and repositories exist elsewhere in your project
// data class Vehicle(...)
// data class Booking(...)
// class VehicleRepository(...)
// class BookingRepository(...)

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vehicleRepository = VehicleRepository()
        val bookingRepository = BookingRepository()

        setContent {
            RideQuestCarRentalAppTheme {
                AdminScreen(vehicleRepository, bookingRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(vehicleRepository: VehicleRepository, bookingRepository: BookingRepository) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Vehicles", "Bookings", "Users")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Orange, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Orange,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> AdminVehiclesTab(vehicleRepository)
                1 -> AdminBookingsTab(bookingRepository, vehicleRepository)
                2 -> AdminUsersTab()
            }
        }
    }
}


@Composable
fun AdminVehiclesTab(vehicleRepository: VehicleRepository) {
    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadVehicles() {
        scope.launch {
            isLoading = true
            vehicles = vehicleRepository.getAllVehicles()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadVehicles()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Orange) {
                Icon(Icons.Default.DirectionsCar, "Add Vehicle", tint = Color.White)
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (vehicles.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "No vehicles icon",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No vehicles yet", color = Color.Gray)
                                Text("Add your first vehicle to get started", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(vehicles) { vehicle ->
                        VehicleAdminCard(
                            vehicle = vehicle,
                            onEdit = { editingVehicle = vehicle },
                            onDelete = { vehicleToDelete ->
                                scope.launch {
                                    vehicleRepository.deleteVehicle(vehicleToDelete.id)
                                    Toast.makeText(context, "Vehicle deleted", Toast.LENGTH_SHORT).show()
                                    loadVehicles()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Add/Edit Vehicle Dialog
        if (showAddDialog || editingVehicle != null) {
            VehicleFormDialog(
                vehicle = editingVehicle,
                onDismiss = {
                    showAddDialog = false
                    editingVehicle = null
                },
                onSave = { vehicle ->
                    scope.launch {
                        val result = if (editingVehicle == null) {
                            vehicleRepository.addVehicle(vehicle)
                        } else {
                            vehicleRepository.updateVehicle(vehicle)
                        }

                        result.onSuccess {
                            Toast.makeText(context, "Vehicle saved successfully", Toast.LENGTH_SHORT).show()
                            loadVehicles()
                        }.onFailure {
                            Toast.makeText(context, "Error saving vehicle: ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                        showAddDialog = false
                        editingVehicle = null
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormDialog(vehicle: Vehicle?, onDismiss: () -> Unit, onSave: (Vehicle) -> Unit) {
    var brand by remember { mutableStateOf(vehicle?.brand ?: "") }
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var plateNumber by remember { mutableStateOf(vehicle?.plateNumber ?: "") }
    var category by remember { mutableStateOf(vehicle?.category ?: "Economy") }
    var transmission by remember { mutableStateOf(vehicle?.transmission ?: "Automatic") }
    var fuelType by remember { mutableStateOf(vehicle?.fuelType ?: "Gasoline") }
    var seats by remember { mutableStateOf(vehicle?.seats?.toString() ?: "5") }
    var pricePerDay by remember { mutableStateOf(vehicle?.pricePerDay?.toString() ?: "") }
    var color by remember { mutableStateOf(vehicle?.color ?: "") }
    var description by remember { mutableStateOf(vehicle?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle != null) "Edit Vehicle" else "Add New Vehicle") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Model Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = plateNumber, onValueChange = { plateNumber = it }, label = { Text("Plate Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                // Category Dropdown
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth())
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        listOf("Economy", "SUV", "Van", "Luxury", "Sedan").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { category = it; catExpanded = false })
                        }
                    }
                }

                // Transmission Dropdown
                var transExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = transExpanded, onExpandedChange = { transExpanded = it }) {
                    OutlinedTextField(value = transmission, onValueChange = {}, readOnly = true, label = { Text("Transmission") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = transExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth())
                    ExposedDropdownMenu(expanded = transExpanded, onDismissRequest = { transExpanded = false }) {
                        listOf("Automatic", "Manual").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { transmission = it; transExpanded = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = seats, onValueChange = { seats = it.filter { c -> c.isDigit() } }, label = { Text("Seats") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = pricePerDay, onValueChange = { pricePerDay = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Price/Day *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
                }

                OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newVehicle = Vehicle(
                        id = vehicle?.id ?: "",
                        name = name,
                        brand = brand,
                        model = "$brand $name",
                        plateNumber = plateNumber,
                        category = category,
                        transmission = transmission,
                        fuelType = fuelType,
                        seats = seats.toIntOrNull() ?: 5,
                        pricePerDay = pricePerDay.toDoubleOrNull() ?: 0.0,
                        pricePerHour = (pricePerDay.toDoubleOrNull() ?: 0.0) / 8,
                        color = color,
                        description = description,
                        status = vehicle?.status ?: "Available",
                        createdAt = vehicle?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(newVehicle)
                },
                enabled = name.isNotBlank() && brand.isNotBlank() && plateNumber.isNotBlank() && pricePerDay.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) { Text(if (vehicle != null) "Update" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AdminBookingsTab(bookingRepository: BookingRepository, vehicleRepository: VehicleRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var vehicles by remember { mutableStateOf<Map<String, Vehicle>>(emptyMap()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(true) }

    val filters = listOf("All", "Pending", "Confirmed", "Ongoing", "Completed", "Cancelled")

    fun loadData() {
        scope.launch {
            isLoading = true
            bookings = if (selectedFilter == "All") {
                bookingRepository.getAllBookings()
            } else {
                bookingRepository.getBookingsByStatus(selectedFilter)
            }
            // Only fetch vehicles if we haven't already or if they might change
            if (vehicles.isEmpty()) {
                vehicles = vehicleRepository.getAllVehicles().associateBy { it.id }
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedFilter) {
        loadData()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filters
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "$selectedFilter Bookings (${bookings.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (bookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    "No bookings icon",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No $selectedFilter bookings found", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(bookings) { booking ->
                        val vehicle = vehicles[booking.vehicleId]
                        BookingAdminCard(
                            booking = booking,
                            vehicle = vehicle,
                            onStatusChange = { newStatus ->
                                scope.launch {
                                    bookingRepository.updateBookingStatus(booking.id, newStatus)
                                        .onSuccess {
                                            Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show()
                                            loadData() // Refresh the list
                                        }
                                        .onFailure {
                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.People, "Users", modifier = Modifier.size(48.dp), tint = Color.Gray)
            Text("User management interface", color = Color.Gray)
        }
    }
}


// Dummy Composables for compilation. Replace with your actual implementation.
@Composable
fun VehicleAdminCard(vehicle: Vehicle, onEdit: () -> Unit, onDelete: (Vehicle) -> Unit) {}

@Composable
fun BookingAdminCard(booking: Booking, vehicle: Vehicle?, onStatusChange: (String) -> Unit) {}

// Dummy data classes for compilation.
data class Vehicle(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val plateNumber: String = "",
    val category: String = "",
    val transmission: String = "",
    val fuelType: String = "",
    val seats: Int = 0,
    val pricePerDay: Double = 0.0,
    val pricePerHour: Double = 0.0,
    val color: String = "",
    val description: String = "",
    val status: String = "Available",
    val createdAt: Long = 0L
)

data class Booking(
    val id: String = "",
    val vehicleId: String = "",
    val status: String = ""
)

// Dummy Repositories for compilation.
class VehicleRepository {
    suspend fun getAllVehicles(): List<Vehicle> = emptyList()
    suspend fun addVehicle(vehicle: Vehicle): Result<Unit> = Result.success(Unit)
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> = Result.success(Unit)
    suspend fun deleteVehicle(id: String) {}
}

class BookingRepository {
    suspend fun getAllBookings(): List<Booking> = emptyList()
    suspend fun getBookingsByStatus(status: String): List<Booking> = emptyList()
    suspend fun updateBookingStatus(id: String, newStatus: String): Result<Unit> = Result.success(Unit)
}
