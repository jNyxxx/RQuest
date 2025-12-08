package com.example.ridequest;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private static final String TAG = "BookingActivity";
    private double dailyRate;
    private RadioGroup rgInsurance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Get Data from Intent
        int vid = getIntent().getIntExtra("VID", -1);
        dailyRate = getIntent().getDoubleExtra("PRICE", 0.0);
        String name = getIntent().getStringExtra("NAME");
        String imgRes = getIntent().getStringExtra("IMG_RES");

        Log.d(TAG, "Booking started - Vehicle ID: " + vid + ", Daily Rate: $" + dailyRate);

        // UI Elements
        TextView tvName = findViewById(R.id.tvBookingCarName);
        TextView tvPrice = findViewById(R.id.tvBookingPrice);
        ImageView ivCar = findViewById(R.id.ivBookingCar);

        EditText etPickup = findViewById(R.id.etPickupDate);
        EditText etReturn = findViewById(R.id.etReturnDate);

        EditText etPickupAddress = findViewById(R.id.etPickupAddress);
        EditText etReturnAddress = findViewById(R.id.etReturnAddress);

        Spinner spTimeP = findViewById(R.id.spPickupTime);
        Spinner spTimeR = findViewById(R.id.spReturnTime);

        // INSURANCE
        rgInsurance = findViewById(R.id.rgInsurance);

        // Set Car Info
        if(tvName != null) tvName.setText(name);
        if(tvPrice != null) tvPrice.setText("$" + dailyRate + " per day");

        if(ivCar != null && imgRes != null) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(imgRes, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivCar.setImageBitmap(bitmap);
                } else {
                    int resId = getResources().getIdentifier(imgRes, "drawable", getPackageName());
                    if(resId != 0) ivCar.setImageResource(resId);
                }
            } catch (Exception e) {
                int resId = getResources().getIdentifier(imgRes, "drawable", getPackageName());
                if(resId != 0) ivCar.setImageResource(resId);
                else ivCar.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Setup Time Spinners
        List<String> times = new ArrayList<>();
        for(int h = 0; h < 24; h++) {
            for(int m = 0; m < 60; m += 30) {
                times.add(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, times);
        spTimeP.setAdapter(timeAdapter);
        spTimeR.setAdapter(timeAdapter);

        // Date Pickers
        etPickup.setFocusable(false);
        etPickup.setClickable(true);
        etPickup.setOnClickListener(v -> showDatePicker(etPickup));

        etReturn.setFocusable(false);
        etReturn.setClickable(true);
        etReturn.setOnClickListener(v -> showDatePicker(etReturn));

        // Continue Button
        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String pickupDate = etPickup.getText().toString().trim();
            String returnDate = etReturn.getText().toString().trim();
            String pickupTime = spTimeP.getSelectedItem().toString();
            String returnTime = spTimeR.getSelectedItem().toString();

            String pickupAddress = etPickupAddress.getText().toString().trim();
            String returnAddress = etReturnAddress.getText().toString().trim();

            // Validate
            if (pickupDate.isEmpty() || returnDate.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pickupAddress.isEmpty()) {
                Toast.makeText(this, "Please enter pickup address", Toast.LENGTH_SHORT).show();
                etPickupAddress.requestFocus();
                return;
            }

            if (returnAddress.isEmpty()) {
                Toast.makeText(this, "Please enter return address", Toast.LENGTH_SHORT).show();
                etReturnAddress.requestFocus();
                return;
            }

            // GET INSURANCE SELECTION
            String insuranceType = "None";
            double insuranceFee = 0.0;

            int selectedInsuranceId = rgInsurance.getCheckedRadioButtonId();
            if (selectedInsuranceId != -1) {
                RadioButton selectedInsurance = findViewById(selectedInsuranceId);
                String insuranceText = selectedInsurance.getText().toString();

                Log.d(TAG, "Selected insurance: " + insuranceText);

                // insurance type
                if (insuranceText.contains("No Insurance")) {
                    insuranceType = "No Insurance (Free)";
                    insuranceFee = 0.0;
                } else if (insuranceText.contains("Personal Insurance")) {
                    insuranceType = "Personal Insurance";
                    insuranceFee = 0.0;
                } else if (insuranceText.contains("Basic Insurance")) {
                    insuranceType = "Basic Insurance";
                    insuranceFee = dailyRate * 0.20; // 20% of DAILY RATE (not total)
                }

                Log.d(TAG, "Insurance Type: " + insuranceType);
                Log.d(TAG, "Insurance Fee: $" + insuranceFee);
            }

            // CALCULATE RENTAL COST
            RentalCalculation calc = calculateRentalCost(pickupDate, pickupTime, returnDate, returnTime, insuranceFee);

            if (calc == null) {
                return;
            }

            Log.d(TAG, "=== Cost Breakdown ===");
            Log.d(TAG, "Days: " + calc.fullDays);
            Log.d(TAG, "Base Cost: $" + calc.baseCost);
            Log.d(TAG, "Insurance: $" + insuranceFee + " x " + calc.fullDays + " days = $" + (insuranceFee * calc.fullDays));
            Log.d(TAG, "Late Fee: $" + calc.lateFee);
            Log.d(TAG, "TOTAL: $" + calc.totalCost);

            // PASS ALL DATA TO PAYMENT
            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra("VID", vid);
            i.putExtra("DAILY_RATE", dailyRate);
            i.putExtra("BASE_COST", calc.baseCost);
            i.putExtra("INSURANCE_TYPE", insuranceType);
            i.putExtra("INSURANCE_FEE", insuranceFee * calc.fullDays);
            i.putExtra("TOTAL_COST", calc.totalCost);
            i.putExtra("NAME", name);
            i.putExtra("PICKUP_DATE", pickupDate);
            i.putExtra("RETURN_DATE", returnDate);
            i.putExtra("PICKUP_TIME", pickupTime);
            i.putExtra("RETURN_TIME", returnTime);
            i.putExtra("PICKUP_ADDRESS", pickupAddress);
            i.putExtra("RETURN_ADDRESS", returnAddress);
            i.putExtra("LATE_HOURS", calc.lateHours);
            i.putExtra("LATE_FEE", calc.lateFee);
            i.putExtra("RENTAL_DAYS", calc.fullDays);
            startActivity(i);
        });

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showDatePicker(EditText targetView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year1;
                    targetView.setText(date);
                },
                year, month, day);

        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();
    }

    private static class RentalCalculation {
        double baseCost;
        double lateFee;
        double totalCost;
        int lateHours;
        int fullDays;
    }

    // Insurance fee calculation
    private RentalCalculation calculateRentalCost(String pickupDateStr, String pickupTimeStr,
                                                  String returnDateStr, String returnTimeStr,
                                                  double insuranceFeePerDay) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy HH:mm", Locale.US);
            String pickupDateTime = pickupDateStr + " " + pickupTimeStr;
            String returnDateTime = returnDateStr + " " + returnTimeStr;

            Date pickupDT = dateFormat.parse(pickupDateTime);
            Date returnDT = dateFormat.parse(returnDateTime);

            if(pickupDT == null || returnDT == null) {
                Toast.makeText(this, "Invalid date/time format", Toast.LENGTH_SHORT).show();
                return null;
            }

            if(returnDT.before(pickupDT)) {
                Toast.makeText(this, "Return date/time cannot be before pickup!", Toast.LENGTH_LONG).show();
                return null;
            }

            long diffInMillis = returnDT.getTime() - pickupDT.getTime();
            long totalHours = diffInMillis / (1000 * 60 * 60);

            int fullDays = (int) (totalHours / 24);

            String[] pickupTimeParts = pickupTimeStr.split(":");
            String[] returnTimeParts = returnTimeStr.split(":");

            int pickupHour = Integer.parseInt(pickupTimeParts[0]);
            int pickupMinute = Integer.parseInt(pickupTimeParts[1]);
            int returnHour = Integer.parseInt(returnTimeParts[0]);
            int returnMinute = Integer.parseInt(returnTimeParts[1]);

            int pickupTotalMinutes = pickupHour * 60 + pickupMinute;
            int returnTotalMinutes = returnHour * 60 + returnMinute;

            RentalCalculation calc = new RentalCalculation();
            calc.fullDays = fullDays;
            calc.lateHours = 0;
            calc.lateFee = 0;

            // Minimum 1 day
            if(fullDays == 0) {
                calc.fullDays = 1;
            }

            // Check 24-hour rule
            if(returnTotalMinutes > pickupTotalMinutes) {
                calc.lateHours = (int) Math.ceil((returnTotalMinutes - pickupTotalMinutes) / 60.0);
                double hourlyRate = dailyRate / 4.0;
                calc.lateFee = calc.lateHours * hourlyRate;

                Toast.makeText(this, "LATE RETURN DETECTED! ", Toast.LENGTH_LONG).show();
            }

            // CALCULATE COSTS
            calc.baseCost = calc.fullDays * dailyRate;

            // TOTAL = base + (insurance per day * days) + late fee
            double totalInsurance = insuranceFeePerDay * calc.fullDays;
            calc.totalCost = calc.baseCost + totalInsurance + calc.lateFee;

            // Show breakdown
            String message;
            if(calc.lateFee > 0) {
                message = String.format(Locale.US,
                        "Base: $%.2f\nInsurance: $%.2f\nLate: $%.2f\nTotal: $%.2f",
                        calc.baseCost, totalInsurance, calc.lateFee, calc.totalCost);
            } else {
                message = String.format(Locale.US,
                        "Base: $%.2f\nInsurance: $%.2f\nTotal: $%.2f ✓",
                        calc.baseCost, totalInsurance, calc.totalCost);
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            return calc;

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing dates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}