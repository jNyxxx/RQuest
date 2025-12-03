package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CarDetailActivity extends AppCompatActivity {

    private static final String TAG = "CarDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        // 1. Receive Data passed from Adapter
        int id = getIntent().getIntExtra("VID", -1);
        double price = getIntent().getDoubleExtra("PRICE", 0.0);
        String name = getIntent().getStringExtra("NAME");
        String imgRes = getIntent().getStringExtra("IMG_RES");
        String transmission = getIntent().getStringExtra("TRANSMISSION");
        int seats = getIntent().getIntExtra("SEATS", 5);

        Log.d(TAG, "Vehicle Details - ID: " + id + ", Name: " + name + ", Transmission: " + transmission + ", Seats: " + seats);

        // 2. Find Views (Ensure IDs match your XML exactly)
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvTransmission = findViewById(R.id.tvTransmission);
        TextView tvSeats = findViewById(R.id.tvSeats);
        ImageView ivCar = findViewById(R.id.ivDetailCar);

        // 3. Set Data (With Null Safety)
        if(tvTitle != null) {
            tvTitle.setText(name != null ? name : "Unknown Car");
        }

        if(tvPrice != null) {
            tvPrice.setText("$" + price);
        }

        // Set transmission
        if(tvTransmission != null) {
            tvTransmission.setText(transmission != null ? transmission : "Manual");
        }

        // Set seats
        if(tvSeats != null) {
            tvSeats.setText(seats + " Seats");
        }

        if(ivCar != null) {
            if (imgRes != null && !imgRes.isEmpty()) {
                try {
                    // Try to decode as Base64
                    byte[] decodedBytes = android.util.Base64.decode(imgRes, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        ivCar.setImageBitmap(bitmap);
                    } else {
                        // Try as drawable resource (old format compatibility)
                        int resId = getResources().getIdentifier(imgRes, "drawable", getPackageName());
                        if(resId != 0) {
                            ivCar.setImageResource(resId);
                        } else {
                            ivCar.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    }
                } catch (Exception e) {
                    // If Base64 decode fails, try as drawable resource
                    int resId = getResources().getIdentifier(imgRes, "drawable", getPackageName());
                    if(resId != 0) {
                        ivCar.setImageResource(resId);
                    } else {
                        ivCar.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            } else {
                // Fallback if no image resource name was passed
                ivCar.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // 4. "Book Now" Click -> Go to Booking
        if (findViewById(R.id.btnBookNow) != null) {
            findViewById(R.id.btnBookNow).setOnClickListener(v -> {
                try {
                    Intent i = new Intent(this, BookingActivity.class);
                    i.putExtra("VID", id);
                    i.putExtra("PRICE", price);
                    i.putExtra("NAME", name);
                    i.putExtra("IMG_RES", imgRes);
                    i.putExtra("TRANSMISSION", transmission);
                    i.putExtra("SEATS", seats);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        }

        // Back Button Logic
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}