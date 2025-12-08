package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private CarRentalData db;
    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new CarRentalData(this);
        uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        load();

        // Edit Profile
        LinearLayout btnEdit = findViewById(R.id.btnEditProfile);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        // Logout
        TextView btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // My Bookings
        TextView btnMyBookings = findViewById(R.id.btnMyBookings);
        if (btnMyBookings != null) {
            btnMyBookings.setOnClickListener(v -> {
                Intent intent = new Intent(this, MyBookingsActivity.class);
                startActivity(intent);
            });
        }

        TextView btnFavorites = findViewById(R.id.btnFavoriteCars);

        if (btnFavorites != null) {
            btnFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
            });
        }

        // Back
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        CarRentalData.Customer c = db.getCustomer(uid);
        if(c != null) {
            TextView tvName = findViewById(R.id.tvName);
            TextView tvEmail = findViewById(R.id.tvEmail);
            if(tvName != null) tvName.setText(c.firstName + " " + c.lastName);
            if(tvEmail != null) tvEmail.setText(c.email);
        }
    }
}