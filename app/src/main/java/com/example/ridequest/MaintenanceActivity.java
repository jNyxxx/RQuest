package com.example.ridequest;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MaintenanceActivity extends AppCompatActivity {

    private CarRentalData db;
    private Spinner spVehicle;
    private TextInputEditText etDate, etDesc, etCost, etMechanic;
    private List<CarRentalData.VehicleItem> vehicleList;
    private List<Integer> vehicleIds; // To map Spinner position to ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        db = new CarRentalData(this);

        // Initialize Views
        spVehicle = findViewById(R.id.spVehicleSelect);
        etDate = findViewById(R.id.etServiceDate);
        etDesc = findViewById(R.id.etDescription);
        etCost = findViewById(R.id.etCost);
        etMechanic = findViewById(R.id.etMechanicName);
        Button btnSave = findViewById(R.id.btnSaveMaintenance);
        ImageView btnBack = findViewById(R.id.btnBack);

        // 1. Setup Vehicle Spinner
        loadVehicleSpinner();

        // 2. Setup Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // 3. Pre-fill Mechanic Name if logged in
        // (Optional: If you want to use the logged-in user's name)
        // String role = getSharedPreferences("UserSession", MODE_PRIVATE).getString("EMP_ROLE", "");
        // if(role.equals("Mechanic")) { ... }

        // 4. Save Button Logic
        btnSave.setOnClickListener(v -> saveMaintenanceRecord());

        // 5. Back Button
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadVehicleSpinner() {
        vehicleList = db.getAllVehicles();
        vehicleIds = new ArrayList<>();
        List<String> vehicleNames = new ArrayList<>();

        for (CarRentalData.VehicleItem v : vehicleList) {
            // Create a readable string "Toyota Wigo (ABC-123)"
            String display = v.title + " (" + v.plate + ")";
            vehicleNames.add(display);
            vehicleIds.add(v.id);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVehicle.setAdapter(adapter);

        // Check if a specific vehicle was passed via Intent
        int targetVid = getIntent().getIntExtra("VEHICLE_ID", -1);
        if (targetVid != -1) {
            for (int i = 0; i < vehicleIds.size(); i++) {
                if (vehicleIds.get(i) == targetVid) {
                    spVehicle.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year1, month1, day1) -> {
                    // Format: YYYY-MM-DD
                    String date = String.format(Locale.US, "%d-%02d-%02d", year1, month1 + 1, day1);
                    etDate.setText(date);
                }, year, month, day);
        dpd.show();
    }

    private void saveMaintenanceRecord() {
        // Get Inputs
        String date = etDate.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String costStr = etCost.getText().toString().trim();
        String mechanicName = etMechanic.getText().toString().trim();

        // Validation
        if (date.isEmpty() || desc.isEmpty() || costStr.isEmpty() || mechanicName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (vehicleIds.isEmpty()) {
            Toast.makeText(this, "No vehicles available to service", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);

            // Get Selected Vehicle ID
            int position = spVehicle.getSelectedItemPosition();
            int vehicleId = vehicleIds.get(position);

            // Get Current Employee ID (Mechanic)
            int employeeId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("EMP_ID", -1);

            // We append the manual mechanic name input to the description
            // since the DB table tracks ID, but sometimes names are useful in description
            String fullDesc = desc + " [Serviced by: " + mechanicName + "]";

            boolean success = db.submitMaintenance(vehicleId, employeeId, date, fullDesc, cost);

            if (success) {
                Toast.makeText(this, "Maintenance Logged Successfully! 🛠️", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            etCost.setError("Invalid number");
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}