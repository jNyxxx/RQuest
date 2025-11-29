package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    private String paymentMethod = "Credit Card";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get all booking details from intent
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

        int uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        // Display Summary
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCarName = findViewById(R.id.tvCarName);
        TextView tvDates = findViewById(R.id.tvDates);
        TextView tvTotal = findViewById(R.id.tvTotal);

        if(tvTitle != null) tvTitle.setText("Payment Summary");
        if(tvCarName != null) tvCarName.setText(carName);
        if(tvDates != null) tvDates.setText(pickupDate + " " + pickupTime + " → " + returnDate + " " + returnTime);
        if(tvTotal != null) tvTotal.setText("Total: $" + String.format("%.2f", totalCost));

        // Payment Method Selection
        RadioGroup rg = findViewById(R.id.rgMethod);
        if(rg != null) {
            rg.setOnCheckedChangeListener((g, checkedId) -> {
                if(checkedId == R.id.rbCard) {
                    paymentMethod = "Credit Card";
                } else if(checkedId == R.id.rbGcash) {
                    paymentMethod = "GCash";
                }
            });
        }

        // Confirm Payment Button
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            CarRentalData db = new CarRentalData(this);

            boolean success = db.processPaymentAndBooking(
                    uid, vid,
                    pickupDate, returnDate,
                    pickupTime, returnTime,
                    pickupLocId, returnLocId,
                    totalCost, paymentMethod
            );

            if(success) {
                Toast.makeText(this, "Booking Confirmed! Total: $" + String.format("%.2f", totalCost), Toast.LENGTH_LONG).show();

                // Return to Main Activity and clear back stack
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Booking Failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}