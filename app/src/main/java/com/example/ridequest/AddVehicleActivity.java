package com.example.ridequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddVehicleActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private TextView tvImageName;
    private String selectedImageBase64 = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private TextInputLayout tilMake, tilModel, tilType, tilYear, tilPrice, tilPlate, tilSeats, tilColor;
    private TextInputEditText etMake, etModel, etType, etYear, etPrice, etPlate, etSeats, etColor;
    private Spinner spTransmission, spCategory, spFuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        CarRentalData db = new CarRentalData(this);

        // Layouts
        tilMake = findViewById(R.id.tilMake);
        tilModel = findViewById(R.id.tilModel);
        tilType = findViewById(R.id.tilType);
        tilYear = findViewById(R.id.tilYear);
        tilPrice = findViewById(R.id.tilPrice);
        tilPlate = findViewById(R.id.tilPlate);
        tilSeats = findViewById(R.id.tilSeats);
        tilColor = findViewById(R.id.tilColor);

        // Inputs
        etMake = findViewById(R.id.etMake);
        etModel = findViewById(R.id.etModel);
        etType = findViewById(R.id.etType);
        etYear = findViewById(R.id.etYear);
        etPrice = findViewById(R.id.etPrice);
        etPlate = findViewById(R.id.etPlate);
        etSeats = findViewById(R.id.etSeats);
        etColor = findViewById(R.id.etColor);

        // Spinners
        spTransmission = findViewById(R.id.spTransmission);
        spCategory = findViewById(R.id.spCategory);
        spFuel = findViewById(R.id.spFuel);

        ivPreview = findViewById(R.id.ivPreview);
        tvImageName = findViewById(R.id.tvImageName);
        Button btnAdd = findViewById(R.id.btnAddCar);
        MaterialCardView cvImageSelector = findViewById(R.id.cvImageSelector);

        // Spinners
        setupSpinner(spTransmission, new String[]{"Manual", "Automatic"});
        setupSpinner(spCategory, new String[]{"Premium", "Luxury", "Economy", "Family", "Sports"});
        setupSpinner(spFuel, new String[]{"Regular Gasoline", "Premium Gasoline", "Diesel"});

        // gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                ivPreview.setImageBitmap(bitmap);

                                selectedImageBase64 = bitmapToBase64(bitmap);

                                tvImageName.setText("Image selected ✓");
                                tvImageName.setTextColor(getResources().getColor(R.color.rq_orange, null));

                                Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // gallery launcehr
        cvImageSelector.setOnClickListener(v -> openGallery());

        // add vehicle button
        btnAdd.setOnClickListener(v -> addVehicle(db));

        // back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void addVehicle(CarRentalData db) {
        clearErrors();

        try {
            // Gets values
            String make = etMake.getText().toString().trim();
            String model = etModel.getText().toString().trim();
            String type = etType.getText().toString().trim();
            String yearStr = etYear.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String plate = etPlate.getText().toString().trim();
            String seatsStr = etSeats.getText().toString().trim();
            String color = etColor.getText().toString().trim();

            String transmission = spTransmission.getSelectedItem().toString();
            String category = spCategory.getSelectedItem().toString();
            String fuel = spFuel.getSelectedItem().toString();

            // Validates all fields
            boolean hasError = false;

            if (make.isEmpty()) {
                tilMake.setError("Make is required");
                if (!hasError) etMake.requestFocus();
                hasError = true;
            }

            if (model.isEmpty()) {
                tilModel.setError("Model is required");
                if (!hasError) etModel.requestFocus();
                hasError = true;
            }

            if (type.isEmpty()) {
                tilType.setError("Type is required");
                if (!hasError) etType.requestFocus();
                hasError = true;
            }

            if (color.isEmpty()) {
                if (tilColor != null) tilColor.setError("Color is required");
                else etColor.setError("Color is required");

                if (!hasError) etColor.requestFocus();
                hasError = true;
            }

            if (yearStr.isEmpty()) {
                tilYear.setError("Year is required");
                if (!hasError) etYear.requestFocus();
                hasError = true;
            } else {
                try {
                    int year = Integer.parseInt(yearStr);
                    if (year < 1900 || year > 2030) {
                        tilYear.setError("Enter a valid year (1900-2030)");
                        if (!hasError) etYear.requestFocus();
                        hasError = true;
                    }
                } catch (NumberFormatException e) {
                    tilYear.setError("Please enter a valid number");
                    if (!hasError) etYear.requestFocus();
                    hasError = true;
                }
            }

            if (priceStr.isEmpty()) {
                tilPrice.setError("Rate is required");
                if (!hasError) etPrice.requestFocus();
                hasError = true;
            } else {
                try {
                    double price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        tilPrice.setError("Rate must be greater than 0");
                        if (!hasError) etPrice.requestFocus();
                        hasError = true;
                    }
                } catch (NumberFormatException e) {
                    tilPrice.setError("Please enter a valid number");
                    if (!hasError) etPrice.requestFocus();
                    hasError = true;
                }
            }

            if (plate.isEmpty()) {
                tilPlate.setError("Plate number is required");
                if (!hasError) etPlate.requestFocus();
                hasError = true;
            }

            if (seatsStr.isEmpty()) {
                tilSeats.setError("Seating capacity is required");
                if (!hasError) etSeats.requestFocus();
                hasError = true;
            } else {
                try {
                    int seats = Integer.parseInt(seatsStr);
                    if (seats < 1 || seats > 50) {
                        tilSeats.setError("Enter a valid number of seats (1-50)");
                        if (!hasError) etSeats.requestFocus();
                        hasError = true;
                    }
                } catch (NumberFormatException e) {
                    tilSeats.setError("Please enter a valid number");
                    if (!hasError) etSeats.requestFocus();
                    hasError = true;
                }
            }

            if (selectedImageBase64 == null || selectedImageBase64.isEmpty()) {
                Toast.makeText(this, "Please select a vehicle image", Toast.LENGTH_SHORT).show();
                hasError = true;
            }

            if (hasError) {
                return;
            }

            int year = Integer.parseInt(yearStr);
            double price = Double.parseDouble(priceStr);
            int seatCount = Integer.parseInt(seatsStr);

            // Add to database
            if (db.addNewCarComplete(make, model, type, year, price, plate, selectedImageBase64,
                    transmission, seatCount, color, category, fuel)) {
                Toast.makeText(this, "Vehicle added successfully! ✓", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add vehicle. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        if(tilMake != null) tilMake.setError(null);
        if(tilModel != null) tilModel.setError(null);
        if(tilType != null) tilType.setError(null);
        if(tilYear != null) tilYear.setError(null);
        if(tilPrice != null) tilPrice.setError(null);
        if(tilPlate != null) tilPlate.setError(null);
        if(tilSeats != null) tilSeats.setError(null);
        if(tilColor != null) tilColor.setError(null);
        else if (etColor != null) etColor.setError(null);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        int maxWidth = 800;
        int maxHeight = 600;

        float scale = Math.min(
                (float) maxWidth / bitmap.getWidth(),
                (float) maxHeight / bitmap.getHeight()
        );

        int newWidth = Math.round(bitmap.getWidth() * scale);
        int newHeight = Math.round(bitmap.getHeight() * scale);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}