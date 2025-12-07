package com.example.ridequest;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class InspectionActivity extends AppCompatActivity {

    private CarRentalData db;
    private int bookingId;
    private int employeeId;

    // Request Codes
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    // UI Elements
    private ImageView ivPhoto;
    private String currentPhotoString = "";
    private TextInputEditText etDamage, etNotes;
    private Spinner spFuel;
    private RadioGroup rgType;
    private RadioButton rbPickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        db = new CarRentalData(this);
        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);

        // GET CORRECT EMPLOYEE ID
        SharedPreferences prefs = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        employeeId = prefs.getInt("EMPLOYEE_ID", -1);

        if (employeeId == -1) {
            Toast.makeText(this, "Error: Session expired. Please Re-Login.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Views
        rgType = findViewById(R.id.rgInspectionType);
        rbPickup = findViewById(R.id.rbPickup);
        spFuel = findViewById(R.id.spFuelLevel);
        etDamage = findViewById(R.id.etDamage);
        etNotes = findViewById(R.id.etNotes);
        Button btnUpload = findViewById(R.id.btnUploadPhoto);
        ivPhoto = findViewById(R.id.ivInspectionPhoto);
        Button btnSubmit = findViewById(R.id.btnSubmitInspection);

        btnUpload.setOnClickListener(v -> showImageSourceDialog());

        // SUBMIT WITH VALIDATION
        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                submitInspectionToDb();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Evidence Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else {
                    Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        builder.show();
    }

    // HANDLE RESULTS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = null;

                if (requestCode == CAMERA_REQUEST) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                }
                else if (requestCode == GALLERY_REQUEST) {
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        bitmap = BitmapFactory.decodeStream(imageStream);
                    }
                }


                if (bitmap != null) {
                    ivPhoto.setVisibility(View.VISIBLE);
                    ivPhoto.setImageBitmap(bitmap);
                    currentPhotoString = bitmapToString(bitmap);
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // VALIDATION
    private boolean validateInputs() {
        String damage = etDamage.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Check Damage Field
        if (damage.isEmpty()) {
            etDamage.setError("Please describe damage (or write 'None')");
            etDamage.requestFocus();
            return false;
        }

        // Check Notes Field
        if (notes.isEmpty()) {
            etNotes.setError("General notes are required");
            etNotes.requestFocus();
            return false;
        }

        // Check Photo
        if (currentPhotoString.isEmpty()) {
            Toast.makeText(this, "Evidence photo is required!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // DATABASE
    private void submitInspectionToDb() {
        String damage = etDamage.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String fuel = spFuel.getSelectedItem().toString();
        String inspectionType = rbPickup.isChecked() ? "Pickup" : "Return";

        boolean success = db.submitInspection(
                bookingId,
                employeeId,
                inspectionType,
                fuel,
                notes,
                damage,
                currentPhotoString
        );

        if (success) {
            String msg = inspectionType.equals("Pickup")
                    ? "Pickup Saved! Vehicle Rented."
                    : "Return Saved! Vehicle Inspected.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Database Error: Could not save inspection.", Toast.LENGTH_SHORT).show();
        }
    }

    private String bitmapToString(Bitmap bitmap) {
        if (bitmap.getWidth() > 1000) {
            double ratio = (double) 1000 / bitmap.getWidth();
            int h = (int) (bitmap.getHeight() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, h, true);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}