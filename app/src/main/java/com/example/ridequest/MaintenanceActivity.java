package com.example.ridequest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MaintenanceActivity extends AppCompatActivity {

    private CarRentalData db;
    private int vehicleId;
    private int mechanicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        db = new CarRentalData(this);

        // gets Vehicle ID
        vehicleId = getIntent().getIntExtra("VEHICLE_ID", -1);
        String carName = getIntent().getStringExtra("CAR_NAME");

        // Get Mechanic ID
        SharedPreferences prefs = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        mechanicId = prefs.getInt("EMPLOYEE_ID", -1);

        if (mechanicId == -1) {
            Toast.makeText(this, "Session Expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        TextView tvCarName = findViewById(R.id.tvCarName);
        TextInputEditText etDesc = findViewById(R.id.etDescription);
        TextInputEditText etCost = findViewById(R.id.etCost);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // Display Car Name
        tvCarName.setText(carName != null ? carName : "Unknown Vehicle");

        // Submit
        btnSubmit.setOnClickListener(v -> {
            String desc = etDesc.getText().toString().trim();
            String costStr = etCost.getText().toString().trim();

            // Validation
            if (desc.isEmpty()) {
                etDesc.setError("Description required");
                return;
            }
            if (costStr.isEmpty()) {
                etCost.setError("Cost required");
                return;
            }

            double cost;
            try {
                cost = Double.parseDouble(costStr);
            } catch (NumberFormatException e) {
                etCost.setError("Invalid cost format");
                return;
            }

            if (db.submitMaintenance(vehicleId, mechanicId, desc, cost)) {
                Toast.makeText(this, "Maintenance Logged Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving record", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}