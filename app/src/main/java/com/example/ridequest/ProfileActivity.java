package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        CarRentalData.Customer c = db.getCustomer(uid);
        if(c != null) {
            ((TextView)findViewById(R.id.tvName)).setText(c.firstName + " " + c.lastName);
            ((TextView)findViewById(R.id.tvEmail)).setText(c.email);
        }
    }
}