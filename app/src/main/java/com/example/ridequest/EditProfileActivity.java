package com.example.ridequest;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatarEdit;
    private CardView cardAvatarEdit;
    private ConstraintLayout layoutAvatar;
    private Uri selectedImageUri = null;
    private int uid;
    private CarRentalData db;

    // Activity Result Launcher for gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Display the selected image
                        ivAvatarEdit.setImageURI(uri);
                        Toast.makeText(this, "Profile picture selected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // Permission launcher for Android 13+ (if needed)
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new CarRentalData(this);
        uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        initializeViews();
        setupClickListeners();
        loadUserData();
    }

    private void initializeViews() {
        ivAvatarEdit = findViewById(R.id.ivAvatarEdit);
        cardAvatarEdit = findViewById(R.id.cardAvatarEdit);
        layoutAvatar = findViewById(R.id.layoutAvatar);
    }

    private void setupClickListeners() {
        EditText etFirst = findViewById(R.id.etFirst);
        EditText etLast = findViewById(R.id.etLast);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);

        // Click on avatar to change profile picture
        layoutAvatar.setOnClickListener(v -> checkPermissionAndOpenGallery());
        cardAvatarEdit.setOnClickListener(v -> checkPermissionAndOpenGallery());
        ivAvatarEdit.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // Save Button
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            String f = etFirst.getText().toString().trim();
            String l = etLast.getText().toString().trim();
            String e = etEmail.getText().toString().trim();
            String p = etPhone.getText().toString().trim();

            // Basic validation
            if (f.isEmpty()) {
                Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
                etFirst.requestFocus();
                return;
            }

            if (l.isEmpty()) {
                Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show();
                etLast.requestFocus();
                return;
            }

            if (e.isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                etEmail.requestFocus();
                return;
            }

            if (db.updateCustomer(uid, f, l, e, p)) {
                // Save profile image URI if selected
                if (selectedImageUri != null) {
                    saveProfileImageUri(selectedImageUri.toString());
                }
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button
        ImageView btnBack = findViewById(R.id.btnBackEdit);
        btnBack.setOnClickListener(v -> finish());

        // Cancel Button
        Button btnCancel = findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
    }

    private void loadUserData() {
        TextView tvName = findViewById(R.id.tvEditNameDisplay);
        EditText etFirst = findViewById(R.id.etFirst);
        EditText etLast = findViewById(R.id.etLast);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);

        // Load customer data from database
        CarRentalData.Customer c = db.getCustomer(uid);
        if (c != null) {
            etFirst.setText(c.firstName);
            etLast.setText(c.lastName);
            etEmail.setText(c.email);
            etPhone.setText(c.phone);
            tvName.setText(c.firstName + " " + c.lastName);
        }

        // Load saved profile image if exists
        String savedImageUri = getProfileImageUri();
        if (savedImageUri != null) {
            try {
                ivAvatarEdit.setImageURI(Uri.parse(savedImageUri));
                selectedImageUri = Uri.parse(savedImageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                showPermissionRationale();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionRationale();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs access to your gallery to select a profile picture.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveProfileImageUri(String uriString) {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        prefs.edit().putString("profileImageUri_" + uid, uriString).apply();
    }

    private String getProfileImageUri() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        return prefs.getString("profileImageUri_" + uid, null);
    }
}