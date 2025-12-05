package com.example.ridequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private ImageView ivReceipt;
    private String receiptImageBase64 = null;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private double downpaymentAmount;
    private double totalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Log.d(TAG, "=== PaymentActivity Started ===");

        // ⭐ GET ALL DATA FROM BOOKING ACTIVITY
        int vid = getIntent().getIntExtra("VID", -1);
        double dailyRate = getIntent().getDoubleExtra("DAILY_RATE", 0.0);
        double baseCost = getIntent().getDoubleExtra("BASE_COST", 0.0);
        String insuranceType = getIntent().getStringExtra("INSURANCE_TYPE");
        double insuranceFee = getIntent().getDoubleExtra("INSURANCE_FEE", 0.0);
        totalCost = getIntent().getDoubleExtra("TOTAL_COST", 0.0);
        String carName = getIntent().getStringExtra("NAME");

        String pickupDate = getIntent().getStringExtra("PICKUP_DATE");
        String returnDate = getIntent().getStringExtra("RETURN_DATE");
        String pickupTime = getIntent().getStringExtra("PICKUP_TIME");
        String returnTime = getIntent().getStringExtra("RETURN_TIME");

        String pickupAddress = getIntent().getStringExtra("PICKUP_ADDRESS");
        String returnAddress = getIntent().getStringExtra("RETURN_ADDRESS");

        int lateHours = getIntent().getIntExtra("LATE_HOURS", 0);
        double lateFee = getIntent().getDoubleExtra("LATE_FEE", 0.0);
        int rentalDays = getIntent().getIntExtra("RENTAL_DAYS", 1);

        int uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        downpaymentAmount = totalCost * 0.30;
        double remainingBalance = totalCost - downpaymentAmount;

        // ⭐ LOG ALL COST DETAILS
        Log.d(TAG, "Base Cost: $" + baseCost);
        Log.d(TAG, "Insurance: " + insuranceType + " - $" + insuranceFee);
        Log.d(TAG, "Late Fee: $" + lateFee);
        Log.d(TAG, "Total Cost: $" + totalCost);
        Log.d(TAG, "Downpayment (30%): $" + downpaymentAmount);
        Log.d(TAG, "Remaining Balance: $" + remainingBalance);

        // Validate required data
        if(vid == -1 || pickupDate == null || returnDate == null ||
                pickupAddress == null || returnAddress == null) {
            Toast.makeText(this, "Error: Missing booking information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind UI Elements
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCarName = findViewById(R.id.tvCarName);
        TextView tvDates = findViewById(R.id.tvDates);
        TextView tvBaseCost = findViewById(R.id.tvBaseCost);
        TextView tvInsurance = findViewById(R.id.tvInsurance);
        TextView tvTotalCost = findViewById(R.id.tvTotalCost);
        TextView tvDownpayment = findViewById(R.id.tvDownpayment);
        TextView tvBalance = findViewById(R.id.tvBalance);
        TextView tvLateFeeWarning = findViewById(R.id.tvLateFeeWarning);
        ImageView ivQRCode = findViewById(R.id.ivQRCode);
        ivReceipt = findViewById(R.id.ivReceipt);
        Button btnUploadReceipt = findViewById(R.id.btnUploadReceipt);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        // Set UI Data
        if(tvTitle != null) tvTitle.setText("Payment Summary");
        if(tvCarName != null) tvCarName.setText(carName != null ? carName : "Unknown Vehicle");
        if(tvDates != null) tvDates.setText(pickupDate + " " + pickupTime + " → " + returnDate + " " + returnTime);

        // ⭐ SHOW COST BREAKDOWN
        if(tvBaseCost != null) tvBaseCost.setText("Base Cost (" + rentalDays + " days): $" + String.format("%.2f", baseCost));
        if(tvInsurance != null) {
            if(insuranceFee > 0) {
                tvInsurance.setText("Insurance (" + insuranceType + "): $" + String.format("%.2f", insuranceFee));
                tvInsurance.setVisibility(View.VISIBLE);
            } else {
                tvInsurance.setVisibility(View.GONE);
            }
        }

        if(tvTotalCost != null) tvTotalCost.setText("Total: $" + String.format("%.2f", totalCost));
        if(tvDownpayment != null) tvDownpayment.setText("Downpayment (30%): $" + String.format("%.2f", downpaymentAmount));
        if(tvBalance != null) tvBalance.setText("Balance (pay at pickup): $" + String.format("%.2f", remainingBalance));

        // Show late fee warning
        if(tvLateFeeWarning != null) {
            if(lateFee > 0) {
                tvLateFeeWarning.setVisibility(View.VISIBLE);
                tvLateFeeWarning.setText("⚠️ Late return penalty included: $" + String.format("%.2f", lateFee));
            } else {
                tvLateFeeWarning.setVisibility(View.GONE);
            }
        }

        // Load QR Code
        if(ivQRCode != null) {
            ivQRCode.setImageResource(R.drawable.qr_code_payment);
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
                                ivReceipt.setImageBitmap(bitmap);
                                ivReceipt.setVisibility(View.VISIBLE);

                                receiptImageBase64 = bitmapToBase64(bitmap);
                                Toast.makeText(this, "✓ Receipt uploaded successfully!", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to load receipt image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        btnUploadReceipt.setOnClickListener(v -> openGallery());

        // ⭐ CONFIRM PAYMENT BUTTON - PASS ALL COST DETAILS
        btnConfirm.setOnClickListener(v -> {
            Log.d(TAG, ">>> Confirm Payment clicked");

            if(receiptImageBase64 == null || receiptImageBase64.isEmpty()) {
                Toast.makeText(this, "⚠️ Please upload your payment receipt", Toast.LENGTH_LONG).show();
                return;
            }

            CarRentalData db = new CarRentalData(this);

            // ⭐ INTEGRATION POINT: Calling the updated createPendingBooking method
            // We removed 'createPendingBookingComplete' and merged it into this single method.
            boolean success = db.createPendingBooking(
                    uid, vid, carName,
                    pickupDate, returnDate,
                    pickupTime, returnTime,
                    pickupAddress, returnAddress,
                    rentalDays, baseCost, insuranceType, insuranceFee,
                    lateHours, lateFee, totalCost,
                    "QR Code Payment", receiptImageBase64
            );

            if(success) {
                Log.d(TAG, "✅ Booking created with all details!");

                String message = "✅ Booking Submitted Successfully!\n\n" +
                        "Your booking is pending admin verification.\n\n" +
                        "Cost Breakdown:\n" +
                        "Base Cost: $" + String.format("%.2f", baseCost) + "\n" +
                        (insuranceFee > 0 ? "Insurance: $" + String.format("%.2f", insuranceFee) + "\n" : "") +
                        (lateFee > 0 ? "Late Fee: $" + String.format("%.2f", lateFee) + "\n" : "") +
                        "Total: $" + String.format("%.2f", totalCost) + "\n\n" +
                        "Downpayment Paid: $" + String.format("%.2f", downpaymentAmount) + "\n" +
                        "Balance Due at Pickup: $" + String.format("%.2f", remainingBalance) + "\n\n" +
                        "Pickup: " + pickupAddress + "\n" +
                        "Return: " + returnAddress + "\n\n" +
                        "⚠️ IMPORTANT - Bring to Pickup:\n" +
                        "• Valid driver's license\n" +
                        "• Government-issued ID\n" +
                        "• Cash for remaining balance\n" +
                        "• Proof of insurance (if applicable)";

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            } else {
                Log.e(TAG, "❌ Booking failed!");
                Toast.makeText(this, "❌ Booking Failed\n\nPlease try again.", Toast.LENGTH_LONG).show();
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
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}