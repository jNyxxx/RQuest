package com.example.ridequest;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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

    private double dailyRate;
    private List<CarRentalData.LocationItem> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Get Data from Intent
        int vid = getIntent().getIntExtra("VID", -1);
        dailyRate = getIntent().getDoubleExtra("PRICE", 0.0);
        String name = getIntent().getStringExtra("NAME");
        String imgRes = getIntent().getStringExtra("IMG_RES");

        // Initialize UI Elements
        TextView tvName = findViewById(R.id.tvBookingCarName);
        TextView tvPrice = findViewById(R.id.tvBookingPrice);
        ImageView ivCar = findViewById(R.id.ivBookingCar);

        EditText etPickup = findViewById(R.id.etPickupDate);
        EditText etReturn = findViewById(R.id.etReturnDate);

        Spinner spPickup = findViewById(R.id.spPickupLoc);
        Spinner spReturn = findViewById(R.id.spReturnLoc);
        Spinner spTimeP = findViewById(R.id.spPickupTime);
        Spinner spTimeR = findViewById(R.id.spReturnTime);

        // Set Car Info
        if(tvName != null) tvName.setText(name);
        if(tvPrice != null) tvPrice.setText("$" + dailyRate + " per day");

        if(ivCar != null && imgRes != null) {
            int resId = getResources().getIdentifier(imgRes, "drawable", getPackageName());
            if(resId != 0) ivCar.setImageResource(resId);
        }

        // Load Locations from Database
        CarRentalData db = new CarRentalData(this);
        locations = db.getAllLocations();

        List<String> locationNames = new ArrayList<>();
        for(CarRentalData.LocationItem loc : locations) {
            locationNames.add(loc.name);
        }

        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locationNames);
        spPickup.setAdapter(locAdapter);
        spReturn.setAdapter(locAdapter);

        // Setup Time Spinners (24-hour format for easier calculation)
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

            if (pickupDate.isEmpty() || returnDate.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate 24-hour rule and calculate cost
            RentalCalculation calc = calculateRentalCost(pickupDate, pickupTime, returnDate, returnTime);

            if (calc == null) {
                return; // Error already shown in calculateRentalCost
            }

            // Get selected location IDs
            int pickupLocId = locations.get(spPickup.getSelectedItemPosition()).id;
            int returnLocId = locations.get(spReturn.getSelectedItemPosition()).id;

            // Pass everything to PaymentActivity
            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra("VID", vid);
            i.putExtra("DAILY_RATE", dailyRate);
            i.putExtra("TOTAL_COST", calc.totalCost);
            i.putExtra("NAME", name);
            i.putExtra("PICKUP_DATE", pickupDate);
            i.putExtra("RETURN_DATE", returnDate);
            i.putExtra("PICKUP_TIME", pickupTime);
            i.putExtra("RETURN_TIME", returnTime);
            i.putExtra("PICKUP_LOC_ID", pickupLocId);
            i.putExtra("RETURN_LOC_ID", returnLocId);
            i.putExtra("LATE_HOURS", calc.lateHours);
            i.putExtra("LATE_FEE", calc.lateFee);
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

    /**
     * Inner class to hold rental calculation results
     */
    private static class RentalCalculation {
        double baseCost;
        double lateFee;
        double totalCost;
        int lateHours;
        int fullDays;
    }

    /**
     * Calculate rental cost with 24-hour rule enforcement
     * Rules:
     * 1. Return time must be SAME or BEFORE pickup time (24hr increments)
     * 2. Late returns charged at 1/4 daily rate per hour
     */
    private RentalCalculation calculateRentalCost(String pickupDateStr, String pickupTimeStr,
                                                  String returnDateStr, String returnTimeStr) {
        try {
            // Parse dates and times
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy HH:mm", Locale.US);
            String pickupDateTime = pickupDateStr + " " + pickupTimeStr;
            String returnDateTime = returnDateStr + " " + returnTimeStr;

            Date pickupDT = dateFormat.parse(pickupDateTime);
            Date returnDT = dateFormat.parse(returnDateTime);

            if(pickupDT == null || returnDT == null) {
                Toast.makeText(this, "Invalid date/time format", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Check if return is before pickup
            if(returnDT.before(pickupDT)) {
                Toast.makeText(this, "Return date/time cannot be before pickup!", Toast.LENGTH_LONG).show();
                return null;
            }

            // Calculate total hours
            long diffInMillis = returnDT.getTime() - pickupDT.getTime();
            long totalHours = diffInMillis / (1000 * 60 * 60);

            // Calculate full 24-hour periods
            int fullDays = (int) (totalHours / 24);
            int remainingHours = (int) (totalHours % 24);

            // Parse times to check 24-hour rule
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

            // Minimum 1 day rental
            if(fullDays == 0 && remainingHours == 0) {
                calc.fullDays = 1;
            }

            // Check 24-hour rule: Return time must be SAME or EARLIER than pickup time
            if(returnTotalMinutes > pickupTotalMinutes) {
                // Return time is LATER than pickup time - this violates the 24hr rule
                calc.lateHours = (int) Math.ceil((returnTotalMinutes - pickupTotalMinutes) / 60.0);

                // Calculate late fee: 1/4 of daily rate per hour
                double hourlyRate = dailyRate / 4.0;
                calc.lateFee = calc.lateHours * hourlyRate;

                // Show warning to user
                Toast.makeText(this,
                        "⚠️ LATE RETURN DETECTED!\n" +
                                "Late by: " + calc.lateHours + " hour(s)\n" +
                                "Late fee: $" + String.format("%.2f", calc.lateFee) + " ($" + String.format("%.2f", hourlyRate) + "/hr)",
                        Toast.LENGTH_LONG).show();
            }

            // Calculate base cost
            calc.baseCost = (fullDays > 0 ? fullDays : 1) * dailyRate;

            // Total cost = base cost + late fee
            calc.totalCost = calc.baseCost + calc.lateFee;

            // Show breakdown to user
            String message;
            if(calc.lateFee > 0) {
                message = String.format(Locale.US,
                        "Rental: %d day(s) × $%.2f = $%.2f\n" +
                                "Late Fee: %d hr(s) × $%.2f = $%.2f\n" +
                                "TOTAL: $%.2f",
                        calc.fullDays, dailyRate, calc.baseCost,
                        calc.lateHours, (dailyRate / 4.0), calc.lateFee,
                        calc.totalCost);
            } else {
                message = String.format(Locale.US,
                        "✓ On-time return!\n" +
                                "Rental: %d day(s) × $%.2f = $%.2f",
                        calc.fullDays, dailyRate, calc.totalCost);
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