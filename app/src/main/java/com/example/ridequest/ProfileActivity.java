package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_UID = "UID";

    private CarRentalData db;
    private SharedPreferences prefs;
    private int uid;

    // UI Elements
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvFavCount;
    private TextView tvBookingCount;
    private ImageView ivProfileAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize SharedPreferences and Database
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        db = new CarRentalData(this);
        uid = prefs.getInt(KEY_UID, -1);

        // Validate user session
        if (uid == -1) {
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        initializeViews();

        // Load profile data
        loadProfileData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvFavCount = findViewById(R.id.tvFavCount);
        tvBookingCount = findViewById(R.id.tvBookingCount);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
    }

    private void setupClickListeners() {
        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Edit Profile
        MaterialCardView cardEditProfile = findViewById(R.id.cardEditProfile);
        if (cardEditProfile != null) {
            cardEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // Favorite Cars
        LinearLayout btnFavoriteCars = findViewById(R.id.btnFavoriteCars);
        if (btnFavoriteCars != null) {
            btnFavoriteCars.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
            });
        }

        // My Bookings
        LinearLayout btnMyBookings = findViewById(R.id.btnMyBookings);
        if (btnMyBookings != null) {
            btnMyBookings.setOnClickListener(v -> {
                Intent intent = new Intent(this, MyBookingsActivity.class);
                startActivity(intent);
            });
        }

        // Settings
        LinearLayout btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                // TODO: Create SettingsActivity
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Help & Support
        LinearLayout btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                // TODO: Create HelpActivity
                Toast.makeText(this, "Help & Support coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // About
        LinearLayout btnAbout = findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                // TODO: Create AboutActivity
                Toast.makeText(this, "About RideQuest v1.0.0", Toast.LENGTH_SHORT).show();
            });
        }

        // Logout
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }
    }

    private void loadProfileData() {
        try {
            CarRentalData.Customer customer = db.getCustomer(uid);

            if (customer != null) {
                // Set name
                if (tvName != null) {
                    String fullName = customer.firstName + " " + customer.lastName;
                    tvName.setText(fullName);
                }

                // Set email
                if (tvEmail != null) {
                    tvEmail.setText(customer.email);
                }

                // Set default text for favorites and bookings
                if (tvFavCount != null) {
                    tvFavCount.setText("View your saved vehicles");
                }

                if (tvBookingCount != null) {
                    tvBookingCount.setText("View your rental history");
                }

                // TODO: Load profile avatar if you have image storage
                // loadProfileAvatar(customer);

            } else {
                Log.e(TAG, "Customer not found for UID: " + uid);
                Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile data", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogout() {
        try {
            // Clear session data
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Redirect to login
            redirectToLogin();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning from other activities
        if (uid != -1) {
            loadProfileData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}