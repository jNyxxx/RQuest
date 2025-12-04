package com.example.ridequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class EditVehicleActivity extends AppCompatActivity {

    private int vehicleId;
    private ImageView ivPreview;
    private String selectedImageBase64 = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

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
        String imageData = getIntent().getStringExtra("IMAGE_RES");
        String transmission = getIntent().getStringExtra("TRANSMISSION");
        int seats = getIntent().getIntExtra("SEATS", 5);

        // make and model
        String[] parts = makeModel != null ? makeModel.split(" ", 2) : new String[]{"", ""};
        String make = parts.length > 0 ? parts[0] : "";
        String model = parts.length > 1 ? parts[1] : "";

        // UI elements
        EditText etMake = findViewById(R.id.etMake);
        EditText etModel = findViewById(R.id.etModel);
        EditText etType = findViewById(R.id.etType);
        EditText etPrice = findViewById(R.id.etPrice);
        EditText etSeats = findViewById(R.id.etSeats);
        Spinner spTransmission = findViewById(R.id.spTransmission);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnUpdate = findViewById(R.id.btnUpdateCar);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);

        // Populate fields with existing data
        etMake.setText(make);
        etModel.setText(model);
        etType.setText(type);
        etPrice.setText(String.valueOf(price));
        etSeats.setText(String.valueOf(seats));

        if (imageData != null && !imageData.isEmpty()) {
            selectedImageBase64 = imageData;
            try {
                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivPreview.setImageBitmap(bitmap);
                } else {
                    int resId = getResources().getIdentifier(imageData, "drawable", getPackageName());
                    if (resId != 0) {
                        ivPreview.setImageResource(resId);
                    }
                }
            } catch (Exception e) {
                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
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

        // Initialize gallery launcher
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

        // Update button
        btnUpdate.setOnClickListener(v -> {
            try {
                String newMake = etMake.getText().toString().trim();
                String newModel = etModel.getText().toString().trim();
                String newType = etType.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String seatsStr = etSeats.getText().toString().trim();
                String trans = spTransmission.getSelectedItem().toString();

                if (newMake.isEmpty() || newModel.isEmpty() || newType.isEmpty() ||
                        priceStr.isEmpty() || seatsStr.isEmpty()) {
                    Toast.makeText(this, "Please fill ALL fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate image
                if (selectedImageBase64 == null || selectedImageBase64.isEmpty()) {
                    Toast.makeText(this, "Please select a vehicle image", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newPrice = Double.parseDouble(priceStr);
                int seatCount = Integer.parseInt(seatsStr);

                // Validate seat count
                if (seatCount < 1 || seatCount > 50) {
                    Toast.makeText(this, "Seat capacity must be between 1 and 50", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.updateVehicle(vehicleId, newMake, newModel, newType, newPrice, selectedImageBase64, trans, seatCount)) {
                    Toast.makeText(this, "Vehicle Updated!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to Update", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers for Price and Seats", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
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