package com.example.ridequest;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MaintenanceDetailsActivity extends AppCompatActivity {

    private TextView tvMaintenanceId, tvCarName, tvMechanic, tvDate, tvCost, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_details);

        // Initialize Views
        tvMaintenanceId = findViewById(R.id.tvMaintenanceId);
        tvCarName = findViewById(R.id.tvCarName);
        tvMechanic = findViewById(R.id.tvMechanic);
        tvDate = findViewById(R.id.tvDate);
        tvCost = findViewById(R.id.tvCost);
        tvDescription = findViewById(R.id.tvDescription);

        // Get Data from Intent
        int id = getIntent().getIntExtra("ID", -1);
        String carName = getIntent().getStringExtra("CAR_NAME");
        String mechanic = getIntent().getStringExtra("MECHANIC");
        String date = getIntent().getStringExtra("DATE");
        double cost = getIntent().getDoubleExtra("COST", 0.0);
        String description = getIntent().getStringExtra("DESCRIPTION");

        // Display Data
        tvMaintenanceId.setText("Maintenance ID: #" + id);
        tvCarName.setText(carName);
        tvMechanic.setText(mechanic);
        tvDate.setText(date);
        tvCost.setText(String.format("₱%.2f", cost));
        tvDescription.setText(description);

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}