package com.example.ridequest;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;

public class InspectionActivity extends AppCompatActivity {

    private CarRentalData db;
    private int bookingId;
    private String inspectionType;
    private int inspectorId; // Store the logged-in ID

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int PERMISSION_CODE = 100;
    private Bitmap evidencePhoto = null;
    private String photoBase64 = "No Photo"; // Default value

    private Spinner spFuelLevel;
    private TextInputEditText etDamage, etNotes;
    private ImageView ivInspectionPhoto;
    private TextView tvHeaderTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        db = new CarRentalData(this);

        // GET LOGGED IN AGENT ID
        SharedPreferences prefs = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        inspectorId = prefs.getInt("EMPLOYEE_ID", -1); // Defaults to -1 if not found

        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);
        inspectionType = getIntent().getStringExtra("INSPECTION_TYPE");

        if (bookingId == -1 || inspectionType == null) {
            Toast.makeText(this, "Error: Missing Info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        spFuelLevel = findViewById(R.id.spFuelLevel);
        etDamage = findViewById(R.id.etDamage);
        etNotes = findViewById(R.id.etNotes);
        ivInspectionPhoto = findViewById(R.id.ivInspectionPhoto);
        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnUpload = findViewById(R.id.btnUploadPhoto);
        Button btnSubmit = findViewById(R.id.btnSubmitInspection);

        // UI
        tvHeaderTitle.setText(inspectionType + " Inspection");
        btnSubmit.setText("Submit " + inspectionType);

        btnBack.setOnClickListener(v -> finish());

        // CAMERA
        btnUpload.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        btnSubmit.setOnClickListener(v -> submitInspection());
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            evidencePhoto = (Bitmap) extras.get("data");
            ivInspectionPhoto.setVisibility(android.view.View.VISIBLE);
            ivInspectionPhoto.setImageBitmap(evidencePhoto);

            photoBase64 = convertBitmapToString(evidencePhoto);
        }
    }

    private String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void submitInspection() {
        String fuel = spFuelLevel.getSelectedItem().toString();
        String damage = etDamage.getText().toString();
        String notes = etNotes.getText().toString();

        // PASS ALL DATA TO DATABASE
        boolean success = db.addInspection(bookingId, inspectionType, fuel, damage, notes, inspectorId, photoBase64);

        if (success) {
            // Update Status based on Type
            if (inspectionType.equals("Pickup")) {
                db.updateBookingStatus(bookingId, "Rented");
            } else {
                db.updateBookingStatus(bookingId, "Inspected");
            }
            Toast.makeText(this, "Inspection Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save inspection", Toast.LENGTH_SHORT).show();
        }
    }
}