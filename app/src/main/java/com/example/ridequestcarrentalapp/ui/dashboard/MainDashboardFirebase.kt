package com.example.ridequestcarrentalapp.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ridequestcarrentalapp.R
import com.example.ridequestcarrentalapp.data.models.Vehicle
import com.example.ridequestcarrentalapp.data.repository.VehicleRepository
import com.example.ridequestcarrentalapp.ui.theme.Helvetica
import com.example.ridequestcarrentalapp.ui.theme.Orange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardFirebase(
    vehicleRepository: VehicleRepository,
    onVehicleClick: (Vehicle) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var featuredVehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var brands by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    val categories = listOf("All", "Economy", "SUV", "Van", "Luxury", "Sedan")

    fun loadVehicles() {
        scope.launch {
            isLoading = true
            vehicles = vehicleRepository.searchVehicles(
                query = searchQuery,
                category = if (selectedCategory == "All") null else selectedCategory,
                brand = if (selectedBrand == "All Brands") null else selectedBrand
            )
            featuredVehicles = vehicleRepository.getFeaturedVehicles()
            brands = vehicleRepository.getBrands()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadVehicles() }

    LaunchedEffect(searchQuery, selectedCategory, selectedBrand) { loadVehicles() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Orange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, "Logo", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("RideQuest", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Orange, fontFamily = Helvetica)
                        Text("Find your perfect ride", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(44.dp).background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                    ) { Icon(Icons.Default.Notifications, "Notifications", tint = Color.Gray) }
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.size(44.dp).background(Orange, CircleShape)
                    ) { Icon(Icons.Default.Person, "Profile", tint = Color.White) }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search cars...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.Tune, "Filter", tint = Orange)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
            Spacer(Modifier.height(16.dp))
        }

        // Categories
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { category ->
                    val isSelected = (selectedCategory == category) || (selectedCategory == null && category == "All")
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) Orange else Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .clickable { selectedCategory = if (category == "All") null else category }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(category, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Filters (Brand selection when expanded)
        if (showFilters && brands.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filter by Brand", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(brands) { brand ->
                                FilterChip(
                                    selected = selectedBrand == brand,
                                    onClick = { selectedBrand = if (selectedBrand == brand) null else brand },
                                    label = { Text(brand, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Orange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Quick Stats
        if (searchQuery.isEmpty() && selectedCategory == null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickStatCard("Total Cars", "${vehicles.size}", Icons.Default.DirectionsCar, Orange)
                    QuickStatCard("Available", "${vehicles.count { it.status == "Available" }}", Icons.Default.CheckCircle, Color(0xFF4CAF50))
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Featured Cars
        if (featuredVehicles.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Cars", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica)
                }
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(featuredVehicles.take(5)) { vehicle ->
                        FeaturedVehicleCard(vehicle = vehicle, onClick = { onVehicleClick(vehicle) })
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Results Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${vehicles.size} cars available", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
        }

        // Loading / Empty / Results
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }
        } else if (vehicles.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("No cars found", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("Try adjusting your search or filters", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { searchQuery = ""; selectedCategory = null; selectedBrand = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) { Text("Reset Filters") }
                }
            }
        } else {
            // Vehicle Grid (2 columns)
            items(vehicles.chunked(2)) { rowVehicles ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowVehicles.forEach { vehicle ->
                        Box(modifier = Modifier.weight(1f)) {
                            VehicleCard(vehicle = vehicle, onClick = { onVehicleClick(vehicle) })
                        }
                    }
                    if (rowVehicles.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun QuickStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FeaturedVehicleCard(vehicle: Vehicle, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(240.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp)
                    .background(Orange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (vehicle.imageUrl.isNotEmpty()) {
                    AsyncImage(model = vehicle.imageUrl, contentDescription = vehicle.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit)
                } else {
                    Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(60.dp), tint = Orange)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("${vehicle.brand} ${vehicle.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(vehicle.category, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("₱${vehicle.pricePerDay.toInt()}/day", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Orange)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Text("${vehicle.rating}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Status badge
            if (vehicle.status != "Available") {
                Box(
                    modifier = Modifier.background(Color.Red, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) { Text(vehicle.status, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Medium) }
                Spacer(Modifier.height(4.dp))
            }

            // Image
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp)
                    .background(Orange.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (vehicle.imageUrl.isNotEmpty()) {
                    AsyncImage(model = vehicle.imageUrl, contentDescription = vehicle.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit)
                } else {
                    Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(50.dp), tint = Orange.copy(alpha = 0.5f))
                }
                // Rating badge
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                        Text("${vehicle.rating}", fontSize = 9.sp, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("${vehicle.brand} ${vehicle.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${vehicle.category} • ${vehicle.seats} seats", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("₱${vehicle.pricePerDay.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Orange)
                    Text("per day", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vehicle.status == "Available") Orange else Color.Gray
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = vehicle.status == "Available"
            ) { Text(if (vehicle.status == "Available") "View Details" else "Unavailable", fontSize = 12.sp) }
        }
    }
}