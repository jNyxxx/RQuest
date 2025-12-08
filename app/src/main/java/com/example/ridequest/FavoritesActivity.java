package com.example.ridequest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmptyState;
    private CarRentalData carRentalData;
    private int currentUserId;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // 1. Initialize DB and Views
        carRentalData = new CarRentalData(this);
        rvFavorites = findViewById(R.id.rvFavorites);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnBack = findViewById(R.id.btnBack);

        // 2. Get User ID
        currentUserId = getSessionUserId();

        // 3. Setup RecyclerView
        if (rvFavorites != null) {
            rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        }

        // 4. Back Button Action
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 5. Load Data
        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (currentUserId == -1) return;

        // Fetch Favorites from Database
        List<CarRentalData.VehicleItem> favoriteCars = carRentalData.getCustomerFavorites(currentUserId);

        if (favoriteCars == null || favoriteCars.isEmpty()) {
            // Show Empty State
            if (rvFavorites != null) rvFavorites.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            // Show List
            if (rvFavorites != null) rvFavorites.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);

            // Use VehicleAdapter (isAdmin = false, Listener = null)
            VehicleAdapter adapter = new VehicleAdapter(this, favoriteCars, false, null);
            if (rvFavorites != null) rvFavorites.setAdapter(adapter);
        }
    }

    private int getSessionUserId() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        // FIX: Changed "CUSTOMER_ID" to "UID" to match your Login/Profile logic
        return prefs.getInt("UID", -1);
    }
}