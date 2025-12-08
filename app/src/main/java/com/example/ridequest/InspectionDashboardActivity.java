package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InspectionDashboardActivity extends AppCompatActivity {

    private CarRentalData db;
    private RecyclerView rvInspectionTasks;
    private TextView tvItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_dashboard);

        db = new CarRentalData(this);
        rvInspectionTasks = findViewById(R.id.rvInspectionTasks);
        tvItemCount = findViewById(R.id.tvItemCount);
        ImageView btnBack = findViewById(R.id.btnBack);
        View btnLogout = findViewById(R.id.btnLogout);

        rvInspectionTasks.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());

        loadInspections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInspections();
    }

    private void loadInspections() {
        List<CarRentalData.AdminBookingItem> list = db.getBookingsForInspection();

        if (list != null) {
            tvItemCount.setText(list.size() + " Pending");
        } else {
            tvItemCount.setText("0 Pending");
        }

        AdminBookingAdapter adapter = new AdminBookingAdapter(this, list, "Inspection Agent", new AdminBookingAdapter.BookingActionListener() {
            @Override
            public void onApprove(CarRentalData.AdminBookingItem booking) {}
            @Override
            public void onCancel(CarRentalData.AdminBookingItem booking) {}
            @Override
            public void onReturn(CarRentalData.AdminBookingItem booking) {}

            // UPDATED METHOD: Receives inspectionType from Adapter
            @Override
            public void onViewDetails(CarRentalData.AdminBookingItem booking, String inspectionType) {
                Intent i = new Intent(InspectionDashboardActivity.this, InspectionActivity.class);
                i.putExtra("BOOKING_ID", booking.id);
                i.putExtra("INSPECTION_TYPE", inspectionType); // "Pickup" or "Return"
                startActivity(i);
            }
        });

        rvInspectionTasks.setAdapter(adapter);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}