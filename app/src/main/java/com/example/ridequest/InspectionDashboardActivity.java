package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InspectionDashboardActivity extends AppCompatActivity {

    private CarRentalData db;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard); // Reusing layout

        db = new CarRentalData(this);
        recyclerView = findViewById(R.id.rvAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Hide Admin specific tabs from Agent view
        findViewById(R.id.cardAdminProfile).setVisibility(View.GONE);
        TextView title = findViewById(R.id.tvSectionTitle);
        title.setText("Pending Return Inspections");

        loadInspections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInspections();
    }

    private void loadInspections() {
        // Only fetch bookings with status 'Rented' or 'Confirmed'
        List<CarRentalData.AdminBookingItem> list = db.getBookingsForInspection();

        // FIX: Added "Inspection Agent" as the 3rd argument
        AdminBookingAdapter adapter = new AdminBookingAdapter(this, list, "Inspection Agent", new AdminBookingAdapter.BookingActionListener() {
            @Override
            public void onApprove(CarRentalData.AdminBookingItem booking) {} // Not used
            @Override
            public void onCancel(CarRentalData.AdminBookingItem booking) {} // Not used
            @Override
            public void onReturn(CarRentalData.AdminBookingItem booking) {} // Not used

            @Override
            public void onViewDetails(CarRentalData.AdminBookingItem booking) {
                // Agent clicks to start inspection
                Intent i = new Intent(InspectionDashboardActivity.this, InspectionActivity.class);
                i.putExtra("BOOKING_ID", booking.id);
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}