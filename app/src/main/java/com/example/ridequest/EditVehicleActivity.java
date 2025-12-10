package com.example.ridequest;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditVehicleActivity extends AppCompatActivity {
    private static final String TAG = "EditVehicleActivity";

    private CarRentalData db;
    private int vehicleId;

    // UI Elements
    private Spinner spMake;
    private Spinner spType;
    private EditText etModel;
    private EditText etColor;
    private EditText etPrice;
    private Spinner spTransmission;
    private Spinner spCategory;
    private Spinner spFuel;
    private EditText etSeats;
    private ImageView ivPreview;
    private MaterialButton btnSelectImage;
    private MaterialButton btnUpdateCar;
    private MaterialButton btnBack;

    // Data
    private String selectedImageBase64 = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        // Get vehicle ID from intent
        vehicleId = getIntent().getIntExtra("VEHICLE_ID", -1);
        if (vehicleId == -1) {
            Toast.makeText(this, "Error: Vehicle ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        db = new CarRentalData(this);

        // Initialize views
        initializeViews();

        // Setup gallery launcher
        setupGalleryLauncher();

        // Setup spinners
        setupMakeSpinner();
        setupTypeSpinner();
        setupTransmissionSpinner();
        setupCategorySpinner();
        setupFuelTypeSpinner();

        // Load vehicle data
        loadVehicleData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        spMake = findViewById(R.id.spMake);
        spType = findViewById(R.id.spType);
        etModel = findViewById(R.id.etModel);
        etColor = findViewById(R.id.etColor);
        etPrice = findViewById(R.id.etPrice);
        spTransmission = findViewById(R.id.spTransmission);
        spCategory = findViewById(R.id.spCategory);
        spFuel = findViewById(R.id.spFuel);
        etSeats = findViewById(R.id.etSeats);
        ivPreview = findViewById(R.id.ivPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpdateCar = findViewById(R.id.btnUpdateCar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupGalleryLauncher() {
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
                                Toast.makeText(this, "Image updated", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Error loading image", e);
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void setupMakeSpinner() {
        List<String> makeList = new ArrayList<>();
        makeList.add("Select Make");

        Cursor cursor = db.getAllMakes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String makeName = cursor.getString(cursor.getColumnIndexOrThrow("make_name"));
                makeList.add(makeName);
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                makeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMake.setAdapter(adapter);
    }

    private void setupTypeSpinner() {
        List<String> typeList = new ArrayList<>();
        typeList.add("Select Type");

        Cursor cursor = db.getAllTypes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String typeName = cursor.getString(cursor.getColumnIndexOrThrow("type_name"));
                typeList.add(typeName);
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                typeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter);
    }

    private void setupTransmissionSpinner() {
        String[] transmissions = {"Manual", "Automatic"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                transmissions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTransmission.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        String[] categories = {"Premium", "Luxury", "Economy", "Family", "Sports"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void setupFuelTypeSpinner() {
        String[] fuelTypes = {"Regular Gasoline", "Premium Gasoline", "Diesel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                fuelTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFuel.setAdapter(adapter);
    }

    private void loadVehicleData() {
        try {
            // Get data from intent
            String makeModel = getIntent().getStringExtra("MAKE_MODEL");
            String type = getIntent().getStringExtra("TYPE");
            double price = getIntent().getDoubleExtra("PRICE", 0.0);
            String imageData = getIntent().getStringExtra("IMAGE_RES");
            String transmission = getIntent().getStringExtra("TRANSMISSION");
            int seats = getIntent().getIntExtra("SEATS", 5);
            String color = getIntent().getStringExtra("COLOR");
            String category = getIntent().getStringExtra("CATEGORY");
            String fuel = getIntent().getStringExtra("FUEL");

            // Parse make and model
            String[] parts = makeModel != null ? makeModel.split(" ", 2) : new String[]{"", ""};
            String make = parts.length > 0 ? parts[0] : "";
            String model = parts.length > 1 ? parts[1] : "";

            // Set Make spinner
            setSpinnerValue(spMake, make);

            // Set Type spinner
            setSpinnerValue(spType, type);

            // Set other fields
            etModel.setText(model);
            etColor.setText(color != null ? color : "");
            etPrice.setText(String.valueOf(price));
            etSeats.setText(String.valueOf(seats));

            // Set transmission
            setSpinnerValue(spTransmission, transmission);

            // Set category
            setSpinnerValue(spCategory, category);

            // Set fuel type
            setSpinnerValue(spFuel, fuel);

            // Set image
            if (imageData != null && !imageData.isEmpty()) {
                selectedImageBase64 = imageData;
                try {
                    // Try loading as base64
                    byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        ivPreview.setImageBitmap(bitmap);
                    } else {
                        // Try loading as drawable resource
                        int resId = getResources().getIdentifier(imageData, "drawable", getPackageName());
                        if (resId != 0) {
                            ivPreview.setImageResource(resId);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image", e);
                    ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading vehicle data", e);
            Toast.makeText(this, "Error loading vehicle data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSelectImage.setOnClickListener(v -> openGallery());

        btnUpdateCar.setOnClickListener(v -> updateVehicle());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void updateVehicle() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Get values from UI
            String makeName = spMake.getSelectedItem().toString();
            String typeName = spType.getSelectedItem().toString();
            String model = etModel.getText().toString().trim();
            String color = etColor.getText().toString().trim();
            double dailyRate = Double.parseDouble(etPrice.getText().toString().trim());
            String transmission = spTransmission.getSelectedItem().toString();
            String category = spCategory.getSelectedItem().toString();
            String fuelType = spFuel.getSelectedItem().toString();
            int seatingCapacity = Integer.parseInt(etSeats.getText().toString().trim());

            // Validate seat capacity
            if (seatingCapacity < 1 || seatingCapacity > 50) {
                Toast.makeText(this, "Seat capacity must be between 1 and 50", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use your existing updateVehicle method
            // Signature: updateVehicle(vehicleId, make, model, type, price, imageRes, transmission, seats, color, category, fuelType)
            boolean success = db.updateVehicle(
                    vehicleId,
                    makeName,
                    model,
                    typeName,
                    dailyRate,
                    selectedImageBase64,
                    transmission,
                    seatingCapacity,
                    color,
                    category,
                    fuelType
            );

            if (success) {
                Toast.makeText(this, "Vehicle updated successfully!", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update vehicle", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format", e);
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error updating vehicle", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        if (spMake.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a make", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etModel.getText().toString().trim().isEmpty()) {
            etModel.setError("Model is required");
            etModel.requestFocus();
            return false;
        }

        if (etColor.getText().toString().trim().isEmpty()) {
            etColor.setError("Color is required");
            etColor.requestFocus();
            return false;
        }

        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        if (etSeats.getText().toString().trim().isEmpty()) {
            etSeats.setError("Seating capacity is required");
            etSeats.requestFocus();
            return false;
        }

        if (selectedImageBase64 == null || selectedImageBase64.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}