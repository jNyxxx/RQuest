package com.example.ridequestcarrentalapp.ui.booking

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridequestcarrentalapp.data.models.Booking
import com.example.ridequestcarrentalapp.data.models.Customer
import com.example.ridequestcarrentalapp.data.models.Location
import com.example.ridequestcarrentalapp.data.models.Vehicle
import com.example.ridequestcarrentalapp.ui.theme.Helvetica
import com.example.ridequestcarrentalapp.ui.theme.Orange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreenFirebase(
    vehicle: Vehicle,
    customer: Customer,
    locations: List<Location>,
    onBackClick: () -> Unit,
    onConfirmBooking: (Booking) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()

    var currentStep by remember { mutableIntStateOf(1) }
    var pickupDate by remember { mutableStateOf<Long?>(null) }
    var returnDate by remember { mutableStateOf<Long?>(null) }
    var pickupTime by remember { mutableStateOf("10:00 AM") }
    var returnTime by remember { mutableStateOf("10:00 AM") }
    var pickupLocation by remember { mutableStateOf(locations.firstOrNull()) }
    var returnLocation by remember { mutableStateOf(locations.firstOrNull()) }
    var addInsurance by remember { mutableStateOf(false) }
    var specialRequests by remember { mutableStateOf("") }

    val timeSlots = listOf("08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM")

    // Calculate pricing
    val totalDays = if (pickupDate != null && returnDate != null) {
        ((returnDate!! - pickupDate!!) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
    } else 1
    val subtotal = vehicle.pricePerDay * totalDays
    val insuranceCost = if (addInsurance) 500.0 * totalDays else 0.0
    val tax = (subtotal + insuranceCost) * 0.12
    val total = subtotal + insuranceCost + tax

    // Date Pickers
    val pickupDatePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d)
        pickupDate = calendar.timeInMillis
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
        datePicker.minDate = System.currentTimeMillis()
    }

    val returnDatePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d)
        returnDate = calendar.timeInMillis
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
        datePicker.minDate = pickupDate ?: System.currentTimeMillis()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 120.dp)) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentStep > 1) currentStep-- else onBackClick() },
                    modifier = Modifier.size(40.dp).background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                ) { Icon(Icons.Default.ArrowBack, "Back") }
                Text(
                    when (currentStep) { 1 -> "Select Dates"; 2 -> "Pickup & Return"; else -> "Review Booking" },
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica
                )
                Text("$currentStep/3", fontSize = 14.sp, color = Orange)
            }

            // Progress Bar
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(3) { step ->
                    Box(
                        modifier = Modifier.weight(1f).height(4.dp)
                            .background(if (step < currentStep) Orange else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    )
                    if (step < 2) Spacer(Modifier.width(8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Car Summary
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, null, tint = Orange, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("${vehicle.brand} ${vehicle.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("₱${vehicle.pricePerDay.toInt()}/day • ${vehicle.seats} seats", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Step Content
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                when (currentStep) {
                    1 -> {
                        // Date Selection
                        Text("Select Your Rental Dates", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { pickupDatePicker.show() },
                            colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = Orange)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("Pickup Date", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        if (pickupDate != null) dateFormat.format(Date(pickupDate!!)) else "Select pickup date",
                                        fontSize = 16.sp, color = if (pickupDate != null) Color.Black else Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (pickupDate != null) {
                                    returnDatePicker.datePicker.minDate = pickupDate!!
                                    returnDatePicker.show()
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, null, tint = Orange)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("Return Date", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        if (returnDate != null) dateFormat.format(Date(returnDate!!)) else "Select return date",
                                        fontSize = 16.sp, color = if (returnDate != null) Color.Black else Color.Gray
                                    )
                                }
                            }
                        }

                        if (pickupDate != null && returnDate != null) {
                            Spacer(Modifier.height(16.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.1f))) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Info, null, tint = Orange, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Total rental duration: $totalDays day${if (totalDays > 1) "s" else ""}", fontSize = 14.sp, color = Orange)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Location & Time Selection
                        Text("Pickup Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        // Pickup Location
                        var pickupExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = pickupExpanded, onExpandedChange = { pickupExpanded = it }) {
                            OutlinedTextField(
                                value = pickupLocation?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pickup Location") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pickupExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = pickupExpanded, onDismissRequest = { pickupExpanded = false }) {
                                locations.forEach { location ->
                                    DropdownMenuItem(
                                        text = { Text(location.name) },
                                        onClick = { pickupLocation = location; pickupExpanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Pickup Time
                        var pickupTimeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = pickupTimeExpanded, onExpandedChange = { pickupTimeExpanded = it }) {
                            OutlinedTextField(
                                value = pickupTime,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pickup Time") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pickupTimeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = pickupTimeExpanded, onDismissRequest = { pickupTimeExpanded = false }) {
                                timeSlots.forEach { time ->
                                    DropdownMenuItem(text = { Text(time) }, onClick = { pickupTime = time; pickupTimeExpanded = false })
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Return Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        // Return Location
                        var returnExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = returnExpanded, onExpandedChange = { returnExpanded = it }) {
                            OutlinedTextField(
                                value = returnLocation?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Return Location") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = returnExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = returnExpanded, onDismissRequest = { returnExpanded = false }) {
                                locations.forEach { location ->
                                    DropdownMenuItem(
                                        text = { Text(location.name) },
                                        onClick = { returnLocation = location; returnExpanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Return Time
                        var returnTimeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = returnTimeExpanded, onExpandedChange = { returnTimeExpanded = it }) {
                            OutlinedTextField(
                                value = returnTime,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Return Time") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = returnTimeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = returnTimeExpanded, onDismissRequest = { returnTimeExpanded = false }) {
                                timeSlots.forEach { time ->
                                    DropdownMenuItem(text = { Text(time) }, onClick = { returnTime = time; returnTimeExpanded = false })
                                }
                            }
                        }
                    }

                    3 -> {
                        // Review & Confirm
                        Text("Booking Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        Card(colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SummaryRow("Car", "${vehicle.brand} ${vehicle.name}")
                                SummaryRow("Pickup", "${dateFormat.format(Date(pickupDate!!))} at $pickupTime")
                                SummaryRow("Return", "${dateFormat.format(Date(returnDate!!))} at $returnTime")
                                SummaryRow("Pickup Location", pickupLocation?.name ?: "")
                                SummaryRow("Return Location", returnLocation?.name ?: "")
                                SummaryRow("Duration", "$totalDays day${if (totalDays > 1) "s" else ""}")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Insurance Option
                        Card(
                            modifier = Modifier.clickable { addInsurance = !addInsurance },
                            colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = addInsurance, onCheckedChange = { addInsurance = it }, colors = CheckboxDefaults.colors(checkedColor = Orange))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Add Insurance", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text("₱500/day - Full coverage", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Special Requests
                        OutlinedTextField(
                            value = specialRequests,
                            onValueChange = { specialRequests = it },
                            label = { Text("Special Requests (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(Modifier.height(16.dp))

                        // Price Breakdown
                        Card(colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.05f))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Price Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                PriceRow("Car Rental ($totalDays days)", subtotal)
                                if (addInsurance) PriceRow("Insurance", insuranceCost)
                                PriceRow("Tax (12%)", tax)
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("₱${String.format("%,.0f", total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Orange)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Button
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (currentStep == 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Amount", fontSize = 12.sp, color = Color.Gray)
                            Text("₱${String.format("%,.0f", total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Orange)
                        }
                        Button(
                            onClick = {
                                val booking = Booking(
                                    customerId = customer.id,
                                    customerName = customer.getFullName(),
                                    customerEmail = customer.email,
                                    customerPhone = customer.phone,
                                    vehicleId = vehicle.id,
                                    vehicleName = "${vehicle.brand} ${vehicle.name}",
                                    vehiclePlate = vehicle.plateNumber,
                                    vehicleImageUrl = vehicle.imageUrl,
                                    pickupDate = pickupDate!!,
                                    returnDate = returnDate!!,
                                    pickupTime = pickupTime,
                                    returnTime = returnTime,
                                    pickupLocationId = pickupLocation?.id ?: "",
                                    pickupLocationName = pickupLocation?.name ?: "",
                                    returnLocationId = returnLocation?.id ?: "",
                                    returnLocationName = returnLocation?.name ?: "",
                                    totalDays = totalDays,
                                    dailyRate = vehicle.pricePerDay,
                                    subtotal = subtotal,
                                    insuranceCost = insuranceCost,
                                    tax = tax,
                                    totalAmount = total,
                                    addInsurance = addInsurance,
                                    specialRequests = specialRequests,
                                    status = "Pending",
                                    paymentStatus = "Pending"
                                )
                                onConfirmBooking(booking)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Orange),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Payment, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Confirm Booking", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        enabled = when (currentStep) {
                            1 -> pickupDate != null && returnDate != null
                            2 -> pickupLocation != null && returnLocation != null
                            else -> true
                        }
                    ) {
                        Text(
                            when (currentStep) { 1 -> "Continue to Location & Time"; else -> "Review Booking" },
                            fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp)
    }
}

@Composable
fun PriceRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text("₱${String.format("%,.0f", amount)}", fontSize = 14.sp)
    }
}