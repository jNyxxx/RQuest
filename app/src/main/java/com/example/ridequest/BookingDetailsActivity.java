package com.example.ridequest;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BookingDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        int bookingId = getIntent().getIntExtra("BOOKING_ID", -1);

        // TODO: Load full booking details from database
        TextView tvTitle = findViewById(R.id.tvTitle);
        if(tvTitle != null) {
            tvTitle.setText("Booking #" + bookingId);
        }

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if(btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}