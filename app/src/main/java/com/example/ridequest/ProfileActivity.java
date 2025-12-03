package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        // Edit Profile Click
        LinearLayout btnEdit = findViewById(R.id.btnEditProfile);
        btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        // Logout Click - UPDATED to go directly to LoginActivity
        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Clear session
            SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            // Go directly to LoginActivity (skip landing page)
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(); // Refresh in case edit changed name
    }

    private void load() {
        CarRentalData.Customer c = db.getCustomer(uid);
        if(c != null) {
            ((TextView)findViewById(R.id.tvName)).setText(c.firstName + " " + c.lastName);
            ((TextView)findViewById(R.id.tvEmail)).setText(c.email);
        }
    }
}