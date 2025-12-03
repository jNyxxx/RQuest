package com.example.ridequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddVehicleActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private String selectedImageBase64 = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        CarRentalData db = new CarRentalData(this);

        EditText etMake = findViewById(R.id.etMake);
        EditText etModel = findViewById(R.id.etModel);
        EditText etType = findViewById(R.id.etType);
        EditText etYear = findViewById(R.id.etYear);
        EditText etPrice = findViewById(R.id.etPrice);
        EditText etPlate = findViewById(R.id.etPlate);
        EditText etSeats = findViewById(R.id.etSeats);
        Spinner spTransmission = findViewById(R.id.spTransmission);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnAdd = findViewById(R.id.btnAddCar);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);

        // Setup transmission spinner
        String[] transmissions = {"Manual", "Automatic"};
        ArrayAdapter<String> transmissionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, transmissions);
        spTransmission.setAdapter(transmissionAdapter);

        // Initialize gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // Load image into ImageView
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                ivPreview.setImageBitmap(bitmap);

                                // Convert to Base64
                                selectedImageBase64 = bitmapToBase64(bitmap);
                                Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // Select Image Button
        btnSelectImage.setOnClickListener(v -> openGallery());

        btnAdd.setOnClickListener(v -> {
            try {
                String make = etMake.getText().toString().trim();
                String model = etModel.getText().toString().trim();
                String type = etType.getText().toString().trim();
                String yearStr = etYear.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String plate = etPlate.getText().toString().trim();
                String seatsStr = etSeats.getText().toString().trim();
                String transmission = spTransmission.getSelectedItem().toString();

                // Validate all fields
                if (make.isEmpty() || model.isEmpty() || type.isEmpty() || yearStr.isEmpty() ||
                        priceStr.isEmpty() || plate.isEmpty() || seatsStr.isEmpty()) {
                    Toast.makeText(this, "Please fill ALL fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate image selection
                if (selectedImageBase64 == null || selectedImageBase64.isEmpty()) {
                    Toast.makeText(this, "Please select a vehicle image", Toast.LENGTH_SHORT).show();
                    return;
                }

                int year = Integer.parseInt(yearStr);
                double price = Double.parseDouble(priceStr);
                int seatCount = Integer.parseInt(seatsStr);

                // Validate seat count
                if (seatCount < 1 || seatCount > 50) {
                    Toast.makeText(this, "Seat capacity must be between 1 and 50", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.addNewCarComplete(make, model, type, year, price, plate, selectedImageBase64, transmission, seatCount)) {
                    Toast.makeText(this, "Vehicle Added!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to Add", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers for Year, Price, and Seats", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        // Resize bitmap to reduce size
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}