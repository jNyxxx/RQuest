package com.example.ridequestcarrentalapp.ui.detail

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ridequestcarrentalapp.data.models.Vehicle
import com.example.ridequestcarrentalapp.ui.theme.Helvetica
import com.example.ridequestcarrentalapp.ui.theme.Orange

@Composable
fun CarDetailScreenFirebase(
    vehicle: Vehicle,
    onBackClick: () -> Unit,
    onBookNowClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp).background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                ) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black) }
                Text("Car Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica)
                Spacer(Modifier.size(40.dp))
            }

            // Car Image
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (vehicle.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = vehicle.imageUrl,
                            contentDescription = vehicle.name,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(100.dp), tint = Orange)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Title and Price
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${vehicle.brand} ${vehicle.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica)
                        Text(vehicle.category, fontSize = 14.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Text(" ${vehicle.rating} • ${vehicle.totalRentals} rentals", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₱${vehicle.pricePerDay.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Orange)
                        Text("per day", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Car Features
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Car Specifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica)
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FeatureItem(Icons.Default.Person, "Seats", "${vehicle.seats}")
                    FeatureItem(Icons.Default.Settings, "Trans", vehicle.transmission.take(4))
                    FeatureItem(Icons.Default.LocalGasStation, "Fuel", vehicle.fuelType.take(3))
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FeatureItem(Icons.Default.Palette, "Color", vehicle.color.ifEmpty { "N/A" })
                    FeatureItem(Icons.Default.ConfirmationNumber, "Plate", vehicle.plateNumber.take(8))
                    FeatureItem(Icons.Default.Speed, "Status", vehicle.status.take(5))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Description
            if (vehicle.description.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica)
                    Spacer(Modifier.height(8.dp))
                    Text(vehicle.description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(24.dp))
            }

            // Features List
            if (vehicle.features.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Features", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Helvetica)
                    Spacer(Modifier.height(12.dp))
                    vehicle.features.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { feature ->
                                Row(
                                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Orange, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(feature, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                            if (row.size < 2) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Book Now Button
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Orange)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(enabled = vehicle.status == "Available") { onBookNowClick() }.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (vehicle.status == "Available") "Book Now" else "Not Available",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(Orange.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, title, tint = Orange, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}