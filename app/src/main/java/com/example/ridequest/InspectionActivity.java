package com.example.ridequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class InspectionActivity extends AppCompatActivity {

    // Database & Logic Variables
    private CarRentalData db;
    private int bookingId;
    private String inspectionType; // Will hold "Pickup" or "Return"
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private Bitmap evidencePhoto = null; // To store the captured image

    // UI Components
    private TextView tvHeaderTitle;
    private Spinner spFuelLevel;
    private TextInputEditText etDamage, etNotes;
    private ImageView ivInspectionPhoto;
    private Button btnSubmit, btnUploadPhoto;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection); // Ensure XML name matches

        // 1. Initialize Database
        db = new CarRentalData(this);

        // 2. Get Data from Intent (Passed from InspectionDashboardActivity)
        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);
        inspectionType = getIntent().getStringExtra("INSPECTION_TYPE");

        // Safety Check: If data is missing, close the screen
        if (bookingId == -1 || inspectionType == null) {
            Toast.makeText(this, "Error: Booking details missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Initialize Views
        // Note: Make sure your XML has the ID 'tvHeaderTitle' for the toolbar title text
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnBack = findViewById(R.id.btnBack);

        spFuelLevel = findViewById(R.id.spFuelLevel);
        etDamage = findViewById(R.id.etDamage);
        etNotes = findViewById(R.id.etNotes);

        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        ivInspectionPhoto = findViewById(R.id.ivInspectionPhoto);
        btnSubmit = findViewById(R.id.btnSubmitInspection);

        // 4. Setup Dynamic UI (Auto-fill text based on type)
        tvHeaderTitle.setText(inspectionType + " Inspection");
        btnSubmit.setText("Submit " + inspectionType);

        // 5. Set Listeners
        btnBack.setOnClickListener(v -> finish());
        btnUploadPhoto.setOnClickListener(v -> dispatchTakePictureIntent());
        btnSubmit.setOnClickListener(v -> submitInspection());
    }

    // --- LOGIC: Submit Inspection ---
    private void submitInspection() {
        String fuelLevel = spFuelLevel.getSelectedItem().toString();
        String damageReport = etDamage.getText().toString().trim();
        String generalNotes = etNotes.getText().toString().trim();

        if (damageReport.isEmpty()) {
            damageReport = "No new damage reported.";
        }
        if (generalNotes.isEmpty()) {
            generalNotes = "None";
        }

        // Save to Database
        // This assumes your db.addInspection method accepts these parameters
        boolean success = db.addInspection(bookingId, inspectionType, fuelLevel, damageReport, generalNotes);

        if (success) {
            // Update the Booking Status based on the workflow
            if (inspectionType.equalsIgnoreCase("Pickup")) {
                // After Pickup Inspection -> Status becomes 'Rented' (Active Rental)
                db.updateBookingStatus(bookingId, "Rented");
            } else {
                // After Return Inspection -> Status becomes 'Inspected' (Waiting for Manager Approval)
                db.updateBookingStatus(bookingId, "Inspected");
            }

            Toast.makeText(this, inspectionType + " Inspection Submitted!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity and return to Dashboard
        } else {
            Toast.makeText(this, "Submission Failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- LOGIC: Camera (Basic Implementation) ---
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            evidencePhoto = (Bitmap) extras.get("data");

            // Show the image in the ImageView
            ivInspectionPhoto.setVisibility(android.view.View.VISIBLE);
            ivInspectionPhoto.setImageBitmap(evidencePhoto);

            // Note: Saving the actual image to the DB requires converting Bitmap to Byte Array or Base64.
            // If you haven't implemented that in db.addInspection yet, the text data is enough for now.
        }
    }
}