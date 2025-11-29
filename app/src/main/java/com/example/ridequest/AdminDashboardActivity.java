package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminDashboardActivity extends AppCompatActivity {
    private CarRentalData db;
    private RecyclerView rv;
    private FloatingActionButton fab;
    private boolean showingVehicles = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new CarRentalData(this);
        rv = findViewById(R.id.rvAdmin);
        fab = findViewById(R.id.fabAdd);
        ImageView btnProfile = findViewById(R.id.btnAdminProfile);

        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnViewVehicles).setOnClickListener(v -> loadVehicles());
        findViewById(R.id.btnViewBookings).setOnClickListener(v -> loadBookings());

        // LOGOUT LOGIC
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(AdminDashboardActivity.this, btnProfile);
                popup.getMenu().add(0, 1, 0, "Log Out");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        Intent intent = new Intent(AdminDashboardActivity.this, LandingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        fab.setOnClickListener(v -> {
            if (showingVehicles) {
                startActivity(new Intent(AdminDashboardActivity.this, AddVehicleActivity.class));
            } else {
                Toast.makeText(this, "Switch to Vehicles tab to add cars", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showingVehicles) loadVehicles();
        else loadBookings();
    }

    private void loadVehicles() {
        showingVehicles = true;
        fab.show();
        rv.setAdapter(new VehicleAdapter(this, db.getAllVehicles(), true, id -> {
            db.deleteVehicle(id);
            loadVehicles();
            Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show();
        }));
    }

    private void loadBookings() {
        showingVehicles = false;
        fab.hide();
        rv.setAdapter(new BookingAdapter(db.getAllBookings()));
    }
}