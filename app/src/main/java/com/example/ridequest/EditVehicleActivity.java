package com.example.ridequest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditVehicleActivity extends AppCompatActivity {

    private int vehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        CarRentalData db = new CarRentalData(this);

        // Get vehicle data from intent
        vehicleId = getIntent().getIntExtra("VEHICLE_ID", -1);
        String makeModel = getIntent().getStringExtra("MAKE_MODEL");
        String type = getIntent().getStringExtra("TYPE");
        double price = getIntent().getDoubleExtra("PRICE", 0.0);
        String imageRes = getIntent().getStringExtra("IMAGE_RES");
        String transmission = getIntent().getStringExtra("TRANSMISSION");
        int seats = getIntent().getIntExtra("SEATS", 5);

        // Parse make and model
        String[] parts = makeModel != null ? makeModel.split(" ", 2) : new String[]{"", ""};
        String make = parts.length > 0 ? parts[0] : "";
        String model = parts.length > 1 ? parts[1] : "";

        // Bind UI elements
        EditText etMake = findViewById(R.id.etMake);
        EditText etModel = findViewById(R.id.etModel);
        EditText etType = findViewById(R.id.etType);
        EditText etPrice = findViewById(R.id.etPrice);
        Spinner spImage = findViewById(R.id.spImage);
        Spinner spTransmission = findViewById(R.id.spTransmission);
        Spinner spSeats = findViewById(R.id.spSeats);
        ImageView ivPreview = findViewById(R.id.ivPreview);
        Button btnUpdate = findViewById(R.id.btnUpdateCar);
        Button btnPreview = findViewById(R.id.btnPreview);

        // Populate fields with existing data
        etMake.setText(make);
        etModel.setText(model);
        etType.setText(type);
        etPrice.setText(String.valueOf(price));

        // Setup image spinner
        String[] images = {"car_wigo", "car_tesla", "car_explorer"};
        ArrayAdapter<String> imageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, images);
        spImage.setAdapter(imageAdapter);

        // Select current image
        for (int i = 0; i < images.length; i++) {
            if (images[i].equals(imageRes)) {
                spImage.setSelection(i);
                break;
            }
        }

        // Setup transmission spinner
        String[] transmissions = {"Manual", "Automatic"};
        ArrayAdapter<String> transmissionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, transmissions);
        spTransmission.setAdapter(transmissionAdapter);

        // Select current transmission
        if (transmission != null) {
            for (int i = 0; i < transmissions.length; i++) {
                if (transmissions[i].equalsIgnoreCase(transmission)) {
                    spTransmission.setSelection(i);
                    break;
                }
            }
        }

        // Setup seats spinner
        String[] seatOptions = {"2", "4", "5", "7", "8"};
        ArrayAdapter<String> seatsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, seatOptions);
        spSeats.setAdapter(seatsAdapter);

        // Select current seat count
        spSeats.setSelection(getSeatsIndex(seats));

        // Preview button
        btnPreview.setOnClickListener(v -> {
            try {
                String selected = spImage.getSelectedItem().toString();
                int resId = getResources().getIdentifier(selected, "drawable", getPackageName());
                if (resId != 0) ivPreview.setImageResource(resId);
            } catch (Exception e) {
                Toast.makeText(this, "Image preview failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Update button
        btnUpdate.setOnClickListener(v -> {
            try {
                String newMake = etMake.getText().toString().trim();
                String newModel = etModel.getText().toString().trim();
                String newType = etType.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String img = spImage.getSelectedItem().toString();
                String trans = spTransmission.getSelectedItem().toString();
                int seatCount = Integer.parseInt(spSeats.getSelectedItem().toString());

                if (newMake.isEmpty() || newModel.isEmpty() || newType.isEmpty() || priceStr.isEmpty()) {
                    Toast.makeText(this, "Please fill ALL fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newPrice = Double.parseDouble(priceStr);

                if (db.updateVehicle(vehicleId, newMake, newModel, newType, newPrice, img, trans, seatCount)) {
                    Toast.makeText(this, "Vehicle Updated!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to Update", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private int getSeatsIndex(int seats) {
        switch (seats) {
            case 2: return 0;
            case 4: return 1;
            case 5: return 2;
            case 7: return 3;
            case 8: return 4;
            default: return 2; // Default to 5 seats
        }
    }
}