package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private CarRentalData db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new CarRentalData(this);

        RecyclerView rv = findViewById(R.id.recyclerView);
        ImageView ivProfile = findViewById(R.id.ivProfile);

        // Safety check: Only setup RecyclerView if it exists in the layout
        if(rv != null) {
            rv.setLayoutManager(new GridLayoutManager(this, 2));
        }

        // Safety check: Only setup Profile click if the image exists
        if(ivProfile != null) {
            ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView rv = findViewById(R.id.recyclerView);
        // Reload the list of cars every time the dashboard appears
        if(rv != null) {
            rv.setAdapter(new VehicleAdapter(this, db.getAllVehicles(), false, null));
        }
    }
}