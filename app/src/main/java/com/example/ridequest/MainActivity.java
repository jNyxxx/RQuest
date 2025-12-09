package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CarRentalData db;
    private RecyclerView recyclerView;
    private VehicleAdapter adapter;
    private TextView tvWelcome;
    private int userId;

    private List<CarRentalData.VehicleItem> allVehicles;
    private List<CarRentalData.VehicleItem> filteredVehicles;

    private String currentFilter = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity onCreate");

        db = new CarRentalData(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvWelcome = findViewById(R.id.tvWelcome);
        ImageView ivProfile = findViewById(R.id.ivProfile);
        EditText etSearch = findViewById(R.id.etSearch);

        // Get user session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getInt("UID", -1);

        // Load and display user's name
        loadUserName();

        // recyclerview
        if(recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.setHasFixedSize(true);
            Log.d(TAG, "RecyclerView configured");
        } else {
            Log.e(TAG, "RecyclerView is null!");
            Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_SHORT).show();
        }

        // profile button
        if(ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }

        // search functionality
        if(etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    filterVehicles();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // filter Buttons
        TextView btnAll = findViewById(R.id.btnFilterAll);
        TextView btnSedan = findViewById(R.id.btnFilterSedan);
        TextView btnHatchback = findViewById(R.id.btnFilterHatchback);
        TextView btnSUV = findViewById(R.id.btnFilterSUV);

        if(btnAll != null) {
            btnAll.setOnClickListener(v -> {
                currentFilter = "All";
                updateFilterButtonStyles();
                filterVehicles();
            });
        }

        if(btnSedan != null) {
            btnSedan.setOnClickListener(v -> {
                currentFilter = "Sedan";
                updateFilterButtonStyles();
                filterVehicles();
            });
        }

        if(btnHatchback != null) {
            btnHatchback.setOnClickListener(v -> {
                currentFilter = "Hatchback";
                updateFilterButtonStyles();
                filterVehicles();
            });
        }

        if(btnSUV != null) {
            btnSUV.setOnClickListener(v -> {
                currentFilter = "SUV";
                updateFilterButtonStyles();
                filterVehicles();
            });
        }

        // loads all vehicles
        loadVehicles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Reloading vehicles and user name");
        loadUserName();
        loadVehicles();
    }

    private void loadUserName() {
        if (tvWelcome == null) {
            Log.e(TAG, "tvWelcome is null!");
            return;
        }

        if (userId == -1) {
            tvWelcome.setText("Welcome, Guest!");
            Log.d(TAG, "No user logged in - showing Guest");
            return;
        }

        try {
            // Fetch user from database
            CarRentalData.Customer customer = db.getCustomer(userId);

            if (customer != null && customer.firstName != null && !customer.firstName.isEmpty()) {
                String welcomeText = "Welcome, " + customer.firstName + "!";
                tvWelcome.setText(welcomeText);
                Log.d(TAG, "✓ Welcome message set for: " + customer.firstName + " (ID: " + userId + ")");
            } else {
                tvWelcome.setText("Welcome, User!");
                Log.e(TAG, "Could not load customer name for ID: " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user name: " + e.getMessage(), e);
            tvWelcome.setText("Welcome, User!");
        }
    }

    private void loadVehicles() {
        try {
            if(recyclerView == null) {
                Log.e(TAG, "Cannot load vehicles: RecyclerView is null");
                return;
            }

            // reload vehicles from database every time
            allVehicles = db.getAllVehicles();
            Log.d(TAG, "Loaded " + allVehicles.size() + " vehicles from database");

            if(allVehicles.isEmpty()) {
                Toast.makeText(this, "No vehicles available. Please add vehicles as admin.", Toast.LENGTH_LONG).show();
            }

            // filters
            filterVehicles();

        } catch (Exception e) {
            Log.e(TAG, "Error loading vehicles: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading vehicles: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void filterVehicles() {
        if(allVehicles == null) {
            Log.w(TAG, "allVehicles is null, cannot filter");
            return;
        }

        filteredVehicles = new ArrayList<>();

        for(CarRentalData.VehicleItem vehicle : allVehicles) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;

            // search query
            if(!currentSearchQuery.isEmpty()) {
                String query = currentSearchQuery.toLowerCase();
                String title = vehicle.title != null ? vehicle.title.toLowerCase() : "";
                String type = vehicle.type != null ? vehicle.type.toLowerCase() : "";

                matchesSearch = title.contains(query) || type.contains(query);
            }

            // category filter
            if(!currentFilter.equals("All")) {
                matchesCategory = vehicle.type != null &&
                        vehicle.type.equalsIgnoreCase(currentFilter);
            }

            // adds vehicle if it matches both filters
            if(matchesSearch && matchesCategory) {
                filteredVehicles.add(vehicle);
            }
        }

        Log.d(TAG, "Filtered: " + filteredVehicles.size() + " vehicles (Search: '" + currentSearchQuery + "', Category: '" + currentFilter + "')");

        // recyclerView with customer view
        adapter = new VehicleAdapter(this, filteredVehicles, false, null);
        recyclerView.setAdapter(adapter);

        // message if no results
        if(filteredVehicles.isEmpty()) {
            if(!currentSearchQuery.isEmpty() || !currentFilter.equals("All")) {
                Toast.makeText(this, "No vehicles found matching your search", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //  visual styles of filter buttons
    private void updateFilterButtonStyles() {
        TextView btnAll = findViewById(R.id.btnFilterAll);
        TextView btnSedan = findViewById(R.id.btnFilterSedan);
        TextView btnHatchback = findViewById(R.id.btnFilterHatchback);
        TextView btnSUV = findViewById(R.id.btnFilterSUV);

        // resets all to inactive style
        if(btnAll != null) setInactiveStyle(btnAll);
        if(btnSedan != null) setInactiveStyle(btnSedan);
        if(btnHatchback != null) setInactiveStyle(btnHatchback);
        if(btnSUV != null) setInactiveStyle(btnSUV);

        // style for selected filter
        switch(currentFilter) {
            case "All":
                if(btnAll != null) setActiveStyle(btnAll);
                break;
            case "Sedan":
                if(btnSedan != null) setActiveStyle(btnSedan);
                break;
            case "Hatchback":
                if(btnHatchback != null) setActiveStyle(btnHatchback);
                break;
            case "SUV":
                if(btnSUV != null) setActiveStyle(btnSUV);
                break;
        }
    }

    private void setActiveStyle(TextView button) {
        button.setBackgroundResource(R.drawable.bg_button_orange);
        button.setTextColor(getResources().getColor(R.color.white));
    }

    private void setInactiveStyle(TextView button) {
        button.setBackgroundResource(R.drawable.bg_input_field);
        button.setTextColor(getResources().getColor(R.color.black));
    }
}