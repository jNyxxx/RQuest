package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {
    private CarRentalData db;
    private int uid;
    private RecyclerView rvBookings;
    private LinearLayout tvNoBookings;
    private CustomerBookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        db = new CarRentalData(this);
        uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        // Initialize views
        rvBookings = findViewById(R.id.rvMyBookings);
        tvNoBookings = findViewById(R.id.tvNoBookings);
        ImageView btnBack = findViewById(R.id.btnBack);

        // RecyclerView
        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Load bookings
        loadBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings(); // Refresh bookings when returning from detail view
    }

    private void loadBookings() {
        List<CarRentalData.CustomerBookingItem> bookings = db.getCustomerBookings(uid);

        if (bookings.isEmpty()) {
            rvBookings.setVisibility(View.GONE);
            tvNoBookings.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            tvNoBookings.setVisibility(View.GONE);

            adapter = new CustomerBookingAdapter(this, bookings, new CustomerBookingAdapter.OnBookingActionListener() {
                @Override
                public void onViewDetails(int bookingId) {
                    Intent intent = new Intent(MyBookingsActivity.this, BookingDetailsActivity.class);
                    intent.putExtra("BOOKING_ID", bookingId);
                    intent.putExtra("IS_ADMIN", false);
                    startActivity(intent);
                }

                @Override
                public void onCancelBooking(int bookingId) {
                    boolean success = db.cancelBooking(bookingId, false);
                    if (success) {
                        loadBookings(); // Refresh list
                    }
                }
            });

            rvBookings.setAdapter(adapter);
        }
    }
}