package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private String paymentMethod = "Credit Card";

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
        Log.d(TAG, "  Daily Rate: $" + dailyRate);
        Log.d(TAG, "  Total Cost: $" + totalCost);
        Log.d(TAG, "  Pickup: " + pickupDate + " " + pickupTime);
        Log.d(TAG, "  Return: " + returnDate + " " + returnTime);
        Log.d(TAG, "  Late Hours: " + lateHours);
        Log.d(TAG, "  Late Fee: $" + lateFee);

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
        TextView tvLateFeeWarning = findViewById(R.id.tvLateFeeWarning); // Optional - add to XML

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
                } else if (checkedId == R.id.rbGcash) {
                    paymentMethod = "GCash";
                }
                Log.d(TAG, "Payment method selected: " + paymentMethod);
            });
        }

        // Confirm Payment Button
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            Log.d(TAG, ">>> Confirm Payment clicked");

            CarRentalData db = new CarRentalData(this);

            // Call processPaymentAndBooking with ALL 10 parameters
            boolean success = db.processPaymentAndBooking(
                    uid,           // Customer ID
                    vid,           // Vehicle ID
                    pickupDate,    // Pickup date
                    returnDate,    // Return date
                    pickupTime,    // Pickup time
                    returnTime,    // Return time
                    pickupLocId,   // Pickup location ID
                    returnLocId,   // Return location ID
                    totalCost,     // Total cost (including late fees)
                    paymentMethod  // Payment method
            );

            if(success) {
                Log.d(TAG, "✓ Payment successful!");

                String message = "Booking Confirmed!\nTotal: $" + String.format("%.2f", totalCost);
                if(lateFee > 0) {
                    message += "\n(Includes $" + String.format("%.2f", lateFee) + " late fee)";
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // Return to main activity and clear back stack
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            } else {
                Log.e(TAG, "✗ Payment failed!");
                Toast.makeText(this, "Payment Failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
    }
}