package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private String paymentMethod = "Credit Card";
    private EditText etPaymentId;
    private EditText etCardholderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Log.d(TAG, "=== PaymentActivity Started ===");

        // Get ALL data from BookingActivity
        int vid = getIntent().getIntExtra("VID", -1);
        double dailyRate = getIntent().getDoubleExtra("DAILY_RATE", 0.0);
        double totalCost = getIntent().getDoubleExtra("TOTAL_COST", 0.0);
        String carName = getIntent().getStringExtra("NAME");

        String pickupDate = getIntent().getStringExtra("PICKUP_DATE");
        String returnDate = getIntent().getStringExtra("RETURN_DATE");
        String pickupTime = getIntent().getStringExtra("PICKUP_TIME");
        String returnTime = getIntent().getStringExtra("RETURN_TIME");

        int pickupLocId = getIntent().getIntExtra("PICKUP_LOC_ID", 1);
        int returnLocId = getIntent().getIntExtra("RETURN_LOC_ID", 1);

        // Late fee information
        int lateHours = getIntent().getIntExtra("LATE_HOURS", 0);
        double lateFee = getIntent().getDoubleExtra("LATE_FEE", 0.0);

        // Get User ID from session
        int uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        // Log all received data
        Log.d(TAG, "Received booking data:");
        Log.d(TAG, "  User ID: " + uid);
        Log.d(TAG, "  Vehicle ID: " + vid);
        Log.d(TAG, "  Car Name: " + carName);
        Log.d(TAG, "  Total Cost: $" + totalCost);

        // Validate required data
        if(vid == -1) {
            Toast.makeText(this, "Error: Invalid vehicle ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(pickupDate == null || returnDate == null || pickupTime == null || returnTime == null) {
            Toast.makeText(this, "Error: Missing booking information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind UI Elements
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCarName = findViewById(R.id.tvCarName);
        TextView tvDates = findViewById(R.id.tvDates);
        TextView tvTotal = findViewById(R.id.tvTotal);
        TextView tvLateFeeWarning = findViewById(R.id.tvLateFeeWarning);

        etPaymentId = findViewById(R.id.etPaymentId);
        etCardholderName = findViewById(R.id.etCardholderName);

        // Set UI Data
        if(tvTitle != null) tvTitle.setText("Payment Summary");
        if(tvCarName != null) tvCarName.setText(carName != null ? carName : "Unknown Vehicle");
        if(tvDates != null) tvDates.setText(pickupDate + " " + pickupTime + " → " + returnDate + " " + returnTime);

        // Show total with late fee breakdown if applicable
        if(tvTotal != null) {
            if(lateFee > 0) {
                double baseCost = totalCost - lateFee;
                String totalText = String.format(
                        "Base: $%.2f\nLate Fee (%d hrs): $%.2f\nTotal: $%.2f",
                        baseCost, lateHours, lateFee, totalCost
                );
                tvTotal.setText(totalText);
            } else {
                tvTotal.setText("Total: $" + String.format("%.2f", totalCost));
            }
        }

        // Show late fee warning if applicable
        if(tvLateFeeWarning != null) {
            if(lateFee > 0) {
                tvLateFeeWarning.setVisibility(View.VISIBLE);
                tvLateFeeWarning.setText("⚠️ Late return penalty: $" + String.format("%.2f", lateFee));
            } else {
                tvLateFeeWarning.setVisibility(View.GONE);
            }
        }

        // Payment Method Selection
        RadioGroup rg = findViewById(R.id.rgMethod);
        if(rg != null) {
            rg.setOnCheckedChangeListener((g, checkedId) -> {
                if (checkedId == R.id.rbCard) {
                    paymentMethod = "Credit Card";
                    if(etPaymentId != null) {
                        etPaymentId.setHint("Card Number (last 4 digits)");
                    }
                } else if (checkedId == R.id.rbGcash) {
                    paymentMethod = "GCash";
                    if(etPaymentId != null) {
                        etPaymentId.setHint("GCash Mobile Number");
                    }
                }
                Log.d(TAG, "Payment method selected: " + paymentMethod);
            });
        }

        // Confirm Payment Button
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            Log.d(TAG, ">>> Confirm Payment clicked");

            // Validate payment details are filled
            String paymentId = etPaymentId.getText().toString().trim();
            String cardholderName = etCardholderName.getText().toString().trim();

            if(paymentId.isEmpty()) {
                Toast.makeText(this, "⚠️ Please enter payment ID/number", Toast.LENGTH_SHORT).show();
                etPaymentId.requestFocus();
                return;
            }

            if(cardholderName.isEmpty()) {
                Toast.makeText(this, "⚠️ Please enter cardholder/account name", Toast.LENGTH_SHORT).show();
                etCardholderName.requestFocus();
                return;
            }

            // Validate payment ID format
            if(paymentMethod.equals("Credit Card") && paymentId.length() != 4) {
                Toast.makeText(this, "⚠️ Please enter last 4 digits of card number", Toast.LENGTH_SHORT).show();
                etPaymentId.requestFocus();
                return;
            }

            if(paymentMethod.equals("GCash") && paymentId.length() < 10) {
                Toast.makeText(this, "⚠️ Please enter valid GCash mobile number", Toast.LENGTH_SHORT).show();
                etPaymentId.requestFocus();
                return;
            }

            // Proceed with booking
            CarRentalData db = new CarRentalData(this);

            // Get location addresses
            String pickupAddr = db.getLocationAddress(pickupLocId);
            String returnAddr = db.getLocationAddress(returnLocId);

            // Create pending booking
            boolean success = db.createPendingBooking(
                    uid, vid, carName,
                    pickupDate, returnDate,
                    pickupTime, returnTime,
                    pickupAddr, returnAddr,
                    totalCost, paymentMethod,
                    paymentId, pickupLocId, returnLocId
            );

            if(success) {
                Log.d(TAG, "✓ Booking created and pending approval!");

                String message = "✓ Booking Submitted Successfully!\n\n" +
                        "Your booking is pending admin approval.\n\n" +
                        "Payment Method: " + paymentMethod + "\n" +
                        "Payment ID: " + paymentId + "\n\n" +
                        "⚠️ IMPORTANT - Bring to Pickup:\n" +
                        "• Valid driver's license\n" +
                        "• Government-issued ID\n" +
                        "• Proof of insurance";

                if(lateFee > 0) {
                    message += "\n\n(Includes $" + String.format("%.2f", lateFee) + " late fee)";
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // Return to main activity and clear back stack
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            } else {
                Log.e(TAG, "✗ Booking failed!");
                Toast.makeText(this, "❌ Booking Failed\n\nThe car may no longer be available for your selected dates.", Toast.LENGTH_LONG).show();
            }
        });

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
    }
}