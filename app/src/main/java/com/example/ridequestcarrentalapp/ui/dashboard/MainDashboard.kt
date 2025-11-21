package com.example.ridequestcarrentalapp.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.ridequestcarrentalapp.R
import com.example.ridequestcarrentalapp.data.*
import com.example.ridequestcarrentalapp.ui.theme.Helvetica
import com.example.ridequestcarrentalapp.ui.theme.Orange
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues


// ViewModel for dashboard state management
class DashboardViewModel(private val repository: CarRepository = CarRepositoryFactory.create()) : androidx.lifecycle.ViewModel() {
    private val _uiState = mutableStateOf(DashboardUiState())
    val uiState: State<DashboardUiState> = _uiState

    private val _cars = mutableStateOf<List<Car>>(emptyList())
    val cars: State<List<Car>> = _cars

    private val _featuredCars = mutableStateOf<List<Car>>(emptyList())
    val featuredCars: State<List<Car>> = _featuredCars

    private val _brands = mutableStateOf<List<String>>(emptyList())
    val brands: State<List<String>> = _brands

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val all = repository.getAllCars()
            _cars.value = all
            _featuredCars.value = repository.getFeaturedCars()
            val computedBrands = listOf("All Brands") + all.map { it.brand }.distinct().sorted()
            _brands.value = computedBrands
        }
    }

    fun updateSearchCriteria(criteria: CarSearchCriteria) {
        _uiState.value = _uiState.value.copy(searchCriteria = criteria, isLoading = true)
        viewModelScope.launch {
            val filteredCars = repository.searchCars(criteria)
            _cars.value = filteredCars
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun toggleFilters() {
        _uiState.value = _uiState.value.copy(showFilters = !_uiState.value.showFilters)
    }

    fun resetFilters() {
        val defaultCriteria = CarSearchCriteria()
        updateSearchCriteria(defaultCriteria)
        _uiState.value = _uiState.value.copy(showFilters = false)
    }
}

data class DashboardUiState(
    val searchCriteria: CarSearchCriteria = CarSearchCriteria(),
    val isLoading: Boolean = false,
    val showFilters: Boolean = false,
    val showAdvancedFilters: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    onCarClick: (Car) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCategoryClick: (CarCategory) -> Unit = {},
    onBrandClick: (String) -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    val cars by viewModel.cars
    val featuredCars by viewModel.featuredCars
    val brands by viewModel.brands

    // Categories with enum support
    val categories = listOf(
        null to "All",
        CarCategory.ECONOMY to "Economy",
        CarCategory.SUV to "SUV",
        CarCategory.VAN_MPV to "Van/MPV",
        CarCategory.LUXURY to "Luxury",
        CarCategory.ELECTRIC_HYBRID to "EV"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Enhanced Top Bar
        item {
            TopBarSection(
                onProfileClick = onProfileClick,
                onNotificationClick = onNotificationClick
            )
        }

        // Enhanced Search Bar
        item {
            SearchBarSection(
                searchCriteria = uiState.searchCriteria,
                onSearchChange = { query ->
                    viewModel.updateSearchCriteria(
                        uiState.searchCriteria.copy(query = query)
                    )
                },
                onFilterToggle = {
                    viewModel.toggleFilters()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Categories Filter
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { (category, displayName) ->
                    CategoryChipEnhanced(
                        category = displayName,
                        isSelected = category == uiState.searchCriteria.category,
                        onClick = {
                            viewModel.updateSearchCriteria(
                                uiState.searchCriteria.copy(category = category)
                            )
                            category?.let { onCategoryClick(it) }
                        }
                    )
                }
            }
        }

        // Advanced Filters Section (collapsible)
        if (uiState.showFilters) {
            item {
                AdvancedFiltersSection(
                    searchCriteria = uiState.searchCriteria,
                    brands = brands,
                    onCriteriaChange = { viewModel.updateSearchCriteria(it) },
                    onReset = { viewModel.resetFilters() }
                )
            }
        }

        // Quick Stats Row
        if (uiState.searchCriteria.query.isEmpty() &&
            uiState.searchCriteria.category == null) {
            item {
                QuickStatsSection(
                    totalCars = cars.size,
                    availableCars = cars.count { it.isAvailable },
                    featuredCount = featuredCars.size
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Featured Cars Section (if no search/filter applied)
        if (uiState.searchCriteria.query.isEmpty() &&
            uiState.searchCriteria.category == null &&
            featuredCars.isNotEmpty()) {
            item {
                FeaturedCarsSection(
                    featuredCars = featuredCars,
                    onCarClick = onCarClick
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Loading state
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Orange)
                }
            }
        } else {
            // Results header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${cars.size} cars available",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        fontFamily = Helvetica
                    )
                    SortingChips(
                        currentSort = uiState.searchCriteria.sortBy,
                        onSortChange = { sortOption ->
                            viewModel.updateSearchCriteria(
                                uiState.searchCriteria.copy(sortBy = sortOption)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Cars Grid - Enhanced layout
            items(cars.chunked(2)) { rowCars ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCars.forEach { car ->
                        Box(modifier = Modifier.weight(1f)) {
                            EnhancedCarCard(
                                car = car,
                                onClick = { onCarClick(car) }
                            )
                        }
                    }
                    if (rowCars.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Empty state
            if (cars.isEmpty()) {
                item {
                    EmptyStateSection(
                        onResetFilters = { viewModel.resetFilters() }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    totalCars: Int,
    availableCars: Int,
    featuredCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(label = "Total Cars", value = totalCars.toString())
        StatItem(label = "Available", value = availableCars.toString())
        StatItem(label = "Featured", value = featuredCount.toString())
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyStateSection(
    onResetFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = "No results",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No cars found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Text(
            text = "Try adjusting your search or filters",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onResetFilters,
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Reset Filters",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainDashboardPreview() {
    MainDashboard()
}
