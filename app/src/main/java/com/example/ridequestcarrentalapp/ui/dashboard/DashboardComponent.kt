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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridequestcarrentalapp.R
import com.example.ridequestcarrentalapp.data.*
import com.example.ridequestcarrentalapp.ui.theme.Helvetica
import com.example.ridequestcarrentalapp.ui.theme.Orange

@Composable
fun TopBarSection(
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadNotificationCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // RideQuest Logo and Title with better styling
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Orange, Orange.copy(alpha = 0.8f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar, // Fallback to default icon
                    contentDescription = "RideQuest Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "RideQuest",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange,
                    fontFamily = Helvetica
                )
                Text(
                    text = "Find your perfect ride",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = Helvetica
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Enhanced notification icon with badge
            Box {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Gray
                    )
                }
                // Animated notification badge
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadNotificationCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Enhanced profile button
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Orange, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSection(
    searchCriteria: CarSearchCriteria,
    onSearchChange: (String) -> Unit,
    onFilterToggle: () -> Unit
) {
    OutlinedTextField(
        value = searchCriteria.query,
        onValueChange = onSearchChange,
        placeholder = {
            Text(
                "Search cars, brands, or features...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        trailingIcon = {
            Row {
                IconButton(onClick = onFilterToggle) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Orange
                    )
                }
                IconButton(onClick = { /* Voice search */ }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = Color.Gray
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f),
            focusedContainerColor = Color.Gray.copy(alpha = 0.05f)
        )
    )
}

@Composable
fun CategoryChipEnhanced(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Orange else Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = category,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = Helvetica
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFiltersSection(
    searchCriteria: CarSearchCriteria,
    brands: List<String>,
    onCriteriaChange: (CarSearchCriteria) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Price Range Filter
            Text(
                text = "Price Range (per day)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            RangeSlider(
                value = searchCriteria.minPrice.toFloat()..searchCriteria.maxPrice.toFloat(),
                onValueChange = { range ->
                    onCriteriaChange(
                        searchCriteria.copy(
                            minPrice = range.start.toDouble(),
                            maxPrice = range.endInclusive.toDouble()
                        )
                    )
                },
                valueRange = 1000f..15000f,
                colors = SliderDefaults.colors(
                    thumbColor = Orange,
                    activeTrackColor = Orange
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Filter
            Text(
                text = "Brand",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(brands) { brand ->
                    BrandChip(
                        brand = brand,
                        isSelected = brand == searchCriteria.brand,
                        onClick = {
                            onCriteriaChange(searchCriteria.copy(brand = brand))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fuel Type Filter
            Text(
                text = "Fuel Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FuelType.values().toList()) { fuel ->
                    FilterChip(
                        onClick = {
                            val newValue = if (searchCriteria.fuelType == fuel) null else fuel
                            onCriteriaChange(searchCriteria.copy(fuelType = newValue))
                        },
                        label = { Text(fuel.displayName, fontSize = 12.sp) },
                        selected = searchCriteria.fuelType == fuel,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transmission Filter
            Text(
                text = "Transmission",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Transmission.values().toList()) { tx ->
                    FilterChip(
                        onClick = {
                            val newValue = if (searchCriteria.transmission == tx) null else tx
                            onCriteriaChange(searchCriteria.copy(transmission = newValue))
                        },
                        label = { Text(tx.displayName, fontSize = 12.sp) },
                        selected = searchCriteria.transmission == tx,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seats Filter (sets minimum seats)
            Text(
                text = "Seats",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            val seatOptions = listOf(2, 4, 5, 7, 8)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(seatOptions) { seat ->
                    FilterChip(
                        onClick = {
                            val newMin = if (searchCriteria.minSeats == seat) 1 else seat
                            onCriteriaChange(searchCriteria.copy(minSeats = newMin))
                        },
                        label = { Text("$seat+", fontSize = 12.sp) },
                        selected = searchCriteria.minSeats == seat,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset button
            TextButton(
                onClick = onReset,
                colors = ButtonDefaults.textButtonColors(contentColor = Orange)
            ) {
                Text(
                    text = "Reset Filters",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

//@Composable
//fun QuickStatsSection(
//    totalCars: Int,
//    availableCars: Int,
//    featuredCount: Int
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        QuickStatCard(
//            title = "Total Cars",
//            value = totalCars.toString(),
//            icon = Icons.Default.DirectionsCar,
//            color = Orange
//        )
//        QuickStatCard(
//            title = "Available",
//            value = availableCars.toString(),
//            icon = Icons.Default.CheckCircle,
//            color = Color(0xFF4CAF50)
//        )
//        QuickStatCard(
//            title = "Featured",
//            value = featuredCount.toString(),
//            icon = Icons.Default.Star,
//            color = Color(0xFFFF9800)
//        )
//    }
//}
//
//@Composable
//private fun QuickStatCard(
//    title: String,
//    value: String,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    color: Color
//) {
//    Card(
//        modifier = Modifier.width(100.dp),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = title,
//                tint = color,
//                modifier = Modifier.size(24.dp)
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = value,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = color
//            )
//            Text(
//                text = title,
//                fontSize = 10.sp,
//                color = Color.Gray,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}

@Composable
fun FeaturedCarsSection(
    featuredCars: List<Car>,
    onCarClick: (Car) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured Cars",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = Helvetica
            )
            TextButton(onClick = { /* See all */ }) {
                Text(
                    text = "See All",
                    color = Orange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(featuredCars) { car ->
                FeaturedCarCard(
                    car = car,
                    onClick = { onCarClick(car) }
                )
            }
        }
    }
}

@Composable
fun FeaturedCarCard(
    car: Car,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Orange.copy(alpha = 0.1f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = car.imageRes),
                    contentDescription = car.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${car.brand} ${car.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = car.category.displayName,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₱${car.pricePerDay.toInt()}/day",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = car.rating.toString(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SortingChips(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            listOf(
                SortOption.PRICE_LOW_TO_HIGH to "Price ↑",
                SortOption.RATING_HIGH_TO_LOW to "Rating ↓",
                SortOption.NEWEST_FIRST to "Newest"
            )
        ) { (option, label) ->
            FilterChip(
                onClick = { onSortChange(option) },
                label = { Text(label, fontSize = 12.sp) },
                selected = currentSort == option,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Orange,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun EnhancedCarCard(
    car: Car,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Availability indicator
            if (!car.isAvailable) {
                Box(
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Unavailable",
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Car Image with enhanced styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(
                                Orange.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = car.imageRes),
                    contentDescription = car.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(65.dp)
                )

                // Rating badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = car.rating.toString(),
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Car Name and Brand
            Text(
                text = "${car.brand} ${car.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = Helvetica,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = car.category.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = Helvetica
                )

                if (car.category == CarCategory.LUXURY) {
                    Box(
                        modifier = Modifier
                            .background(Orange, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Premium",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Helvetica
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enhanced car specs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CarSpecItem(
                    icon = Icons.Default.Person,
                    text = "${car.seats}"
                )
                CarSpecItem(
                    icon = Icons.Default.LocalGasStation,
                    text = car.fuelType.displayName.take(3)
                )
                CarSpecItem(
                    icon = Icons.Default.Settings,
                    text = if (car.transmission == Transmission.AUTOMATIC) "A" else "M"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced price section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "₱${car.pricePerDay.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange,
                        fontFamily = Helvetica
                    )
                    Text(
                        text = "per day",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = Helvetica
                    )
                }

                Text(
                    text = "₱${car.pricePerHour.toInt()}/hr",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = Helvetica
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced action button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (car.isAvailable) Orange else Color.Gray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = car.isAvailable
            ) {
                Text(
                    text = if (car.isAvailable) "View Details" else "Unavailable",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Helvetica
                )
            }
        }
    }
}

@Composable
private fun CarSpecItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Color.Gray
        )
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.Gray,
            fontFamily = Helvetica
        )
    }
}

@Composable
fun BrandChip(
    brand: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Orange.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = brand,
            color = if (isSelected) Orange else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontFamily = Helvetica
        )
    }
}


// Enhanced MainDashboard with FABs and bottom navigation space
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMainDashboard(
    onCarClick: (Car) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCategoryClick: (CarCategory) -> Unit = {},
    onBrandClick: (String) -> Unit = {},
    onQuickBookClick: () -> Unit = {},
    onMapViewClick: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Main dashboard content
        MainDashboard(
            onCarClick = onCarClick,
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick,
            onCategoryClick = onCategoryClick,
            onBrandClick = onBrandClick,
            viewModel = viewModel
        )

    }
}